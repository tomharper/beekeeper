package com.beekeeper.app.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Represents a beehive in the apiary
 */
@Serializable
data class Hive(
    val id: String,
    val name: String,
    val location: HiveLocation,
    val installationDate: LocalDate,
    val hiveType: HiveType,
    val frameCount: Int = 10,
    val boxConfiguration: List<HiveBox> = emptyList(),
    val queenInfo: QueenInfo? = null,
    val status: HiveStatus = HiveStatus.ACTIVE,
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

/**
 * Physical location of the hive
 */
@Serializable
data class HiveLocation(
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val apiaryName: String? = null,
    val positionInApiary: String? = null  // e.g., "Row 2, Position 3"
)

/**
 * Information about the hive's queen bee
 */
@Serializable
data class QueenInfo(
    val age: Int? = null,  // Age in years
    val breed: String? = null,  // e.g., "Italian", "Carniolan", "Russian"
    val marked: Boolean = false,
    val markColor: String? = null,  // Color coding by year
    val source: String? = null,  // Where the queen came from
    val temperament: QueenTemperament = QueenTemperament.AVERAGE,
    val productivity: QueenProductivity = QueenProductivity.AVERAGE,
    val installDate: LocalDate? = null,
    val notes: String = ""
)

/**
 * Represents a single box/super in the hive
 */
@Serializable
data class HiveBox(
    val id: String,
    val position: Int,  // 1 = bottom, 2 = second from bottom, etc.
    val boxType: HiveBoxType,
    val frameCount: Int,
    val purpose: BoxPurpose
)

/**
 * Type of beehive
 */
@Serializable
enum class HiveType {
    LANGSTROTH,
    TOP_BAR,
    WARRE,
    FLOW_HIVE,
    NATIONAL,
    OTHER
}

/**
 * Type of hive box/super
 */
@Serializable
enum class HiveBoxType {
    DEEP_BOX,      // Deep super (9 5/8")
    MEDIUM_BOX,    // Medium super (6 5/8")
    SHALLOW_BOX,   // Shallow super (5 11/16")
    CUSTOM
}

/**
 * Purpose of a hive box
 */
@Serializable
enum class BoxPurpose {
    BROOD,         // For raising bees
    HONEY,         // For honey production
    HONEY_SUPER,   // Specifically for honey harvest
    MIXED          // Mixed brood and honey
}

/**
 * Current status of the hive
 */
@Serializable
enum class HiveStatus {
    ACTIVE,        // Actively maintained
    QUEENLESS,     // No queen detected
    WEAK,          // Colony is weak
    COMBINING,     // Being combined with another hive
    DEAD,          // Colony has died
    SPLIT,         // Colony has been split
    INACTIVE       // Not currently being worked
}

/**
 * Queen temperament rating
 */
@Serializable
enum class QueenTemperament {
    CALM,
    AVERAGE,
    DEFENSIVE,
    AGGRESSIVE
}

/**
 * Queen productivity rating
 */
@Serializable
enum class QueenProductivity {
    POOR,
    BELOW_AVERAGE,
    AVERAGE,
    ABOVE_AVERAGE,
    EXCELLENT
}
