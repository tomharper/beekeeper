"""Weather client for the sibling VRUsafety hazard-service weather API.

Beekeeper's original ``weather_service.py`` returned mock data; this client
hits the real service at ``settings.WEATHER_SERVICE_URL`` over httpx. It is a
thin pass-through that returns parsed dicts and is tolerant of missing fields,
so verticals can read whatever the upstream service provides without coupling
to a fixed schema.

Upstream endpoints (mounted at ``/api/v1/weather`` in the hazard service):
  - GET /current?lat&lon         -> current conditions dict
  - GET /forecast?lat&lon&hours  -> {location, source, hours: [...]}
"""
import logging
from typing import Any, Optional

import httpx

from assistive_core.settings import settings

logger = logging.getLogger(__name__)

# Base path the hazard-service mounts its weather router under.
_WEATHER_PREFIX = "/api/v1/weather"


class WeatherClient:
    """Thin httpx client for the VRUsafety hazard-service weather API."""

    def __init__(self, base_url: Optional[str] = None):
        self.base_url = (base_url or settings.WEATHER_SERVICE_URL).rstrip("/")

    def _url(self, path: str) -> str:
        return f"{self.base_url}{_WEATHER_PREFIX}{path}"

    async def get_current_weather(
        self, lat: float, lon: float
    ) -> Optional[dict[str, Any]]:
        """Get current weather conditions for a location.

        Returns the upstream JSON dict (e.g. ``temperature_c``, ``weather_code``,
        ``weather_description``, ``wind_speed_ms``, ``source``,
        ``severity_modifier``), or ``None`` if the service is not configured or
        the request fails. Callers should treat any individual field as optional.
        """
        if not self.base_url:
            logger.warning("WEATHER_SERVICE_URL not configured; skipping weather fetch")
            return None

        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    self._url("/current"),
                    params={"lat": lat, "lon": lon},
                    timeout=10.0,
                )
                response.raise_for_status()
                return response.json()
        except httpx.HTTPError as e:
            logger.warning(f"Failed to fetch current weather: {e}")
            return None

    async def get_forecast(
        self, lat: float, lon: float, hours: int = 24
    ) -> Optional[dict[str, Any]]:
        """Get the hourly weather forecast for a location.

        Returns the upstream JSON dict (``{location, source, hours: [...]}``),
        or ``None`` if the service is not configured or the request fails.
        Callers should treat any individual field as optional.
        """
        if not self.base_url:
            logger.warning("WEATHER_SERVICE_URL not configured; skipping forecast fetch")
            return None

        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    self._url("/forecast"),
                    params={"lat": lat, "lon": lon, "hours": hours},
                    timeout=10.0,
                )
                response.raise_for_status()
                return response.json()
        except httpx.HTTPError as e:
            logger.warning(f"Failed to fetch weather forecast: {e}")
            return None


# Singleton instance
weather_client = WeatherClient()
