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


def test_get_recommendations_for_hive():
    """Test getting recommendations for a specific hive"""
    # Get hives first
    hives_response = client.get("/api/hives")
    hives = hives_response.json()

    if hives:
        hive_id = hives[0]["id"]
        response = client.get(f"/api/recommendations?hive_id={hive_id}")
        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        # All recommendations should be for this hive
        for rec in data:
            assert rec["hiveId"] == hive_id
