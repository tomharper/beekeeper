"""Index-bootstrap tests for the Mongo/Beanie migration — TEST_PLAN §4.8 (P1).

Purpose: assert that ``init_core`` (Beanie ``init_beanie``) actually created the
indexes each model declares in its ``Settings.indexes`` on the live Mongo. This
catches index typos / a forgotten index migration, which silently degrade query
performance (the repositories' filters/sorts in §4.4–§4.7 rely on these indexes)
without ever failing a functional test.

Each case reads the *server-side* index catalogue via
``await Model.get_motor_collection().index_information()`` and matches on the
index **key spec** (the ``("field", direction)`` tuples) rather than the
auto-generated index name, so a rename of the index never breaks the assertion.

Declared indexes (source of truth — ``app/models/*.py`` ``class Settings``):
  - Hive (``hives``):            ["apiary_id"]
  - Alert (``alerts``):          [[("dismissed", 1), ("timestamp", -1)]]
  - Recommendation (``recommendations``): ["hive_id"]
  - Task (``tasks``):            ["hive_id", "apiary_id",
                                  [("user_id", 1), ("due_date", 1)],
                                  [("user_id", 1), ("status", 1)]]
  - Inspection (``inspections``):["hive_id", "user_id",
                                  [("hive_id", 1), ("inspection_date", -1)],
                                  [("user_id", 1), ("inspection_date", -1)],
                                  [("user_id", 1), ("is_public", 1),
                                   ("inspection_date", -1)]]
  - Apiary (``apiaries``):       no custom indexes (only the default ``_id``).

No model declares ``unique=True``, so there is no uniqueness to assert beyond the
default ``_id`` index (per TEST_PLAN §4.8 note).

Infra note (TEST_PLAN §3): there is no reliance on shared conftest infra here;
like the sibling migration test files this module is self-contained so it
COLLECTS and RUNS cleanly on its own. ``mongomock-motor`` is NOT installed, so the
tests target a **real** Mongo and **skip** the whole module when none is
reachable, keeping DB-less CI green. Env overrides (test DBs, non-production ENV)
are set at the very top, BEFORE importing ``app``/``assistive_core`` (whose
``Settings`` reads env at import time). pytest-asyncio 0.24 runs each test on its
own loop, so a FUNCTION-scoped autouse fixture re-inits Beanie on that loop (a
module/session-scoped Motor client raises "attached to a different loop").
"""
import os

# --- Env overrides: MUST happen BEFORE importing app / assistive_core. ---------
# assistive_core.settings.Settings reads the environment at import time, so the
# test-DB / non-production overrides have to be in place first. ``setdefault`` so
# a conftest (or CI env) that already set these wins.
os.environ.setdefault("MONGODB_DB", "beekeeper_test")
os.environ.setdefault("IDENTITY_DB", "assistive_identity_test")
# Keep init_core out of its production fail-fast path (placeholder JWT key).
os.environ.setdefault("ASSISTIVE_ENV", "test")
os.environ.setdefault("ENV", "test")
MONGODB_URI = os.environ.setdefault("MONGODB_URI", "mongodb://localhost:27017")

import pytest
import pytest_asyncio

from assistive_core import init_core, close_core  # noqa: E402
from assistive_core.settings import settings  # noqa: E402

from app.feed_sources import FEED_SOURCES  # noqa: E402
from app.models import (  # noqa: E402
    DOMAIN_DOCUMENTS,
    Apiary,
    Hive,
    Alert,
    Recommendation,
    Task,
    Inspection,
)


# --- Mongo reachability guard: skip the whole module if no live Mongo. ---------
def _mongo_reachable(uri: str = MONGODB_URI) -> bool:
    """Return True iff a Mongo at ``uri`` answers a ping quickly.

    These tests assert on the *server-side* index catalogue, so they require a
    real Mongo (mongomock-motor is not installed). When none is reachable we skip
    the whole module rather than error, so collection stays green on DB-less CI.
    A short ``serverSelectionTimeoutMS`` avoids the default 30s hang.
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


if not _mongo_reachable():
    pytest.skip(
        f"No MongoDB reachable at {MONGODB_URI}; skipping index-bootstrap suite "
        "(mongomock-motor not installed).",
        allow_module_level=True,
    )


# --- Fixtures ------------------------------------------------------------------
@pytest_asyncio.fixture(autouse=True)
async def initialized_db():
    """Initialise Beanie exactly as app/main.py's lifespan does, per test.

    pytest-asyncio 0.24 gives each test its own event loop and Motor/Beanie bind
    their client to the running loop, so ``init_core`` MUST run on the test's own
    loop (a module/session-scoped client raises "attached to a different loop").

    ``init_core`` -> ``init_beanie`` is what creates each model's declared
    ``Settings.indexes`` on the collections; that index bootstrap is precisely the
    behaviour under test here. Tears down the shared Motor client after each test
    (resetting assistive_core's module-global ``_client``).

    NOTE: unlike the repository suites this fixture intentionally does NOT wipe
    collections — index tests inspect the catalogue, not documents, and dropping
    documents leaves indexes intact anyway.
    """
    await init_core(vertical_documents=DOMAIN_DOCUMENTS, feed_sources=FEED_SOURCES)
    try:
        yield
    finally:
        await close_core()


# --- Helpers -------------------------------------------------------------------
async def _index_key_specs(model) -> list[list[tuple]]:
    """Return the live key spec of every index on ``model``'s collection.

    ``index_information()`` maps each index name -> info dict whose ``"key"`` is
    an ordered list of ``(field, direction)`` pairs. We normalise each to a list
    of ``(str, int)`` tuples so a declared single-field index (e.g. ``"apiary_id"``,
    which Beanie creates ascending) and a declared compound index can both be
    matched structurally, independent of the auto-generated index name.
    """
    info = await model.get_motor_collection().index_information()
    specs: list[list[tuple]] = []
    for meta in info.values():
        specs.append([(field, direction) for field, direction in meta["key"]])
    return specs


def _has_index(specs: list[list[tuple]], expected: list[tuple]) -> bool:
    """True iff ``expected`` (an ordered key spec) is present in ``specs``."""
    return expected in specs


# --- Hive ----------------------------------------------------------------------
async def test_hive_index_on_apiary_id():
    """``hives`` has a single-field index keyed on ``apiary_id`` (ascending)."""
    specs = await _index_key_specs(Hive)
    assert _has_index(specs, [("apiary_id", 1)]), (
        f"expected an apiary_id index on hives; got {specs}"
    )


# --- Alert ---------------------------------------------------------------------
async def test_alert_compound_index():
    """``alerts`` has the compound ``(dismissed:1, timestamp:-1)`` index.

    Backs ``AlertRepository.get_active`` (``dismissed == False`` newest-first).
    """
    specs = await _index_key_specs(Alert)
    assert _has_index(specs, [("dismissed", 1), ("timestamp", -1)]), (
        f"expected compound (dismissed:1, timestamp:-1) index on alerts; got {specs}"
    )


# --- Recommendation ------------------------------------------------------------
async def test_recommendation_index_on_hive_id():
    """``recommendations`` has a single-field index keyed on ``hive_id``."""
    specs = await _index_key_specs(Recommendation)
    assert _has_index(specs, [("hive_id", 1)]), (
        f"expected a hive_id index on recommendations; got {specs}"
    )


# --- Task ----------------------------------------------------------------------
async def test_task_indexes():
    """``tasks`` has hive_id, apiary_id, and the two user-scoped compounds."""
    specs = await _index_key_specs(Task)
    expected = [
        [("hive_id", 1)],
        [("apiary_id", 1)],
        [("user_id", 1), ("due_date", 1)],
        [("user_id", 1), ("status", 1)],
    ]
    missing = [e for e in expected if not _has_index(specs, e)]
    assert not missing, f"tasks missing declared indexes {missing}; got {specs}"


# --- Inspection ----------------------------------------------------------------
async def test_inspection_indexes():
    """``inspections`` has both single-field and the three feed/sort compounds."""
    specs = await _index_key_specs(Inspection)
    expected = [
        [("hive_id", 1)],
        [("user_id", 1)],
        [("hive_id", 1), ("inspection_date", -1)],
        [("user_id", 1), ("inspection_date", -1)],
        [("user_id", 1), ("is_public", 1), ("inspection_date", -1)],
    ]
    missing = [e for e in expected if not _has_index(specs, e)]
    assert not missing, f"inspections missing declared indexes {missing}; got {specs}"


# --- Apiary --------------------------------------------------------------------
async def test_apiary_has_only_default_index():
    """``apiaries`` declares no custom indexes — only the default ``_id`` index.

    Guards against a scaffolder inventing indexes the model never declares: the
    collection's index catalogue should contain exactly the implicit ``_id``
    index and nothing else.
    """
    info = await Apiary.get_motor_collection().index_information()
    # The only index is the default _id_ (key spec [("_id", 1)]).
    names = set(info)
    assert names == {"_id_"}, (
        f"apiaries should have only the default _id_ index; got {sorted(names)}"
    )


# --- Sanity: the suite is wired to the test database, not a real one. ----------
def test_index_suite_targets_test_db():
    """Defensive guard: these tests only ever touch the *_test databases.

    ``index_information()`` is read-only, but ``init_core`` bootstraps indexes on
    whatever DB the settings resolve to, so confirm we are pinned to the test DB
    namespace (set via the env overrides at module top) before any DB work runs.
    """
    assert settings.MONGODB_DB.endswith("_test"), settings.MONGODB_DB
