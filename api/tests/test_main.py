"""Tests for app/main.py — root/health routes and full lifespan boot.

Migration rewrite (Mongo/Beanie): the previous version of this file imported
``sqlalchemy``, ``app.database.get_db`` and ``app.models.Base`` — none of which
exist after the SQLAlchemy -> Motor/Beanie migration. That stale file aborted
pytest *collection* (``ModuleNotFoundError: sqlalchemy``) and blocked the rest
of the suite. This rewrite has no SQLAlchemy and exercises the real wiring.

Test DB / env overrides MUST be set before ``assistive_core`` (and anything that
imports it, e.g. ``app.main``) is imported, because
``assistive_core.settings.Settings`` reads the environment at import time. We do
that at the very top of this module. NOTE: when ``tests/conftest.py`` is added
(see the migration TEST_PLAN §4.1) it will own these overrides + the Mongo
skip guard; this module sets them defensively so it still collects and runs
standalone before that conftest exists.
"""
import os

# --- Env overrides: must happen BEFORE importing app.main / assistive_core. ---
# setdefault so a conftest (or CI env) that already set these wins.
os.environ.setdefault("MONGODB_DB", "beekeeper_test")
os.environ.setdefault("IDENTITY_DB", "assistive_identity_test")
# Keep init_core out of its production fail-fast path (placeholder JWT key).
os.environ.setdefault("ASSISTIVE_ENV", "test")
os.environ.setdefault("ENV", "test")
MONGODB_URI = os.environ.setdefault("MONGODB_URI", "mongodb://localhost:27017")

import pytest
from fastapi.testclient import TestClient

from app.main import app


def _mongo_reachable(uri: str) -> bool:
    """Return True if a Mongo at ``uri`` answers a ping quickly.

    The lifespan boot test calls ``init_core`` which needs a live Mongo. When
    none is reachable (e.g. CI without a Mongo service) we skip rather than fail,
    matching the migration plan's "skip the async suite when Mongo is
    unreachable" approach. Uses a short serverSelection timeout so the skip is
    fast.
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


# --- Pure route tests: these do NOT enter the lifespan, so no DB is needed. ---
# A plain module-level TestClient (as the rest of this suite uses) does not run
# startup/shutdown events, so ``/`` and ``/health`` are served without Beanie.
client = TestClient(app)


def test_root_endpoint():
    """GET / returns the welcome banner with version."""
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Welcome to Beekeeper API",
        "version": "1.0.0",
    }


def test_health_endpoint():
    """GET /health returns the healthy status payload."""
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "healthy"}


def test_app_boots_with_lifespan():
    """App boots cleanly under the real lifespan (init_core + seed_database).

    Using ``with TestClient(app) as c:`` triggers FastAPI startup, which runs the
    lifespan in app/main.py: ``init_core(vertical_documents=DOMAIN_DOCUMENTS,
    feed_sources=FEED_SOURCES)`` then ``seed_database()``. If Beanie init or the
    seed raises, the context manager entry raises and this test fails. We assert a
    basic route still answers 200 once startup has completed.

    Skips (not fails) when no Mongo is reachable so collection stays green in
    DB-less CI.
    """
    if not _mongo_reachable(MONGODB_URI):
        pytest.skip(f"No MongoDB reachable at {MONGODB_URI}; skipping lifespan boot test")

    with TestClient(app) as booted_client:
        # Startup (lifespan) has now run: init_core + seed_database completed
        # without raising. Confirm the app is serving requests post-startup.
        response = booted_client.get("/health")
        assert response.status_code == 200
        assert response.json() == {"status": "healthy"}

        root_response = booted_client.get("/")
        assert root_response.status_code == 200
