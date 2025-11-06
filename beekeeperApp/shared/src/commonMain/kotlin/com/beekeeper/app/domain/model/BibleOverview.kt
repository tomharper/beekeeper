package com.beekeeper.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Canonical BibleOverview Structure (V2)
 *
 * Simplified, flatter structure for project bibles that better represents
 * world rules and systems in a unified manner. This is the canonical format
 * stored in bible_overview_json column.
 *
 * Note: This is separate from the legacy ProjectBible which has more detailed
 * nested structures. Both are supported for backward compatibility.
 */

/**
 * Complete canonical Bible overview for a project.
 * Contains the fundamental creative and production guidelines.
 */
@Serializable
data class BibleOverview(
    val coreConcepts: CoreConcepts,
    val storyStructure: StoryStructure,
    val worldLogic: WorldLogicCanonical,
    val productionGuidelines: ProductionGuidelinesCanonical,
    val seriesCharacterArcs: SeriesCharacterArcs? = null
)

/**
 * Core project concepts - high-level identity and purpose
 */
@Serializable
data class CoreConcepts(
    val title: String,
    val premise: String,
    val format: String,              // "Season 1", "Movie", etc.
    val duration: String,            // "12 episodes x 22 minutes"
    val genre: String,
    val tone: String,
    val targetAudience: String,
    val setting: String,
    val timeperiod: String
)

/**
 * Story structure and narrative framework
 */
@Serializable
data class StoryStructure(
    val mainConflict: String,
    val stakes: String,
    val keyCharacterRoles: List<String>,
    val majorBeats: List<String>,
    val themes: List<String>
)

/**
 * World logic and rules - how this universe operates
 * Note: Uses "Canonical" suffix to avoid conflicts with the detailed WorldLogic in ProjectBible
 */
@Serializable
data class WorldLogicCanonical(
    val fundamentalPrinciple: String,
    val worldRules: List<WorldRule> = emptyList()
)

/**
 * A single world rule describing how things work
 */
@Serializable
data class WorldRule(
    val ruleName: String,
    val description: String,
    val alwaysTrue: String,
    val neverTrue: String,
    val exceptions: List<String> = emptyList(),
    val howItWorks: String,
    val storyImplications: List<String> = emptyList()
)

/**
 * Production guidelines for visual and audio style
 * Note: Uses "Canonical" suffix to distinguish from detailed ProductionGuidelines in ProjectBible
 */
@Serializable
data class ProductionGuidelinesCanonical(
    val visualStyle: String,
    val audioStyle: String,
    val colorPalette: List<String> = emptyList(),
    val keyLocations: List<String> = emptyList(),
    val keyProps: List<String> = emptyList(),
    val productionNotes: String? = null
)

/**
 * Canonical Episode Blueprint (simplified from the detailed EpisodeBlueprint in ProjectBible)
 * This represents the planning/design phase before Story production.
 */
@Serializable
data class EpisodeBlueprintCanonical(
    val episodeNumber: Int,
    val title: String,
    val logline: String,
    val synopsis: String,
    val themes: List<String> = emptyList(),
    val keyMoments: List<String> = emptyList(),
    val worldBuilding: String? = null,
    val characterFocus: String? = null
)

/**
 * A milestone in a character's series arc
 */
@Serializable
data class SeriesArcMilestone(
    val episode: Int,
    val milestone: String,
    val emotionalState: String? = null
)

/**
 * Character arc across entire series (V3)
 *
 * Stored in bible.seriesCharacterArcs
 * Referenced by episode blueprints via characterBeats
 */
@Serializable
data class SeriesCharacterArc(
    val characterId: String,
    val arcName: String,
    val startingState: String,
    val endingState: String,
    val transformation: String,
    val milestones: List<SeriesArcMilestone> = emptyList(),
    val inferredStartingOCEAN: Map<String, Double>? = null,
    val inferredEndingOCEAN: Map<String, Double>? = null
)

/**
 * Collection of series character arcs
 */
@Serializable
data class SeriesCharacterArcs(
    val arcs: List<SeriesCharacterArc> = emptyList()
)

/**
 * Temporary conversion function from EpisodeBlueprintCanonical to EpisodeBlueprintV3
 * TODO: Replace with proper V3 blueprints from API
 */
fun EpisodeBlueprintCanonical.toV3(
    archetypeId: String = "unknown",
    archetype: String = "standard",
    structurePattern: String = "three_act",
    genres: List<String> = listOf("drama"),
    tones: List<String> = listOf("balanced"),
    duration: String = "1hour",
    pacingStyle: String = "normal",
    charactersPresent: List<String> = emptyList()
): EpisodeBlueprintV3 {
    return EpisodeBlueprintV3(
        episodeMetadata = EpisodeMetadataV3(
            episodeNumber = this.episodeNumber,
            title = this.title,
            logline = this.logline,
            synopsis = this.synopsis,
            themes = this.themes,
            narrativeFunction = null,
            worldBuilding = this.worldBuilding
        ),
        archetypeId = archetypeId,
        archetype = archetype,
        structurePattern = structurePattern,
        genres = genres,
        tones = tones,
        duration = duration,
        pacingStyle = pacingStyle,
        charactersPresent = charactersPresent,
        actStructure = listOf(
            ActStructureV3(
                actPosition = "act_1",
                actName = "Overview",
                description = "Episode content",
                keyMoments = this.keyMoments
            )
        ),
        essentialEvents = emptyList()
    )
}
