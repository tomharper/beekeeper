package com.beekeeper.app.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Alert(
    val id: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: LocalDateTime,
    val hiveIds: List<String> = emptyList(),
    val dismissed: Boolean = false
)

enum class AlertType {
    SWARM_WARNING,
    VARROA_MITE,
    INSPECTION_DUE,
    TREATMENT_DUE,
    WEATHER_WARNING,
    HONEY_FLOW,
    GENERAL
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}
