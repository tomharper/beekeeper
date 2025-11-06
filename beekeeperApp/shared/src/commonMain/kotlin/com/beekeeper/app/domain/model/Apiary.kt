package com.beekeeper.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents an apiary (bee yard) - a collection of hives in one location
 */
@Serializable
data class Apiary(
    val id: String,
    val name: String,
    val location: ApiaryLocation,
    val description: String = "",

    // Apiary details
    val hiveCount: Int = 0,
    val maxCapacity: Int? = null,
    val established: String? = null,  // Date string

    // Environment
    val floraSources: List<FloraSource> = emptyList(),
    val waterSource: WaterSource? = null,
    val sunExposure: SunExposure = SunExposure.FULL_SUN,
    val windProtection: WindProtection = WindProtection.MODERATE,

    // Registration
    val registrationNumber: String? = null,
    val registeredWith: String? = null,

    // Management
    val ownerId: String,
    val managerId: String? = null,
    val accessibility: ApiaryAccessibility = ApiaryAccessibility.EASY,
    val visitationNotes: String = "",

    // Media
    val photos: List<String> = emptyList(),
    val notes: String = "",

    val createdAt: String,
    val updatedAt: String
)

/**
 * Location details for an apiary
 */
@Serializable
data class ApiaryLocation(
    val address: String,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val elevation: Double? = null,  // meters
    val propertyType: PropertyType = PropertyType.PERSONAL
)

/**
 * Flora/forage source near the apiary
 */
@Serializable
data class FloraSource(
    val plantName: String,
    val bloomSeason: List<Season>,
    val distance: Double? = null,  // km
    val abundance: FloraAbundance = FloraAbundance.MODERATE,
    val notes: String = ""
)

/**
 * Water source information
 */
@Serializable
data class WaterSource(
    val sourceType: WaterSourceType,
    val distance: Double? = null,  // meters
    val reliability: WaterReliability = WaterReliability.RELIABLE,
    val notes: String = ""
)

/**
 * Beekeeping record/note
 */
@Serializable
data class BeekeepingNote(
    val id: String,
    val title: String,
    val content: String,
    val noteType: NoteType,
    val relatedHiveId: String? = null,
    val relatedApiaryId: String? = null,
    val relatedInspectionId: String? = null,
    val relatedTaskId: String? = null,
    val tags: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

// Enums

@Serializable
enum class PropertyType {
    PERSONAL,
    RENTED,
    COMMUNITY,
    PUBLIC,
    COMMERCIAL
}

@Serializable
enum class SunExposure {
    FULL_SUN,
    PARTIAL_SHADE,
    FULL_SHADE,
    DAPPLED_SHADE
}

@Serializable
enum class WindProtection {
    NONE,
    LIGHT,
    MODERATE,
    FULL
}

@Serializable
enum class ApiaryAccessibility {
    EASY,
    MODERATE,
    DIFFICULT,
    REQUIRES_4WD
}

@Serializable
enum class FloraAbundance {
    SCARCE,
    LIGHT,
    MODERATE,
    ABUNDANT,
    EXCELLENT
}

@Serializable
enum class WaterSourceType {
    NATURAL_POND,
    STREAM,
    RIVER,
    LAKE,
    ARTIFICIAL_POND,
    WATER_BUCKET,
    BIRDBATH,
    OTHER
}

@Serializable
enum class WaterReliability {
    UNRELIABLE,
    SEASONAL,
    RELIABLE,
    YEAR_ROUND
}

@Serializable
enum class NoteType {
    OBSERVATION,
    TIP,
    REMINDER,
    LESSON_LEARNED,
    RESEARCH,
    GENERAL
}
