// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/Storyboard.kt
package com.beekeeper.app.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Domain entity representing a Storyboard
 * ENHANCED with proper story/script relationships
 */
@Serializable
data class Storyboard(
    val id: String,
    val projectId: String,
    val storyId: String,
    val scriptId: String,
    val storyboardType: StoryboardType = StoryboardType.FULL,
    val title: String,
    val description: String? = null,
    val scenes: List<Scene>,
    val sceneCount: Int = 0,
    val duration: Int = 0, // in seconds
    val createdAt: Instant,
    val updatedAt: Instant,
    val createdBy: String = "",
    val version: Int = 1,
    val isLocked: Boolean = false,
    val thumbnailUrl: String? = null,
    val completionPercentage: Int = 0,
    val aspectRatio: AspectRatio = AspectRatio.RATIO_16_9,
    val resolution: Resolution = Resolution.HD_1080p,
    val platformSettings: Map<String, StoryboardPlatformSettings> = emptyMap(),

    // AI generation prompts for external tools
    val visualPrompt: String? = null,    // For visual generation (Midjourney, Stable Diffusion)
    val voicePrompt: String? = null,     // For voice/dialogue generation (ElevenLabs)
    val ambientPrompt: String? = null    // For ambient audio/music generation
)

@Serializable
enum class BoardType {
    STORYBOARD,
    CHARACTER_BOARD
}

@Serializable
enum class StoryboardType {
    FULL,               // Complete episode storyboard (SAME AS MainStoryBoard)
    SEQUENCE,           // Specific sequence (action, emotional, etc.)
    CHARACTER_FOCUSED,  // Character-specific scenes
    ALTERNATE,          // Alternative version
    ROUGH,              // Initial rough boards
    FINAL,              // Locked/approved version
    VFX,                // NEW: VFX-specific storyboard
    STUNT,              // NEW: Stunt sequence storyboard
    MARKETING,          // NEW: Marketing/trailer storyboard
    ANIMATED,           // NEW: Animated storyboard with timing
    CONCEPT             // NEW: Concept/mood boards
}

@Serializable
data class Scene(
    val id: String,
    val storyboardId: String,
    val sceneScriptId: String,
    val sceneNumber: Int,
    val title: String,
    val description: String,
    val frames: List<Frame> = emptyList(),
    val duration: Int = 0, // in seconds
    val dialogue: String? = null,
    val notes: String? = null,
    val cameraInstructions: String? = null,
    val soundEffects: List<String> = emptyList(),
    val musicCues: List<String> = emptyList(),
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val dialogueSnippet: String? = null,
    val cameraDirection: String? = null,
    val location: String? = null,
    val timeOfDay: String? = null,
    val characterIds: List<String> = emptyList(),
    val transitionType: TransitionType = TransitionType.NONE,
    val isKeyScene: Boolean = false,
    val aiSuggestions: List<String> = emptyList(),
    val lightingNotes: String? = null,
    val soundNotes: String? = null,
    val vfxNotes: String? = null,
    val transitionIn: TransitionType = TransitionType.CUT,
    val transitionOut: TransitionType = TransitionType.CUT,

    // AI generation prompts for external tools
    val visualPrompt: String? = null,    // For visual generation (Midjourney, Stable Diffusion)
    val voicePrompt: String? = null,     // For voice/dialogue generation (ElevenLabs)
    val ambientPrompt: String? = null    // For ambient audio/music generation
)

@Serializable
data class Frame(
    val id: String,
    val sceneId: String,
    val frameNumber: Int,
    val dialogueLineId: String? = null,
    val imageUrl: String? = null,
    val thumbnailUrl: String? = null,
    val description: String,
    val shotType: ShotType,
    val cameraAngle: CameraAngle,
    val cameraMovement: CameraMovement? = null,
    val timestamp: String = "00:00",
    val aspectRatio: String = "16:9",
    val resolution: String = "1920x1080",
    val frameRate: String = "24 fps",
    val duration: Float = 1.0f, // in seconds
    val transitionIn: TransitionType? = null,
    val transitionOut: TransitionType? = null,
    val action: String? = null,
    val vfxNotes: String? = null,
    val audioNotes: String? = null,
    val visualNotes: String? = null,
    val generationSettings: GenerationSettings? = null,

    // AI generation prompts for external tools
    val visualPrompt: String? = null,    // For visual generation (Midjourney, Stable Diffusion, Runway)
    val voicePrompt: String? = null,     // For voice/dialogue generation (ElevenLabs)
    val ambientPrompt: String? = null    // For ambient audio/SFX generation
)

@Serializable
data class Prop(
    val id: String,
    val name: String,
    val description: String,
    val category: PropCategory = PropCategory.OTHER,  // HANDHELD, FURNITURE, VEHICLE, WEAPON, etc.
    val projectId: String? = null,  // Add project reference for organization
    val characterId: String? = null,  // If held/worn by character
    val storyIds: List<String> = listOf(),  // Stories where prop appears
    val scriptIds: List<String> = listOf(),  // Scripts where prop appears
    val sceneIds: List<String> = listOf(),  // Scenes where prop appears
    val size: String? = null,  // SMALL, MEDIUM, LARGE
    val imageUrl: String? = null,  // Reference image URL
    val importance:  String? = null,  // HERO, FEATURED, BACKGROUND
    val interactionType: String? = null,  //    HOLDABLE, WEARABLE, RIDEABLE, CONSUMABLE, OPERABLE,    // Can be operated (door, switch, etc.) STATIC       // No interaction
    val texture: String = "default",  // "metallic", "wooden", "fabric", etc.
    val color: String = "#FFFFFF",
    val reflectivity: Float = 0.5f,
    val transparency: Float = 1.0f,
    val continuityNotes: String? = null
)



enum class PropCategory {
    HANDHELD,
    FURNITURE,
    VEHICLE,
    WEAPON,
    CLOTHING,
    FOOD,
    ELECTRONICS,
    DOCUMENT,
    CONTAINER,
    DECORATION,
    TOOL,
    OTHER,
    FABRIC,
    UTENSIL,
    PEOPLE,
    WINDOW,
    APPLIANCE,
    DOOR,
    LOCATION,
    MEDIA,
    EQUIPMENT,
    VFX,
    ARCHITECTURE,
    SIGNAGE,
    JEWELRY,
    INSTRUMENT,
    ARTIFACT,
    CONCEPT
}


// NEW SUPPORTING DATA CLASSES

@Serializable
data class StoryboardPlatformSettings(
    val platformName: String,
    val includeAnimated: Boolean,
    val includeAudioTrack: Boolean,
    val exportResolution: Resolution
)

@Serializable
data class GenerationSettings(
    val generationType: GenerationType,
    val aiModel: String? = null,
    val prompts: Map<String, String> = emptyMap(),
    val parameters: Map<String, String> = emptyMap()
)

@Serializable
enum class GenerationType {
    AI_GENERATED,
    TRADITIONAL,
    HYBRID,
    STOCK_FOOTAGE,
    ANIMATION,
    MOTION_CAPTURE
}

@Serializable
enum class TransitionType {
    ADDITIVE_BLEND,
    CUT,
    FADE, // out and in
    FADE_IN,
    FADE_OUT,
    DISSOLVE,
    WIPE,
    IRIS,
    FLASH_CUT,
    MATCH_CUT,
    JUMP_CUT,
    CROSS_FADE,
    SMASH_CUT,
    NONE,
    CUT_TO_BLACK,
    HARD_CUT,
    STROBE_CUT,
    GLITCH_CUT,
    FADE_TO_BLACK,
    SLOW_MOTION
}


@Serializable
enum class AspectRatio {
    RATIO_16_9,    // TV/YouTube
    RATIO_9_16,    // TikTok/Reels/Shorts
    RATIO_1_1,     // Instagram Square
    RATIO_4_5,     // Instagram Portrait
    RATIO_21_9,    // Cinematic
    RATIO_2_35_1,  // Anamorphic
    RATIO_4_3,      // Classic TV
    RATIO_2_3,      // Classic TV
    CINEMA_235
}

@Serializable
data class ContentGenerationRequest(
    val type: String,
    val prompt: String,
    val parameters: Map<String, String> = emptyMap()
)

@Serializable
data class ContentGenerationResult(
    val content: String,
    val metadata: Map<String, String> = emptyMap(),
    val success: Boolean = true
)

@Serializable
enum class ContentCreationStep {
    SCRIPT_GENERATION,
    CHARACTER_CREATION,
    STORYBOARD_GENERATION,
    SCENE_GENERATION,
    AUDIO_GENERATION,
    VIDEO_ASSEMBLY,
    EXPORT
}

// Content Creation State
@Serializable
data class ContentCreationState(
    val currentStep: ContentCreationStep = ContentCreationStep.SCRIPT_GENERATION,
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val script: String = "",
    val characters: List<CharacterData> = emptyList(),
    val storyboard: List<StoryboardFrame> = emptyList(),
    val scenes: List<SceneData> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val finalVideo: VideoData? = null,
    val errors: List<String> = emptyList()
)

@Serializable
data class CharacterData(
    val id: String,
    val name: String,
    val description: String,
    val avatarUrl: String? = null,
    val voiceId: String? = null
)

@Serializable
data class StoryboardFrame(
    val id: String,
    val sceneNumber: Int,
    val frameNumber: Int,
    val description: String,
    val duration: Float = 5f,
    val sceneData: SceneData
)

@Serializable
data class SceneData(
    val id: String,
    val frameId: String,
    val characterIds: List<String>,
    val dialogue: String,
    val visualDescription: String,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val videoUrl: String? = null,
    val text: String? = null
)

@Serializable
data class AudioTrack(
    val id: String,
    val type: String, // "dialogue", "music", "sfx"
    val content: String,
    val audioUrl: String? = null,
    val duration: Float = 0f
)

@Serializable
data class VideoData(
    val id: String,
    val title: String,
    val format: String = "MP4",
    val resolution: String = "1080p",
    val duration: Float = 0f,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null
)

@Serializable
data class StoryboardState(
    val scenes: List<Scene> = emptyList(),
    val selectedSceneId: String? = null,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val isDragging: Boolean = false,
    val totalDuration: Int = 0,
    val viewMode: ViewMode = ViewMode.GRID
)

@Serializable
enum class ViewMode {
    GRID, TIMELINE, LIST
}

@Serializable
// All enums remain the same
enum class ShotType {
    WIDE_SHOT,
    MEDIUM_SHOT,
    LOW_ANGLE,
    TOP_DOWN,
    VFX_PLATE,
    REACTION_SHOT,
    LONG_SHOT,
    HIGH_ANGLE,
    HERO_SHOT,
    CLOSE_UP,
    INSERT_SHOT,
    EXTREME_CLOSE_UP,
    MEDIUM_TWO_SHOT,
    OVER_THE_SHOULDER,
    TWO_SHOT,
    ESTABLISHING_SHOT,
    POV_SHOT,
    TRACKING_SHOT,
    AERIAL_SHOT,
    EXTREME_WIDE_SHOT,
    MONTAGE,
    MEDIUM_CLOSE_UP,
    OVER_SHOULDER,
    STATIC,
    THREE_SHOT,
    FULL_SCREEN,
    SURVEILLANCE_FOOTAGE,
    GROUND_LEVEL,
    INSERT,
    TITLES,
    MEDIUM_WIDE_SHOT,
    MEDIUM_THREE_SHOT,
    EXTREME_WIDE,
    SLOW_MOTION,
    SPLIT_SCREEN
}

@Serializable
enum class CameraAngle {
    EYE_LEVEL,
    OVER_THE_SHOULDER,
    HIGH_ANGLE,
    NEUTRAL,
    DIRECT,
    LOW_ANGLE,
    SLIGHT_LOW_ANGLE,
    DUTCH_ANGLE,
    PROFILE,
    BIRDS_EYE,
    WORMS_EYE,
    CLOSE_UP,
    TOP_DOWN,
    VARIED,
    CANTED,
    OVER_SHOULDER,
    AERIAL,
    SURVEILLANCE,
    HANDHELD,
    TILTED,
    VARIOUS,
    MACRO,
    EXTREME_CLOSE_UP,
    SLIGHTLY_HIGH_ANGLE,
    WIDE_ANGLE,
    UPWARD_TILT,
    GROUND_LEVEL
}

@Serializable
enum class CameraMovement {
    CRANE_SHOT,
    STATIC,
    PAN_LEFT,
    CIRCULAR_TRACK,
    DYNAMIC,
    TIME_LAPSE,
    TRACK_WITH_SUBJECT,
    TRACK_LEFT,
    TRACK_RIGHT,
    QUICK_PAN,
    SLOW_TILT_UP,
    SWEEPING,
    SLOW_PAN_RIGHT,
    SLOW_PAN_LEFT,
    SUBTLE_PUSH_IN,
    SUBTLE_SHAKE,
    SLOW_PULL_BACK,
    SLOW_DOLLY_OUT,
    ROTATE_AROUND,
    LOCKED_OFF,
    PULL_BACK,
    DRONE_SHOT,
    CRASH_ZOOM_OUT,
    TRACKING_UP,
    PAN_RIGHT,
    PUSH_IN,
    TILT_UP,
    TILT_DOWN,
    DOLLY_IN,
    DOLLY_OUT,
    CRANE_UP,
    CRANE_DOWN,
    TRACKING,
    HANDHELD,
    SLOW_ZOOM_IN,
    SLOW_ZOOM_OUT,
    ZOOM_IN,
    MONTAGE,
    SLOW_PUSH_IN,
    STEADICAM,
    QUICK_ZOOM,
    SLOW_PULL_OUT,
    PULL_BACK_FAST,
    SHAKY,
    SLOW_ZOOM,
    CRASH_ZOOM,
    SLOW_DOLLY_IN,
    SPIRALING,
    HELICOPTER_SHOT,
    DOCUMENTARY_STYLE,
    DIGITAL_ZOOM,
    WHIP_PAN,
    NONE,
    CHAOTIC,
    RUNNING,
    HELMET_CAM,
    SLOW_PAN,
    CRASH_ZOOM_IN,
    SLOW_CRANE_UP,
    SLIGHT_PUSH_IN,
    ENERGETIC_MOVEMENT,
    SLIGHT_PULL_BACK,
    QUICK_PAN_RIGHT,
    QUICK_PUSH_IN,
    VIOLENT_SHAKE,
    QUICK_CUTS,
    SLOW_MOTION,
    FOLLOW,
    ROTATE_AROUND_SLOW,
    SLOW_CRANE_DOWN,
    FLOAT_THROUGH,
    ROTATE_AROUND_FAST,
    INTERCUT,
    SLOW_PAN_UP,
    ROTATE_AROUND_360,
    SLOW_PUSH_OUT,
    SLOW_REVEAL
}

