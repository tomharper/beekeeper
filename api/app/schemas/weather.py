from pydantic import BaseModel, ConfigDict
from enum import Enum


def to_camel(string: str) -> str:
    """Convert snake_case to camelCase"""
    words = string.split('_')
    return words[0] + ''.join(word.capitalize() for word in words[1:])


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

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
