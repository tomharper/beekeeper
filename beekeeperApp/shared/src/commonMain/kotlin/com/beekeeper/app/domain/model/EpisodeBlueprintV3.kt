package com.beekeeper.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Episode Blueprint V3 - Series Arc Focused
 *
 * CHANGES FROM V2:
 * - Removed characterArcs from episode level (moved to Bible)
 * - Series character arcs now in bible.seriesCharacterArcs
 * - Episode blueprints reference series arcs via characterBeats
 * - Cleaner separation: Bible defines WHO they become, Blueprint defines WHAT happens
 * - Added archetype fields (archetypeId, archetype, structurePattern)
 * - Added production metadata (genres, tones, duration, pacingStyle)
 * - Flexible act count (not hardcoded to 4)
 */

/**
 * Basic episode information
 */
@Serializable
data class EpisodeMetadataV3(
    val episodeNumber: Int,
    val title: String,
    val logline: String,
    val synopsis: String? = null,
    val themes: List<String> = emptyList(),
    val narrativeFunction: String? = null,
    val worldBuilding: String? = null
)

// EssentialEvent, Revelation, and PlantedSeed are imported from ProjectBible.kt
// to avoid duplication

/**
 * A cause-effect relationship within this act
 */
@Serializable
data class CausalRelationship(
    val cause: String,
    val effect: String,
    val inevitability: String? = null
)

/**
 * A specific character moment within an act
 *
 * References the series character arc defined in the bible
 */
@Serializable
data class CharacterBeat(
    val characterId: String,
    val arcReference: String? = null,
    val arcMilestone: String? = null,
    val moment: String,
    val purpose: String,
    val emotionalTone: String? = null
)

/**
 * Programmatic scene beat for generation (can be AI-generated from act structure)
 */
@Serializable
data class SceneBeat(
    val beatNumber: Int,
    val location: String,
    val locationType: String,
    val timeOfDay: String,
    val description: String,
    val charactersPresent: List<String>,
    val targetDialogueLines: Int,
    val tension: String = "medium",
    val pace: String = "normal"
)

/**
 * One act of the episode
 *
 * Consolidates narrative moments, story logic, and character beats
 */
@Serializable
data class ActStructureV3(
    val actPosition: String,
    val actName: String,
    val description: String,
    val charactersPresent: List<String> = emptyList(),
    val keyMoments: List<String> = emptyList(),
    val revelations: List<Revelation> = emptyList(),
    val plantedSeeds: List<PlantedSeed> = emptyList(),
    val payoffs: List<String> = emptyList(),
    val causality: List<CausalRelationship> = emptyList(),
    val characterBeats: List<CharacterBeat> = emptyList(),
    val thematicFocus: List<String> = emptyList(),
    val sceneBeats: List<SceneBeat> = emptyList()
)

/**
 * Validation checks for story consistency
 */
@Serializable
data class ValidationV3(
    val characterConsistency: Map<String, String> = emptyMap(),
    val worldRuleCompliance: List<String> = emptyList(),
    val causalityIntact: Boolean? = null,
    val thematicAlignment: Boolean? = null,
    val issues: List<String> = emptyList()
)

/**
 * Episode Blueprint V3 - Complete structure
 *
 * Character transformation is defined at SERIES level in the bible.
 * Episodes show what happens through characterBeats that reference those arcs.
 */
@Serializable
data class EpisodeBlueprintV3(
    val episodeMetadata: EpisodeMetadataV3,
    val archetypeId: String,
    val archetype: String,
    val structurePattern: String,
    val genres: List<String>,
    val tones: List<String>,
    val duration: String,
    val pacingStyle: String,
    val charactersPresent: List<String>,
    val actStructure: List<ActStructureV3>,
    val essentialEvents: List<EssentialEvent> = emptyList(),
    val validation: ValidationV3? = null
)
