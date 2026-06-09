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

Infra note: this file uses the shared ``tests/conftest.py`` infrastructure. The
FUNCTION-scoped ``init_core`` fixture there initialises Beanie across both DBs
exactly as ``app/main.py``'s lifespan does (pytest-asyncio 0.24 gives each test
its own event loop, so Beanie must be initialised on the test's own loop) and
wipes every domain + identity-users collection before AND after each test — a
superset of the alerts/recommendations cleanup this suite needs, so the
get_all / get_active / get_by_hive_id assertions stay deterministic. The conftest
also registers ``asyncio_mode=auto`` (the explicit ``@pytest.mark.asyncio``
markers below remain harmless) and skips the async suite when no Mongo is
reachable. The ``alert_factory`` / ``recommendation_factory`` fixtures provide the
``make_alert`` / ``make_recommendation`` builders (conftest's ``make_alert``
already sets the required ``Alert.timestamp`` field); they are only invoked inside
test bodies (after ``init_core`` has run) since Beanie Documents cannot be
instantiated before init.

Beanie round-trips return NAIVE UTC datetimes (the Motor client is not tz_aware),
but these cases assert on enums/booleans/list fields rather than datetime values,
so no tz handling is needed here.
"""
import uuid

import pytest

from app.models import (
    AlertSeverity,
    RecommendationType,
    Priority,
)
from app.repositories.alert_repository import AlertRepository
from app.repositories.recommendation_repository import (
    RecommendationRepository,
)


# --- Fixtures ------------------------------------------------------------------


@pytest.fixture
def alert_repo() -> AlertRepository:
    return AlertRepository()


@pytest.fixture
def recommendation_repo() -> RecommendationRepository:
    return RecommendationRepository()


# --- AlertRepository tests -----------------------------------------------------


@pytest.mark.asyncio
async def test_alert_crud(init_core, alert_repo, alert_factory):
    """create -> get_by_id -> get_all (contains it) -> update -> delete -> None."""
    alert = alert_factory(title="CRUD Alert", message="initial message")

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
async def test_alert_get_by_id_missing_returns_none(init_core, alert_repo):
    """Unknown alert id resolves to None (not an error)."""
    missing = await alert_repo.get_by_id(str(uuid.uuid4()))
    assert missing is None


@pytest.mark.asyncio
async def test_alert_get_active_excludes_dismissed(init_core, alert_repo, alert_factory):
    """get_active returns only alerts with ``dismissed == False``."""
    active_1 = await alert_repo.create(alert_factory(title="Active 1", dismissed=False))
    active_2 = await alert_repo.create(alert_factory(title="Active 2", dismissed=False))
    dismissed_1 = await alert_repo.create(
        alert_factory(title="Dismissed 1", dismissed=True)
    )

    active = await alert_repo.get_active()
    active_ids = {a.id for a in active}

    assert active_ids == {active_1.id, active_2.id}
    assert dismissed_1.id not in active_ids
    # Every returned alert is genuinely not dismissed.
    assert all(a.dismissed is False for a in active)


@pytest.mark.asyncio
async def test_alert_get_all_includes_dismissed(init_core, alert_repo, alert_factory):
    """get_all returns both active and dismissed alerts (no filtering)."""
    active = await alert_repo.create(alert_factory(title="Active", dismissed=False))
    dismissed = await alert_repo.create(
        alert_factory(title="Dismissed", dismissed=True)
    )

    all_alerts = await alert_repo.get_all()
    all_ids = {a.id for a in all_alerts}

    assert {active.id, dismissed.id}.issubset(all_ids)
    # init_core wipes collections before each test, so exactly these two exist.
    assert len(all_alerts) == 2


@pytest.mark.asyncio
async def test_alert_hive_ids_persisted_via_repo(init_core, alert_repo, alert_factory):
    """hive_ids round-trips as a real list[str] through the repository.

    Repo-level guard complementing the serialization test in §4.3: was a
    comma-separated string pre-migration, must now be a genuine BSON array.
    """
    hive_ids = ["hive-1", "hive-2", "hive-3"]
    created = await alert_repo.create(
        alert_factory(title="With hives", hive_ids=hive_ids)
    )

    fetched = await alert_repo.get_by_id(created.id)
    assert fetched is not None
    assert isinstance(fetched.hive_ids, list)
    assert all(isinstance(h, str) for h in fetched.hive_ids)
    assert fetched.hive_ids == hive_ids

    # Empty-default case: a freshly built alert has an empty list, not None.
    empty = await alert_repo.create(alert_factory(title="No hives"))
    empty_fetched = await alert_repo.get_by_id(empty.id)
    assert empty_fetched is not None
    assert empty_fetched.hive_ids == []


# --- RecommendationRepository tests --------------------------------------------


@pytest.mark.asyncio
async def test_recommendation_crud(init_core, recommendation_repo, recommendation_factory):
    """create -> get_by_id -> get_all (contains it) -> update -> delete -> None."""
    rec = recommendation_factory(
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
async def test_recommendation_get_by_id_missing_returns_none(init_core, recommendation_repo):
    """Unknown recommendation id resolves to None."""
    missing = await recommendation_repo.get_by_id(str(uuid.uuid4()))
    assert missing is None


@pytest.mark.asyncio
async def test_recommendation_get_by_hive_id_filters(init_core, recommendation_repo, recommendation_factory):
    """get_by_hive_id returns only recommendations for the matching hive id."""
    hive_a = "hive-a"
    hive_b = "hive-b"

    a_rec_1 = await recommendation_repo.create(
        recommendation_factory(hive_id=hive_a, title="A-1")
    )
    a_rec_2 = await recommendation_repo.create(
        recommendation_factory(hive_id=hive_a, title="A-2")
    )
    b_rec_1 = await recommendation_repo.create(
        recommendation_factory(hive_id=hive_b, title="B-1")
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
