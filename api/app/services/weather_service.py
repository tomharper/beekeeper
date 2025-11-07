from app.schemas import WeatherResponse, WeatherCondition


class WeatherService:
    def get_current_weather(self) -> WeatherResponse:
        """Returns mock weather data. In production, this would call a real weather API."""
        return WeatherResponse(
            temperature=72,
            humidity=10,
            wind_speed=5,
            condition=WeatherCondition.SUNNY,
            description="Good conditions for hive work. The bees will be calm and active today.",
        )
