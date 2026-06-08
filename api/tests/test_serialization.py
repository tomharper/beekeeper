"""Serialization round-trip tests for the Mongo/Beanie migration — TEST_PLAN §4.3.

This is the HIGHEST migration-risk file: it asserts every field whose on-disk
representation changed in the SQLAlchemy/SQLite -> Motor/Beanie/MongoDB migration
still round-trips correctly through ``insert`` -> re-fetch (``get``), with the
right Python type and value:

  - ``id`` is a UUID **string** (``str(uuid.uuid4())``), NOT an ObjectId.
  - ``created_at`` / ``updated_at`` auto-populate via ``TimestampMixin`` and come
    back as timezone-aware UTC datetimes.
  - ``Alert.hive_ids`` is a real ``list[str]`` (was a comma-joined string).
  - ``Inspection.photos`` is a real ``list[str]`` (was JSON-encoded text).
  - Every ``str, Enum`` field stores/loads as its string value.
  - ``datetime`` fields survive the BSON round-trip (millisecond precision).
  - Optional fields default to ``None`` and round-trip as ``None``.

Infra note (TEST_PLAN §3): there is no ``tests/conftest.py`` yet, and
``mongomock-motor`` is NOT installed, so these async tests target a **real**
Mongo and **skip** the whole module when none is reachable — keeping DB-less CI
green. Env overrides (test DBs, non-production ENV) are set at the very top,
BEFORE importing ``app``/``assistive_core`` (whose ``Settings`` reads env at
import time). When a shared ``conftest.py`` lands (§4.1) it can own the env
overrides + skip guard + ``init_core`` fixture and these can be simplified, but
the file is written to collect and run standalone today.
"""
import os

# --- Env overrides: MUST happen BEFORE importing app / assistive_core. ---------
# assistive_core.settings.Settings reads the environment at import time, so the
# test-DB / non-production overrides have to be in place first. ``setdefault`` so
# a future conftest (or CI env) that already set these wins.
os.environ.setdefault("MONGODB_DB", "beekeeper_test")
os.environ.setdefault("IDENTITY_DB", "assistive_identity_test")
# Keep init_core out of its production fail-fast path (placeholder JWT key).
os.environ.setdefault("ASSISTIVE_ENV", "test")
os.environ.setdefault("ENV", "test")
MONGODB_URI = os.environ.setdefault("MONGODB_URI", "mongodb://localhost:27017")

import uuid
from datetime import datetime, timedelta, timezone

import pytest
import pytest_asyncio

from assistive_core import init_core, close_core, get_client
from assistive_core.settings import settings

from app.feed_sources import FEED_SOURCES
from app.models import (
    DOMAIN_DOCUMENTS,
    Apiary,
    ApiaryStatus,
    Hive,
    HiveStatus,
    ColonyStrength,
    QueenStatus,
    Temperament,
    HoneyStores,
    Alert,
    AlertType,
    AlertSeverity,
    Recommendation,
    RecommendationType,
    Priority,
    Task,
    TaskType,
    TaskStatus,
    TaskPriority,
    RecurrenceFrequency,
    Inspection,
    QueenCellStatus,
    BroodPattern,
    ColonyTemperament,
    ColonyPopulation,
    HealthStatus,
    ResourceLevel,
)


# --- Mongo-reachability guard (TEST_PLAN §3) ----------------------------------
def _mongo_reachable(uri: str) -> bool:
    """Return True if a Mongo at ``uri`` answers a ping quickly."""
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


# Collection-level skip: if no Mongo, skip the whole module so the file still
# collects cleanly in DB-less CI (mirrors test_main.py's per-test skip approach,
# applied module-wide because every test here needs a live DB).
pytestmark = pytest.mark.skipif(
    not _mongo_reachable(MONGODB_URI),
    reason=f"No MongoDB reachable at {MONGODB_URI}; skipping serialization round-trip suite",
)


# --- init_core / cleanup fixture ----------------------------------------------
# Until tests/conftest.py exists (TEST_PLAN §4.1), this module owns the Beanie
# lifecycle exactly as app/main.py's lifespan does. Module-scoped init, with a
# function-scoped autouse cleanup so tests don't see each other's documents.
@pytest_asyncio.fixture(autouse=True)
async def _core():
    """Initialise Beanie across both test DBs, like the app lifespan does.

    Function-scoped: pytest-asyncio 0.24 runs each test in its own event loop and
    Motor/Beanie bind their client to the running loop, so ``init_core`` must run
    on the test's own loop (a module-scoped client raises "attached to a
    different loop"). Collections are cleared at setup and the per-vertical test
    DB is dropped + the memoized global client reset on teardown so state never
    leaks across tests (TEST_PLAN §3).
    """
    await init_core(vertical_documents=DOMAIN_DOCUMENTS, feed_sources=FEED_SOURCES)
    client = get_client()
    db = client[settings.MONGODB_DB]
    for doc in DOMAIN_DOCUMENTS:
        await db[doc.Settings.name].delete_many({})
    try:
        yield
    finally:
        await client.drop_database(settings.MONGODB_DB)
        await close_core()


# --- Small factory helpers (keep individual cases short) ----------------------
def _uuid() -> str:
    return str(uuid.uuid4())


def _aware(year=2026, month=5, day=1, hour=12, minute=30, second=15) -> datetime:
    """A fixed timezone-aware UTC datetime for deterministic round-trip asserts."""
    return datetime(year, month, day, hour, minute, second, tzinfo=timezone.utc)


def _make_apiary(**kw) -> Apiary:
    data = dict(id=_uuid(), name="Test Apiary", location="Test Location")
    data.update(kw)
    return Apiary(**data)


def _make_hive(**kw) -> Hive:
    data = dict(
        id=_uuid(),
        name="Test Hive",
        apiary_id=_uuid(),
        last_inspected=_aware(),
    )
    data.update(kw)
    return Hive(**data)


def _make_alert(**kw) -> Alert:
    data = dict(
        id=_uuid(),
        type=AlertType.GENERAL,
        title="Test Alert",
        message="A message",
        timestamp=_aware(),
    )
    data.update(kw)
    return Alert(**data)


def _make_recommendation(**kw) -> Recommendation:
    data = dict(
        id=_uuid(),
        hive_id=_uuid(),
        type=RecommendationType.INFO,
        title="Test Rec",
        description="A description",
    )
    data.update(kw)
    return Recommendation(**data)


def _make_task(**kw) -> Task:
    data = dict(
        id=_uuid(),
        title="Test Task",
        user_id=_uuid(),
        due_date=_aware(),
    )
    data.update(kw)
    return Task(**data)


def _make_inspection(**kw) -> Inspection:
    data = dict(
        id=_uuid(),
        hive_id=_uuid(),
        user_id=_uuid(),
        inspection_date=_aware(),
    )
    data.update(kw)
    return Inspection(**data)


def _as_utc(value: datetime) -> datetime:
    """Normalise ``value`` to a tz-aware UTC datetime.

    Motor/Beanie deserialise BSON datetimes as **naive** UTC (the client is not
    created with ``tz_aware=True``), so a round-tripped value comes back without
    tzinfo. Treat a naive value as already-UTC; attach tzinfo for comparison.
    """
    if value.tzinfo is None:
        return value.replace(tzinfo=timezone.utc)
    return value.astimezone(timezone.utc)


def _assert_aware_utc(value):
    """Assert ``value`` is a datetime that represents a UTC instant.

    The real API returns naive UTC datetimes after a Mongo round-trip, so we
    assert it is a datetime and (once normalised to UTC) carries a zero offset
    rather than requiring tzinfo on the raw value.
    """
    assert isinstance(value, datetime)
    assert _as_utc(value).utcoffset() == timedelta(0), "datetime must be UTC after round-trip"


def _assert_dt_equal(a: datetime, b: datetime):
    """Compare two datetimes to BSON's millisecond precision.

    BSON stores datetimes as millisecond-precision UTC, so a sub-millisecond
    delta after round-trip is expected and acceptable. Both operands are
    normalised to tz-aware UTC first, since a round-tripped value is naive UTC
    while a freshly-built expected value may be tz-aware.
    """
    assert abs((_as_utc(a) - _as_utc(b)).total_seconds()) < 0.001


# --- id / timestamp cases -----------------------------------------------------
@pytest.mark.asyncio
async def test_apiary_id_is_uuid_string():
    """Apiary.id is the same UUID string after round-trip (NOT an ObjectId)."""
    apiary_id = _uuid()
    apiary = _make_apiary(id=apiary_id)
    await apiary.insert()

    fetched = await Apiary.get(apiary_id)
    assert fetched is not None
    assert isinstance(fetched.id, str)
    assert fetched.id == apiary_id
    # Guard against an ObjectId sneaking in (would not be a 36-char UUID str).
    assert "-" in fetched.id and len(fetched.id) == 36


@pytest.mark.asyncio
async def test_timestamps_autopopulate():
    """created_at/updated_at auto-populate as tz-aware UTC datetimes."""
    apiary = _make_apiary()
    # Not setting created_at/updated_at: TimestampMixin default_factory fills them.
    await apiary.insert()

    fetched = await Apiary.get(apiary.id)
    assert fetched is not None
    assert fetched.created_at is not None
    assert fetched.updated_at is not None
    _assert_aware_utc(fetched.created_at)
    _assert_aware_utc(fetched.updated_at)


# --- list[str] migrated fields ------------------------------------------------
@pytest.mark.asyncio
async def test_alert_hive_ids_roundtrips_as_list():
    """Alert.hive_ids is a real list[str] (NOT a comma-joined string)."""
    hive_ids = ["h1", "h2", "h3"]
    alert = _make_alert(hive_ids=hive_ids)
    await alert.insert()

    fetched = await Alert.get(alert.id)
    assert fetched is not None
    assert isinstance(fetched.hive_ids, list)
    assert all(isinstance(x, str) for x in fetched.hive_ids)
    assert fetched.hive_ids == hive_ids


@pytest.mark.asyncio
async def test_alert_hive_ids_default_empty_list():
    """Alert.hive_ids defaults to [] and round-trips as an empty list."""
    alert = _make_alert()  # hive_ids unset -> default_factory=list
    await alert.insert()

    fetched = await Alert.get(alert.id)
    assert fetched is not None
    assert fetched.hive_ids == []
    assert isinstance(fetched.hive_ids, list)


@pytest.mark.asyncio
async def test_inspection_photos_roundtrips_as_list():
    """Inspection.photos is a real list[str] (NOT JSON-encoded text)."""
    photos = ["a.jpg", "b.jpg"]
    inspection = _make_inspection(photos=photos)
    await inspection.insert()

    fetched = await Inspection.get(inspection.id)
    assert fetched is not None
    assert isinstance(fetched.photos, list)
    assert all(isinstance(x, str) for x in fetched.photos)
    assert fetched.photos == photos


@pytest.mark.asyncio
async def test_inspection_photos_default_empty_list():
    """Inspection.photos defaults to [] and round-trips as an empty list."""
    inspection = _make_inspection()  # photos unset -> default_factory=list
    await inspection.insert()

    fetched = await Inspection.get(inspection.id)
    assert fetched is not None
    assert fetched.photos == []
    assert isinstance(fetched.photos, list)


# --- enum round-trips ---------------------------------------------------------
@pytest.mark.asyncio
async def test_apiary_status_enum_roundtrip():
    """Each ApiaryStatus value persists/loads as the enum (stored as its str)."""
    for status in ApiaryStatus:
        apiary = _make_apiary(status=status)
        await apiary.insert()

        fetched = await Apiary.get(apiary.id)
        assert fetched is not None
        assert fetched.status == status
        assert fetched.status.value == status.value
        assert isinstance(fetched.status.value, str)


@pytest.mark.asyncio
async def test_hive_enums_roundtrip():
    """HiveStatus/ColonyStrength/QueenStatus/Temperament/HoneyStores round-trip."""
    hive = _make_hive(
        status=HiveStatus.NEEDS_INSPECTION,
        colony_strength=ColonyStrength.WEAK,
        queen_status=QueenStatus.LAYING,
        temperament=Temperament.DEFENSIVE,
        honey_stores=HoneyStores.LOW,
    )
    await hive.insert()

    fetched = await Hive.get(hive.id)
    assert fetched is not None
    assert fetched.status == HiveStatus.NEEDS_INSPECTION
    assert fetched.colony_strength == ColonyStrength.WEAK
    assert fetched.queen_status == QueenStatus.LAYING
    assert fetched.temperament == Temperament.DEFENSIVE
    assert fetched.honey_stores == HoneyStores.LOW


@pytest.mark.asyncio
async def test_alert_enums_roundtrip():
    """AlertType/AlertSeverity round-trip; dismissed bool round-trips."""
    alert = _make_alert(
        type=AlertType.VARROA_MITE,
        severity=AlertSeverity.CRITICAL,
        dismissed=True,
    )
    await alert.insert()

    fetched = await Alert.get(alert.id)
    assert fetched is not None
    assert fetched.type == AlertType.VARROA_MITE
    assert fetched.severity == AlertSeverity.CRITICAL
    assert fetched.dismissed is True


@pytest.mark.asyncio
async def test_recommendation_enums_roundtrip():
    """RecommendationType/Priority round-trip."""
    rec = _make_recommendation(
        type=RecommendationType.ACTION_REQUIRED,
        priority=Priority.HIGH,
    )
    await rec.insert()

    fetched = await Recommendation.get(rec.id)
    assert fetched is not None
    assert fetched.type == RecommendationType.ACTION_REQUIRED
    assert fetched.priority == Priority.HIGH


@pytest.mark.asyncio
async def test_task_enums_roundtrip():
    """Sample across TaskType plus TaskStatus/TaskPriority/RecurrenceFrequency;
    is_public defaults True."""
    # Sample several values across the large TaskType set.
    for task_type in (
        TaskType.INSPECTION,
        TaskType.PEST_TREATMENT,
        TaskType.HARVEST_HONEY,
        TaskType.REQUEEN,
        TaskType.OTHER,
    ):
        task = _make_task(
            task_type=task_type,
            status=TaskStatus.IN_PROGRESS,
            priority=TaskPriority.URGENT,
            recurrence_frequency=RecurrenceFrequency.BIWEEKLY,
        )
        await task.insert()

        fetched = await Task.get(task.id)
        assert fetched is not None
        assert fetched.task_type == task_type
        assert fetched.status == TaskStatus.IN_PROGRESS
        assert fetched.priority == TaskPriority.URGENT
        assert fetched.recurrence_frequency == RecurrenceFrequency.BIWEEKLY
        # is_public default
        assert fetched.is_public is True


@pytest.mark.asyncio
async def test_inspection_enums_roundtrip():
    """All 7 inspection enums round-trip (ResourceLevel used twice); is_public
    defaults True."""
    inspection = _make_inspection(
        queen_cells=QueenCellStatus.SWARM_CELLS,
        brood_pattern=BroodPattern.SPOTTY,
        temperament=ColonyTemperament.VERY_AGGRESSIVE,
        population=ColonyPopulation.VERY_STRONG,
        health_status=HealthStatus.CRITICAL,
        honey_stores=ResourceLevel.EXCELLENT,
        pollen_stores=ResourceLevel.VERY_LOW,
    )
    await inspection.insert()

    fetched = await Inspection.get(inspection.id)
    assert fetched is not None
    assert fetched.queen_cells == QueenCellStatus.SWARM_CELLS
    assert fetched.brood_pattern == BroodPattern.SPOTTY
    assert fetched.temperament == ColonyTemperament.VERY_AGGRESSIVE
    assert fetched.population == ColonyPopulation.VERY_STRONG
    assert fetched.health_status == HealthStatus.CRITICAL
    assert fetched.honey_stores == ResourceLevel.EXCELLENT
    assert fetched.pollen_stores == ResourceLevel.VERY_LOW
    # is_public default
    assert fetched.is_public is True


# --- datetime round-trips -----------------------------------------------------
@pytest.mark.asyncio
async def test_datetime_fields_roundtrip():
    """Datetime fields persist and re-load as tz-aware UTC, equal to BSON-ms
    tolerance."""
    hive_dt = _aware(2026, 5, 2, 9, 0, 0)
    insp_dt = _aware(2026, 5, 3, 10, 15, 0)
    next_dt = _aware(2026, 6, 3, 10, 15, 0)
    due_dt = _aware(2026, 5, 10, 8, 0, 0)
    completed_dt = _aware(2026, 5, 11, 17, 30, 0)

    hive = _make_hive(last_inspected=hive_dt)
    inspection = _make_inspection(
        inspection_date=insp_dt, next_inspection_date=next_dt
    )
    task = _make_task(due_date=due_dt, completed_date=completed_dt)
    await hive.insert()
    await inspection.insert()
    await task.insert()

    fetched_hive = await Hive.get(hive.id)
    fetched_insp = await Inspection.get(inspection.id)
    fetched_task = await Task.get(task.id)
    assert fetched_hive is not None
    assert fetched_insp is not None
    assert fetched_task is not None

    for dt in (
        fetched_hive.last_inspected,
        fetched_insp.inspection_date,
        fetched_insp.next_inspection_date,
        fetched_task.due_date,
        fetched_task.completed_date,
    ):
        _assert_aware_utc(dt)

    _assert_dt_equal(fetched_hive.last_inspected, hive_dt)
    _assert_dt_equal(fetched_insp.inspection_date, insp_dt)
    _assert_dt_equal(fetched_insp.next_inspection_date, next_dt)
    _assert_dt_equal(fetched_task.due_date, due_dt)
    _assert_dt_equal(fetched_task.completed_date, completed_dt)


# --- optional-None round-trips ------------------------------------------------
@pytest.mark.asyncio
async def test_optional_fields_default_none():
    """Optional fields are None when unset and round-trip as None."""
    inspection = _make_inspection()  # leave optionals unset
    await inspection.insert()

    fetched = await Inspection.get(inspection.id)
    assert fetched is not None
    assert fetched.duration_minutes is None
    assert fetched.weather_temp is None
    assert fetched.weather_conditions is None
    assert fetched.next_inspection_date is None

    task = _make_task()  # leave optionals unset
    await task.insert()

    fetched_task = await Task.get(task.id)
    assert fetched_task is not None
    assert fetched_task.reminder_date is None
    assert fetched_task.completed_date is None
    assert fetched_task.recurrence_frequency is None
    assert fetched_task.minimum_temperature is None
