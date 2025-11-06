package com.beekeeper.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Plot Archetype Integration for Story Bible
 * Maps the 100 timeless plot archetypes to the existing PlotLogic structure
 */
@Serializable
data class PlotArchetype(
    val id: String,
    val name: String,
    val description: String,
    val category: PlotArchetypeCategory,

    // Maps directly to existing PlotLogic components
    val archetypePlotLogic: PlotLogic,

    // Archetype-specific narrative beats
    val archetypeBeats: List<ArchetypeBeat>,

    // How this archetype drives theme
    val thematicCore: ThematicCore,

    // Examples and variations
    val examples: List<String>,
    val modernAdaptations: List<String>,
    val subversions: List<String>
)

@Serializable
enum class PlotArchetypeCategory {
    JOURNEY,           // Hero's Journey, Quest, Voyage and Return
    TRANSFORMATION,    // Coming of Age, Redemption, Fall from Grace
    CONFLICT,          // Overcoming the Monster, Revenge, War
    RELATIONSHIP,      // Love Triangle, Forbidden Love, Unrequited Love
    MYSTERY,           // Mystery, Discovery, Hidden Truth
    TRAGEDY,           // Tragedy, Sacrifice, Downfall
    COMEDY,            // Fish Out of Water, Mistaken Identity
    SURVIVAL,          // Survival, Escape, Apocalypse
    POWER,             // Rags to Riches, Power and Corruption, Underdog
    SOCIETAL          // Rebellion, Dystopia, Society vs Individual
}

@Serializable
data class ArchetypeBeat(
    val beatName: String,
    val beatDescription: String,
    val typicalPlacement: Float,  // 0.0 to 1.0 position in story
    val narrativeFunction: NarrativeFunction,
    val isEssential: Boolean = true,
    val episodeMapping: String? = null  // Suggested episode for this beat
)

@Serializable
data class ThematicCore(
    val centralTheme: String,
    val moralQuestion: String,
    val emotionalJourney: String,
    val universalTruth: String?
)

/**
 * Enhanced ProjectBible with Archetype Selection
 */
@Serializable
data class ProjectBibleWithArchetype(
    val projectBible: ProjectBible,
    val primaryArchetype: PlotArchetype,
    val secondaryArchetypes: List<PlotArchetype> = emptyList(),
    val archetypeAdaptation: ArchetypeAdaptation
)

@Serializable
data class ArchetypeAdaptation(
    val archetypeId: String,
    val customizations: Map<String, String>,  // Beat name -> Custom implementation
    val episodicMapping: Map<Int, String>,    // Episode number -> Which archetype beat
    val modernizations: List<String>,         // Contemporary updates
    val genreBlending: List<String>          // How archetypes blend in this story
)