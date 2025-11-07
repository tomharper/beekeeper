from pydantic import BaseModel
from enum import Enum


class WeatherCondition(str, Enum):
    SUNNY = "SUNNY"
    PARTLY_CLOUDY = "PARTLY_CLOUDY"
    CLOUDY = "CLOUDY"
    RAINY = "RAINY"
    STORMY = "STORMY"


class WeatherResponse(BaseModel):
    temperature: int
    humidity: int
    wind_speed: int
    condition: WeatherCondition
    description: str
