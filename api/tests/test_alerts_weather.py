import pytest
from fastapi.testclient import TestClient
from app.main import app

from .conftest import requires_mongo

pytestmark = requires_mongo

client = TestClient(app)


@pytest.fixture(scope="module", autouse=True)
def _app_lifespan():
    """Run the app lifespan (init_core + seed) for this module so DB-backed route
    handlers hit a live, freshly-bound Beanie client on the TestClient's own loop.
    Without it the module-level TestClient never triggers startup, so these tests
    inherit Beanie bound to a closed client from a prior async (init_core-fixture)
    test -> 'Event loop is closed'."""
    with client:
        yield


def test_get_all_alerts():
    """Test getting all alerts"""
    response = client.get("/api/alerts")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)


def test_get_active_alerts():
    """Test getting active alerts"""
    response = client.get("/api/alerts/active")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    # All should be non-dismissed
    for alert in data:
        assert alert["dismissed"] == False


def test_dismiss_alert():
    """Test dismissing an alert"""
    # Get active alerts
    response = client.get("/api/alerts/active")
    alerts = response.json()

    if alerts:
        alert_id = alerts[0]["id"]
        # Dismiss it
        response = client.patch(
            f"/api/alerts/{alert_id}", json={"dismissed": True}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["dismissed"] == True


def test_get_weather():
    """Test getting weather data"""
    response = client.get("/api/weather")
    assert response.status_code == 200
    data = response.json()
    assert "temperature" in data
    assert "humidity" in data
    assert "windSpeed" in data
    assert "condition" in data
    assert "description" in data
