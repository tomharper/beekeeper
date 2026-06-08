"""Service tests for ApiaryService (Mongo/Beanie).

Migration plan reference: TEST_PLAN_MONGO_MIGRATION.md §4.9 (P1).

Source under test:
  - app/services/apiary_service.py  (ApiaryService)

Purpose: cover the migration-affected service logic that the SQLAlchemy ->
Motor/Beanie move introduced:

  * ``get_all_apiaries`` aggregates hive counts with ``Counter`` over a single
    ``Hive.find_all()`` query (0 for an apiary with no hives).
  * ``get_apiary`` counts hives with ``Hive.find(...).count()``.
  * ``create_apiary`` reports ``hive_count == 0`` for a fresh apiary.
  * ``get_apiary`` / ``update_apiary`` / ``delete_apiary`` raise
    ``HTTPException`` 404 on an unknown id.

Infra note: the shared ``tests/conftest.py`` (plan §4.1) owns env overrides, the
Mongo skip guard, the ``init_core`` fixture and per-test cleanup, plus
``make_apiary`` / ``make_hive`` factory helpers. This file is intentionally
self-contained anyway (mirroring tests/test_apiary_hive_repositories.py) so it
COLLECTS and RUNS cleanly on its own even if the conftest is absent or its
factories change:

  * Env overrides are set at the very top, BEFORE importing ``app`` /
    ``assistive_core`` (their settings read os.environ at import time). We use
    ``setdefault`` so the conftest / CI env wins.
  * A Mongo-reachability guard skips the whole module when no Mongo is reachable
    (``mongomock-motor`` is NOT installed), matching tests/test_main.py and the
    plan's "skip the async suite when Mongo is unreachable" approach.
  * pytest-asyncio 0.24.0 gives each test its own event loop, so the
    ``initialized_db`` fixture is FUNCTION-scoped (a module/session-scoped Motor
    client would bind to a different loop and raise "attached to a different
    loop"). Each async test carries an explicit ``@pytest.mark.asyncio`` marker
    since the repo ships no ``asyncio_mode=auto`` ini.
  * Beanie Documents can't be instantiated before init_beanie runs, so the
    ``make_apiary`` / ``make_hive`` builders are only called inside test bodies
    (after ``initialized_db`` has run).
"""
import os

# --- Env overrides: MUST happen before importing app/assistive_core. ---
# assistive_core.settings.Settings reads env at import time; point the suite at
# dedicated *_test databases and keep init_core out of its production fail-fast
# path (placeholder JWT key). setdefault so the conftest / CI env that already
# set these wins.
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

    These service tests hit a live Mongo (no mongomock-motor installed). When
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
        f"No MongoDB reachable at {MONGODB_URI}; skipping ApiaryService suite "
        "(mongomock-motor not installed).",
        allow_module_level=True,
    )

# Imported only after the skip guard + env overrides so a DB-less collection
# does not pay the import cost / side effects.
from fastapi import HTTPException, status  # noqa: E402

from app.models import Apiary, ApiaryStatus, Hive, HiveStatus  # noqa: E402
from app.models import DOMAIN_DOCUMENTS  # noqa: E402
from app.feed_sources import FEED_SOURCES  # noqa: E402
from app.repositories.apiary_repository import ApiaryRepository  # noqa: E402
from app.repositories.hive_repository import HiveRepository  # noqa: E402
from app.schemas import ApiaryCreate, ApiaryResponse, ApiaryUpdate  # noqa: E402
from app.services.apiary_service import ApiaryService  # noqa: E402
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

    Keeps the Counter / .count() aggregations deterministic by guaranteeing the
    only documents present are the ones a test creates.
    """
    db = get_client()[settings.MONGODB_DB]
    await db["apiaries"].delete_many({})
    await db["hives"].delete_many({})
    yield
    await db["apiaries"].delete_many({})
    await db["hives"].delete_many({})


@pytest.fixture
def service() -> ApiaryService:
    return ApiaryService()


@pytest.fixture
def apiary_repo() -> ApiaryRepository:
    return ApiaryRepository()


@pytest.fixture
def hive_repo() -> HiveRepository:
    return HiveRepository()


# --- Factory helpers -----------------------------------------------------------
# Only invoked inside test bodies (after init_beanie has run via initialized_db);
# Beanie Documents cannot be instantiated before init.


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


# --- hive_count aggregation tests ----------------------------------------------


@pytest.mark.asyncio
async def test_get_all_apiaries_hive_counts(service, apiary_repo, hive_repo):
    """get_all_apiaries reports the Counter-aggregated hive_count per apiary,
    including 0 for an apiary that has no hives."""
    apiary_a = await apiary_repo.create(make_apiary(name="Apiary A"))
    apiary_b = await apiary_repo.create(make_apiary(name="Apiary B"))
    apiary_empty = await apiary_repo.create(make_apiary(name="Empty Apiary"))

    # A gets 2 hives, B gets 1, empty gets none.
    await hive_repo.create(make_hive(apiary_a.id, name="A-1"))
    await hive_repo.create(make_hive(apiary_a.id, name="A-2"))
    await hive_repo.create(make_hive(apiary_b.id, name="B-1"))

    responses = await service.get_all_apiaries()
    assert all(isinstance(r, ApiaryResponse) for r in responses)

    counts = {r.id: r.hive_count for r in responses}
    assert counts[apiary_a.id] == 2
    assert counts[apiary_b.id] == 1
    assert counts[apiary_empty.id] == 0


@pytest.mark.asyncio
async def test_get_apiary_counts_hives(service, apiary_repo, hive_repo):
    """get_apiary returns the correct hive_count via Hive.find(...).count()."""
    apiary = await apiary_repo.create(make_apiary())
    other = await apiary_repo.create(make_apiary(name="Other"))

    await hive_repo.create(make_hive(apiary.id, name="H-1"))
    await hive_repo.create(make_hive(apiary.id, name="H-2"))
    await hive_repo.create(make_hive(apiary.id, name="H-3"))
    # A hive on a *different* apiary must NOT be counted.
    await hive_repo.create(make_hive(other.id, name="Other-1"))

    response = await service.get_apiary(apiary.id)
    assert isinstance(response, ApiaryResponse)
    assert response.id == apiary.id
    assert response.hive_count == 3


@pytest.mark.asyncio
async def test_create_apiary_starts_with_zero_hives(service):
    """A freshly created apiary reports hive_count == 0."""
    apiary_id = str(uuid.uuid4())
    create_data = ApiaryCreate(
        name="Brand New",
        location="Nowhere",
        latitude=1.0,
        longitude=2.0,
    )

    response = await service.create_apiary(create_data, apiary_id)
    assert isinstance(response, ApiaryResponse)
    assert response.id == apiary_id
    assert response.name == "Brand New"
    assert response.hive_count == 0

    # And it is persisted: fetching it back also reports 0 hives.
    refetched = await service.get_apiary(apiary_id)
    assert refetched.hive_count == 0


# --- 404 paths -----------------------------------------------------------------


@pytest.mark.asyncio
async def test_get_apiary_not_found_raises_404(service):
    """Unknown id -> HTTPException 404 from get_apiary."""
    with pytest.raises(HTTPException) as exc_info:
        await service.get_apiary(str(uuid.uuid4()))
    assert exc_info.value.status_code == status.HTTP_404_NOT_FOUND


@pytest.mark.asyncio
async def test_update_apiary_not_found_raises_404(service):
    """Unknown id -> HTTPException 404 from update_apiary."""
    with pytest.raises(HTTPException) as exc_info:
        await service.update_apiary(
            str(uuid.uuid4()), ApiaryUpdate(name="Renamed")
        )
    assert exc_info.value.status_code == status.HTTP_404_NOT_FOUND


@pytest.mark.asyncio
async def test_delete_apiary_not_found_raises_404(service):
    """Unknown id -> HTTPException 404 from delete_apiary."""
    with pytest.raises(HTTPException) as exc_info:
        await service.delete_apiary(str(uuid.uuid4()))
    assert exc_info.value.status_code == status.HTTP_404_NOT_FOUND
