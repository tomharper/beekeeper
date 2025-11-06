// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/CommonEnums.kt
package com.beekeeper.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Common enums used across the application
 * Superset of all enum values to support cross-platform content creation
 */

// Gender enum for avatar and character demographics
@Serializable(with = CaseInsensitiveGenderSerializer::class)
enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    UNSPECIFIED
}

/**
 * Custom serializer for Gender that handles case-insensitive deserialization
 * Accepts both "MALE" and "male" formats from API
 */
object CaseInsensitiveGenderSerializer : kotlinx.serialization.KSerializer<Gender> {
    override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
        kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("Gender", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Gender) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): Gender {
        val value = decoder.decodeString().uppercase() // Convert to uppercase for matching
        return Gender.valueOf(value)
    }
}

// Age range enum for demographics
enum class AgeRange {
    CHILD,      // 0-12
    TEEN,       // 13-17
    YOUNG,      // 18-25
    ADULT,      // 26-45
    MIDDLE_AGE, // 46-65
    SENIOR,     // 65+
    UNSPECIFIED
}


// Additional enum that might be missing
@Serializable
enum class StoryStructureType {
    THREE_ACT,
    FIVE_ACT,
    SEVEN_ACT,
    HERO_JOURNEY,
    SAVE_THE_CAT,
    FREYTAG_PYRAMID,
    KISHŌTENKETSU,
    NONLINEAR,
    EPISODIC,
    ANTHOLOGY,
    CIRCULAR,
    PARALLEL,
    FRAME_STORY,
    IN_MEDIAS_RES,
    REVERSE_CHRONOLOGY
}
