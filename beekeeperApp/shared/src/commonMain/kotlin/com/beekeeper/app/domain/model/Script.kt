// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/Script.kt
package com.beekeeper.app.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Complete Enhanced Script Domain Models with Superset Approach
 * Adds functionality without removing any existing fields
 */

/**
 * Unified domain entity representing a Script
 * Superset of all properties from various sources
 */
@Serializable
data class Script(
    val id: String,
    val projectId: String,
    val storyId: String, // every script has a story
    val title: String,
    val version: String = "1.0",
    val content: String, // The actual script text
    val format: ScriptFormat,
    val pages: Int = 0,
    val wordCount: Int = 0,
    val duration: Int = 0, // in minutes (some use this name)
    val estimatedDuration: Int = duration, // in minutes (alias for compatibility)
    val sceneScripts: List<SceneScript>, // REQUIRED
    val acts: List<Act> = emptyList(), // NOT REQUIRED
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastEditedBy: String,
    val collaborators: List<String> = emptyList(),
    val isLocked: Boolean = false,
    val status: ContentStatus = ContentStatus.DRAFT,
    val structure: String = ScriptStructureType.THREE_ACT,
    val draftNumber: Int = 1,
    val writtenBy: String = lastEditedBy, // Use existing field
    val author: String = "cinefiller",
    val characters: List<String> = emptyList(),

    // Enhanced fields (superset additions)
    val language: String = "en",                           // ADD: Script language
    val genre: List<String> = emptyList(),                // ADD: Genre tags
    val targetAudience: String? = null,                   // ADD: Target demographic
    val rating: String? = null,                           // ADD: Content rating
    val logline: String? = null,                          // ADD: One-line summary
    val synopsis: String? = null,                         // ADD: Brief synopsis
    val treatment: String? = null,                        // ADD: Extended treatment
    val characterCount: Int = 0,                          // ADD: Number of characters
    val locationCount: Int = 0,                           // ADD: Number of locations
    val dayScenes: Int = 0,                              // ADD: Day scene count
    val nightScenes: Int = 0,                            // ADD: Night scene count
    val interiorScenes: Int = 0,                         // ADD: Interior scene count
    val exteriorScenes: Int = 0,                         // ADD: Exterior scene count
    val revisionHistory: List<ScriptRevision> = emptyList(), // ADD: Revision tracking
    val colorRevision: String? = null,                    // ADD: Color-coded revision
    val shootingScriptNumber: String? = null,             // ADD: Production script number
    val registrationNumber: String? = null,               // ADD: WGA/Copyright registration
    val copyrightInfo: String? = null,                    // ADD: Copyright details
    val contactInfo: String? = null,                      // ADD: Contact information
    val agent: String? = null,                            // ADD: Agent/representation
    val notes: List<String> = emptyList(),           // ADD: Production notes
    val breakdowns: ScriptBreakdowns? = null,            // ADD: Various breakdowns
    val budget: BudgetInfo? = null,                      // ADD: Budget information
    val schedule: ProductionSchedule? = null,            // ADD: Shooting schedule
    val metadata: Map<String, String> = emptyMap(),       // ADD: Flexible metadata
    val revisionNotes: String = "",
    val themes: List<String> = emptyList<String>(),
    val props: List<Prop> = emptyList()
) {
    // Computed properties for Long timestamps
    val createdAtMillis: Long get() = createdAt.toEpochMilliseconds()
    val updatedAtMillis: Long get() = updatedAt.toEpochMilliseconds()

    // Helper functions
    fun getTotalScenes(): Int = acts.sumOf { it.sceneScripts.size }
    fun getAverageSceneDuration(): Float = if (getTotalScenes() > 0) duration.toFloat() / getTotalScenes() else 0f
    fun hasRevisions(): Boolean = draftNumber > 1 || revisionHistory.isNotEmpty()
    fun isProduction(): Boolean = shootingScriptNumber != null
    fun getComplexityScore(): Int {
        var score = 0
        score += (characterCount / 5).coerceAtMost(3)
        score += (locationCount / 10).coerceAtMost(3)
        score += if (exteriorScenes > interiorScenes) 2 else 1
        score += if (nightScenes > dayScenes) 2 else 1
        return score.coerceIn(1, 10)
    }
}

@Serializable
object ScriptStructureType {
    const val THREE_ACT = "THREE_ACT"
    const val FIVE_ACT = "FIVE_ACT"
    const val HEROES_JOURNEY = "HEROES_JOURNEY"
    const val SAVE_THE_CAT = "SAVE_THE_CAT"
    const val SEVEN_POINT = "SEVEN_POINT"
    const val FREYTAG_PYRAMID = "FREYTAG_PYRAMID"
    const val EPISODIC = "EPISODIC"
    const val NONLINEAR = "NONLINEAR"
    const val CUSTOM = "CUSTOM"
}

@Serializable
enum class ScriptFormat {
    SCREENPLAY,
    TELEPLAY,
    STAGE_PLAY,
    RADIO_PLAY,
    COMIC_SCRIPT,
    NOVEL,
    SHORT_STORY,
    TREATMENT,
    OUTLINE,
    // Enhanced additions
    PODCAST_SCRIPT,         // ADD
    VIDEO_GAME_SCRIPT,     // ADD
    VR_SCRIPT,             // ADD
    INTERACTIVE_SCRIPT,    // ADD
    DOCUMENTARY_SCRIPT,    // ADD
    COMMERCIAL_SCRIPT,     // ADD
    MUSIC_VIDEO_SCRIPT,
    AUDIO_DRAMA,
    DOCUMENTARY,
    COMMERCIAL,
    MUSIC_VIDEO,
    ANIMATION,
    WEBISODE,
    SHORT_FILM,
    FEATURE_FILM,
    PILOT,
    SPEC_SCRIPT,
    SHOOTING_SCRIPT,
    BEAT_SHEET,
    FOUNTAIN,
    FINAL_DRAFT,
    CELTX,
    PLAIN_TEXT
}


// DEPRECATED CONCEPT
@Serializable
data class Act(
    val id: String,
    val scriptId: String,
    val actNumber: Int,
    val number: Int = actNumber, // Alias for compatibility
    val title: String? = null,
    val sceneScripts: List<SceneScript> = emptyList(), // its here but ignore this
    val description: String? = null,
    val pageStart: Int = 0,
    val pageEnd: Int = 0,

    // Enhanced fields (superset additions)
    val duration: Int = 0,                              // ADD: Act duration in minutes
    val purpose: String? = null,                        // ADD: Narrative purpose
    val turningPoint: String? = null,                   // ADD: Major plot point
    val emotionalArc: String? = null,                   // ADD: Emotional journey
    val themes: List<String> = emptyList(),            // ADD: Thematic elements
    val notes: String? = null,                         // ADD: Director/writer notes
    val colorCode: String? = null,                     // ADD: Visual color coding
    val isFlashback: Boolean = false,                  // ADD: Temporal marker
    val isFlashforward: Boolean = false,               // ADD: Temporal marker
    val parallelAction: List<String> = emptyList(),    // ADD: Simultaneous scenes
    val metadata: Map<String, String> = emptyMap()     // ADD: Flexible metadata
) {
    // For compatibility with models that expect List<String> for scenes
    val sceneIds: List<String> get() = sceneScripts.map { it.id }

    // Helper functions
    fun getTotalScenes(): Int = sceneScripts.size
    fun getTotalDuration(): Int = sceneScripts.sumOf { it.duration }
    fun getLocations(): Set<String> = sceneScripts.mapNotNull { it.location }.toSet()
}


@Serializable
data class SceneScript(
    val id: String,
    val scriptId: String,
    val actId: String? = null,
    val number: Int = 0,
    val emotionalTone: EmotionalTone,
    val narrativeFunction: NarrativeFunction,
    val dialogue: List<DialogueLine>, // this should probably be dialogueLines
    val sceneNumber: String = "", // e.g., "1A", "2", "3B" - not important - use Number for ordering
    val title: String = "",
    val description: String = "",
    val action: String? = null,
    val heading: String? = null, // e.g., "INT. COFFEE SHOP - DAY"
    val duration: Int = 0, // in seconds
    val characterIds: List<String>,
    val propIds: List<String> = emptyList(), //make optional for now
    val isCompleted: Boolean = false,
    val location: String? = null,
    val timeOfDay: String? = null,
    val cameraDirections: List<String>? = null,
    val notes: String? = null,

    // Enhanced fields (superset additions)
    val subheading: String? = null,                        // ADD: Secondary heading
    val pageStart: Float? = null,                          // ADD: Decimal page number
    val pageEnd: Float? = null,                            // ADD: Decimal page number
    val beatSheet: List<StoryBeat> = emptyList(),         // ADD: Story beats
    val transitions: TransitionInfo? = null,               // ADD: Scene transitions
    val blocking: List<BlockingNote> = emptyList(),       // ADD: Actor blocking
    val shotList: List<ShotInfo> = emptyList(),          // ADD: Shot planning
    val colorPalette: List<String> = emptyList(),        // ADD: Color scheme
    val musicCues: List<MusicCue> = emptyList(),         // ADD: Music timing
    val soundDesign: SoundDesign? = null,                 // ADD: Sound details
    val vfxRequirements: List<String> = emptyList(), // ADD: VFX needs
    val wardrobe: Map<String, WardrobeInfo> = emptyMap(), // ADD: Costume details
    val makeup: Map<String, MakeupInfo> = emptyMap(),     // ADD: Makeup details
    val rehearsalNotes: String? = null,                   // ADD: Rehearsal plans
    val safetyNotes: String? = null,                      // ADD: Safety requirements
    val intimacyNotes: String? = null,                    // ADD: Intimacy coordination
    val goldenHour: Boolean = false,                      // ADD: Lighting requirement
    val nightShoot: Boolean = false,                      // ADD: Night filming
    val underwaterShoot: Boolean = false,                 // ADD: Special conditions
    val aerialShoot: Boolean = false,                     // ADD: Drone/helicopter
    val stunts: List<StuntInfo> = emptyList(),           // ADD: Stunt requirements
    val extras: ExtrasInfo? = null,                       // ADD: Background actors
    val productPlacement: List<Prop> = emptyList(),     // ADD: Brand placement
    val weather: String? = null,
    val metadata: Map<String, String> = emptyMap(),        // ADD: Flexible metadata
    val pageCount: Float = 0f
)

@Serializable
data class DialogueLine(
    val characterName: String,
    val dialogue: String,
    val id: String,                    // ADD: Unique identifier
    val characterId: String,
    val parenthetical: String? = null,
    val isDualDialogue: Boolean = false,
    val emotion: EmotionalTone = EmotionalTone.NEUTRAL,
    val timestamp: Float = 0f,

    // Enhanced fields (superset additions)
    val lineNumber: Int? = null,                          // ADD: Script line number
    val revisionColor: String? = null,                    // ADD: Revision marking
    val isVoiceOver: Boolean = false,                     // ADD: V.O. flag
    val isOffScreen: Boolean = false,                     // ADD: O.S. flag
    val isPreLap: Boolean = false,                        // ADD: Audio bridge
    val isContinued: Boolean = false,                     // ADD: CONT'D marker
    val isAdLib: Boolean = false,                         // ADD: Improvised line
    val deliveryNote: String? = null,                     // ADD: Performance note
    val dialect: String? = null,                          // ADD: Accent/dialect
    val language: String? = null,                         // ADD: If not default
    val subtitleRequired: Boolean = false,                // ADD: Subtitle flag
    val alternateLines: List<String> = emptyList(),      // ADD: Alt options
    val audioFileId: String? = null,                      // ADD: Recorded audio
    val duration: Float? = null,                          // ADD: Line duration
    val overlapping: Boolean = false,                     // ADD: Simultaneous dialogue
    val emphasis: List<EmphasisInfo> = emptyList(),      // ADD: Word emphasis
    val pronunciation: Map<String, String> = emptyMap(),  // ADD: Pronunciation guide
    val culturalNote: String? = null,                     // ADD: Cultural context
    val censorshipNote: String? = null,                   // ADD: Content warning
    val metadata: Map<String, String> = emptyMap() ,       // ADD: Flexible metadata
    val propIds: List<String> = emptyList()
)

@Serializable
enum class EmotionalTone {
    ACADEMIC,
    ACCEPTANCE,
    ACCEPTING,
    ACCUSATORY,
    ACTION,
    ADMIRING,
    ADVENTUROUS,
    AFFECTIONATE,
    AGONIZED,
    ALARMED,
    ALERT,
    AMAZED,
    AMAZEMENT,
    AMUSED,
    AMUSEMENT,
    ANCIENT,
    ANGRY,
    ANGUISHED,
    ANNOYED,
    ANTICIPATION,
    ANXIOUS,
    APOCALYPTIC,
    APOLOGETIC,
    APPEALING,
    APPRECIATIVE,
    APPREHENSIVE,
    APPROVING,
    ARROGANT,
    ASHAMED,
    ASTONISHED,
    AUTHORITATIVE,
    AWESTRUCK,
    AWE_INSPIRING,
    ANALYTICAL,
    BEGGING,
    BETRAYAL,
    BEWILDERED,
    BITTER,
    BITTERSWEET,
    BLESSED,
    BLESSING,
    BOASTFUL,
    BOLD,
    BRAVE,
    BROODING,
    BROKEN,
    BRUTAL,
    CALCULATING,
    CALM,
    CALMNESS,
    CALLOUS,
    CAREFUL,
    CARING,
    CATHARTIC,
    CAUTIOUS,
    CELEBRATORY,
    CEREMONIAL,
    CERTAIN,
    CHALLENGING,
    CHAOTIC,
    CHARMING,
    CHEERFUL,
    CHILLING,
    CLIMACTIC,
    CLINICAL,
    COLD,
    COMEDIC,
    COMMANDING,
    COMMUNITY_SPIRIT,
    COMPASSION,
    COMPASSIONATE,
    COMPETITIVE,
    COMFORTING,
    CONCILIATORY,
    CONDESCENDING,
    CONFIDENT,
    CONFLICTED,
    CONFRONTATIONAL,
    CONFUSED,
    CONFUSION,
    CONSOLING,
    CONTEMPLATIVE,
    CONTENT,
    CONTEMPTUOUS,
    COSMIC,
    COURAGEOUS,
    COWARDLY,
    CRUEL,
    CRUMBLING,
    CUNNING,
    CURIOUS,
    CURIOSITY,
    CURSING,
    DARK,
    DARK_TRIUMPH,
    DEAD_INSIDE,
    DECLARING,
    DECEPTIVE,
    DEFEATED,
    DEFENSIVE,
    DEFIANT,
    DELIGHTED,
    DELIGHT,
    DEMANDING,
    DEMONSTRATIVE,
    DENIAL,
    DESPAIRING,
    DESPERATE,
    DETERMINED,
    DEVASTATED,
    DEVOTION,
    DIGNIFIED,
    DIPLOMATIC,
    DIRE,
    DISAPPROVING,
    DISAPPOINTED,
    DISBELIEF,
    DISBELIEVING,
    DISCOVERING,
    DISGUSTED,
    DISMISSIVE,
    DISMAYED,
    DISORIENTING,
    DISSAPOINTMENT,
    DISTANT,
    DISTRAUGHT,
    DISTURBED,
    DIVINE,
    DOUBTFUL,
    DRAMATIC,
    DREAD,
    DREAMY,
    DRY,
    DUTIFUL,
    DYING,
    EAGER,
    EARNEST,
    ECSTATIC,
    EERIE,
    EMBARRASSED,
    EMERGENCY,
    EMOTIONAL,
    ENCOURAGING,
    ENERGETIC,
    ENIGMATIC,
    ENLIGHTENED,
    ENRAGED,
    ENTHUSIASTIC,
    ENTRANCED,
    ENVIOUS,
    EPIC,
    ETHEREAL,
    EUPHORIC,
    EVIL,
    EXASPERATED,
    EXCITED,
    EXCITING,
    EXHILARATED,
    EXUBERANT,
    EXPECTANT,
    EXPLAINING,
    FANATIC,
    FASCINATION,
    FATHERLY,
    FATALISTIC,
    FEARFUL,
    FIERCE,
    FINAL,
    FOCUSED,
    FOREBODING,
    FORGIVING,
    FORMAL,
    FRANTIC,
    FRIENDLY,
    FRIGHTENED,
    FRUSTRATED,
    FURIOUS,
    GENEROUS,
    GENTLE,
    GENUINE,
    GLEEFUL,
    GLOOMY,
    GRACEFUL,
    GRACIOUS,
    GRANDIOSE,
    GRATEFUL,
    GRATEFUL_BUT_SAD,
    GRAVE,
    GREEDY,
    GRIEF,
    GRIM,
    GRUDGING,
    GUARDED,
    GUILT,
    GUILTY,
    HAPPY,
    HARMONIOUS,
    HAUNTED,
    HAUNTING,
    HEARTBREAKING,
    HEARTBROKEN,
    HEARTWARMING,
    HELPFUL,
    HEROIC,
    HESITANT,
    HONEST,
    HOPEFUL,
    HOPELESS,
    HORROR,
    HORRIFIED,
    HOSPITABLE,
    HOSTILE,
    HUMBLE,
    HUMILIATED,
    HUMILITY,
    HUMOROUS,
    HURRIED,
    HYSTERIA,
    IMPATIENT,
    IMPRESSED,
    INCREDULOUS,
    INDIGNANT,
    INEVITABLE,
    INFORMATIVE,
    INNOCENT,
    INSPIRING,
    INSPIRATIONAL,
    INSISTENT,
    INSTRUCTIVE,
    INTELLECTUAL,
    INTENSE,
    INTIMATE,
    INTRIGUED,
    INTRIGUING,
    INVITING,
    IRONIC,
    INVESTIGATIVE,
    JEALOUS,
    JOYFUL,
    JUDGEMENT,
    JUST,
    KNOWING,
    LECHEROUS,
    LEWD,
    LOGICAL,
    LONGING,
    LOSS,
    LOVING,
    LUSTFUL,
    LYING,
    MAGICAL,
    MAJESTIC,
    MANIC,
    MANIACAL,
    MANIPULATIVE,
    MATERNAL,
    MATTEROFFACT,
    MELANCHOLIC,
    MELANCHOLY,
    MENACING,
    MERCIFUL,
    MERCILESS,
    MIRACULOUS,
    MOCKING,
    MOROSE,
    MOTHERLY,
    MYSTERIOUS,
    MYSTICAL,
    MYTHIC,
    NAIVE,
    NERVOUS,
    NEUTRAL,
    NOBLE,
    NOSTALGIC,
    OATH_TAKING,
    OBSERVANT,
    OBSESSED,
    OBSESSIVE,
    OFFICIAL,
    OMINOUS,
    OMNISCIENT,
    OPTIMISTIC,
    OPPRESSIVE,
    OUTRAGED,
    OTHERWORLDLY,
    OVERCONFIDENT,
    OVERWHELMED,
    PANICKED,
    PASSIONATE,
    PATIENT,
    PEACEFUL,
    PERSUASIVE,
    PETULANT,
    PHILOSOPHICAL,
    PITY,
    PLAYFUL,
    PLEASED,
    PLEADING,
    POLITE,
    POMPOUS,
    POWERFUL,
    PRAGMATIC,
    PRAYING,
    PREPARATORY,
    PRESCIENT,
    PRESSING,
    PROBING,
    PROFESSIONAL,
    PROFOUND,
    PROPHETIC,
    PROTECTIVE,
    PROUD,
    PURE,
    PURPOSEFUL,
    PUZZLED,
    QUESTIONING,
    RALLYING,
    RATIONAL,
    REALISTIC,
    REALIZATION,
    REASONABLE,
    REASSURING,
    REDEMPTIVE,
    REFLECTIVE,
    REGRETFUL,
    RELUCTANT,
    RELIEF,
    RELIEVED,
    REMORSEFUL,
    RESENTFUL,
    RESIGNED,
    RESISTANT,
    RESOLVED,
    RESOLUTE,
    RESOUNDING,
    RESPECTFUL,
    REVELATION,
    REVELATORY,
    REVERENT,
    REVENGE,
    RIGHTEOUS,
    RIGHTEOUS_ANGER,
    ROMANTIC,
    SACRED,
    SACRIFICE,
    SACRIFICIAL,
    SAD,
    SADISTIC,
    SARCASTIC,
    SARDONIC,
    SATISFIED,
    SCARED,
    SCHEMING,
    SCHOLARLY,
    SERIOUS,
    SHARP,
    SHOCKED,
    SHOCKING,
    SIMPLE,
    SINCERE,
    SINISTER,
    SKEPTICAL,
    SOLEMN,
    SOMBER,
    SORROWFUL,
    STRAINED,
    STERN,
    STRATEGIC,
    SUBSERVIENT,
    SUPPLICATION,
    SUPPORTIVE,
    SURPRISE,
    SURPRISED,
    SURRENDERING,
    SURREAL,
    SUSPENSEFUL,
    SUSPICIOUS,
    SYCOPHANTIC,
    SYMPATHETIC,
    TACTICAL,
    TAUNTING,
    TEARFUL,
    TEMPTING,
    TENDER,
    TENSE,
    TERRIFIED,
    TERRIFYING,
    THOUGHTFUL,
    THREATENING,
    THRILLING,
    TOUCHED,
    TRAGIC,
    TRAITOROUS,
    TRANSCENDENT,
    TRANSFORMATIVE,
    TRAUMATIZED,
    TRIUMPHANT,
    TRUSTING,
    UNCERTAIN,
    UNCONVINCED,
    UNDERSTANDING,
    UNEASY,
    UNHAPPY,
    UNITED,
    UPSET,
    URGENT,
    URGENCY,
    VALOROUS,
    VENGEFUL,
    VICIOUS,
    VICTORIOUS,
    VILLAINOUS,
    VIOLENT,
    VISIONARY,
    VULNERABLE,
    WARNING,
    WARM,
    WARY,
    WEARY,
    WELCOMING,
    WHIMSICAL,
    WISE,
    WISTFUL,
    WONDER,
    WONDERING,
    WORRIED,
    WRATHFUL,
    WRY,
    YEARNING,
    EXHAUSTED,
    MECHANICAL,
    DESTROYED,
    TORMENTED,
    SELF_LOATHING,
    CONCERNED,
    DISTRESSED,
    REVEALING,
    PRACTICAL,
    REALIZED,
    QUIET,
    PRACTICED,
    ROBOTIC,
    PATERNAL,
    SCIENTIFIC,
    TROUBLED,
    DECISIVE,
    TIRED,
    FIRM,
    PREPARED,
    SUPERNATURAL,
    LIBERATION,
    SETUP,
    DISORIENTED,
    REGAL,
    SERENE
}
fun getEmotionalToneColor(tone: EmotionalTone): Color {
    return when (tone) {
        // Existing tones
        EmotionalTone.TENSE -> Color(0xFFE91E63)          // Deep Pink - anxiety, stress
        EmotionalTone.ROMANTIC -> Color(0xFFFF4081)       // Pink - love, passion
        EmotionalTone.COMEDIC -> Color(0xFFFFD54F)        // Amber - humor, lightness
        EmotionalTone.DRAMATIC -> Color(0xFF7E57C2)       // Deep Purple - gravity, importance
        EmotionalTone.ACTION -> Color(0xFFFF5722)         // Deep Orange - energy, movement
        EmotionalTone.MYSTERIOUS -> Color(0xFF607D8B)     // Blue Grey - unknown, enigma
        EmotionalTone.JOYFUL -> Color(0xFFFF4081)         // Pink - happiness, celebration
        EmotionalTone.MELANCHOLY -> Color(0xFF5C6BC0)     // Indigo - sadness, reflection
        EmotionalTone.HOPEFUL -> Color(0xFF66BB6A)        // Green - growth, optimism
        EmotionalTone.ANGRY -> Color(0xFFE91E63)          // Deep Pink/Red - rage, fury
        EmotionalTone.FEARFUL -> Color(0xFF424242)        // Dark Grey - fear, dread
        EmotionalTone.NEUTRAL -> Color(0xFF9E9E9E)        // Grey - balanced, calm
        EmotionalTone.INTRIGUING -> Color(0xFF607D8B)     // Blue Grey - curiosity
        EmotionalTone.SHOCKING -> Color(0xFFE91E63)       // Deep Pink/Red - surprise, shock
        EmotionalTone.INTENSE -> Color(0xFFD32F2F)        // Red - high energy, passion

        // New tones with color assignments
        EmotionalTone.SUSPENSEFUL -> Color(0xFF4A148C)    // Deep Purple - tension, anticipation
        EmotionalTone.WHIMSICAL -> Color(0xFFBA68C8)      // Light Purple - playful, magical
        EmotionalTone.NOSTALGIC -> Color(0xFF8D6E63)      // Brown - memories, past
        EmotionalTone.OMINOUS -> Color(0xFF212121)        // Near Black - foreboding, dark
        EmotionalTone.BITTERSWEET -> Color(0xFF9C27B0)    // Purple - mixed emotions
        EmotionalTone.TRIUMPHANT -> Color(0xFFFFC107)     // Gold/Amber - victory, achievement
        EmotionalTone.DESPERATE -> Color(0xFF6A1B9A)      // Dark Purple - urgency, despair
        EmotionalTone.PEACEFUL -> Color(0xFF81C784)       // Light Green - calm, serenity
        EmotionalTone.CHAOTIC -> Color(0xFFFF3D00)        // Red-Orange - disorder, confusion
        EmotionalTone.SURREAL -> Color(0xFF00BCD4)        // Cyan - dreamlike, unreal
        EmotionalTone.ANTICIPATION -> Color(0xFF4A148C)        // same as SUSPENSE
        EmotionalTone.WONDER -> Color(0xFF00BCD4)
        else -> Color(0xFF9E9E9E)
    }
}

/**
 * Get opacity for emotional tone overlays
 */
fun getEmotionalToneOpacity(tone: EmotionalTone): Float {
    return when (tone) {
        EmotionalTone.NEUTRAL -> 0.3f
        EmotionalTone.PEACEFUL -> 0.4f
        EmotionalTone.WHIMSICAL -> 0.5f
        EmotionalTone.NOSTALGIC -> 0.5f
        EmotionalTone.HOPEFUL -> 0.6f
        EmotionalTone.BITTERSWEET -> 0.6f
        EmotionalTone.MYSTERIOUS, EmotionalTone.INTRIGUING -> 0.6f
        EmotionalTone.MELANCHOLY -> 0.6f
        EmotionalTone.COMEDIC, EmotionalTone.JOYFUL -> 0.7f
        EmotionalTone.ROMANTIC -> 0.7f
        EmotionalTone.DRAMATIC -> 0.7f
        EmotionalTone.SURREAL -> 0.7f
        EmotionalTone.TRIUMPHANT -> 0.8f
        EmotionalTone.SUSPENSEFUL -> 0.8f
        EmotionalTone.ACTION -> 0.8f
        EmotionalTone.TENSE, EmotionalTone.FEARFUL -> 0.8f
        EmotionalTone.DESPERATE -> 0.85f
        EmotionalTone.INTENSE, EmotionalTone.ANGRY -> 0.9f
        EmotionalTone.SHOCKING -> 0.9f
        EmotionalTone.CHAOTIC -> 0.9f
        EmotionalTone.OMINOUS -> 0.95f
        EmotionalTone.ANTICIPATION -> 0.95f
        EmotionalTone.WONDER -> 0.95f
        else -> 0.95f
    }
}

fun getEmotionalToneTextColor(tone: EmotionalTone): Color {
    return when (tone) {
        // Dark backgrounds need white text
        EmotionalTone.OMINOUS,
        EmotionalTone.FEARFUL,
        EmotionalTone.MYSTERIOUS,
        EmotionalTone.INTRIGUING,
        EmotionalTone.SUSPENSEFUL,
        EmotionalTone.DESPERATE -> Color.White

        // Light backgrounds need dark text
        EmotionalTone.COMEDIC,
        EmotionalTone.TRIUMPHANT,
        EmotionalTone.PEACEFUL,
        EmotionalTone.HOPEFUL,
        EmotionalTone.NEUTRAL -> Color.Black

        // Medium backgrounds - use white for better contrast
        else -> Color.White
    }
}

/**
 * Data class for emotional tone gradient
 */
data class EmotionalToneGradient(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color? = null
)

/**
 * Get gradient colors for more complex emotional tone visualization
 */
fun getEmotionalToneGradient(tone: EmotionalTone): EmotionalToneGradient {
    return when (tone) {
        EmotionalTone.TENSE -> EmotionalToneGradient(
            Color(0xFFE91E63),
            Color(0xFFC2185B),
            Color(0xFF880E4F)
        )
        EmotionalTone.ROMANTIC -> EmotionalToneGradient(
            Color(0xFFFF4081),
            Color(0xFFF50057),
            Color(0xFFC51162)
        )
        EmotionalTone.COMEDIC -> EmotionalToneGradient(
            Color(0xFFFFD54F),
            Color(0xFFFFC107),
            Color(0xFFFFB300)
        )
        EmotionalTone.DRAMATIC -> EmotionalToneGradient(
            Color(0xFF7E57C2),
            Color(0xFF673AB7),
            Color(0xFF5E35B1)
        )
        EmotionalTone.ACTION -> EmotionalToneGradient(
            Color(0xFFFF5722),
            Color(0xFFF4511E),
            Color(0xFFE64A19)
        )
        EmotionalTone.MYSTERIOUS -> EmotionalToneGradient(
            Color(0xFF607D8B),
            Color(0xFF546E7A),
            Color(0xFF455A64)
        )
        EmotionalTone.JOYFUL -> EmotionalToneGradient(
            Color(0xFFFF4081),
            Color(0xFFFFAB40),
            Color(0xFFFFD740)
        )
        EmotionalTone.MELANCHOLY -> EmotionalToneGradient(
            Color(0xFF5C6BC0),
            Color(0xFF3F51B5),
            Color(0xFF3949AB)
        )
        EmotionalTone.HOPEFUL -> EmotionalToneGradient(
            Color(0xFF66BB6A),
            Color(0xFF81C784),
            Color(0xFFA5D6A7)
        )
        EmotionalTone.ANGRY -> EmotionalToneGradient(
            Color(0xFFE91E63),
            Color(0xFFD32F2F),
            Color(0xFFB71C1C)
        )
        EmotionalTone.FEARFUL -> EmotionalToneGradient(
            Color(0xFF424242),
            Color(0xFF212121),
            Color(0xFF000000)
        )
        EmotionalTone.NEUTRAL -> EmotionalToneGradient(
            Color(0xFF9E9E9E),
            Color(0xFF757575),
            Color(0xFF616161)
        )
        EmotionalTone.INTRIGUING -> EmotionalToneGradient(
            Color(0xFF607D8B),
            Color(0xFF37474F),
            Color(0xFF263238)
        )
        EmotionalTone.SHOCKING -> EmotionalToneGradient(
            Color(0xFFE91E63),
            Color(0xFFFF5252),
            Color(0xFFFF1744)
        )
        EmotionalTone.INTENSE -> EmotionalToneGradient(
            Color(0xFFD32F2F),
            Color(0xFFC62828),
            Color(0xFFB71C1C)
        )
        EmotionalTone.SUSPENSEFUL -> EmotionalToneGradient(
            Color(0xFF4A148C),
            Color(0xFF311B92),
            Color(0xFF1A237E)
        )
        EmotionalTone.WHIMSICAL -> EmotionalToneGradient(
            Color(0xFFBA68C8),
            Color(0xFFE91E63),
            Color(0xFF00BCD4)
        )
        EmotionalTone.NOSTALGIC -> EmotionalToneGradient(
            Color(0xFF8D6E63),
            Color(0xFF795548),
            Color(0xFF6D4C41)
        )
        EmotionalTone.OMINOUS -> EmotionalToneGradient(
            Color(0xFF212121),
            Color(0xFF000000),
            Color(0xFF4A148C)
        )
        EmotionalTone.BITTERSWEET -> EmotionalToneGradient(
            Color(0xFF9C27B0),
            Color(0xFF5C6BC0),
            Color(0xFF8D6E63)
        )
        EmotionalTone.TRIUMPHANT -> EmotionalToneGradient(
            Color(0xFFFFC107),
            Color(0xFFFFD54F),
            Color(0xFFFFEB3B)
        )
        EmotionalTone.DESPERATE -> EmotionalToneGradient(
            Color(0xFF6A1B9A),
            Color(0xFF4A148C),
            Color(0xFF311B92)
        )
        EmotionalTone.PEACEFUL -> EmotionalToneGradient(
            Color(0xFF81C784),
            Color(0xFFA5D6A7),
            Color(0xFFC8E6C9)
        )
        EmotionalTone.CHAOTIC -> EmotionalToneGradient(
            Color(0xFFFF3D00),
            Color(0xFFE91E63),
            Color(0xFF4A148C)
        )
        EmotionalTone.SURREAL -> EmotionalToneGradient(
            Color(0xFF00BCD4),
            Color(0xFF9C27B0),
            Color(0xFFFF4081)
        )
        EmotionalTone.ANTICIPATION -> EmotionalToneGradient(
            Color(0xFF4A148C),
            Color(0xFF311B92),
            Color(0xFF1A237E)
        )
        EmotionalTone.WONDER -> EmotionalToneGradient(
            Color(0xFF607D8B),
            Color(0xFF37474F),
            Color(0xFF263238)
        )
        else -> EmotionalToneGradient(
            Color(0xFF81C784),
            Color(0xFFA5D6A7),
            Color(0xFFC8E6C9)
        )
    }
}

@Serializable
enum class NarrativeFunction {
    OPENING,
    EXPOSITION,
    RISING_ACTION,
    BUILDUP,
    FIRST_PLOT_POINT,
    CLIMAX,
    WORLD_BUILDING,
    DILEMMA,
    CATALYST,
    DENOUEMENT,
    TRANSFORMATION,
    TURNING_POINT,
    FALLING_ACTION,
    RESOLUTION,
    INCITING_INCIDENT,
    CONFLICT_INTRODUCTION,
    CONFLICT_RESOLUTION,
    STAKES_RAISING,
    MENTOR_INTRODUCTION,
    TRAINING,
    PLOT_TWIST,
    EMOTIONAL_BEAT,
    OBSTACLE,
    // Enhanced additions
    HOOK,                    // ADD
    SETUP,                   // ADD
    PAYOFF,                  // ADD
    REVERSAL,               // ADD
    REVELATION,             // ADD
    MIDPOINT,               // ADD
    PINCH_POINT,           // ADD
    FALSE_VICTORY,          // ADD
    FALSE_DEFEAT,           // ADD
    DARK_NIGHT_OF_SOUL,    // ADD
    BREAKTHROUGH,           // ADD
    CALLBACK,               // ADD
    FORESHADOWING,         // ADD
    RED_HERRING,           // ADD
    CLIFFHANGER,            // ADD
    COMIC_RELIEF,
    CHARACTER_DEVELOPMENT,
    CLIMAX_PEAK,
    CRISIS,
    BACKSTORY,
    DEVELOPMENT,
    COMPLICATION,
    FLASHBACK,
    WARNING,
    ANTAGONIST_PLAN,
    CONFRONTATION,
    ESCAPE
}

@Serializable
data class ScriptAnalysis(
    val scriptId: String,
    val structureType: String,
    val structureScore: Float,
    val suggestions: List<String>,

    // Enhanced fields (superset additions)
    val pacing: PacingAnalysis? = null,                  // ADD
    val characterBalance: Map<String, Float>? = null,    // ADD
    val emotionalJourney: List<EmotionalBeat>? = null,  // ADD
    val thematicElements: List<String>? = null,         // ADD
    val marketComparisons: List<String>? = null,        // ADD
    val audienceAppeal: Float? = null,                  // ADD
    val productionFeasibility: Float? = null,           // ADD
    val dialogueQuality: Float? = null,                 // ADD
    val visualPotential: Float? = null,                 // ADD
    val originalityScore: Float? = null,                // ADD
    val commercialViability: Float? = null              // ADD
)

@Serializable
data class RecentPattern(
    val id: String,
    val name: String,
    val description: String,
    val lastUsed: String,
    val rating: Int,
    val isModified: Boolean = false,

    // Enhanced fields (superset additions)
    val category: String? = null,                       // ADD
    val genre: List<String> = emptyList(),             // ADD
    val examples: List<String> = emptyList(),          // ADD
    val frequency: Int = 0,                            // ADD
    val successRate: Float? = null                     // ADD
)

@Serializable
data class AISuggestion(
    val id: String,
    val title: String,
    val description: String,
    val confidence: String,  // High, Medium, Low
    val impact: String,      // High, Medium, Low

    // Enhanced fields (superset additions)
    val category: String? = null,                      // ADD
    val implementation: String? = null,                // ADD
    val examples: List<String> = emptyList(),         // ADD
    val estimatedTime: Int? = null,                   // ADD
    val dependencies: List<String> = emptyList(),     // ADD
    val alternativeSuggestions: List<String> = emptyList() // ADD
)

// ===== Additional Supporting Classes =====

@Serializable
data class ScriptRevision(
    val id: String,
    val version: String,
    val date: Instant,
    val author: String,
    val colorCode: String? = null,
    val changes: List<String>,
    val notes: String? = null
)

@Serializable
data class ScriptNote(
    val id: String,
    val type: NoteType = NoteType.PRODUCTION,
    val content: String,
    val author: String,
    val timestamp: Instant,
    val sceneId: String? = null,
    val resolved: Boolean = false
)

@Serializable
enum class NoteType {
    GENERAL,
    PRODUCTION,
    DIRECTION,
    PERFORMANCE,
    TECHNICAL,
    LEGAL,
    CONTINUITY,
    BUDGET,
    SCHEDULE
}

@Serializable
data class ScriptBreakdowns(
    val characterBreakdown: Map<String, CharacterBreakdown>,
    val locationBreakdown: Map<String, LocationBreakdown>,
    val propBreakdown: List<PropBreakdown>,
    val wardrobeBreakdown: Map<String, List<WardrobeItem>>,
    val vehicleBreakdown: List<VehicleRequirement>,
    val animalBreakdown: List<AnimalRequirement>,
    val specialEffectsBreakdown: List<SpecialEffect>
)

@Serializable
data class CharacterBreakdown(
    val characterId: String,
    val name: String,
    val sceneCount: Int,
    val lineCount: Int,
    val screenTime: Float,
    val description: String? = null,
    val ageRange: String? = null,
    val physicalRequirements: String? = null,
    val skillsRequired: List<String> = emptyList()
)

@Serializable
data class LocationBreakdown(
    val locationId: String,
    val name: String,
    val sceneCount: Int,
    val dayScenes: Int,
    val nightScenes: Int,
    val interiorScenes: Int,
    val exteriorScenes: Int,
    val description: String? = null,
    val requirements: List<String> = emptyList(),
    val permits: List<String> = emptyList()
)

@Serializable
data class PropBreakdown(
    val propId: String,
    val name: String,
    val scenes: List<String>,
    val description: String? = null,
    val quantity: Int = 1,
    val source: String? = null,
    val cost: Float? = null
)

@Serializable
data class WardrobeItem(
    val id: String,
    val description: String,
    val scenes: List<String>,
    val changes: Int = 1,
    val condition: String? = null
)

@Serializable
data class VehicleRequirement(
    val id: String,
    val type: String,
    val make: String? = null,
    val model: String? = null,
    val year: String? = null,
    val scenes: List<String>,
    val action: Boolean = false
)

@Serializable
data class AnimalRequirement(
    val id: String,
    val species: String,
    val breed: String? = null,
    val name: String? = null,
    val scenes: List<String>,
    val handler: String? = null,
    val specialNeeds: String? = null
)

@Serializable
data class SpecialEffect(
    val id: String,
    val type: String,
    val description: String,
    val scenes: List<String>,
    val practical: Boolean,
    val digital: Boolean,
    val safetyRequirements: String? = null
)

@Serializable
data class BudgetInfo(
    val total: Float,
    val aboveTheLine: Float,
    val belowTheLine: Float,
    val postProduction: Float,
    val contingency: Float,
    val breakdown: Map<String, Float>
)

@Serializable
data class ProductionSchedule(
    val prepDays: Int,
    val shootDays: Int,
    val postDays: Int,
    val startDate: Instant? = null,
    val wrapDate: Instant? = null,
    val dailySchedule: List<DaySchedule> = emptyList()
)

@Serializable
data class DaySchedule(
    val day: Int,
    val date: Instant,
    val scenes: List<String>,
    val location: String,
    val callTime: String,
    val estimatedWrap: String
)

@Serializable
data class ConflictInfo(
    val type: ConflictTypeEnum,
    val description: String,
    val stakes: String? = null,
    val resolution: String? = null
)

@Serializable
enum class ConflictTypeEnum {
    CHARACTER_VS_CHARACTER,
    CHARACTER_VS_SELF,
    CHARACTER_VS_NATURE,
    CHARACTER_VS_SOCIETY,
    CHARACTER_VS_TECHNOLOGY,
    CHARACTER_VS_SUPERNATURAL,
    CHARACTER_VS_FATE
}

@Serializable
data class TransitionInfo(
    val transitionIn: TransitionType? = null,
    val transitionOut: TransitionType? = null,
    val audioTransition: AudioTransition? = null
)

@Serializable
enum class AudioTransition {
    CUT,
    CROSSFADE,
    FADE_IN,
    FADE_OUT,
    BRIDGE,
    OVERLAP,
    PRELAP,
    POSTLAP
}

@Serializable
data class BlockingNote(
    val id: String,
    val characterName: String,
    val movement: String,
    val position: String? = null,
    val timing: Float? = null
)

@Serializable
data class ShotInfo(
    val id: String,
    val shotNumber: String,
    val shotType: ShotType,
    val angle: CameraAngle,
    val movement: CameraMovement? = null,
    val lens: String? = null,
    val duration: Float? = null,
    val description: String? = null
)

@Serializable
data class MusicCue(
    val id: String,
    val cueNumber: String,
    val title: String? = null,
    val composer: String? = null,
    val timing: Float,
    val duration: Float? = null,
    val description: String? = null,
    val emotion: String? = null
)

@Serializable
data class SoundDesign(
    val ambience: List<String> = emptyList(),
    val effects: List<SoundEffect> = emptyList(),
    val foley: List<String> = emptyList(),
    val roomTone: String? = null
)

@Serializable
data class SoundEffect(
    val id: String,
    val name: String,
    val type: String,
    val url: String,
    val description: String,
    val timing: Float? = null,
    val startTime: Float,
    val duration: Float,
    val volume: Float = 1f
)

@Serializable
data class VFXRequirement(
    val id: String,
    val type: String,
    val description: String,
    val complexity: String,
    val shots: List<String> = emptyList()
)

@Serializable
data class WardrobeInfo(
    val description: String,
    val changes: List<String> = emptyList(),
    val condition: String? = null,
    val notes: String? = null
)

@Serializable
data class MakeupInfo(
    val baseDescription: String,
    val specialEffects: String? = null,
    val prosthetics: String? = null,
    val continuityNotes: String? = null
)

@Serializable
data class StuntInfo(
    val id: String,
    val type: String,
    val description: String,
    val performer: String? = null,
    val coordinator: String? = null,
    val rehearsalTime: Float? = null,
    val safetyMeasures: List<String> = emptyList()
)

@Serializable
data class ExtrasInfo(
    val count: Int,
    val description: String? = null,
    val wardrobe: String? = null,
    val action: String? = null
)

@Serializable
data class EmphasisInfo(
    val word: String,
    val startIndex: Int,
    val endIndex: Int,
    val type: EmphasisType
)

@Serializable
enum class EmphasisType {
    ITALIC,
    BOLD,
    UNDERLINE,
    CAPS,
    WHISPER,
    SHOUT,
    STRONG
}

@Serializable
data class EmotionalBeat(
    val position: Float,
    val emotion: EmotionalTone,
    val intensity: Float,          // 0.0 to 1.0
    val duration: String,
    val trigger: String,
    val resolution: String,
    val description: String? = null
)

/**
 * Data class for additional script metadata
 */
@Serializable
data class ScriptMetadata(
    val episodeNumber: Int,
    val seasonNumber: Int,
    val seriesName: String,
    val genre: List<String>,
    val themes: List<String>,
    val logline: String,
    val productionNotes: List<String>,
    val keyLocations: List<String>
)