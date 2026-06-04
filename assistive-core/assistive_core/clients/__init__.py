"""External-service clients subpackage.

TO BE CREATED BY THE CLIENTS MODULE AGENT:
  - bunny.py   -> class BunnyStorageService (port beekeeper
                  services/bunny_storage_service.py) reading config from
                  assistive_core.settings (BUNNY_STORAGE_ZONE, BUNNY_API_KEY,
                  BUNNY_CDN_URL). Methods:
                    async def upload_photo(self, file_content: bytes,
                        filename: str, folder: str = "inspections") -> str | None
                    async def delete_photo(self, file_path: str) -> bool
                  Export a module-level `bunny_storage` instance.
  - weather.py -> class WeatherClient calling the sibling VRUsafety weather
                  service at settings.WEATHER_SERVICE_URL via httpx. (Beekeeper's
                  weather_service.py returns mock data; this client should hit the
                  real service. Keep a get_current_weather() entrypoint.)
                  Export a module-level `weather_client` instance.

This __init__ must continue to export: BunnyStorageService, bunny_storage,
WeatherClient, weather_client.
"""
try:
    from .bunny import BunnyStorageService, bunny_storage  # type: ignore
except Exception:  # pragma: no cover
    BunnyStorageService = None  # type: ignore
    bunny_storage = None

try:
    from .weather import WeatherClient, weather_client  # type: ignore
except Exception:  # pragma: no cover
    WeatherClient = None  # type: ignore
    weather_client = None

__all__ = [
    "BunnyStorageService",
    "bunny_storage",
    "WeatherClient",
    "weather_client",
]
