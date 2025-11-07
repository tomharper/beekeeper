from fastapi import APIRouter
from app.services import WeatherService
from app.schemas import WeatherResponse

router = APIRouter(prefix="/weather", tags=["weather"])


@router.get("", response_model=WeatherResponse)
def get_weather():
    """Get current weather conditions"""
    service = WeatherService()
    return service.get_current_weather()
