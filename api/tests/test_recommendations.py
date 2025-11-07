import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


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
            assert rec["hive_id"] == hive_id
