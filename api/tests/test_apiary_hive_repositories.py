"""Repository tests for ApiaryRepository and HiveRepository (Mongo/Beanie).

Migration plan reference: TEST_PLAN_MONGO_MIGRATION.md §4.6.

Source under test:
  - app/repositories/apiary_repository.py  (ApiaryRepository)
  - app/repositories/hive_repository.py    (HiveRepository)

These exercise the real persistence layer the SQLAlchemy -> Motor/Beanie
migration introduced: full CRUD round-trips, ``get_by_id`` -> None on a missing
id, and the apiary<->hive relationship via ``Hive.get_by_apiary_id`` (equality
filter + empty result).

Infra note: there is no ``tests/conftest.py`` yet (the migration plan §4.1 adds
one as separate P0 infra and will own env overrides, the Mongo skip guard and
cleanup). Until it lands, this file is intentionally self-contained so it
COLLECTS and RUNS cleanly on its own:

  * Env overrides are set at the very top, BEFORE importing ``app`` /
    ``assistive_core`` (their settings read os.environ at import time). We use
    ``setdefault`` so a future conftest or CI env wins.
  * A Mongo-reachability guard skips the whole module when no Mongo is reachable
    (``mongomock-motor`` is NOT installed), matching the pattern in
    tests/test_main.py and the plan's "skip the async suite when Mongo is
    unreachable" approach.
  * pytest-asyncio 0.24.0 is installed but UNCONFIGURED (no asyncio_mode=auto),
    so every async test carries an explicit ``@pytest.mark.asyncio`` marker.

When the shared conftest arrives, its session ``init_core`` fixture + per-test
cleanup can replace the local fixtures here with no changes to the test bodies.
"""
import os

# --- Env overrides: MUST happen before importing app/assistive_core. ---
# assistive_core.settings.Settings reads env at import time; point the suite at
# dedicated *_test databases and keep init_core out of its production fail-fast
# path (placeholder JWT key). setdefault so a conftest / CI env that already set
# these wins.
os.environ.setdefault("MONGODB_DB", "beekeeper_test")
os.environ.setdefault("IDENTITY_DB", "assistive_identity_test")
os.environ.setdefault("ASSISTIVE_ENV", "test")
os.environ.setdefault("ENV", "test")
MONGODB_URI = os.environ.setdefault("MONGODB_URI", "mongodb://localhost:27017")

import uuid
from datetime import datetime, timezone

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
from app.models import Apiary, ApiaryStatus, Hive, HiveStatus  # noqa: E402
from app.models import DOMAIN_DOCUMENTS  # noqa: E402
from app.feed_sources import FEED_SOURCES  # noqa: E402
from app.repositories.apiary_repository import ApiaryRepository  # noqa: E402
from app.repositories.hive_repository import HiveRepository  # noqa: E402
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
    """Drop the apiaries/hives collections before and after each test.

    Keeps tests isolated from each other and from any seeded dev data. Dropping
    (rather than deleting docs) is fine here; Beanie re-creates declared indexes
    lazily and these tests don't assert on indexes.
    """
    db = get_client()[settings.MONGODB_DB]
    await db["apiaries"].delete_many({})
    await db["hives"].delete_many({})
    yield
    await db["apiaries"].delete_many({})
    await db["hives"].delete_many({})


@pytest.fixture
def apiary_repo() -> ApiaryRepository:
    return ApiaryRepository()


@pytest.fixture
def hive_repo() -> HiveRepository:
    return HiveRepository()


# --- Factory helpers -----------------------------------------------------------


def make_apiary(**overrides) -> Apiary:
    """Build a valid Apiary with a uuid string id (ids come from the routers as
    ``str(uuid.uuid4())``, NOT ObjectIds)."""
    data = {
        "id": str(uuid.uuid4()),
        "name": "Test Apiary",
        "location": "Test Location",
        "latitude": 40.7128,
        "longitude": -74.0060,
        "status": ApiaryStatus.HEALTHY,
    }
    data.update(overrides)
    return Apiary(**data)


def make_hive(apiary_id: str, **overrides) -> Hive:
    """Build a valid Hive bound to ``apiary_id``."""
    data = {
        "id": str(uuid.uuid4()),
        "name": "Test Hive",
        "apiary_id": apiary_id,
        "status": HiveStatus.STRONG,
        "last_inspected": datetime.now(timezone.utc),
    }
    data.update(overrides)
    return Hive(**data)


# --- ApiaryRepository tests ----------------------------------------------------


@pytest.mark.asyncio
async def test_apiary_crud(apiary_repo):
    """create -> get_by_id -> get_all (contains it) -> update -> delete -> None."""
    apiary = make_apiary(name="CRUD Apiary", location="Origin")

    # create
    created = await apiary_repo.create(apiary)
    assert created.id == apiary.id

    # get_by_id
    fetched = await apiary_repo.get_by_id(apiary.id)
    assert fetched is not None
    assert fetched.id == apiary.id
    assert fetched.name == "CRUD Apiary"
    assert fetched.location == "Origin"
    # id is the uuid string we set, NOT an ObjectId.
    assert isinstance(fetched.id, str)
    assert fetched.id == apiary.id

    # get_all contains it
    all_apiaries = await apiary_repo.get_all()
    assert any(a.id == apiary.id for a in all_apiaries)

    # update
    fetched.name = "Renamed Apiary"
    fetched.status = ApiaryStatus.WARNING
    await apiary_repo.update(fetched)
    reread = await apiary_repo.get_by_id(apiary.id)
    assert reread is not None
    assert reread.name == "Renamed Apiary"
    assert reread.status == ApiaryStatus.WARNING

    # delete -> get_by_id is None
    await apiary_repo.delete(reread)
    assert await apiary_repo.get_by_id(apiary.id) is None


@pytest.mark.asyncio
async def test_apiary_get_by_id_missing_returns_none(apiary_repo):
    """Unknown apiary id resolves to None (not an error)."""
    missing = await apiary_repo.get_by_id(str(uuid.uuid4()))
    assert missing is None


# --- HiveRepository tests ------------------------------------------------------


@pytest.mark.asyncio
async def test_hive_crud(apiary_repo, hive_repo):
    """Full Hive lifecycle: create -> get -> get_all -> update -> delete -> None."""
    apiary = await apiary_repo.create(make_apiary())
    hive = make_hive(apiary.id, name="CRUD Hive")

    # create
    created = await hive_repo.create(hive)
    assert created.id == hive.id

    # get_by_id
    fetched = await hive_repo.get_by_id(hive.id)
    assert fetched is not None
    assert fetched.id == hive.id
    assert fetched.name == "CRUD Hive"
    assert fetched.apiary_id == apiary.id
    assert isinstance(fetched.id, str)

    # get_all contains it
    all_hives = await hive_repo.get_all()
    assert any(h.id == hive.id for h in all_hives)

    # update
    fetched.name = "Renamed Hive"
    fetched.status = HiveStatus.NEEDS_INSPECTION
    await hive_repo.update(fetched)
    reread = await hive_repo.get_by_id(hive.id)
    assert reread is not None
    assert reread.name == "Renamed Hive"
    assert reread.status == HiveStatus.NEEDS_INSPECTION

    # delete -> get_by_id is None
    await hive_repo.delete(reread)
    assert await hive_repo.get_by_id(hive.id) is None


@pytest.mark.asyncio
async def test_hive_get_by_id_missing_returns_none(hive_repo):
    """Unknown hive id resolves to None."""
    missing = await hive_repo.get_by_id(str(uuid.uuid4()))
    assert missing is None


@pytest.mark.asyncio
async def test_hive_get_by_apiary_id_filters(apiary_repo, hive_repo):
    """get_by_apiary_id returns only the hives of the matching apiary."""
    apiary_a = await apiary_repo.create(make_apiary(name="Apiary A"))
    apiary_b = await apiary_repo.create(make_apiary(name="Apiary B"))

    a_hive_1 = await hive_repo.create(make_hive(apiary_a.id, name="A-1"))
    a_hive_2 = await hive_repo.create(make_hive(apiary_a.id, name="A-2"))
    b_hive_1 = await hive_repo.create(make_hive(apiary_b.id, name="B-1"))

    a_hives = await hive_repo.get_by_apiary_id(apiary_a.id)
    a_ids = {h.id for h in a_hives}
    assert a_ids == {a_hive_1.id, a_hive_2.id}
    assert all(h.apiary_id == apiary_a.id for h in a_hives)
    assert b_hive_1.id not in a_ids

    b_hives = await hive_repo.get_by_apiary_id(apiary_b.id)
    b_ids = {h.id for h in b_hives}
    assert b_ids == {b_hive_1.id}


@pytest.mark.asyncio
async def test_hive_get_by_apiary_id_empty(apiary_repo, hive_repo):
    """An apiary id with no hives (and an unknown id) returns an empty list."""
    apiary = await apiary_repo.create(make_apiary())
    # Some hives exist under a *different* apiary to prove filtering, not emptiness.
    other = await apiary_repo.create(make_apiary(name="Other"))
    await hive_repo.create(make_hive(other.id))

    assert await hive_repo.get_by_apiary_id(apiary.id) == []
    assert await hive_repo.get_by_apiary_id(str(uuid.uuid4())) == []


@pytest.mark.asyncio
async def test_hive_get_all(apiary_repo, hive_repo):
    """get_all returns every inserted hive regardless of apiary."""
    apiary_a = await apiary_repo.create(make_apiary(name="Apiary A"))
    apiary_b = await apiary_repo.create(make_apiary(name="Apiary B"))

    h1 = await hive_repo.create(make_hive(apiary_a.id, name="A-1"))
    h2 = await hive_repo.create(make_hive(apiary_a.id, name="A-2"))
    h3 = await hive_repo.create(make_hive(apiary_b.id, name="B-1"))

    all_hives = await hive_repo.get_all()
    all_ids = {h.id for h in all_hives}
    assert {h1.id, h2.id, h3.id}.issubset(all_ids)
    # clean_collections guarantees isolation, so exactly these three exist.
    assert len(all_hives) == 3
