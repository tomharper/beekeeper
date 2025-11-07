#!/usr/bin/env python3
"""
API Verification Script
Verifies that the FastAPI backend is running and responding correctly
"""

import requests
import json
import sys
from datetime import datetime


API_BASE_URL = "http://localhost:2020"


def print_success(message):
    print(f"✓ {message}")


def print_error(message):
    print(f"✗ {message}")


def print_info(message):
    print(f"ℹ {message}")


def test_api_endpoint(endpoint, description):
    """Test a GET endpoint"""
    try:
        response = requests.get(f"{API_BASE_URL}{endpoint}", timeout=5)
        if response.status_code == 200:
            print_success(f"{description}: {response.status_code}")
            return response.json()
        else:
            print_error(
                f"{description}: Got status {response.status_code}"
            )
            return None
    except requests.exceptions.ConnectionError:
        print_error(f"{description}: Cannot connect to API at {API_BASE_URL}")
        print_info(
            "Make sure the API is running: uvicorn app.main:app --reload --port 2020"
        )
        return None
    except Exception as e:
        print_error(f"{description}: {str(e)}")
        return None


def main():
    print("=" * 60)
    print("Beekeeper API Verification")
    print("=" * 60)
    print()

    # Test basic endpoints
    print("Testing Basic Endpoints:")
    print("-" * 60)
    test_api_endpoint("/", "Root endpoint")
    test_api_endpoint("/health", "Health check")
    print()

    # Test Apiaries
    print("Testing Apiaries:")
    print("-" * 60)
    apiaries = test_api_endpoint("/api/apiaries", "GET /api/apiaries")
    if apiaries:
        print_info(f"  Found {len(apiaries)} apiaries")
        for apiary in apiaries[:3]:  # Show first 3
            print_info(
                f"  - {apiary['name']} ({apiary['location']}) - {apiary['hive_count']} hives"
            )
    print()

    # Test Hives
    print("Testing Hives:")
    print("-" * 60)
    hives = test_api_endpoint("/api/hives", "GET /api/hives")
    if hives:
        print_info(f"  Found {len(hives)} hives")
        for hive in hives[:3]:  # Show first 3
            print_info(
                f"  - {hive['name']} ({hive['status']}) - Last inspected: {hive['last_inspected']}"
            )
    print()

    # Test Alerts
    print("Testing Alerts:")
    print("-" * 60)
    alerts = test_api_endpoint("/api/alerts/active", "GET /api/alerts/active")
    if alerts:
        print_info(f"  Found {len(alerts)} active alerts")
        for alert in alerts[:3]:  # Show first 3
            print_info(f"  - {alert['title']} ({alert['severity']})")
    print()

    # Test Weather
    print("Testing Weather:")
    print("-" * 60)
    weather = test_api_endpoint("/api/weather", "GET /api/weather")
    if weather:
        print_info(
            f"  Temperature: {weather['temperature']}°F, "
            f"Humidity: {weather['humidity']}%, "
            f"Wind: {weather['wind_speed']} mph"
        )
        print_info(f"  Condition: {weather['condition']}")
    print()

    # Test Recommendations
    print("Testing Recommendations:")
    print("-" * 60)
    if hives and len(hives) > 0:
        hive_id = hives[0]["id"]
        recommendations = test_api_endpoint(
            f"/api/recommendations?hive_id={hive_id}",
            f"GET /api/recommendations for {hives[0]['name']}",
        )
        if recommendations:
            print_info(f"  Found {len(recommendations)} recommendations")
            for rec in recommendations:
                print_info(f"  - {rec['title']} ({rec['type']})")
    print()

    print("=" * 60)
    print("Verification Complete!")
    print("=" * 60)
    print()
    print("API Documentation available at:")
    print(f"  - Swagger UI: {API_BASE_URL}/docs")
    print(f"  - ReDoc:      {API_BASE_URL}/redoc")
    print()


if __name__ == "__main__":
    main()
