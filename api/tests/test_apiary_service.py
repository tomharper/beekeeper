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
``make_apiary`` / ``make_hive`` factory helpers. This file now depends on that
conftest ``init_core`` fixture for Beanie init + per-test cleanup (it wipes all
domain collections before and after each test, so the local apiaries/hives
cleanup is no longer needed). The skip-when-no-Mongo behaviour is carried by
``init_core`` itself (it ``pytest.skip``s when no Mongo is reachable).

  * The local ``make_apiary`` / ``make_hive`` factories are KEPT (not swapped for
    the conftest factory fixtures): this file's ``make_apiary`` sets
    ``latitude``/``longitude`` and its ``make_hive`` sets a tz-aware
    ``last_inspected``, which the conftest factories do not, so keeping them
    preserves the exact documents these tests persist.
  * Beanie Documents can't be instantiated before init_beanie runs, so the
    ``make_apiary`` / ``make_hive`` builders are only called inside test bodies
    (after ``init_core`` has run).
"""
import uuid
from datetime import datetime, timezone

import pytest

from fastapi import HTTPException, status

from app.models import Apiary, ApiaryStatus, Hive, HiveStatus
from app.repositories.apiary_repository import ApiaryRepository
from app.repositories.hive_repository import HiveRepository
from app.schemas import ApiaryCreate, ApiaryResponse, ApiaryUpdate
from app.services.apiary_service import ApiaryService


# --- Fixtures ------------------------------------------------------------------


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
# Only invoked inside test bodies (after init_beanie has run via init_core);
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
async def test_get_all_apiaries_hive_counts(init_core, service, apiary_repo, hive_repo):
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
async def test_get_apiary_counts_hives(init_core, service, apiary_repo, hive_repo):
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
async def test_create_apiary_starts_with_zero_hives(init_core, service):
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
async def test_get_apiary_not_found_raises_404(init_core, service):
    """Unknown id -> HTTPException 404 from get_apiary."""
    with pytest.raises(HTTPException) as exc_info:
        await service.get_apiary(str(uuid.uuid4()))
    assert exc_info.value.status_code == status.HTTP_404_NOT_FOUND


@pytest.mark.asyncio
async def test_update_apiary_not_found_raises_404(init_core, service):
    """Unknown id -> HTTPException 404 from update_apiary."""
    with pytest.raises(HTTPException) as exc_info:
        await service.update_apiary(
            str(uuid.uuid4()), ApiaryUpdate(name="Renamed")
        )
    assert exc_info.value.status_code == status.HTTP_404_NOT_FOUND


@pytest.mark.asyncio
async def test_delete_apiary_not_found_raises_404(init_core, service):
    """Unknown id -> HTTPException 404 from delete_apiary."""
    with pytest.raises(HTTPException) as exc_info:
        await service.delete_apiary(str(uuid.uuid4()))
    assert exc_info.value.status_code == status.HTTP_404_NOT_FOUND
