package com.beekeeper.app.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Inspection(
    val id: String,
    val hiveId: String,
    val userId: String,
    val inspectionDate: LocalDateTime,
    val durationMinutes: Int? = null,
    val queenSeen: Boolean = false,
    val queenMarked: Boolean = false,
    val queenColor: String? = null,
    val queenCells: QueenCellStatus = QueenCellStatus.NONE,
    val eggsSeen: Boolean = false,
    val larvaeSeen: Boolean = false,
    val cappedBroodSeen: Boolean = false,
    val broodPattern: BroodPattern = BroodPattern.GOOD,
    val broodFrames: Int? = null,
    val population: ColonyPopulation = ColonyPopulation.MEDIUM,
    val temperament: ColonyTemperament = ColonyTemperament.CALM,
    val healthStatus: InspectionHealthStatus = InspectionHealthStatus.HEALTHY,
    val honeyStores: ResourceLevel = ResourceLevel.MEDIUM,
    val honeyFrames: Int? = null,
    val pollenStores: ResourceLevel = ResourceLevel.MEDIUM,
    val pollenFrames: Int? = null,
    val varroaMitesDetected: Boolean = false,
    val varroaLevel: String? = null,
    val otherPestsDetected: Boolean = false,
    val pestDescription: String? = null,
    val diseaseDetected: Boolean = false,
    val diseaseDescription: String? = null,
    val feedingDone: Boolean = false,
    val feedingType: String? = null,
    val feedingAmount: String? = null,
    val treatmentApplied: Boolean = false,
    val treatmentType: String? = null,
    val equipmentChanges: String? = null,
    val weatherTemp: Double? = null,
    val weatherConditions: String? = null,
    val notes: String? = null,
    val photos: List<String>? = null,
    val nextInspectionDate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class QueenCellStatus {
    NONE,
    PLAY_CUPS,
    QUEEN_CELLS,
    CHARGED_CELLS,
    CAPPED_CELLS,
    EMERGENCY_CELLS
}

enum class BroodPattern {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    SPOTTY
}

enum class ColonyTemperament {
    VERY_CALM,
    CALM,
    MODERATE,
    DEFENSIVE,
    VERY_DEFENSIVE,
    AGGRESSIVE
}

enum class ColonyPopulation {
    VERY_STRONG,
    STRONG,
    MEDIUM,
    WEAK,
    VERY_WEAK
}

enum class InspectionHealthStatus {
    EXCELLENT,
    HEALTHY,
    FAIR,
    CONCERNING,
    CRITICAL
}

enum class ResourceLevel {
    FULL,
    HIGH,
    MEDIUM,
    LOW,
    EMPTY
}
