package com.beekeeper.app.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Represents a hive inspection record
 */
@Serializable
data class Inspection(
    val id: String,
    val hiveId: String,
    val inspectionDate: LocalDateTime,
    val durationMinutes: Int? = null,
    val weather: WeatherConditions? = null,

    // Queen observations
    val queenSeen: Boolean = false,
    val queenMarked: Boolean = false,
    val queenCells: QueenCellStatus = QueenCellStatus.NONE,

    // Brood observations
    val broodPattern: BroodPattern = BroodPattern.GOOD,
    val broodStages: BroodStages? = null,
    val estimatedBroodFrames: Int? = null,

    // Colony observations
    val temperament: ColonyTemperament = ColonyTemperament.CALM,
    val population: ColonyPopulation = ColonyPopulation.MEDIUM,
    val estimatedFramesCovered: Int? = null,

    // Health observations
    val healthStatus: HealthStatus = HealthStatus.HEALTHY,
    val pests: List<PestObservation> = emptyList(),
    val diseases: List<DiseaseObservation> = emptyList(),

    // Resources
    val honeyStores: ResourceLevel = ResourceLevel.ADEQUATE,
    val pollenStores: ResourceLevel = ResourceLevel.ADEQUATE,
    val cappedHoney: Boolean = false,

    // Actions taken
    val actionsTaken: List<InspectionAction> = emptyList(),
    val feedingDone: FeedingRecord? = null,
    val treatmentApplied: TreatmentRecord? = null,

    // Media and notes
    val photos: List<String> = emptyList(),  // Photo URLs/paths
    val aiAnalysis: List<AIAnalysisResult> = emptyList(),
    val notes: String = "",
    val nextInspectionDate: LocalDateTime? = null,

    val createdAt: String,
    val updatedAt: String
)

/**
 * Weather conditions during inspection
 */
@Serializable
data class WeatherConditions(
    val temperature: Double,  // in Celsius
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val humidity: Int? = null,  // percentage
    val windSpeed: Double? = null,
    val conditions: String? = null,  // "Sunny", "Cloudy", etc.
)

/**
 * Brood stages observed
 */
@Serializable
data class BroodStages(
    val eggs: Boolean = false,
    val larvae: Boolean = false,
    val cappedBrood: Boolean = false,
    val emergingBees: Boolean = false
)

/**
 * Pest observation
 */
@Serializable
data class PestObservation(
    val pestType: PestType,
    val severity: PestSeverity,
    val location: String? = null,  // Where in hive
    val count: Int? = null,  // Mite count if applicable
    val notes: String = ""
)

/**
 * Disease observation
 */
@Serializable
data class DiseaseObservation(
    val diseaseType: DiseaseType,
    val severity: DiseaseSeverity,
    val affectedFrames: Int? = null,
    val symptoms: String = "",
    val notes: String = ""
)

/**
 * Action taken during inspection
 */
@Serializable
data class InspectionAction(
    val actionType: ActionType,
    val description: String,
    val boxesAffected: List<Int> = emptyList()
)

/**
 * Feeding record
 */
@Serializable
data class FeedingRecord(
    val feedType: FeedType,
    val amount: Double,
    val unit: String,  // "kg", "lbs", "liters", etc.
    val notes: String = ""
)

/**
 * Treatment application record
 */
@Serializable
data class TreatmentRecord(
    val treatmentType: TreatmentType,
    val product: String,
    val dosage: String,
    val targetPest: PestType? = null,
    val notes: String = ""
)

/**
 * AI analysis result for a photo
 */
@Serializable
data class AIAnalysisResult(
    val photoUrl: String,
    val analysisType: AIAnalysisType,
    val confidence: Double,  // 0.0 to 1.0
    val findings: Map<String, String>,
    val recommendations: List<String>,
    val analysisDate: LocalDateTime
)

// Enums

@Serializable
enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

@Serializable
enum class QueenCellStatus {
    NONE,
    QUEEN_CUPS,
    CHARGED_CELLS,
    CAPPED_CELLS,
    SUPERSEDURE_CELLS,
    SWARM_CELLS
}

@Serializable
enum class BroodPattern {
    EXCELLENT,
    GOOD,
    SPOTTY,
    POOR,
    NONE
}

@Serializable
enum class ColonyTemperament {
    VERY_CALM,
    CALM,
    MODERATE,
    DEFENSIVE,
    AGGRESSIVE,
    VERY_AGGRESSIVE
}

@Serializable
enum class ColonyPopulation {
    VERY_WEAK,
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}

@Serializable
enum class HealthStatus {
    EXCELLENT,
    HEALTHY,
    CONCERNING,
    NEEDS_ATTENTION,
    CRITICAL
}

@Serializable
enum class ResourceLevel {
    NONE,
    VERY_LOW,
    LOW,
    ADEQUATE,
    GOOD,
    EXCELLENT
}

@Serializable
enum class PestType {
    VARROA_MITES,
    SMALL_HIVE_BEETLE,
    WAX_MOTH,
    ANTS,
    WASPS,
    MICE,
    BEARS,
    OTHER
}

@Serializable
enum class PestSeverity {
    NONE,
    LOW,
    MODERATE,
    HIGH,
    SEVERE
}

@Serializable
enum class DiseaseType {
    AMERICAN_FOULBROOD,
    EUROPEAN_FOULBROOD,
    CHALKBROOD,
    NOSEMA,
    DEFORMED_WING_VIRUS,
    SACBROOD,
    OTHER
}

@Serializable
enum class DiseaseSeverity {
    NONE,
    MILD,
    MODERATE,
    SEVERE,
    CRITICAL
}

@Serializable
enum class ActionType {
    ADDED_BOX,
    REMOVED_BOX,
    ADDED_FRAMES,
    REMOVED_FRAMES,
    REVERSED_BOXES,
    HARVESTED_HONEY,
    FED_COLONY,
    APPLIED_TREATMENT,
    REPLACED_QUEEN,
    COMBINED_HIVES,
    SPLIT_HIVE,
    OTHER
}

@Serializable
enum class FeedType {
    SUGAR_SYRUP_1_1,
    SUGAR_SYRUP_2_1,
    FONDANT,
    CANDY_BOARD,
    POLLEN_PATTY,
    PROTEIN_SUPPLEMENT,
    OTHER
}

@Serializable
enum class TreatmentType {
    VARROA_TREATMENT,
    ANTIBIOTIC,
    FUNGICIDE,
    ORGANIC_ACID,
    ESSENTIAL_OIL,
    OTHER
}

@Serializable
enum class AIAnalysisType {
    HIVE_HEALTH,
    QUEEN_DETECTION,
    BROOD_PATTERN,
    PEST_DETECTION,
    DISEASE_DETECTION,
    POPULATION_ESTIMATE,
    GENERAL_ANALYSIS
}
