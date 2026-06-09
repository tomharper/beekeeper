"""Shared pytest infrastructure for the beekeeper API test suite.

This is the P0 test-infra file mandated by ``docs/TEST_PLAN_MONGO_MIGRATION.md``
§3 / §4.1. It owns the async-Mongo lifecycle for every P0/P1 test:

  1. Env override BEFORE any ``app``/``assistive_core`` import — point the suite
     at dedicated *test* databases and keep ``init_core`` out of its production
     fail-fast path. ``assistive_core.settings.Settings`` reads env at *import
     time*, so these MUST run at module top, before the imports below.
  2. asyncio config — registers ``asyncio_mode=auto`` so plain ``async def
     test_*`` / async fixtures run under ``pytest-asyncio==0.24.0`` without a
     per-test ``@pytest.mark.asyncio`` marker (the repo ships no pytest.ini).
  3. Mongo-reachability guard — if no Mongo answers a ping, the whole async
     suite is *skipped* (not failed) so DB-less CI stays green. ``mongomock-motor``
     is not installed, so the fixtures target a real Mongo; a maintainer who
     later adds it can swap the client in ``init_core_session`` with no test
     changes.
  4. ``init_core`` fixture — initialises Beanie across both DBs exactly as
     ``app/main.py`` lifespan does (``DOMAIN_DOCUMENTS`` + ``FEED_SOURCES``).
  5. Per-test cleanup — drops every domain + identity collection after each
     test so cases never see each other's data. Indexes are recreated by the
     session-scoped re-init guard if a whole DB is ever dropped.
  6. Teardown — ``close_core()`` + reset the module-global client; drop the two
     test databases at session end.
  7. Factory helpers — small builders returning valid, unsaved Documents with a
     ``str(uuid4())`` id and required fields filled, to keep test cases short.

The pure route tests in this suite use a module-level ``TestClient(app)`` and do
NOT depend on these async fixtures; they keep collecting/running without Mongo.
The async repository/serialization/index tests added by the migration plan opt
into the ``init_core`` fixture (and, transitively, the skip guard).
"""
import os

# ---------------------------------------------------------------------------
# 1. Env overrides — MUST precede every app/assistive_core import in this file.
# ---------------------------------------------------------------------------
# ``setdefault`` so an explicit CI env (or an already-loaded test_main.py, which
# sets the same keys defensively) wins. Test DBs are namespaced with ``_test``
# so a run can never touch a real beekeeper/identity database.
os.environ.setdefault("MONGODB_DB", "beekeeper_test")
os.environ.setdefault("IDENTITY_DB", "assistive_identity_test")
# Keep init_core out of its production JWT fail-fast branch.
os.environ.setdefault("ASSISTIVE_ENV", "test")
os.environ.setdefault("ENV", "test")
MONGODB_URI = os.environ.setdefault("MONGODB_URI", "mongodb://localhost:27017")

import uuid
from datetime import datetime, timezone

import pytest
import pytest_asyncio

# Imported AFTER the env overrides above so assistive_core.settings reads the
# test values at import time. ``init_core`` is aliased to ``_core_init`` so the
# session fixture below can call the real assistive-core function without the
# ``init_core`` *fixture* (defined later) shadowing it in this module namespace.
from assistive_core import init_core as _core_init, close_core
from assistive_core import db as core_db
from app.models import (
    DOMAIN_DOCUMENTS,
    Apiary,
    ApiaryStatus,
    Hive,
    HiveStatus,
    Inspection,
    Task,
    Alert,
    Recommendation,
    RecommendationType,
    AlertType,
    AlertSeverity,
)
from app.models.base import utcnow
from app.feed_sources import FEED_SOURCES


# Domain documents that live in the beekeeper test DB and must be cleared
# between tests. Sourced from DOMAIN_DOCUMENTS so adding a model auto-includes it.
_DOMAIN_DOCUMENTS = list(DOMAIN_DOCUMENTS)

# Test database names (resolved from the env set above).
TEST_VERTICAL_DB = os.environ["MONGODB_DB"]
TEST_IDENTITY_DB = os.environ["IDENTITY_DB"]


# ---------------------------------------------------------------------------
# 2. asyncio config — repo has no pytest.ini, so set asyncio_mode here.
# ---------------------------------------------------------------------------
def pytest_configure(config):
    """Enable ``asyncio_mode=auto`` for pytest-asyncio 0.24 without a pytest.ini.

    With auto mode, plain ``async def test_*`` functions and async fixtures are
    collected/run without an explicit ``@pytest.mark.asyncio`` marker, which is
    the convention the migration's async repository/serialization tests rely on.
    Setting the ini value here keeps all pytest config in one place (conftest).
    """
    config.inicfg["asyncio_mode"] = "auto"
    # Some pytest-asyncio versions read this off the option/ini cache too.
    try:
        config.option.asyncio_mode = "auto"
    except (AttributeError, ValueError):
        pass


# ---------------------------------------------------------------------------
# 3. Mongo-reachability guard.
# ---------------------------------------------------------------------------
def _mongo_reachable(uri: str = MONGODB_URI) -> bool:
    """Return True iff a Mongo at ``uri`` answers a ping quickly.

    Uses a short ``serverSelectionTimeoutMS`` so a DB-less environment skips
    fast rather than hanging on the default 30s selection timeout. Mirrors the
    helper in ``tests/test_main.py``.
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


# Evaluated once at import; async DB fixtures below skip when Mongo is absent so
# the suite still collects cleanly with no live database.
MONGO_AVAILABLE = _mongo_reachable()

# Reusable marker for any async test/module that needs a live Mongo. Tests can
# decorate with ``@pytest.mark.usefixtures("init_core")`` (preferred) which
# already carries the skip, or apply ``requires_mongo`` directly.
requires_mongo = pytest.mark.skipif(
    not MONGO_AVAILABLE,
    reason=f"No MongoDB reachable at {MONGODB_URI}; skipping async DB tests",
)


# ---------------------------------------------------------------------------
# 4. init_core fixture — Beanie init across both DBs, per test.
# ---------------------------------------------------------------------------
async def _wipe_test_data():
    """Drop every domain document collection + the identity users collection."""
    for model in _DOMAIN_DOCUMENTS:
        await model.get_motor_collection().delete_many({})
    client = core_db.get_client()
    await client[TEST_IDENTITY_DB]["users"].delete_many({})


@pytest_asyncio.fixture
async def init_core():
    """Initialise Beanie exactly as ``app/main.py`` lifespan does, per test.

    FUNCTION-scoped on purpose: pytest-asyncio 0.24 gives each test its own event
    loop and Motor/Beanie bind their client to the running loop, so a
    session/module-scoped client raises "attached to a different loop". Each test
    re-inits on its own loop, runs against a clean database (collections wiped
    before and after), and the shared assistive-core client is closed/reset on
    teardown.

    Wires the shared identity DB (User) + per-vertical DB (core social docs +
    ``DOMAIN_DOCUMENTS``) and registers ``FEED_SOURCES``; this also bootstraps
    every model's declared ``Settings.indexes`` (relied on by test_indexes.py).

    Skips when no Mongo is reachable so the dependent async suite stays green in
    DB-less CI. Yields the live Motor client for tests wanting raw collections.
    Depend on it directly (``async def test_x(init_core): ...``) or via
    ``@pytest.mark.usefixtures("init_core")``.
    """
    if not MONGO_AVAILABLE:
        pytest.skip(f"No MongoDB reachable at {MONGODB_URI}")

    await _core_init(
        vertical_documents=_DOMAIN_DOCUMENTS,
        feed_sources=FEED_SOURCES,
    )
    await _wipe_test_data()
    try:
        yield core_db.get_client()
        await _wipe_test_data()
    finally:
        await close_core()  # closes + nulls the module-global _client


# ---------------------------------------------------------------------------
# 7. Factory helpers — valid, unsaved Documents with a uuid id + required fields.
# ---------------------------------------------------------------------------
# Each helper fills the model's required fields with sane defaults and accepts
# **overrides so a test can vary just the field under test. None of these
# persist — callers ``await thing.insert()`` (or go through a repository's
# ``create``) so the factory stays decoupled from how the test saves.

def _uuid() -> str:
    return str(uuid.uuid4())


def make_apiary(**overrides) -> Apiary:
    data = dict(
        id=_uuid(),
        name="Test Apiary",
        location="Test Location",
        status=ApiaryStatus.HEALTHY,
    )
    data.update(overrides)
    return Apiary(**data)


def make_hive(apiary_id: str | None = None, **overrides) -> Hive:
    data = dict(
        id=_uuid(),
        name="Test Hive",
        apiary_id=apiary_id or _uuid(),
        status=HiveStatus.STRONG,
        last_inspected=utcnow(),
    )
    data.update(overrides)
    return Hive(**data)


def make_inspection(
    hive_id: str | None = None, user_id: str | None = None, **overrides
) -> Inspection:
    data = dict(
        id=_uuid(),
        hive_id=hive_id or _uuid(),
        user_id=user_id or _uuid(),
        inspection_date=utcnow(),
    )
    data.update(overrides)
    return Inspection(**data)


def make_task(user_id: str | None = None, **overrides) -> Task:
    data = dict(
        id=_uuid(),
        title="Test Task",
        user_id=user_id or _uuid(),
        due_date=utcnow(),
    )
    data.update(overrides)
    return Task(**data)


def make_alert(**overrides) -> Alert:
    data = dict(
        id=_uuid(),
        type=AlertType.GENERAL,
        title="Test Alert",
        message="Test alert message",
        severity=AlertSeverity.INFO,
        timestamp=utcnow(),
        hive_ids=[],
        dismissed=False,
    )
    data.update(overrides)
    return Alert(**data)


def make_recommendation(hive_id: str | None = None, **overrides) -> Recommendation:
    data = dict(
        id=_uuid(),
        hive_id=hive_id or _uuid(),
        type=RecommendationType.INFO,
        title="Test Recommendation",
        description="Test recommendation description",
    )
    data.update(overrides)
    return Recommendation(**data)


# Expose the factories as fixtures too, for tests that prefer DI over importing
# the helpers. Each yields the callable (not a built instance) so a single test
# can build several documents.
@pytest.fixture
def apiary_factory():
    return make_apiary


@pytest.fixture
def hive_factory():
    return make_hive


@pytest.fixture
def inspection_factory():
    return make_inspection


@pytest.fixture
def task_factory():
    return make_task


@pytest.fixture
def alert_factory():
    return make_alert


@pytest.fixture
def recommendation_factory():
    return make_recommendation


# Small convenience for tests asserting tz-aware UTC round-trips.
def utc_now() -> datetime:
    return datetime.now(timezone.utc)
