package com.beekeeper.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Weather(
    val temperature: Int,
    val humidity: Int,
    val windSpeed: Int,
    val condition: WeatherCondition,
    val description: String
)

enum class WeatherCondition {
    SUNNY,
    PARTLY_CLOUDY,
    CLOUDY,
    RAINY,
    STORMY
}
