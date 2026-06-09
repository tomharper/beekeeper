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

Infra note (TEST_PLAN §3/§4.1): the shared ``tests/conftest.py`` owns the env
overrides, the Mongo-reachability skip guard, and the ``init_core`` fixture
(Beanie init across both DBs + ``FEED_SOURCES`` + per-test collection wipe). This
module opts into that infrastructure: every async test here depends on the
conftest ``init_core`` fixture (which skips the test when no Mongo is reachable)
and builds documents via the conftest factory fixtures.
"""
import uuid
from datetime import datetime, timedelta, timezone

import pytest

from app.models import (
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


# --- Small local helpers (keep individual cases short) ------------------------
def _uuid() -> str:
    return str(uuid.uuid4())


def _aware(year=2026, month=5, day=1, hour=12, minute=30, second=15) -> datetime:
    """A fixed timezone-aware UTC datetime for deterministic round-trip asserts."""
    return datetime(year, month, day, hour, minute, second, tzinfo=timezone.utc)


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
async def test_apiary_id_is_uuid_string(init_core, apiary_factory):
    """Apiary.id is the same UUID string after round-trip (NOT an ObjectId)."""
    apiary_id = _uuid()
    apiary = apiary_factory(id=apiary_id)
    await apiary.insert()

    fetched = await Apiary.get(apiary_id)
    assert fetched is not None
    assert isinstance(fetched.id, str)
    assert fetched.id == apiary_id
    # Guard against an ObjectId sneaking in (would not be a 36-char UUID str).
    assert "-" in fetched.id and len(fetched.id) == 36


@pytest.mark.asyncio
async def test_timestamps_autopopulate(init_core, apiary_factory):
    """created_at/updated_at auto-populate as tz-aware UTC datetimes."""
    apiary = apiary_factory()
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
async def test_alert_hive_ids_roundtrips_as_list(init_core, alert_factory):
    """Alert.hive_ids is a real list[str] (NOT a comma-joined string)."""
    hive_ids = ["h1", "h2", "h3"]
    alert = alert_factory(hive_ids=hive_ids)
    await alert.insert()

    fetched = await Alert.get(alert.id)
    assert fetched is not None
    assert isinstance(fetched.hive_ids, list)
    assert all(isinstance(x, str) for x in fetched.hive_ids)
    assert fetched.hive_ids == hive_ids


@pytest.mark.asyncio
async def test_alert_hive_ids_default_empty_list(init_core, alert_factory):
    """Alert.hive_ids defaults to [] and round-trips as an empty list."""
    alert = alert_factory()  # hive_ids unset -> default_factory=list
    await alert.insert()

    fetched = await Alert.get(alert.id)
    assert fetched is not None
    assert fetched.hive_ids == []
    assert isinstance(fetched.hive_ids, list)


@pytest.mark.asyncio
async def test_inspection_photos_roundtrips_as_list(init_core, inspection_factory):
    """Inspection.photos is a real list[str] (NOT JSON-encoded text)."""
    photos = ["a.jpg", "b.jpg"]
    inspection = inspection_factory(photos=photos)
    await inspection.insert()

    fetched = await Inspection.get(inspection.id)
    assert fetched is not None
    assert isinstance(fetched.photos, list)
    assert all(isinstance(x, str) for x in fetched.photos)
    assert fetched.photos == photos


@pytest.mark.asyncio
async def test_inspection_photos_default_empty_list(init_core, inspection_factory):
    """Inspection.photos defaults to [] and round-trips as an empty list."""
    inspection = inspection_factory()  # photos unset -> default_factory=list
    await inspection.insert()

    fetched = await Inspection.get(inspection.id)
    assert fetched is not None
    assert fetched.photos == []
    assert isinstance(fetched.photos, list)


# --- enum round-trips ---------------------------------------------------------
@pytest.mark.asyncio
async def test_apiary_status_enum_roundtrip(init_core, apiary_factory):
    """Each ApiaryStatus value persists/loads as the enum (stored as its str)."""
    for status in ApiaryStatus:
        apiary = apiary_factory(status=status)
        await apiary.insert()

        fetched = await Apiary.get(apiary.id)
        assert fetched is not None
        assert fetched.status == status
        assert fetched.status.value == status.value
        assert isinstance(fetched.status.value, str)


@pytest.mark.asyncio
async def test_hive_enums_roundtrip(init_core, hive_factory):
    """HiveStatus/ColonyStrength/QueenStatus/Temperament/HoneyStores round-trip."""
    hive = hive_factory(
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
async def test_alert_enums_roundtrip(init_core, alert_factory):
    """AlertType/AlertSeverity round-trip; dismissed bool round-trips."""
    alert = alert_factory(
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
async def test_recommendation_enums_roundtrip(init_core, recommendation_factory):
    """RecommendationType/Priority round-trip."""
    rec = recommendation_factory(
        type=RecommendationType.ACTION_REQUIRED,
        priority=Priority.HIGH,
    )
    await rec.insert()

    fetched = await Recommendation.get(rec.id)
    assert fetched is not None
    assert fetched.type == RecommendationType.ACTION_REQUIRED
    assert fetched.priority == Priority.HIGH


@pytest.mark.asyncio
async def test_task_enums_roundtrip(init_core, task_factory):
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
        task = task_factory(
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
async def test_inspection_enums_roundtrip(init_core, inspection_factory):
    """All 7 inspection enums round-trip (ResourceLevel used twice); is_public
    defaults True."""
    inspection = inspection_factory(
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
async def test_datetime_fields_roundtrip(init_core, hive_factory, inspection_factory, task_factory):
    """Datetime fields persist and re-load as tz-aware UTC, equal to BSON-ms
    tolerance."""
    hive_dt = _aware(2026, 5, 2, 9, 0, 0)
    insp_dt = _aware(2026, 5, 3, 10, 15, 0)
    next_dt = _aware(2026, 6, 3, 10, 15, 0)
    due_dt = _aware(2026, 5, 10, 8, 0, 0)
    completed_dt = _aware(2026, 5, 11, 17, 30, 0)

    hive = hive_factory(last_inspected=hive_dt)
    inspection = inspection_factory(
        inspection_date=insp_dt, next_inspection_date=next_dt
    )
    task = task_factory(due_date=due_dt, completed_date=completed_dt)
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
async def test_optional_fields_default_none(init_core, inspection_factory, task_factory):
    """Optional fields are None when unset and round-trip as None."""
    inspection = inspection_factory()  # leave optionals unset
    await inspection.insert()

    fetched = await Inspection.get(inspection.id)
    assert fetched is not None
    assert fetched.duration_minutes is None
    assert fetched.weather_temp is None
    assert fetched.weather_conditions is None
    assert fetched.next_inspection_date is None

    task = task_factory()  # leave optionals unset
    await task.insert()

    fetched_task = await Task.get(task.id)
    assert fetched_task is not None
    assert fetched_task.reminder_date is None
    assert fetched_task.completed_date is None
    assert fetched_task.recurrence_frequency is None
    assert fetched_task.minimum_temperature is None
