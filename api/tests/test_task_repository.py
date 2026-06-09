"""Async repository tests for ``app.repositories.task_repository.TaskRepository``.

Part of the Mongo/Beanie migration test plan (see
``docs/TEST_PLAN_MONGO_MIGRATION.md`` §4.5). This is a NEW convention the
migration requires: direct ``await Repo().method(...)`` calls against a live
test Mongo, exercising the persistence logic that the existing (synchronous,
route-level) suite never touches.

What this file covers (the migration-affected query/mutation logic):
  - equality filters: ``get_by_user_id`` / ``get_by_hive_id`` / ``get_by_apiary_id``
  - status filter (``get_by_status``) and ``In(...)`` status filters
    (``get_pending_and_overdue``)
  - datetime-window queries (``get_upcoming`` / ``get_overdue``)
  - the follow-feed cursor (``get_feed``: ``is_public`` + ``In(user_ids)`` +
    ``limit`` + ``before`` cursor, newest-first)
  - ``mark_as_completed`` (stamps ``completed_date`` + flips status)
  - the bulk ``mark_overdue_tasks`` mutation and its ``modified_count`` return

Infra notes
-----------
This file now uses the shared ``tests/conftest.py`` infrastructure (TEST_PLAN
§4.1): conftest owns the test-DB env overrides, the Mongo-reachability skip, and
the ``init_core`` fixture (Beanie init across both DBs + per-test wipe of every
domain + identity collection, before AND after each test). DB tests depend on
``init_core`` directly — its built-in wipe gives the per-test isolation these
repository assertions need, so a stray task from a prior test can't skew counts.

``pytest-asyncio==0.24.0`` runs in ``asyncio_mode=auto`` (set by conftest); the
explicit ``@pytest.mark.asyncio`` markers below remain harmless.
"""
import uuid
from datetime import datetime, timedelta, timezone

import pytest

from app.models import Task, TaskStatus
from app.repositories.task_repository import TaskRepository


def _utcnow() -> datetime:
    return datetime.now(timezone.utc)


def _as_utc(value: datetime) -> datetime:
    """Normalise to tz-aware UTC for comparison.

    Beanie/Motor round-trips BSON datetimes as **naive** UTC (the client is not
    ``tz_aware=True``), and ``Document.save()`` refreshes the in-memory document
    from that round-trip, so a stamped ``completed_date`` is naive after save.
    Treat a naive value as already-UTC so it compares against ``_utcnow()``.
    """
    if value.tzinfo is None:
        return value.replace(tzinfo=timezone.utc)
    return value.astimezone(timezone.utc)


# --------------------------------------------------------------------------- #
# Fixtures
# --------------------------------------------------------------------------- #
@pytest.fixture
def repo() -> TaskRepository:
    return TaskRepository()


# --------------------------------------------------------------------------- #
# Factory helper
# --------------------------------------------------------------------------- #
def make_task(
    *,
    user_id: str = "user-1",
    title: str = "Task",
    due_date: datetime | None = None,
    status: TaskStatus = TaskStatus.PENDING,
    hive_id: str | None = None,
    apiary_id: str | None = None,
    is_public: bool = True,
) -> Task:
    """Build a valid, unsaved ``Task`` with a UUID-string id and required fields.

    ``id`` is a ``str(uuid.uuid4())`` to match how the routers create tasks (the
    migration stores ids as UUID strings, NOT ObjectIds). ``due_date`` defaults
    to "now" so callers only set it when the window matters.
    """
    return Task(
        id=str(uuid.uuid4()),
        title=title,
        user_id=user_id,
        due_date=due_date if due_date is not None else _utcnow(),
        status=status,
        hive_id=hive_id,
        apiary_id=apiary_id,
        is_public=is_public,
    )


# --------------------------------------------------------------------------- #
# CRUD
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_create_get_update_delete(init_core, repo):
    """Full CRUD round-trip; ``get_by_id`` on a missing id returns ``None``."""
    task = make_task(title="Original")
    created = await repo.create(task)
    assert created.id == task.id

    fetched = await repo.get_by_id(task.id)
    assert fetched is not None
    assert fetched.id == task.id
    assert fetched.title == "Original"

    fetched.title = "Renamed"
    await repo.update(fetched)
    re_fetched = await repo.get_by_id(task.id)
    assert re_fetched is not None
    assert re_fetched.title == "Renamed"

    await repo.delete(re_fetched)
    assert await repo.get_by_id(task.id) is None

    # Unknown id -> None (not an error).
    assert await repo.get_by_id(str(uuid.uuid4())) is None


# --------------------------------------------------------------------------- #
# Equality filters
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_get_by_user_id(init_core, repo):
    await repo.create(make_task(user_id="user-a", title="A1"))
    await repo.create(make_task(user_id="user-a", title="A2"))
    await repo.create(make_task(user_id="user-b", title="B1"))

    results = await repo.get_by_user_id("user-a")
    assert len(results) == 2
    assert {t.title for t in results} == {"A1", "A2"}
    assert all(t.user_id == "user-a" for t in results)


@pytest.mark.asyncio
async def test_get_by_hive_id(init_core, repo):
    await repo.create(make_task(hive_id="hive-1", title="H1"))
    await repo.create(make_task(hive_id="hive-2", title="H2"))
    await repo.create(make_task(hive_id=None, title="None"))

    results = await repo.get_by_hive_id("hive-1")
    assert len(results) == 1
    assert results[0].title == "H1"
    assert results[0].hive_id == "hive-1"


@pytest.mark.asyncio
async def test_get_by_apiary_id(init_core, repo):
    await repo.create(make_task(apiary_id="ap-1", title="AP1"))
    await repo.create(make_task(apiary_id="ap-1", title="AP1b"))
    await repo.create(make_task(apiary_id="ap-2", title="AP2"))

    results = await repo.get_by_apiary_id("ap-1")
    assert len(results) == 2
    assert all(t.apiary_id == "ap-1" for t in results)


@pytest.mark.asyncio
async def test_get_by_status(init_core, repo):
    """Filters by user_id AND a given TaskStatus (other users/statuses excluded)."""
    await repo.create(make_task(user_id="u1", status=TaskStatus.PENDING, title="p1"))
    await repo.create(make_task(user_id="u1", status=TaskStatus.COMPLETED, title="c1"))
    await repo.create(make_task(user_id="u2", status=TaskStatus.PENDING, title="p2"))

    results = await repo.get_by_status("u1", TaskStatus.PENDING)
    assert len(results) == 1
    assert results[0].title == "p1"
    assert results[0].status == TaskStatus.PENDING


# --------------------------------------------------------------------------- #
# In(...) status filter
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_get_pending_and_overdue(init_core, repo):
    """Returns only PENDING/OVERDUE/IN_PROGRESS (In), excl. COMPLETED/CANCELLED,
    sorted by due_date ascending."""
    now = _utcnow()
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now + timedelta(days=3), title="pending")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.OVERDUE,
                  due_date=now - timedelta(days=2), title="overdue")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.IN_PROGRESS,
                  due_date=now + timedelta(days=1), title="in_progress")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.COMPLETED,
                  due_date=now, title="completed")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.CANCELLED,
                  due_date=now, title="cancelled")
    )
    # Different user — must be excluded.
    await repo.create(
        make_task(user_id="u2", status=TaskStatus.PENDING,
                  due_date=now, title="other_user")
    )

    results = await repo.get_pending_and_overdue("u1")
    titles = [t.title for t in results]
    assert titles == ["overdue", "in_progress", "pending"]  # due_date asc
    assert "completed" not in titles
    assert "cancelled" not in titles
    assert "other_user" not in titles


# --------------------------------------------------------------------------- #
# Datetime windows
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_get_upcoming_window(init_core, repo):
    """get_upcoming(days=7): due_date <= now+7d and status in {PENDING,IN_PROGRESS};
    excludes beyond-window and COMPLETED."""
    now = _utcnow()
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now + timedelta(days=2), title="within_pending")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.IN_PROGRESS,
                  due_date=now + timedelta(days=6), title="within_in_progress")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now + timedelta(days=30), title="beyond_window")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.COMPLETED,
                  due_date=now + timedelta(days=1), title="completed_within")
    )

    results = await repo.get_upcoming("u1", days=7)
    titles = [t.title for t in results]
    assert titles == ["within_pending", "within_in_progress"]  # due_date asc
    assert "beyond_window" not in titles
    assert "completed_within" not in titles


@pytest.mark.asyncio
async def test_get_upcoming_custom_days(init_core, repo):
    """days=1 narrows the window: a task due in 5 days drops out."""
    now = _utcnow()
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now + timedelta(hours=12), title="soon")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now + timedelta(days=5), title="later")
    )

    results = await repo.get_upcoming("u1", days=1)
    titles = [t.title for t in results]
    assert titles == ["soon"]
    assert "later" not in titles


@pytest.mark.asyncio
async def test_get_overdue(init_core, repo):
    """get_overdue: due_date < now and status in {PENDING,IN_PROGRESS}, sorted by
    due_date; excludes future and COMPLETED."""
    now = _utcnow()
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now - timedelta(days=5), title="old_overdue")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.IN_PROGRESS,
                  due_date=now - timedelta(days=1), title="recent_overdue")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now + timedelta(days=2), title="future")
    )
    await repo.create(
        make_task(user_id="u1", status=TaskStatus.COMPLETED,
                  due_date=now - timedelta(days=3), title="completed_past")
    )

    results = await repo.get_overdue("u1")
    titles = [t.title for t in results]
    assert titles == ["old_overdue", "recent_overdue"]  # due_date asc
    assert "future" not in titles
    assert "completed_past" not in titles


# --------------------------------------------------------------------------- #
# Feed: is_public + In(user_ids) + limit + before cursor
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_get_feed_public_in_users_limit_before(init_core, repo):
    """get_feed: only is_public tasks from In(user_ids), newest-first, honoring
    limit and the ``before`` cursor (due_date < before)."""
    now = _utcnow()

    # Followed users with public tasks at distinct due_dates (newest -> oldest).
    t_newest = make_task(user_id="followee-1", title="newest",
                         due_date=now + timedelta(days=3))
    t_mid = make_task(user_id="followee-2", title="mid",
                      due_date=now + timedelta(days=1))
    t_old = make_task(user_id="followee-1", title="old",
                      due_date=now - timedelta(days=1))
    # Private task from a followed user -> excluded.
    t_private = make_task(user_id="followee-1", title="private",
                          due_date=now + timedelta(days=2), is_public=False)
    # Public task from a NON-followed user -> excluded.
    t_stranger = make_task(user_id="stranger", title="stranger",
                           due_date=now + timedelta(days=5))

    for t in (t_newest, t_mid, t_old, t_private, t_stranger):
        await repo.create(t)

    user_ids = ["followee-1", "followee-2"]

    # No cursor: newest-first, private + stranger excluded.
    feed = await repo.get_feed(user_ids)
    titles = [t.title for t in feed]
    assert titles == ["newest", "mid", "old"]
    assert "private" not in titles
    assert "stranger" not in titles

    # limit caps the page size (still newest-first).
    limited = await repo.get_feed(user_ids, limit=2)
    assert [t.title for t in limited] == ["newest", "mid"]

    # before cursor: only records strictly older than the cursor.
    cursor = t_newest.due_date  # excludes "newest" itself (strict <)
    page = await repo.get_feed(user_ids, before=cursor)
    page_titles = [t.title for t in page]
    assert page_titles == ["mid", "old"]
    assert "newest" not in page_titles

    # Paging across two pages yields no overlap and no gap.
    first_page = await repo.get_feed(user_ids, limit=2)
    assert [t.title for t in first_page] == ["newest", "mid"]
    next_cursor = first_page[-1].due_date  # "mid" due_date
    second_page = await repo.get_feed(user_ids, limit=2, before=next_cursor)
    assert [t.title for t in second_page] == ["old"]
    overlap = {t.id for t in first_page} & {t.id for t in second_page}
    assert overlap == set()


# --------------------------------------------------------------------------- #
# mark_as_completed
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_mark_as_completed(init_core, repo):
    """Sets status=COMPLETED, stamps a non-null tz-aware completed_date, persists."""
    task = await repo.create(
        make_task(status=TaskStatus.PENDING, title="to_complete")
    )
    assert task.completed_date is None

    before = _utcnow()
    completed = await repo.mark_as_completed(task)
    after = _utcnow()

    assert completed.status == TaskStatus.COMPLETED
    assert completed.completed_date is not None
    # save() refreshes the doc from its naive-UTC Mongo round-trip; normalise to
    # tz-aware UTC. BSON floors datetimes to millisecond precision, so allow a
    # 1ms slack on each bound (the stored value can sit just outside the
    # microsecond-precision before/after window).
    stamped = _as_utc(completed.completed_date)
    slack = timedelta(milliseconds=1)
    assert before - slack <= stamped <= after + slack

    # Persisted: a fresh fetch reflects the change.
    re_fetched = await repo.get_by_id(task.id)
    assert re_fetched is not None
    assert re_fetched.status == TaskStatus.COMPLETED
    assert re_fetched.completed_date is not None


# --------------------------------------------------------------------------- #
# mark_overdue_tasks bulk mutation + modified_count
# --------------------------------------------------------------------------- #
@pytest.mark.asyncio
async def test_mark_overdue_tasks_modifies_matching(init_core, repo):
    """Past-due PENDING tasks flip to OVERDUE; returns modified_count == #flipped."""
    now = _utcnow()
    for i in range(3):
        await repo.create(
            make_task(user_id="u1", status=TaskStatus.PENDING,
                      due_date=now - timedelta(days=i + 1), title=f"overdue-{i}")
        )

    modified = await repo.mark_overdue_tasks("u1")
    assert modified == 3

    refreshed = await repo.get_by_user_id("u1")
    assert all(t.status == TaskStatus.OVERDUE for t in refreshed)


@pytest.mark.asyncio
async def test_mark_overdue_tasks_ignores_non_pending_and_future(init_core, repo):
    """Future-due tasks and non-PENDING tasks are left untouched."""
    now = _utcnow()
    future = await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now + timedelta(days=2), title="future_pending")
    )
    in_progress_past = await repo.create(
        make_task(user_id="u1", status=TaskStatus.IN_PROGRESS,
                  due_date=now - timedelta(days=2), title="past_in_progress")
    )
    past_pending = await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now - timedelta(days=1), title="past_pending")
    )

    modified = await repo.mark_overdue_tasks("u1")
    assert modified == 1  # only past_pending

    assert (await repo.get_by_id(future.id)).status == TaskStatus.PENDING
    assert (await repo.get_by_id(in_progress_past.id)).status == TaskStatus.IN_PROGRESS
    assert (await repo.get_by_id(past_pending.id)).status == TaskStatus.OVERDUE


@pytest.mark.asyncio
async def test_mark_overdue_tasks_returns_zero_when_none(init_core, repo):
    """No matching tasks -> returns 0, nothing mutated."""
    now = _utcnow()
    future = await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now + timedelta(days=5), title="future")
    )

    modified = await repo.mark_overdue_tasks("u1")
    assert modified == 0
    assert (await repo.get_by_id(future.id)).status == TaskStatus.PENDING


@pytest.mark.asyncio
async def test_mark_overdue_tasks_scoped_to_user(init_core, repo):
    """Another user's past-due PENDING tasks are NOT flipped."""
    now = _utcnow()
    mine = await repo.create(
        make_task(user_id="u1", status=TaskStatus.PENDING,
                  due_date=now - timedelta(days=1), title="mine")
    )
    theirs = await repo.create(
        make_task(user_id="u2", status=TaskStatus.PENDING,
                  due_date=now - timedelta(days=1), title="theirs")
    )

    modified = await repo.mark_overdue_tasks("u1")
    assert modified == 1

    assert (await repo.get_by_id(mine.id)).status == TaskStatus.OVERDUE
    assert (await repo.get_by_id(theirs.id)).status == TaskStatus.PENDING
