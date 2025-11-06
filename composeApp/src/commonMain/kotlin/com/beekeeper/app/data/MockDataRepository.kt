package com.beekeeper.app.data

import com.beekeeper.app.domain.model.*
import kotlinx.datetime.LocalDateTime

object MockDataRepository {

    val apiaries = listOf(
        Apiary(
            id = "1",
            name = "Backyard Garden",
            location = "Sunnyvale, CA",
            latitude = 37.3688,
            longitude = -122.0363,
            hiveCount = 5,
            status = ApiaryStatus.HEALTHY
        ),
        Apiary(
            id = "2",
            name = "Hillside Meadow",
            location = "45.123, -122.456",
            latitude = 45.123,
            longitude = -122.456,
            hiveCount = 8,
            status = ApiaryStatus.WARNING
        ),
        Apiary(
            id = "3",
            name = "Riverbend Apiary",
            location = "Cloverdale, OR",
            latitude = 45.2271,
            longitude = -123.4023,
            hiveCount = 3,
            status = ApiaryStatus.ALERT
        )
    )

    val hives = listOf(
        // Backyard Garden hives
        Hive(
            id = "h1",
            name = "Hive A-01",
            apiaryId = "1",
            status = HiveStatus.STRONG,
            lastInspected = LocalDateTime(2024, 11, 3, 14, 30),
            imageUrl = null,
            colonyStrength = ColonyStrength.STRONG,
            queenStatus = QueenStatus.LAYING,
            temperament = Temperament.CALM,
            honeyStores = HoneyStores.FULL
        ),
        Hive(
            id = "h2",
            name = "Hive A-02",
            apiaryId = "1",
            status = HiveStatus.NEEDS_INSPECTION,
            lastInspected = LocalDateTime(2024, 10, 23, 10, 15),
            imageUrl = null,
            colonyStrength = ColonyStrength.MODERATE,
            queenStatus = QueenStatus.LAYING,
            temperament = Temperament.MODERATE,
            honeyStores = HoneyStores.ADEQUATE
        ),
        Hive(
            id = "h3",
            name = "Hive B-01",
            apiaryId = "1",
            status = HiveStatus.STRONG,
            lastInspected = LocalDateTime(2024, 11, 1, 9, 0),
            imageUrl = null,
            colonyStrength = ColonyStrength.STRONG,
            queenStatus = QueenStatus.LAYING,
            temperament = Temperament.CALM,
            honeyStores = HoneyStores.FULL
        ),
        Hive(
            id = "h4",
            name = "Hive B-02",
            apiaryId = "1",
            status = HiveStatus.ALERT,
            lastInspected = LocalDateTime(2024, 11, 4, 15, 45),
            imageUrl = null,
            colonyStrength = ColonyStrength.WEAK,
            queenStatus = QueenStatus.NOT_LAYING,
            temperament = Temperament.DEFENSIVE,
            honeyStores = HoneyStores.LOW
        ),
        // Hillside Meadow hives
        Hive(
            id = "h5",
            name = "Hive 01",
            apiaryId = "2",
            status = HiveStatus.STRONG,
            lastInspected = LocalDateTime(2024, 11, 5, 11, 0),
            imageUrl = null,
            colonyStrength = ColonyStrength.STRONG,
            queenStatus = QueenStatus.LAYING,
            temperament = Temperament.CALM,
            honeyStores = HoneyStores.FULL
        ),
        Hive(
            id = "h6",
            name = "Hive 02",
            apiaryId = "2",
            status = HiveStatus.STRONG,
            lastInspected = LocalDateTime(2024, 11, 4, 13, 30),
            imageUrl = null,
            colonyStrength = ColonyStrength.STRONG,
            queenStatus = QueenStatus.LAYING,
            temperament = Temperament.CALM,
            honeyStores = HoneyStores.ADEQUATE
        ),
        Hive(
            id = "h7",
            name = "Hive 03",
            apiaryId = "2",
            status = HiveStatus.NEEDS_INSPECTION,
            lastInspected = LocalDateTime(2024, 10, 28, 16, 0),
            imageUrl = null,
            colonyStrength = ColonyStrength.MODERATE,
            queenStatus = QueenStatus.LAYING,
            temperament = Temperament.MODERATE,
            honeyStores = HoneyStores.ADEQUATE
        ),
        Hive(
            id = "h8",
            name = "Hive 04",
            apiaryId = "2",
            status = HiveStatus.ALERT,
            lastInspected = LocalDateTime(2024, 11, 2, 14, 0),
            imageUrl = null,
            colonyStrength = ColonyStrength.MODERATE,
            queenStatus = QueenStatus.LAYING,
            temperament = Temperament.CALM,
            honeyStores = HoneyStores.ADEQUATE
        ),
        // Riverbend hives
        Hive(
            id = "h9",
            name = "Hive 01",
            apiaryId = "3",
            status = HiveStatus.WEAK,
            lastInspected = LocalDateTime(2024, 10, 30, 10, 0),
            imageUrl = null,
            colonyStrength = ColonyStrength.WEAK,
            queenStatus = QueenStatus.MISSING,
            temperament = Temperament.DEFENSIVE,
            honeyStores = HoneyStores.LOW
        )
    )

    val alerts = listOf(
        Alert(
            id = "a1",
            type = AlertType.SWARM_WARNING,
            title = "Swarm Warning",
            message = "AI Alert: High Swarm Probability in this area. Check hives A-01 and C-04.",
            severity = AlertSeverity.WARNING,
            timestamp = LocalDateTime(2024, 11, 6, 8, 0),
            hiveIds = listOf("h1", "h4")
        ),
        Alert(
            id = "a2",
            type = AlertType.TREATMENT_DUE,
            title = "Mite treatment due",
            message = "For Hive 02",
            severity = AlertSeverity.WARNING,
            timestamp = LocalDateTime(2024, 11, 6, 0, 0),
            hiveIds = listOf("h2")
        ),
        Alert(
            id = "a3",
            type = AlertType.WEATHER_WARNING,
            title = "High humidity overnight",
            message = "Ensure hive ventilation",
            severity = AlertSeverity.INFO,
            timestamp = LocalDateTime(2024, 11, 6, 18, 0)
        )
    )

    val recommendations = mapOf(
        "h1" to listOf(
            Recommendation(
                id = "r1",
                hiveId = "h1",
                type = RecommendationType.POSITIVE,
                title = "Hive is Thriving",
                description = "Excellent honey stores and strong population. Consider adding a super soon to prevent swarming.",
                priority = Priority.LOW
            ),
            Recommendation(
                id = "r2",
                hiveId = "h1",
                type = RecommendationType.WARNING,
                title = "Monitor Varroa Mites",
                description = "Your last mite count was 3 weeks ago. It's recommended to perform a new count within the next 7 days.",
                priority = Priority.MEDIUM
            )
        ),
        "h4" to listOf(
            Recommendation(
                id = "r3",
                hiveId = "h4",
                type = RecommendationType.ACTION_REQUIRED,
                title = "Queen Issue Detected",
                description = "No laying pattern observed. Consider requeening within 2 weeks.",
                priority = Priority.HIGH
            )
        )
    )

    val weather = Weather(
        temperature = 72,
        humidity = 10,
        windSpeed = 5,
        condition = WeatherCondition.SUNNY,
        description = "Good conditions for hive work. The bees will be calm and active today."
    )

    fun getApiaryById(id: String) = apiaries.find { it.id == id }

    fun getHiveById(id: String) = hives.find { it.id == id }

    fun getHivesForApiary(apiaryId: String) = hives.filter { it.apiaryId == apiaryId }

    fun getRecommendationsForHive(hiveId: String) = recommendations[hiveId] ?: emptyList()

    fun getActiveAlerts() = alerts.filter { !it.dismissed }
}
