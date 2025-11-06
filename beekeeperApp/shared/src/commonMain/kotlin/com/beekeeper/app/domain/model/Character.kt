// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/Character.kt
package com.beekeeper.app.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Domain entity representing a Character in the story
 */


@Serializable
data class CharacterProfile(
    val id: String,
    val projectId: String = "", // Optional - API may not provide this for all characters
    val name: String,
    val role: CharacterRole,
    val archetype: String,
    val description: String? = null,
    val personality: PersonalityProfile,
    val age: Int,
    val height: String,
    val build: String,
    val hairColor: String,
    val eyeColor: String,
    val physicalAttributes: PhysicalAttributes,
    val distinctiveFeatures: List<String>,
    val gender: Gender = Gender.UNSPECIFIED,
    val voiceProfile: VoiceProfile? = null,
    val imageUrl: String? = null,
    val createdAt: Instant = kotlinx.datetime.Clock.System.now(),
    val updatedAt: Instant = kotlinx.datetime.Clock.System.now(),
    val relationships: List<CharacterRelationship> = emptyList(),
    val assignedAvatarId: String? = null,
    val primaryIconMediaId: String? = null,
    val screenTime: Float = 0f, // Percentage
    val dialogueCount: Int = 0,
    val metadata: Map<String, String> = emptyMap<String, String>(),
    //val voiceId: String? = null,  // Additional property for voice assignment
    val traits: List<String> = emptyList(),
    val skills: List<String> = emptyList(),

    // THIS IS NEW STUFF FROM CHARACTER BIBLE
    // The deep backstory that shapes everything
    val formativeExperiences: List<FormativeExperience> = emptyList(),

    // What drives every decision
    val decisionLogic: DecisionLogic? = null,

    // How the character changes and why
    val evolutionPath: CharacterEvolutionPath? = null,

    // Internal conflicts that create drama
    val internalConflicts: List<InternalConflict> = emptyList(),

    // Secrets that drive behavior
    val secrets: List<CharacterSecret> = emptyList(),

    // Specific knowledge this character has
    val knowledge: CharacterKnowledge? = null,

    // Behavioral patterns and triggers
    val behaviorPatterns: List<BehaviorPattern> = emptyList(),

    // AI generation prompts for external tools
    val visualPrompt: String? = null,    // For character visual generation (Midjourney, Stable Diffusion)
    val voicePrompt: String? = null,     // For voice generation (ElevenLabs)
    val ambientPrompt: String? = null    // For character theme/ambient audio
)




@Serializable
data class PhysicalAttributes(
    val height: String,
    val build: String,
    val hairColor: String,
    val eyeColor: String,
    val distinctiveFeatures: List<String>
)

@Serializable
data class PersonalityProfile(
    val backstory: String,
    val archetype: String? = null,
    @kotlinx.serialization.Serializable(with = FlexibleTraitListSerializer::class)
    val traits: List<PersonalityTrait> = emptyList(),
    val motivations: List<String> = emptyList(),
    val fears: List<String> = emptyList(),
    val aiInsights: String? = null,
    val oceanScores: OceanPersonality? = null,

    // we need to f
    val coreWound: String? = null,      // The deepest hurt
    val coreFear: String? = null,       // What they most fear
    val coreDesire: String? = null,     // What they most want
    val coreMisbelief: String? = null,  // The lie they believe
    val coreValue: String? = null,      // What they'd never compromise
    val coreTruth: String? = null,      // The truth they need to learn
    val copingMechanisms: List<String> = emptyList(),
    val defenseMechanisms: List<String> = emptyList(),
    val blindSpots: List<String> = emptyList(),
)

/**
 * Custom serializer that handles both string and PersonalityTrait object formats from API
 * Converts strings like ["Charismatic"] to PersonalityTrait objects with default strength
 */
object FlexibleTraitListSerializer : kotlinx.serialization.KSerializer<List<PersonalityTrait>> {
    private val serializer = kotlinx.serialization.builtins.ListSerializer(PersonalityTrait.serializer())
    override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: List<PersonalityTrait>) {
        serializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): List<PersonalityTrait> {
        // Must use JsonDecoder to inspect the element type first
        val jsonDecoder = decoder as? kotlinx.serialization.json.JsonDecoder
            ?: return emptyList()

        val element = jsonDecoder.decodeJsonElement()
        if (element !is kotlinx.serialization.json.JsonArray) {
            return emptyList()
        }

        // Check if first element is a string or object
        val firstElement = element.firstOrNull() ?: return emptyList()

        return if (firstElement is kotlinx.serialization.json.JsonPrimitive && firstElement.isString) {
            // Array of strings - convert each to PersonalityTrait with default strength
            element.mapNotNull { jsonElement ->
                (jsonElement as? kotlinx.serialization.json.JsonPrimitive)?.let {
                    if (it.isString) PersonalityTrait(it.content, 0.75f) else null
                }
            }
        } else if (firstElement is kotlinx.serialization.json.JsonObject) {
            // Array of PersonalityTrait objects - deserialize normally
            element.mapNotNull { jsonElement ->
                try {
                    kotlinx.serialization.json.Json.decodeFromJsonElement(PersonalityTrait.serializer(), jsonElement)
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            emptyList()
        }
    }
}

@Serializable
data class CharacterRelationship(
    val targetCharacterId: String = "", // Optional - API may not provide this
    val targetCharacterName: String,
    val relationshipType: String, // Changed from enum to String to handle descriptive text from API
    val description: String,
    val strength: Float = 0.5f,

    // new fields
    val whyItExists: String? = null,       // Relationship Logic
    val whatTheyNeed: String? = null,      // What they get from it
    val whatTheyGive: String? = null,      // What they provide
    val powerDynamic: String? = null,      // Who has power and why
    val conflictTriggers: List<String> = emptyList(),
    val bondStrengtheners: List<String> = emptyList(),
    val breakingPoint: String? = null,    // What would end it
    val evolution: String? = null          // How it changes over time
)



@Serializable
data class VoiceProfile(
    val voiceId: String? = null,           // Reference to voice library/model
    val voiceModelType: String? = null,    // "elevenlabs", "azure", "custom"
    val pitch: Float = 1.0f,               // 0.5 to 2.0
    val speed: Float = 1.0f,               // 0.5 to 2.0
    val volume: Float = 1.0f,              // 0.0 to 1.0
    val accent: String? = null,            // "british", "american", "neutral"
    val emotionalTone: String? = null,     // "warm", "authoritative", "friendly"
    val speakingStyle: String? = null,     // "narrative", "conversational", "dramatic"
    val customSettings: Map<String, String> = emptyMap(), // Provider-specific settings
    val description: String? = null,
    val previewUrl: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
enum class RelationshipType {
    FAMILY,
    FRIEND,
    ACQUAINTANCE,
    RIVAL,
    MENTOR,
    SUBORDINATE,
    ROMANTIC,
    PROFESSIONAL,
    COLLEAGUE,
    ENEMY,
    ALLY,
    CONVENIENCE,
    ENEMY_OF_MY_ENEMY,
    LEADER,
    NEUTRAL,
    GUIDE,
    SERVANT,
    FALSE_ALLY,
    SECRET_ALLY,
    SHARED_AWARENESS,
    JUDGE,
    ANCIENT_PRIMORDIAL_BOND,
    CORRUPTOR,
    TEACHER,
    STUDENT,
    CONFIDANT,
    HUNTER,
    HEALER
}

@Serializable(with = CaseInsensitiveCharacterRoleSerializer::class)
enum class CharacterRole {
    PROTAGONIST,
    ANTAGONIST,
    SUPPORTING,
    MINOR,
    BETRAYOR,
    ORACULAR_FATES,
    EXTRA,
    NARRATOR,
    MENTOR,
    LOVE_INTEREST,
    COMIC_RELIEF,
    SIDEKICK,
    BACKGROUND,
    DEUTERAGONIST,
    VICTIM,
    ALLY,
    EMOTIONAL_CORE,
    CATALYST,
    COMPANION,
    LEADER,
    GUIDE_ANTAGONIST,
    ANTAGONIST_VICTIM,
    MENTOR_SAVIOR,
    KEY_CHILD
}

/**
 * Custom serializer for CharacterRole that handles case-insensitive deserialization
 * Accepts both "PROTAGONIST" and "Protagonist" formats from API
 */
object CaseInsensitiveCharacterRoleSerializer : kotlinx.serialization.KSerializer<CharacterRole> {
    override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
        kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("CharacterRole", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: CharacterRole) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): CharacterRole {
        val value = decoder.decodeString().uppercase() // Convert to uppercase for matching
        return CharacterRole.valueOf(value)
    }
}

@Serializable
data class PersonalityTrait(
    val name: String,
    val strength: Float // 0.0 to 1.0
)

@Serializable
data class OceanPersonality(
    val openness: Float,
    val conscientiousness: Float,
    val extraversion: Float,
    val agreeableness: Float,
    val neuroticism: Float
)

