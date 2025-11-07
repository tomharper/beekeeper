import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_get_apiaries():
    """Test getting all apiaries"""
    response = client.get("/api/apiaries")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    # Should have seeded data
    assert len(data) >= 3


def test_get_apiary_by_id():
    """Test getting a specific apiary"""
    # First get all apiaries to get a valid ID
    response = client.get("/api/apiaries")
    apiaries = response.json()

    if apiaries:
        apiary_id = apiaries[0]["id"]
        response = client.get(f"/api/apiaries/{apiary_id}")
        assert response.status_code == 200
        data = response.json()
        assert data["id"] == apiary_id
        assert "name" in data
        assert "location" in data
        assert "hive_count" in data


def test_get_nonexistent_apiary():
    """Test getting a non-existent apiary"""
    response = client.get("/api/apiaries/nonexistent")
    assert response.status_code == 404


def test_create_apiary():
    """Test creating a new apiary"""
    new_apiary = {
        "name": "Test Apiary",
        "location": "Test Location",
        "latitude": 40.7128,
        "longitude": -74.0060,
    }
    response = client.post("/api/apiaries", json=new_apiary)
    assert response.status_code == 201
    data = response.json()
    assert data["name"] == "Test Apiary"
    assert data["location"] == "Test Location"
    assert "id" in data


def test_update_apiary():
    """Test updating an apiary"""
    # First create an apiary
    new_apiary = {
        "name": "Test Apiary",
        "location": "Test Location",
    }
    create_response = client.post("/api/apiaries", json=new_apiary)
    apiary_id = create_response.json()["id"]

    # Update it
    update_data = {"name": "Updated Apiary"}
    response = client.put(f"/api/apiaries/{apiary_id}", json=update_data)
    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "Updated Apiary"


def test_delete_apiary():
    """Test deleting an apiary"""
    # First create an apiary
    new_apiary = {
        "name": "Test Apiary to Delete",
        "location": "Test Location",
    }
    create_response = client.post("/api/apiaries", json=new_apiary)
    apiary_id = create_response.json()["id"]

    # Delete it
    response = client.delete(f"/api/apiaries/{apiary_id}")
    assert response.status_code == 204

    # Verify it's deleted
    get_response = client.get(f"/api/apiaries/{apiary_id}")
    assert get_response.status_code == 404
