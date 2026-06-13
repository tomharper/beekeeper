"""Integration tests for the assistive-core social surface, exercised through
the beekeeper app (its real inspection/task/event feed sources).

Covers SSO auth, the follow graph, the registry-driven activity feed (merge +
cursor pagination), notification fan-out, and the shared calendar — the layer
that previously had only ad-hoc smoke coverage.

Like the other route tests these run the app lifespan (``with client:`` →
``init_core`` + seed) so handlers hit a live Beanie client on the TestClient's
own loop, and skip when no Mongo is reachable. Each test registers its OWN
fresh users (unique emails) so cases stay isolated without a per-test DB wipe:
a user's feed/notifications only ever reflect the follow graph that test built.
"""
import uuid

import pytest
from fastapi.testclient import TestClient
from app.main import app

from .conftest import requires_mongo

pytestmark = requires_mongo

client = TestClient(app)


@pytest.fixture(scope="module", autouse=True)
def _app_lifespan():
    with client:
        yield


# --- helpers ---------------------------------------------------------------
def _register(full_name: str = "User"):
    """Register a fresh user; return (token, user_id, email)."""
    email = f"{uuid.uuid4().hex}@example.com"
    resp = client.post(
        "/api/auth/register",
        json={"email": email, "password": "pw12345678", "fullName": full_name},
    )
    assert resp.status_code == 200, resp.text
    body = resp.json()
    return body["accessToken"], body["user"]["id"], email


def _auth(token: str) -> dict:
    return {"Authorization": f"Bearer {token}"}


def _public_inspection(token: str, when: str = "2026-05-20T10:00:00", **extra):
    body = {"hiveId": "h1", "inspectionDate": when, "isPublic": True, **extra}
    return client.post("/api/inspections", headers=_auth(token), json=body)


def _public_task(token: str, when: str = "2026-05-25T09:00:00", title: str = "Add super"):
    return client.post(
        "/api/tasks",
        headers=_auth(token),
        json={"title": title, "dueDate": when, "isPublic": True},
    )


# --- auth / SSO ------------------------------------------------------------
def test_register_me_and_duplicate_email():
    token, uid, email = _register("Mee")
    me = client.get("/api/auth/me", headers=_auth(token))
    assert me.status_code == 200
    assert me.json()["id"] == uid

    dup = client.post(
        "/api/auth/register",
        json={"email": email, "password": "pw12345678", "fullName": "Dup"},
    )
    assert dup.status_code == 400


def test_login_returns_token():
    _, _, email = _register("Loginner")
    resp = client.post(
        "/api/auth/login", json={"email": email, "password": "pw12345678"}
    )
    assert resp.status_code == 200
    assert resp.json().get("accessToken")


# --- follow graph ----------------------------------------------------------
def test_follow_unfollow_and_lists():
    ta, aid, _ = _register("Alice")
    tb, bid, _ = _register("Bob")

    assert client.post(f"/api/follows/{bid}", headers=_auth(ta)).status_code == 204
    # Idempotent — following again is a no-op, not an error.
    assert client.post(f"/api/follows/{bid}", headers=_auth(ta)).status_code == 204

    following = client.get("/api/follows/following", headers=_auth(ta)).json()
    assert any(u["id"] == bid for u in following)
    followers = client.get("/api/follows/followers", headers=_auth(tb)).json()
    assert any(u["id"] == aid for u in followers)

    assert client.delete(f"/api/follows/{bid}", headers=_auth(ta)).status_code == 204
    following_after = client.get("/api/follows/following", headers=_auth(ta)).json()
    assert all(u["id"] != bid for u in following_after)


def test_self_follow_rejected():
    token, uid, _ = _register("Solo")
    assert client.post(f"/api/follows/{uid}", headers=_auth(token)).status_code == 400


def test_user_search_finds_by_name_excludes_self():
    name = "Zedric" + uuid.uuid4().hex[:8]
    ta, aid, _ = _register("Searcher")
    _, bid, _ = _register(name)

    results = client.get(f"/api/users/search?q={name}", headers=_auth(ta)).json()
    assert any(u["id"] == bid for u in results)
    assert all(u["id"] != aid for u in results)


# --- feed ------------------------------------------------------------------
def test_feed_merges_inspection_and_task_sorted_desc():
    ta, _, _ = _register("Alice")
    tb, bid, _ = _register("Bob")
    client.post(f"/api/follows/{bid}", headers=_auth(ta))

    assert _public_inspection(tb, "2026-05-20T10:00:00", treatmentApplied=True).status_code == 201
    assert _public_task(tb, "2026-05-25T09:00:00").status_code == 201

    feed = client.get("/api/feed", headers=_auth(ta))
    assert feed.status_code == 200
    items = feed.json()
    assert {"inspection", "task"} <= {i["type"] for i in items}
    occurred = [i["occurredAt"] for i in items]
    assert occurred == sorted(occurred, reverse=True)
    # Author is denormalised onto each item.
    assert all(i["author"]["id"] == bid for i in items)


def test_feed_excludes_private_records():
    ta, _, _ = _register("Alice")
    tb, bid, _ = _register("Bob")
    client.post(f"/api/follows/{bid}", headers=_auth(ta))
    # B's only record is private → must not surface.
    assert _public_inspection(tb, "2026-05-21T10:00:00", isPublic=False).status_code == 201

    feed = client.get("/api/feed", headers=_auth(ta)).json()
    assert all(i["author"]["id"] != bid for i in feed)


def test_feed_empty_without_follows():
    token, _, _ = _register("Loner")
    assert client.get("/api/feed", headers=_auth(token)).json() == []


def test_feed_cursor_pagination():
    ta, _, _ = _register("Alice")
    tb, bid, _ = _register("Bob")
    client.post(f"/api/follows/{bid}", headers=_auth(ta))
    _public_inspection(tb, "2026-05-20T10:00:00")
    _public_task(tb, "2026-05-25T09:00:00")

    page1 = client.get("/api/feed", params={"limit": 1}, headers=_auth(ta)).json()
    assert len(page1) == 1
    cursor = page1[0]["occurredAt"]

    page2 = client.get(
        "/api/feed", params={"limit": 1, "before": cursor}, headers=_auth(ta)
    ).json()
    assert len(page2) == 1
    assert page2[0]["occurredAt"] < cursor


# --- notifications (registry-driven fan-out) -------------------------------
def test_notification_fanout_and_mark_read():
    ta, _, _ = _register("Alice")
    tb, bid, _ = _register("Bob")
    client.post(f"/api/follows/{bid}", headers=_auth(ta))
    _public_inspection(tb, "2026-05-20T10:00:00")

    resp = client.get("/api/notifications", headers=_auth(ta))
    assert resp.status_code == 200
    notes = resp.json()
    assert any(n["type"] == "inspection" for n in notes)

    nid = notes[0]["id"]
    read = client.post(f"/api/notifications/{nid}/read", headers=_auth(ta))
    assert read.status_code == 200
    assert read.json()["isRead"] is True


def test_no_fanout_to_non_followers():
    ta, _, _ = _register("Alice")
    tb, _, _ = _register("Bob")
    # A does NOT follow B; B's public create must not notify A.
    _public_inspection(tb, "2026-05-20T10:00:00")
    assert client.get("/api/notifications", headers=_auth(ta)).json() == []


# --- shared calendar -------------------------------------------------------
def test_calendar_includes_followed_public_event():
    ta, _, _ = _register("Alice")
    tb, bid, _ = _register("Bob")
    client.post(f"/api/follows/{bid}", headers=_auth(ta))

    created = client.post(
        "/api/events",
        headers=_auth(tb),
        json={"title": "Open hive day", "eventDate": "2026-05-23T12:00:00", "isPublic": True},
    )
    assert created.status_code == 201, created.text
    event_id = created.json()["id"]

    cal = client.get(
        "/api/events/calendar",
        params={"start": "2026-05-01T00:00:00", "end": "2026-06-01T00:00:00"},
        headers=_auth(ta),
    )
    assert cal.status_code == 200, cal.text
    assert any(e["id"] == event_id for e in cal.json())
