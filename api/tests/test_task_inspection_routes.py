"""Route tests for the tasks and inspections HTTP APIs (Mongo/Beanie).

Migration plan reference: TEST_PLAN_MONGO_MIGRATION.md §4.10 (route rewrites).

Source under test:
  - app/routers/tasks.py        (TaskService-backed CRUD)
  - app/routers/inspections.py  (InspectionService-backed CRUD)

Style (per plan §4.10): a module-level FastAPI ``TestClient`` drives the real
app through its lifespan, so requests run against an initialized Beanie + the
seeded test database. Both routers depend on ``assistive_core.get_current_user``;
we override it with a fixed stub user so created documents are owned by — and
therefore retrievable by — the same identity across requests (the services
return 403 on an owner mismatch, 404 when missing).

The whole module skips when no Mongo is reachable (``mongomock-motor`` is not
installed), matching the rest of the migration suite. tests/conftest.py owns the
env overrides that point the app at the ``*_test`` databases and is imported by
pytest before this module, so ``app`` picks up the test DBs on import.
"""
import os
import uuid
from datetime import datetime, timezone

import pytest
from fastapi.testclient import TestClient

# tests/conftest.py runs its env overrides (the *_test database names) at import
# time, before this module loads, so ``app`` is already pointed at the test DBs.
# conftest isn't importable by name under pytest's import mode, so the Mongo
# reachability guard is kept local here (matching the other suite files).
MONGODB_URI = os.environ.get("MONGODB_URI", "mongodb://localhost:27017")


def _mongo_reachable(uri: str = MONGODB_URI) -> bool:
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


pytestmark = pytest.mark.skipif(
    not _mongo_reachable(),
    reason=f"No MongoDB reachable at {MONGODB_URI}; skipping route tests",
)

from app.main import app  # noqa: E402
from assistive_core import get_current_user  # noqa: E402


_TEST_USER_ID = str(uuid.uuid4())


class _StubUser:
    """Minimal stand-in for assistive_core.User: the routers only read ``.id``."""

    id = _TEST_USER_ID


def _override_get_current_user() -> _StubUser:
    return _StubUser()


@pytest.fixture(scope="module")
def client():
    """Module-level TestClient that runs the app lifespan (init_core + seed).

    Auth is overridden with a fixed stub user for the module's duration, then
    ``dependency_overrides`` is restored on teardown so the shared ``app`` object
    isn't left mutated for other test files. Using ``with TestClient(app)`` runs
    startup/shutdown so Beanie is initialised against the test DBs and the
    assistive-core client is closed afterwards.
    """
    app.dependency_overrides[get_current_user] = _override_get_current_user
    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.pop(get_current_user, None)


def _iso(dt: datetime | None = None) -> str:
    return (dt or datetime.now(timezone.utc)).isoformat()


def _task_payload(**overrides) -> dict:
    # camelCase keys: the schemas use alias_generator=to_camel.
    data = {"title": "Inspect hive", "dueDate": _iso()}
    data.update(overrides)
    return data


def _inspection_payload(**overrides) -> dict:
    data = {"hiveId": str(uuid.uuid4()), "inspectionDate": _iso()}
    data.update(overrides)
    return data


# ── Tasks ────────────────────────────────────────────────────────────────

def test_create_task_returns_201_with_id(client):
    resp = client.post("/api/tasks", json=_task_payload(title="Create me"))
    assert resp.status_code == 201
    body = resp.json()
    assert body["id"]
    assert body["title"] == "Create me"
    assert body["status"] == "PENDING"


def test_get_task_round_trips(client):
    created = client.post("/api/tasks", json=_task_payload(title="Round trip")).json()
    resp = client.get(f"/api/tasks/{created['id']}")
    assert resp.status_code == 200
    assert resp.json()["id"] == created["id"]


def test_list_tasks_includes_created(client):
    created = client.post("/api/tasks", json=_task_payload(title="In the list")).json()
    resp = client.get("/api/tasks")
    assert resp.status_code == 200
    assert created["id"] in [t["id"] for t in resp.json()]


def test_complete_task_sets_status_completed(client):
    created = client.post("/api/tasks", json=_task_payload(title="Finish me")).json()
    resp = client.post(f"/api/tasks/{created['id']}/complete")
    assert resp.status_code == 200
    assert resp.json()["status"] == "COMPLETED"


def test_delete_task_then_404(client):
    created = client.post("/api/tasks", json=_task_payload(title="Delete me")).json()
    assert client.delete(f"/api/tasks/{created['id']}").status_code == 204
    assert client.get(f"/api/tasks/{created['id']}").status_code == 404


def test_get_unknown_task_returns_404(client):
    assert client.get(f"/api/tasks/{uuid.uuid4()}").status_code == 404


# ── Inspections ──────────────────────────────────────────────────────────

def test_create_inspection_returns_201_with_id(client):
    resp = client.post("/api/inspections", json=_inspection_payload())
    assert resp.status_code == 201
    body = resp.json()
    assert body["id"]


def test_get_inspection_round_trips(client):
    created = client.post("/api/inspections", json=_inspection_payload()).json()
    resp = client.get(f"/api/inspections/{created['id']}")
    assert resp.status_code == 200
    assert resp.json()["id"] == created["id"]


def test_list_inspections_includes_created(client):
    created = client.post("/api/inspections", json=_inspection_payload()).json()
    resp = client.get("/api/inspections")
    assert resp.status_code == 200
    assert created["id"] in [i["id"] for i in resp.json()]


def test_delete_inspection_then_404(client):
    created = client.post("/api/inspections", json=_inspection_payload()).json()
    assert client.delete(f"/api/inspections/{created['id']}").status_code == 204
    assert client.get(f"/api/inspections/{created['id']}").status_code == 404


def test_get_unknown_inspection_returns_404(client):
    assert client.get(f"/api/inspections/{uuid.uuid4()}").status_code == 404
