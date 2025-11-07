package com.beekeeper.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Recommendation(
    val id: String,
    val hiveId: String,
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: Priority
)

enum class RecommendationType {
    POSITIVE,
    WARNING,
    ACTION_REQUIRED,
    INFO
}

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}
