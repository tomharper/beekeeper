package com.beekeeper.app.domain.model

import com.beekeeper.app.domain.model.*
import kotlinx.serialization.Serializable

/**
 * Comprehensive Project Bible Structures
 *
 * These structures capture the INTERNAL LOGIC that drives the entire project.
 * This is the writer's complete notes about WHY things happen, not just WHAT happens.
 * Every character decision, plot twist, and world rule should be traceable back to these structures.
 * Each episode within the project is an individual story following this overarching bible.
 */

// ==================== ENHANCED PROJECT BIBLE ====================

@Serializable
data class ProjectBible(
    // Original fields using existing models
    val id: String = "",
    val version: String = "1.0",

    // Core existing model reference - now optional with defaults for API compatibility
    val characters: List<CharacterProfile> = emptyList(),
    val props: List<Prop> = emptyList(),

    // ===== NEW SYSTEMATIC STRUCTURES =====
    // These fields now have default empty/basic values to handle incomplete API data

    // The fundamental rules that govern this universe
    val worldLogic: WorldLogic? = null,

    // The cause-and-effect chains that drive the plot
    val plotLogic: PlotLogic? = null,

    // Systematic theme exploration
    val thematicStructure: ThematicStructure? = null,

    // Episode-by-episode narrative logic (both structural and audience-facing)
    val episodeBlueprints: List<EpisodeBlueprint> = emptyList(),

    // Simple production guidelines for visual and audio
    val productionGuidelines: ProductionGuidelines? = null,

    // ===== ORIGINAL FIELDS =====
    val platformSettings: Map<String, PlatformSettings> = emptyMap(),
    val supportedExportFormats: List<ExportFormat> = emptyList(),
    val contentStatus: ContentStatus = ContentStatus.DRAFT,
    val contentType: ContentType = ContentType.STORY,
    val metadata: Map<String, String> = emptyMap(),
    val tags: Map<String, String> = emptyMap(),
    val descriptors: List<String> = emptyList(),

    // Legacy fields from old API structure (ignore if present)
    val title: String? = null,
    val worldBuilding: String? = null,
    val themes: List<String>? = null,
    val rules: List<String>? = null
)

/**
 * The fundamental rules that govern how this universe works.
 * This explains WHY things are possible or impossible.
 */
@Serializable
data class WorldLogic(
    // The core operating principle of this world
    val fundamentalPrinciple: String,

    // What makes this world different from reality
    val divergenceFromReality: DivergencePoint,

    // The systematic rules that everything must follow
    val universalRules: List<UniversalRule>,

    // How the special elements of this world work
    val mechanicsSystem: MechanicsSystem,

    // What is possible and impossible
    val possibilitySpace: PossibilitySpace,

    // How cause and effect work in this world
    val causalityRules: List<CausalityRule>
)

@Serializable
data class DivergencePoint(
    val whatDiverges: String,  // "Alchemy is real"
    val whenItDiverges: String,           // "Always existed, hidden"
    val why: String,            // "Fundamental force of universe"
    val implications: List<String>, // All the things this changes
    val limits: List<String>    // What stays the same despite divergence
)

@Serializable
data class UniversalRule(
    val ruleName: String,
    val description: String,
    val alwaysTrue: String,     // What ALWAYS happens
    val neverTrue: String,      // What NEVER happens
    val exceptions: List<String>, // Any exceptions and why
    val storyImplications: List<String> // How this drives plot
)

@Serializable
data class MechanicsSystem(
    val systemName: String,     // "Alchemy System"
    val howItWorks: String,     // Core mechanic explanation
    val energySource: String,   // Where power comes from
    val limitations: List<String>, // What limits the system
    val costs: List<Cost>,      // What using it costs
    val requirements: List<String>, // What's needed to use it
    val amplifiers: List<String>,  // What makes it stronger
    val inhibitors: List<String>,  // What makes it weaker
    val scientificBasis: String?,  // Any scientific grounding
    val symbolicMeaning: String    // What it represents thematically
)

@Serializable
data class Cost(
    val type: String,           // Physical, mental, spiritual, social
    val description: String,
    val severity: String,       // Minor, moderate, severe, fatal
    val accumulation: String,   // How costs add up over time
    val mitigation: String?     // How to reduce the cost
)

@Serializable
data class PossibilitySpace(
    val possible: List<String>,     // What CAN happen
    val impossible: List<String>,   // What CANNOT happen
    val difficult: List<String>,    // What's hard but possible
    val consequences: Map<String, String> // Action -> Consequence mapping
)

@Serializable
data class CausalityRule(
    val cause: String,
    val effect: String,
    val reliability: String,    // Always, usually, sometimes, rarely
    val delay: String?,         // Immediate, delayed, variable
    val visibility: String      // Obvious, subtle, hidden
)

// ==================== CHARACTER BIBLE ====================

/**
 * The complete internal logic of a character.
 * This explains WHY they make every decision.
 */
@Serializable
data class FormativeExperience(
    val age: String,
    val event: String,
    val impact: String,         // How it shaped them
    val lesson: String,         // What they learned
    val falseLesson: String?,   // What they learned wrong
    val trauma: String?,        // Any lasting damage
    val strength: String?       // Any strength gained
)


@Serializable
data class DecisionLogic(
    val priorities: List<String>,  // Ordered list of what matters
    val dealBreakers: List<String>, // What they'd never do
    val weaknesses: List<String>,   // What makes them compromise
    val decisionProcess: String,    // How they make choices
    val underPressure: String,      // How they act when stressed
    val rationalizations: Map<String, String> // Bad decision -> Justification
)


@Serializable
data class CharacterEvolutionPath(
    val startingPoint: String,
    val catalyst: String,          // What starts their change
    val resistance: List<String>,  // Why they resist changing
    val breakthroughs: List<EvolutionBreakthrough>,
    val regressions: List<String>, // When they backslide
    val finalState: String,
    val growthMechanism: String    // HOW they change
)

@Serializable
data class EvolutionBreakthrough(
    val trigger: String,
    val realization: String,
    val newBehavior: String,
    val episodeNumber: Int
)

@Serializable
data class InternalConflict(
    val desire1: String,
    val desire2: String,
    val whyIncompatible: String,
    val manifestation: String,     // How it shows up
    val resolution: String?        // How it's resolved (if ever)
)

@Serializable
data class CharacterSecret(
    val secret: String,
    val whyHidden: String,
    val whoKnows: List<String>,
    val revealTrigger: String?,    // What would expose it
    val revealEpisode: Int?,       // When it's revealed
    val consequences: String       // What happens if exposed
)

@Serializable
data class CharacterKnowledge(
    val expertise: List<String>,   // What they're expert in
    val ignorance: List<String>,   // What they don't know
    val misconceptions: List<String>, // What they're wrong about
    val hiddenKnowledge: List<String>, // What they know but don't share
    val learningCurve: Map<String, Int> // Skill -> Episode learned
)

@Serializable
data class BehaviorPattern(
    val trigger: String,           // What sets it off
    val response: String,          // What they do
    val frequency: String,         // How often
    val awareness: String,         // Do they know they do it?
    val origin: String            // Where pattern comes from
)

// ==================== PLOT LOGIC ====================

/**
 * The cause-and-effect chains that drive the plot.
 * This explains WHY each event leads to the next.
 */
@Serializable
data class PlotLogic(
    // The engine that drives everything
    val centralEngine: CentralEngine,

    // Chains of cause and effect
    val causalChains: List<CausalChain>,

    // The ticking clocks that create urgency
    val tickingClocks: List<TickingClock>,

    // Points where everything could change
    val pivotPoints: List<PivotPoint>,

    // The dominoes that must fall in order
    val dominoChains: List<DominoChain>,

    // Why certain things MUST happen
    val inevitabilities: List<Inevitability>
)

@Serializable
data class CentralEngine(
    val mainConflict: String,
    val whyUnresolvable: String,   // Why it can't be easily fixed
    val stakesEscalation: List<String>, // How stakes increase
    val pointOfNoReturn: String,   // When there's no going back
    val onlyPossibleResolution: String // The only way it can end
)

@Serializable
data class CausalChain(
    val chainName: String,
    val initialCause: String,
    val links: List<CausalLink>,
    val finalEffect: String,
    val episodeStart: Int,
    val episodeEnd: Int,
)

@Serializable
data class TickingClock(
    val clockName: String,
    val deadline: String,
    val consequences: String,       // What happens if time runs out
    val accelerators: List<String>, // What makes it go faster
    val decelerators: List<String>, // What slows it down
    val visibility: String,         // Who knows about it
    val episodeIntroduced: Int,
    val episodeResolved: Int?
)

@Serializable
data class PivotPoint(
    val episodeNumber: Int,
    val decision: String,
    val decisionMaker: String,
    val options: List<String>,
    val chosenPath: String,
    val whyChosen: String,         // Internal logic for choice
    val consequences: String,
    val alternateTimelines: List<String> // What if they chose differently
)

@Serializable
data class DominoChain(
    val firstDomino: String,
    val dominoes: List<String>,    // Events that must happen in order
    val finalDomino: String,
    val interruptions: List<String>, // What could stop the chain
    val episodeStart: Int,
    val episodeEnd: Int
)

@Serializable
data class Inevitability(
    val event: String,
    val whyInevitable: String,     // Why it MUST happen
    val setupRequired: List<String>, // What must be in place
    val episode: Int,
    val foreshadowing: List<String> // How it's hinted at
)

// ==================== THEMATIC STRUCTURE ====================

/**
 * How themes are systematically explored throughout the story.
 */
@Serializable
data class ThematicStructure(
    val centralThesis: String,     // The main argument/question
    val antithesis: String,        // The counter-argument
    val synthesis: String?,        // The resolution (if any)

    val thematicThreads: List<ThematicThread>,
    val thematicConflicts: List<ThematicConflict>,
    val philosophicalQuestions: List<PhilosophicalQuestion>
)

@Serializable
data class ThematicThread(
    val theme: String,
    val introduction: String,       // How it's introduced
    val development: List<String>,  // How it's explored
    val climax: String,            // Peak thematic moment
    val resolution: String?,        // How it's resolved
    val carriers: List<String>      // Characters/elements that embody it
)

@Serializable
data class ThematicConflict(
    val value1: String,
    val value2: String,
    val whyInConflict: String,
    val battlefield: String,        // Where this conflict plays out
    val champions: Map<String, String>, // Character -> Which value they represent
    val resolution: String?         // Which wins or how they reconcile
)

@Serializable
data class PhilosophicalQuestion(
    val question: String,
    val exploredThrough: List<String>, // Plot points that explore it
    val perspectives: Map<String, String>, // Character -> Their answer
    val seriesAnswer: String?      // The story's answer (if any)
)


/**
 * The internal logic for a specific episode.
 */
@Serializable
data class EpisodeBlueprint(
    val episodeNumber: Int,
    val title: String,

    // ===== AUDIENCE-FACING SUMMARY FIELDS (from EpisodeOutline) =====
    val logline: String,                    // One-sentence episode hook
    val synopsis: String,                   // Paragraph summary
    val themes: List<String>,               // Episode-specific themes
    val keyMoments: List<String>,           // Simplified list of key beats
    val worldBuilding: String,              // World development for this episode
    val characterFocus: String,             // Which characters are featured

    // ===== INTERNAL STRUCTURAL LOGIC FIELDS (original EpisodeBlueprint) =====
    // What MUST happen and why
    val essentialEvents: List<EssentialEvent>,

    // Character logic for this episode
    val characterObjectives: List<CharacterObjective>,

    // How information is revealed
    val revelations: List<Revelation>,

    // The cause-effect engine for this episode
    val episodeCausality: List<CausalLink>,

    // Setup for future episodes
    val plantedSeeds: List<PlantedSeed>,

    // Payoffs from previous episodes
    val payoffs: List<Payoff>,

    // Why this episode exists
    val narrativeFunction: String,

    // Internal logic checks - optional
    val logicValidation: LogicValidation? = null
)

@Serializable
data class EssentialEvent(
    val event: String,
    val whyEssential: String,      // Why story breaks without it
    val prerequisites: List<String>, // What must happen first
    val consequences: List<String>  // What it causes
)

@Serializable
data class CharacterObjective(
    val characterId: String,
    val want: String,              // What they want this episode
    val need: String,              // What they actually need
    val plan: String,              // How they try to get it
    val obstacles: List<String>,
    val outcome: String,           // Success/failure/unexpected
    val learning: String?          // What they learn
)

@Serializable
data class Revelation(
    val information: String,
    val revealer: String,          // Who/what reveals it
    val audience: List<String>,    // Who learns it (characters)
    val trigger: String,           // What causes revelation
    val impact: String,            // How it changes things
    val misdirection: String?      // Any false interpretation
)

@Serializable
data class PlantedSeed(
    val seed: String,
    val type: String,              // Foreshadowing, setup, etc.
    val payoffEpisode: Int,
    val subtlety: String           // Obvious, subtle, hidden
)

@Serializable
data class Payoff(
    val setup: String,
    val fromEpisode: Int,
    val satisfaction: String,      // How satisfying the payoff is
    val surprise: String           // Expected or unexpected
)

@Serializable
data class LogicValidation(
    val characterConsistency: Map<String, Boolean>, // Character -> Consistent?
    val worldRuleCompliance: List<String>, // Rules followed
    val causalityIntact: Boolean,
    val themeAlignment: Boolean,
    val issues: List<String>       // Any logic problems
)

// ==================== PRODUCTION GUIDELINES ====================

/**
 * Simple, flat production guidelines for visual and audio style.
 * Designed for AI generation section by section - no complex nesting.
 */
@Serializable
data class ProductionGuidelines(
    // Visual style guidelines
    val visualStyle: String,               // Overall visual approach and aesthetic
    val colorPalette: List<String>,        // Key colors used in the production
    val keyVisualMotifs: List<String>,     // Important recurring visual elements

    // Audio style guidelines
    val audioStyle: String,                // Overall audio/music approach
    val keyAudioMotifs: List<String>,      // Important recurring audio elements

    // Optional detailed notes
    val productionNotes: String? = null    // Additional production guidance
)


// ==================== VALIDATION FUNCTIONS ====================

/**
 * Check if a character's action is consistent with their bible
 */
fun CharacterProfile.validateAction(action: String, context: String): Boolean {
    // Check against decision logic
    decisionLogic?.dealBreakers?.forEach { dealBreaker ->
        if (action.contains(dealBreaker, ignoreCase = true)) {
            return false // Character wouldn't do this
        }
    }

    // Check if action aligns with priorities
    // More complex validation logic would go here

    return true
}

/**
 * Validate that the Project Bible is internally consistent
 */
fun ProjectBible.validate(): List<String> {
    val errors = mutableListOf<String>()

    // Check character consistency
    characters.forEach { bible ->
        val character = characters.find { it.id == bible.id }
        if (character == null) {
            errors.add("Character bible for $bible.id has no matching CharacterProfile")
        }

        // Validate decision logic aligns with psychological profile
        if (bible.decisionLogic?.priorities?.contains(bible.personality.coreFear) == true ) {
            errors.add("${bible.name}'s core fear not reflected in priorities")
        }
    }

    // Check world logic consistency
    worldLogic?.universalRules?.forEach { rule ->
        // Check that rules don't contradict each other
        worldLogic.universalRules.filter { it != rule }.forEach { otherRule ->
            if (rule.alwaysTrue == otherRule.neverTrue) {
                errors.add("Rule conflict: ${rule.ruleName} contradicts ${otherRule.ruleName}")
            }
        }
    }

    // Check plot causality
    plotLogic?.causalChains?.forEach { chain ->
        if (chain.links.isEmpty()) {
            errors.add("Causal chain ${chain.chainName} has no links")
        }
    }

    // Check episode blueprints
    episodeBlueprints.forEach { blueprint ->
        // Verify payoffs have matching setups
        blueprint.payoffs.forEach { payoff ->
            val setupExists = episodeBlueprints
                .filter { it.episodeNumber < blueprint.episodeNumber }
                .any { it.plantedSeeds.any { seed -> seed.seed == payoff.setup } }

            if (!setupExists) {
                errors.add("Episode ${blueprint.episodeNumber} payoff '${payoff.setup}' has no setup")
            }
        }
    }

    return errors
}


/**
 * Verify that an event follows world logic
 */
fun WorldLogic.validateEvent(event: String): Boolean {
    // Check against universal rules
    universalRules.forEach { rule ->
        if (event.contains(rule.neverTrue, ignoreCase = true)) {
            return false // This can't happen in this world
        }
    }

    // Check possibility space
    if (event in possibilitySpace.impossible) {
        return false
    }

    return true
}


@Serializable
data class CausalLink(
    val cause: String,
    val effect: String,
    val inevitability: String,     // How inevitable this link is
    val alternatives: List<String> // Other possible outcomes
)

/**
 * Supporting data classes for Episode and Character structures
 */
// EpisodeOutline has been consolidated into EpisodeBlueprint
// All episode data is now handled by the enhanced EpisodeBlueprint structure

/**
 * Temporary conversion function from EpisodeBlueprint (V2) to EpisodeBlueprintV3
 * TODO: Replace with proper V3 blueprints from API
 */
fun EpisodeBlueprint.toV3(
    archetypeId: String = "unknown",
    archetype: String = "standard",
    structurePattern: String = "three_act",
    genres: List<String> = listOf("drama"),
    tones: List<String> = listOf("balanced"),
    duration: String = "1hour",
    pacingStyle: String = "normal"
): EpisodeBlueprintV3 {
    return EpisodeBlueprintV3(
        episodeMetadata = EpisodeMetadataV3(
            episodeNumber = this.episodeNumber,
            title = this.title,
            logline = this.logline,
            synopsis = this.synopsis,
            themes = this.themes,
            narrativeFunction = this.narrativeFunction,
            worldBuilding = this.worldBuilding
        ),
        archetypeId = archetypeId,
        archetype = archetype,
        structurePattern = structurePattern,
        genres = genres,
        tones = tones,
        duration = duration,
        pacingStyle = pacingStyle,
        charactersPresent = this.characterObjectives.map { it.characterId },
        actStructure = listOf(
            ActStructureV3(
                actPosition = "act_1",
                actName = "Episode Overview",
                description = this.narrativeFunction,
                keyMoments = this.keyMoments,
                revelations = this.revelations,
                plantedSeeds = this.plantedSeeds
            )
        ),
        essentialEvents = this.essentialEvents
    )
}
