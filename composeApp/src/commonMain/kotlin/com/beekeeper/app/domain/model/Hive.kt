package com.beekeeper.app.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Hive(
    val id: String,
    val name: String,
    val apiaryId: String,
    val status: HiveStatus,
    val lastInspected: LocalDateTime,
    val imageUrl: String?,
    val colonyStrength: ColonyStrength,
    val queenStatus: QueenStatus,
    val temperament: Temperament,
    val honeyStores: HoneyStores
)

enum class HiveStatus {
    STRONG,
    ALERT,
    NEEDS_INSPECTION,
    WEAK
}

enum class ColonyStrength {
    STRONG,
    MODERATE,
    WEAK
}

enum class QueenStatus {
    LAYING,
    NOT_LAYING,
    MISSING,
    UNKNOWN
}

enum class Temperament {
    CALM,
    MODERATE,
    DEFENSIVE
}

enum class HoneyStores {
    FULL,
    ADEQUATE,
    LOW,
    EMPTY
}
