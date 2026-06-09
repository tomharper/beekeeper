"""P0 repository tests for ``app.repositories.inspection_repository.InspectionRepository``.

This is the most query-heavy DAO in the post-Mongo-migration beekeeper backend.
It exercises the real query logic: equality filters, ``-inspection_date`` desc
sort, ``get_latest_for_hive`` (``find_one`` + sort), ``get_recent`` limit, and the
follow-feed (``In(user_ids)`` + ``is_public == True`` + ``before`` cursor + limit),
plus CRUD.

Infra note
----------
The shared ``tests/conftest.py`` now owns the async-Mongo lifecycle: the test-DB
env overrides, the Mongo-reachability skip guard, ``asyncio_mode=auto``, and the
function-scoped ``init_core`` fixture (Beanie init across both DBs + per-test
collection wipe). Each DB test here depends on that ``init_core`` fixture rather
than a local bootstrap.

A local ``make_inspection`` factory is kept (rather than reusing the conftest
``inspection_factory``) because these tests assert on specific defaults
(``user_id == "user-1"`` / ``hive_id == "hive-1"``) and build with a *tz-aware*
``inspection_date`` so the naive-UTC round-trip handled by ``_as_utc`` compares
equal — the conftest factory uses random-uuid ids and ``utcnow()`` instead.
"""
import uuid
from datetime import datetime, timedelta, timezone

import pytest

from app.models import Inspection  # noqa: E402
from app.repositories.inspection_repository import InspectionRepository  # noqa: E402


# --------------------------------------------------------------------------- #
# Fixtures
# --------------------------------------------------------------------------- #
@pytest.fixture
def repo() -> InspectionRepository:
    return InspectionRepository()


# --------------------------------------------------------------------------- #
# Helpers
# --------------------------------------------------------------------------- #
def _utc(*args) -> datetime:
    return datetime(*args, tzinfo=timezone.utc)


def _as_utc(value: datetime) -> datetime:
    """Normalise to tz-aware UTC for comparison.

    Motor/Beanie deserialise BSON datetimes as **naive** UTC (the client is not
    ``tz_aware=True``), so a round-tripped ``inspection_date`` comes back without
    tzinfo. Treat a naive value as already-UTC so it compares equal to the
    tz-aware datetimes the tests build with ``_utc(...)``.
    """
    if value.tzinfo is None:
        return value.replace(tzinfo=timezone.utc)
    return value.astimezone(timezone.utc)


def make_inspection(
    *,
    user_id: str = "user-1",
    hive_id: str = "hive-1",
    inspection_date: datetime | None = None,
    is_public: bool = True,
) -> Inspection:
    """Build a valid Inspection with a uuid string id and required fields set.

    Only the fields the repository queries on are parametrised; everything else
    relies on the model's defaults.
    """
    return Inspection(
        id=str(uuid.uuid4()),
        hive_id=hive_id,
        user_id=user_id,
        inspection_date=inspection_date or _utc(2026, 1, 1, 12, 0, 0),
        is_public=is_public,
    )


# --------------------------------------------------------------------------- #
# CRUD
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_create_and_get_by_id(init_core, repo: InspectionRepository):
    insp = make_inspection()
    created = await repo.create(insp)
    assert created.id == insp.id

    fetched = await repo.get_by_id(insp.id)
    assert fetched is not None
    assert fetched.id == insp.id
    assert fetched.user_id == "user-1"
    assert fetched.hive_id == "hive-1"


@pytest.mark.asyncio
async def test_get_by_id_missing_returns_none(init_core, repo: InspectionRepository):
    assert await repo.get_by_id(str(uuid.uuid4())) is None


@pytest.mark.asyncio
async def test_update_persists(init_core, repo: InspectionRepository):
    insp = await repo.create(make_inspection())
    insp.notes = "varroa check next week"
    insp.duration_minutes = 25
    await repo.update(insp)

    refetched = await repo.get_by_id(insp.id)
    assert refetched is not None
    assert refetched.notes == "varroa check next week"
    assert refetched.duration_minutes == 25


@pytest.mark.asyncio
async def test_delete_removes(init_core, repo: InspectionRepository):
    insp = await repo.create(make_inspection())
    assert await repo.get_by_id(insp.id) is not None

    await repo.delete(insp)
    assert await repo.get_by_id(insp.id) is None


# --------------------------------------------------------------------------- #
# Sorted listing / filters
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_get_all_sorted_desc(init_core, repo: InspectionRepository):
    d_old = _utc(2026, 1, 1)
    d_mid = _utc(2026, 2, 1)
    d_new = _utc(2026, 3, 1)
    # Insert out of order to prove the sort actually runs.
    await repo.create(make_inspection(inspection_date=d_mid))
    await repo.create(make_inspection(inspection_date=d_new))
    await repo.create(make_inspection(inspection_date=d_old))

    results = await repo.get_all()
    dates = [_as_utc(r.inspection_date) for r in results]
    assert dates == [d_new, d_mid, d_old]


@pytest.mark.asyncio
async def test_get_by_user_id_filters_and_sorts(init_core, repo: InspectionRepository):
    d_old = _utc(2026, 1, 1)
    d_new = _utc(2026, 2, 1)
    await repo.create(make_inspection(user_id="user-A", inspection_date=d_old))
    await repo.create(make_inspection(user_id="user-A", inspection_date=d_new))
    await repo.create(make_inspection(user_id="user-B", inspection_date=d_new))

    results = await repo.get_by_user_id("user-A")
    assert len(results) == 2
    assert all(r.user_id == "user-A" for r in results)
    assert [_as_utc(r.inspection_date) for r in results] == [d_new, d_old]


@pytest.mark.asyncio
async def test_get_by_hive_id_filters_and_sorts(init_core, repo: InspectionRepository):
    d_old = _utc(2026, 1, 1)
    d_new = _utc(2026, 2, 1)
    await repo.create(make_inspection(hive_id="hive-A", inspection_date=d_old))
    await repo.create(make_inspection(hive_id="hive-A", inspection_date=d_new))
    await repo.create(make_inspection(hive_id="hive-B", inspection_date=d_new))

    results = await repo.get_by_hive_id("hive-A")
    assert len(results) == 2
    assert all(r.hive_id == "hive-A" for r in results)
    assert [_as_utc(r.inspection_date) for r in results] == [d_new, d_old]


@pytest.mark.asyncio
async def test_get_by_hive_and_user(init_core, repo: InspectionRepository):
    target = _utc(2026, 3, 1)
    # Matches both filters.
    match = await repo.create(
        make_inspection(hive_id="hive-X", user_id="user-X", inspection_date=target)
    )
    # Same hive, wrong user.
    await repo.create(make_inspection(hive_id="hive-X", user_id="user-Y"))
    # Same user, wrong hive.
    await repo.create(make_inspection(hive_id="hive-Z", user_id="user-X"))

    results = await repo.get_by_hive_and_user("hive-X", "user-X")
    assert len(results) == 1
    assert results[0].id == match.id


@pytest.mark.asyncio
async def test_get_latest_for_hive(init_core, repo: InspectionRepository):
    d_old = _utc(2026, 1, 1)
    d_new = _utc(2026, 6, 1)
    await repo.create(make_inspection(hive_id="hive-L", inspection_date=d_old))
    newest = await repo.create(
        make_inspection(hive_id="hive-L", inspection_date=d_new)
    )
    # A different hive should never bleed into the result.
    await repo.create(
        make_inspection(hive_id="hive-other", inspection_date=_utc(2026, 12, 1))
    )

    latest = await repo.get_latest_for_hive("hive-L")
    assert latest is not None
    assert latest.id == newest.id
    assert _as_utc(latest.inspection_date) == d_new


@pytest.mark.asyncio
async def test_get_latest_for_hive_none_when_empty(init_core, repo: InspectionRepository):
    await repo.create(make_inspection(hive_id="hive-has-data"))
    assert await repo.get_latest_for_hive("hive-no-data") is None


# --------------------------------------------------------------------------- #
# get_recent
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_get_recent_limit(init_core, repo: InspectionRepository):
    base = _utc(2026, 1, 1)
    # 5 inspections, ascending dates so newest is day 5.
    for i in range(5):
        await repo.create(
            make_inspection(user_id="user-R", inspection_date=base + timedelta(days=i))
        )

    results = await repo.get_recent("user-R", limit=2)
    assert len(results) == 2
    # Newest-first: the two latest days.
    assert _as_utc(results[0].inspection_date) == base + timedelta(days=4)
    assert _as_utc(results[1].inspection_date) == base + timedelta(days=3)


@pytest.mark.asyncio
async def test_get_recent_default_limit_10(init_core, repo: InspectionRepository):
    base = _utc(2026, 1, 1)
    for i in range(15):
        await repo.create(
            make_inspection(user_id="user-R", inspection_date=base + timedelta(days=i))
        )

    results = await repo.get_recent("user-R")
    assert len(results) == 10
    # Capped at the 10 newest, newest first.
    assert _as_utc(results[0].inspection_date) == base + timedelta(days=14)
    assert _as_utc(results[-1].inspection_date) == base + timedelta(days=5)


# --------------------------------------------------------------------------- #
# get_feed: In(user_ids) + is_public + limit + before cursor
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_get_feed_only_public(init_core, repo: InspectionRepository):
    pub = await repo.create(make_inspection(user_id="user-F", is_public=True))
    await repo.create(make_inspection(user_id="user-F", is_public=False))

    results = await repo.get_feed(["user-F"])
    assert len(results) == 1
    assert results[0].id == pub.id
    assert all(r.is_public is True for r in results)


@pytest.mark.asyncio
async def test_get_feed_in_user_ids(init_core, repo: InspectionRepository):
    a = await repo.create(make_inspection(user_id="user-A"))
    b = await repo.create(make_inspection(user_id="user-B"))
    await repo.create(make_inspection(user_id="user-C"))  # not in the list

    results = await repo.get_feed(["user-A", "user-B"])
    got_ids = {r.id for r in results}
    assert got_ids == {a.id, b.id}
    assert all(r.user_id in {"user-A", "user-B"} for r in results)


@pytest.mark.asyncio
async def test_get_feed_limit(init_core, repo: InspectionRepository):
    base = _utc(2026, 1, 1)
    for i in range(5):
        await repo.create(
            make_inspection(user_id="user-F", inspection_date=base + timedelta(days=i))
        )

    results = await repo.get_feed(["user-F"], limit=3)
    assert len(results) == 3
    # Newest-first.
    assert _as_utc(results[0].inspection_date) == base + timedelta(days=4)


@pytest.mark.asyncio
async def test_get_feed_before_cursor(init_core, repo: InspectionRepository):
    base = _utc(2026, 1, 1)
    dates = [base + timedelta(days=i) for i in range(6)]  # day0..day5
    for d in dates:
        await repo.create(make_inspection(user_id="user-F", inspection_date=d))

    # First page: 3 newest -> day5, day4, day3.
    page1 = await repo.get_feed(["user-F"], limit=3)
    assert [_as_utc(r.inspection_date) for r in page1] == [dates[5], dates[4], dates[3]]

    # Second page: strictly older than the last item of page1 (day3).
    cursor = page1[-1].inspection_date
    page2 = await repo.get_feed(["user-F"], limit=3, before=cursor)
    assert [_as_utc(r.inspection_date) for r in page2] == [dates[2], dates[1], dates[0]]

    # `before` is strict (<), so the cursor record itself is excluded:
    # no overlap and no gap across the two pages.
    ids1 = {r.id for r in page1}
    ids2 = {r.id for r in page2}
    assert ids1.isdisjoint(ids2)
    assert len(ids1 | ids2) == 6
    assert all(r.inspection_date < cursor for r in page2)
