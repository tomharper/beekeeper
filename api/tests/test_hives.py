import pytest
from fastapi.testclient import TestClient
from datetime import datetime
from app.main import app

client = TestClient(app)


def test_get_all_hives():
    """Test getting all hives"""
    response = client.get("/api/hives")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)


def test_get_hives_by_apiary():
    """Test filtering hives by apiary"""
    # Get apiaries first
    apiaries_response = client.get("/api/apiaries")
    apiaries = apiaries_response.json()

    if apiaries:
        apiary_id = apiaries[0]["id"]
        response = client.get(f"/api/hives?apiary_id={apiary_id}")
        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        # All hives should belong to the specified apiary
        for hive in data:
            assert hive["apiary_id"] == apiary_id


def test_get_hive_by_id():
    """Test getting a specific hive"""
    response = client.get("/api/hives")
    hives = response.json()

    if hives:
        hive_id = hives[0]["id"]
        response = client.get(f"/api/hives/{hive_id}")
        assert response.status_code == 200
        data = response.json()
        assert data["id"] == hive_id
        assert "name" in data
        assert "status" in data
        assert "colony_strength" in data
        assert "queen_status" in data


def test_create_hive():
    """Test creating a new hive"""
    # Get an apiary to assign the hive to
    apiaries_response = client.get("/api/apiaries")
    apiaries = apiaries_response.json()

    if apiaries:
        new_hive = {
            "name": "Test Hive",
            "apiary_id": apiaries[0]["id"],
            "last_inspected": datetime.now().isoformat(),
        }
        response = client.post("/api/hives", json=new_hive)
        assert response.status_code == 201
        data = response.json()
        assert data["name"] == "Test Hive"
        assert "id" in data


def test_update_hive():
    """Test updating a hive"""
    response = client.get("/api/hives")
    hives = response.json()

    if hives:
        hive_id = hives[0]["id"]
        update_data = {"name": "Updated Hive Name"}
        response = client.put(f"/api/hives/{hive_id}", json=update_data)
        assert response.status_code == 200
        data = response.json()
        assert data["name"] == "Updated Hive Name"
