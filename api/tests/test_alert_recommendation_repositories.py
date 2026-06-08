"""Repository tests for AlertRepository and RecommendationRepository (Mongo/Beanie).

Migration plan reference: TEST_PLAN_MONGO_MIGRATION.md §4.7 (P0).

Source under test:
  - app/repositories/alert_repository.py           (AlertRepository)
  - app/repositories/recommendation_repository.py  (RecommendationRepository)

These exercise the persistence behaviors the SQLAlchemy -> Motor/Beanie migration
introduced for these two DAOs:

  * Full CRUD round-trips (create / get_by_id / update / delete), with a missing
    id resolving to ``None`` (not an error).
  * ``AlertRepository.get_active`` -> only ``dismissed == False`` alerts, while
    ``get_all`` still includes dismissed ones.
  * ``Alert.hive_ids`` round-trips as a real ``list[str]`` through the repo (was a
    comma-separated string pre-migration) — a repo-level guard complementing the
    serialization tests in §4.3.
  * ``RecommendationRepository.get_by_hive_id`` -> equality filter, with an
    unknown hive id returning ``[]``.

Infra note: this file is intentionally self-contained (mirroring
tests/test_apiary_hive_repositories.py) so it COLLECTS and RUNS cleanly on its own.
The shared ``tests/conftest.py`` ships a session-scoped ``init_core`` fixture, but
pytest-asyncio 0.24 gives each test its own event loop, so Beanie must be
initialised on the test's own loop — hence the FUNCTION-scoped ``initialized_db``
fixture below (a module/session-scoped Motor client would raise "attached to a
different loop"). Each async test carries an explicit ``@pytest.mark.asyncio``
marker since the repo ships no ``asyncio_mode=auto`` ini at the project root.

  * Env overrides are set at the very top, BEFORE importing ``app`` /
    ``assistive_core`` (their settings read os.environ at import time). We use
    ``setdefault`` so the conftest / CI env wins.
  * A Mongo-reachability guard skips the whole module when no Mongo is reachable
    (``mongomock-motor`` is NOT installed).
  * Beanie Documents can't be instantiated before init_beanie runs, so the
    ``make_alert`` / ``make_recommendation`` builders are only called inside test
    bodies (after ``initialized_db`` has run).

Beanie round-trips return NAIVE UTC datetimes (the Motor client is not tz_aware),
but these cases assert on enums/booleans/list fields rather than datetime values,
so no tz handling is needed here.
"""
import os

# --- Env overrides: MUST happen before importing app/assistive_core. ---
os.environ.setdefault("MONGODB_DB", "beekeeper_test")
os.environ.setdefault("IDENTITY_DB", "assistive_identity_test")
os.environ.setdefault("ASSISTIVE_ENV", "test")
os.environ.setdefault("ENV", "test")
MONGODB_URI = os.environ.setdefault("MONGODB_URI", "mongodb://localhost:27017")

import uuid

import pytest
import pytest_asyncio


def _mongo_reachable(uri: str) -> bool:
    """Return True if a Mongo at ``uri`` answers a ping quickly.

    These repository tests hit a live Mongo (no mongomock-motor installed). When
    none is reachable (e.g. DB-less CI) we skip the module rather than error, so
    collection stays green.
    """
    try:
        from pymongo import MongoClient
    except Exception:
        return False
    try:
        probe = MongoClient(uri, serverSelectionTimeoutMS=1000)
        try:
            probe.admin.command("ping")
            return True
        finally:
            probe.close()
    except Exception:
        return False


# Collection-time skip guard: if no Mongo, skip the entire module so the file
# still imports/collects cleanly without a live DB.
if not _mongo_reachable(MONGODB_URI):
    pytest.skip(
        f"No MongoDB reachable at {MONGODB_URI}; skipping repository suite "
        "(mongomock-motor not installed).",
        allow_module_level=True,
    )

# Imported only after the skip guard + env overrides so a DB-less collection
# does not pay the import cost / side effects.
from app.models import (  # noqa: E402
    Alert,
    AlertType,
    AlertSeverity,
    Recommendation,
    RecommendationType,
    Priority,
)
from app.models import DOMAIN_DOCUMENTS  # noqa: E402
from app.models.base import utcnow  # noqa: E402
from app.feed_sources import FEED_SOURCES  # noqa: E402
from app.repositories.alert_repository import AlertRepository  # noqa: E402
from app.repositories.recommendation_repository import (  # noqa: E402
    RecommendationRepository,
)
from assistive_core import init_core, close_core, get_client  # noqa: E402
from assistive_core.settings import settings  # noqa: E402


# --- Fixtures ------------------------------------------------------------------


@pytest_asyncio.fixture
async def initialized_db():
    """Initialise Beanie exactly as app/main.py's lifespan does.

    Function-scoped: pytest-asyncio 0.24 gives each test its own event loop and
    Motor/Beanie bind their client to the running loop, so init_core must run on
    the test's own loop (a module-scoped client raises "attached to a different
    loop"). Tears down the shared motor client after each test (resetting
    assistive_core's module-global _client).
    """
    await init_core(
        vertical_documents=DOMAIN_DOCUMENTS,
        feed_sources=FEED_SOURCES,
    )
    yield
    await close_core()


@pytest_asyncio.fixture(autouse=True)
async def clean_collections(initialized_db):
    """Drop the alerts/recommendations collections before and after each test.

    Keeps tests isolated from each other and from any seeded dev data, so the
    get_all / get_active / get_by_hive_id assertions stay deterministic.
    """
    db = get_client()[settings.MONGODB_DB]
    await db["alerts"].delete_many({})
    await db["recommendations"].delete_many({})
    yield
    await db["alerts"].delete_many({})
    await db["recommendations"].delete_many({})


@pytest.fixture
def alert_repo() -> AlertRepository:
    return AlertRepository()


@pytest.fixture
def recommendation_repo() -> RecommendationRepository:
    return RecommendationRepository()


# --- Factory helpers -----------------------------------------------------------
# Only invoked inside test bodies (after init_beanie has run via initialized_db);
# Beanie Documents cannot be instantiated before init.


def make_alert(**overrides) -> Alert:
    """Build a valid Alert with a uuid string id."""
    data = {
        "id": str(uuid.uuid4()),
        "type": AlertType.GENERAL,
        "title": "Test Alert",
        "message": "Test alert message",
        "severity": AlertSeverity.INFO,
        "timestamp": utcnow(),
        "hive_ids": [],
        "dismissed": False,
    }
    data.update(overrides)
    return Alert(**data)


def make_recommendation(hive_id: str | None = None, **overrides) -> Recommendation:
    """Build a valid Recommendation bound to ``hive_id``."""
    data = {
        "id": str(uuid.uuid4()),
        "hive_id": hive_id or str(uuid.uuid4()),
        "type": RecommendationType.INFO,
        "title": "Test Recommendation",
        "description": "Test recommendation description",
    }
    data.update(overrides)
    return Recommendation(**data)


# --- AlertRepository tests -----------------------------------------------------


@pytest.mark.asyncio
async def test_alert_crud(alert_repo):
    """create -> get_by_id -> get_all (contains it) -> update -> delete -> None."""
    alert = make_alert(title="CRUD Alert", message="initial message")

    # create
    created = await alert_repo.create(alert)
    assert created.id == alert.id

    # get_by_id
    fetched = await alert_repo.get_by_id(alert.id)
    assert fetched is not None
    assert fetched.id == alert.id
    assert fetched.title == "CRUD Alert"
    assert fetched.message == "initial message"
    # id is the uuid string we set, NOT an ObjectId.
    assert isinstance(fetched.id, str)
    assert fetched.id == alert.id

    # get_all contains it
    all_alerts = await alert_repo.get_all()
    assert any(a.id == alert.id for a in all_alerts)

    # update
    fetched.title = "Renamed Alert"
    fetched.severity = AlertSeverity.CRITICAL
    fetched.dismissed = True
    await alert_repo.update(fetched)
    reread = await alert_repo.get_by_id(alert.id)
    assert reread is not None
    assert reread.title == "Renamed Alert"
    assert reread.severity == AlertSeverity.CRITICAL
    assert reread.dismissed is True

    # delete -> get_by_id is None
    await alert_repo.delete(reread)
    assert await alert_repo.get_by_id(alert.id) is None


@pytest.mark.asyncio
async def test_alert_get_by_id_missing_returns_none(alert_repo):
    """Unknown alert id resolves to None (not an error)."""
    missing = await alert_repo.get_by_id(str(uuid.uuid4()))
    assert missing is None


@pytest.mark.asyncio
async def test_alert_get_active_excludes_dismissed(alert_repo):
    """get_active returns only alerts with ``dismissed == False``."""
    active_1 = await alert_repo.create(make_alert(title="Active 1", dismissed=False))
    active_2 = await alert_repo.create(make_alert(title="Active 2", dismissed=False))
    dismissed_1 = await alert_repo.create(
        make_alert(title="Dismissed 1", dismissed=True)
    )

    active = await alert_repo.get_active()
    active_ids = {a.id for a in active}

    assert active_ids == {active_1.id, active_2.id}
    assert dismissed_1.id not in active_ids
    # Every returned alert is genuinely not dismissed.
    assert all(a.dismissed is False for a in active)


@pytest.mark.asyncio
async def test_alert_get_all_includes_dismissed(alert_repo):
    """get_all returns both active and dismissed alerts (no filtering)."""
    active = await alert_repo.create(make_alert(title="Active", dismissed=False))
    dismissed = await alert_repo.create(
        make_alert(title="Dismissed", dismissed=True)
    )

    all_alerts = await alert_repo.get_all()
    all_ids = {a.id for a in all_alerts}

    assert {active.id, dismissed.id}.issubset(all_ids)
    # clean_collections guarantees isolation, so exactly these two exist.
    assert len(all_alerts) == 2


@pytest.mark.asyncio
async def test_alert_hive_ids_persisted_via_repo(alert_repo):
    """hive_ids round-trips as a real list[str] through the repository.

    Repo-level guard complementing the serialization test in §4.3: was a
    comma-separated string pre-migration, must now be a genuine BSON array.
    """
    hive_ids = ["hive-1", "hive-2", "hive-3"]
    created = await alert_repo.create(
        make_alert(title="With hives", hive_ids=hive_ids)
    )

    fetched = await alert_repo.get_by_id(created.id)
    assert fetched is not None
    assert isinstance(fetched.hive_ids, list)
    assert all(isinstance(h, str) for h in fetched.hive_ids)
    assert fetched.hive_ids == hive_ids

    # Empty-default case: a freshly built alert has an empty list, not None.
    empty = await alert_repo.create(make_alert(title="No hives"))
    empty_fetched = await alert_repo.get_by_id(empty.id)
    assert empty_fetched is not None
    assert empty_fetched.hive_ids == []


# --- RecommendationRepository tests --------------------------------------------


@pytest.mark.asyncio
async def test_recommendation_crud(recommendation_repo):
    """create -> get_by_id -> get_all (contains it) -> update -> delete -> None."""
    rec = make_recommendation(
        hive_id="hive-abc",
        title="CRUD Rec",
        description="initial description",
    )

    # create
    created = await recommendation_repo.create(rec)
    assert created.id == rec.id

    # get_by_id
    fetched = await recommendation_repo.get_by_id(rec.id)
    assert fetched is not None
    assert fetched.id == rec.id
    assert fetched.hive_id == "hive-abc"
    assert fetched.title == "CRUD Rec"
    assert fetched.description == "initial description"
    assert isinstance(fetched.id, str)

    # get_all contains it
    all_recs = await recommendation_repo.get_all()
    assert any(r.id == rec.id for r in all_recs)

    # update
    fetched.title = "Renamed Rec"
    fetched.type = RecommendationType.ACTION_REQUIRED
    fetched.priority = Priority.HIGH
    await recommendation_repo.update(fetched)
    reread = await recommendation_repo.get_by_id(rec.id)
    assert reread is not None
    assert reread.title == "Renamed Rec"
    assert reread.type == RecommendationType.ACTION_REQUIRED
    assert reread.priority == Priority.HIGH

    # delete -> get_by_id is None
    await recommendation_repo.delete(reread)
    assert await recommendation_repo.get_by_id(rec.id) is None


@pytest.mark.asyncio
async def test_recommendation_get_by_id_missing_returns_none(recommendation_repo):
    """Unknown recommendation id resolves to None."""
    missing = await recommendation_repo.get_by_id(str(uuid.uuid4()))
    assert missing is None


@pytest.mark.asyncio
async def test_recommendation_get_by_hive_id_filters(recommendation_repo):
    """get_by_hive_id returns only recommendations for the matching hive id."""
    hive_a = "hive-a"
    hive_b = "hive-b"

    a_rec_1 = await recommendation_repo.create(
        make_recommendation(hive_id=hive_a, title="A-1")
    )
    a_rec_2 = await recommendation_repo.create(
        make_recommendation(hive_id=hive_a, title="A-2")
    )
    b_rec_1 = await recommendation_repo.create(
        make_recommendation(hive_id=hive_b, title="B-1")
    )

    a_recs = await recommendation_repo.get_by_hive_id(hive_a)
    a_ids = {r.id for r in a_recs}
    assert a_ids == {a_rec_1.id, a_rec_2.id}
    assert all(r.hive_id == hive_a for r in a_recs)
    assert b_rec_1.id not in a_ids

    b_recs = await recommendation_repo.get_by_hive_id(hive_b)
    assert {r.id for r in b_recs} == {b_rec_1.id}

    # Unknown hive id -> empty list.
    assert await recommendation_repo.get_by_hive_id(str(uuid.uuid4())) == []
