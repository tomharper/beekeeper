// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/Story.kt
package com.beekeeper.app.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.beekeeper.app.Screen
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Unified domain models for Content Repository
 * Superset of all properties from repository interfaces and existing models
 */

@Serializable
data class Story(
    val id: String,
    val projectId: String,
    val scriptId: String,  // CHANGED: Single script reference
    val title: String,
    val synopsis: String,
    val genre: String,
    val themes: List<String>,
    val setting: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val status: ContentStatus,
    val duration: Int? = null, // Duration in minutes
    val lastEditedBy: String? = null,

    // ===== UPDATED EPISODE TRACKING FIELDS =====
    val episodeNumber: Int? = null,
    val seasonNumber: Int? = null,
    val episodeCode: String? = null,
    val episodeTitle: String? = null,
    val isStandalone: Boolean = false,
    val sequenceOrder: Int? = null,
    val storyboardIds: List<String>,
    val storyCategory: StoryCategory = StoryCategory.EPISODE,
    val targetAudience: String? = null,
    val estimatedBudget: Float? = null,

    // Characters are attached at the script level
    // acts are completely optional and should not be used to connect anything together
    val acts: List<StoryAct> = emptyList<StoryAct>(),// THIS SHOULD NEVER BE USED FOR ANYTHING IMPORTANT

    // Platform-specific optimizations
    val metadata: Map<String, PlatformSettings> = emptyMap(),
    val tags: Map<String, String> = emptyMap(),
    val extradata: List<String> = emptyList(),
    val descriptors: List<String> = emptyList(),
    val exportFormats: List<ExportFormat> = emptyList(),
    val logline: Map<String, String> = emptyMap(),

    // AI generation prompts for external tools
    val visualPrompt: String? = null,    // For story visual generation (Midjourney, Stable Diffusion)
    val voicePrompt: String? = null,     // For narration/voice generation (ElevenLabs)
    val ambientPrompt: String? = null    // For story theme/ambient audio
)

@Serializable
enum class StoryCategory {
    EPISODE,
    PILOT,
    STANDALONE,
    MOVIE,
    SHORT,
    SPECIAL,
    FINALE
}

// acts are completely optional and should not be used to connect anything together
@Serializable
data class StoryAct(
    val number: Int,
    val title: String,
    val description: String,
    val sceneScriptIds: List<String> = emptyList() // These are SCENE SCRIPT IDs, NOT SCENE IDs
)

@Serializable
data class StoryPattern(
    val id: String,
    val name: String,
    val description: String,
    val structure: PatternStructure,
    val examples: List<String>,
    val frequency: Int = 0,
    val confidence: Float = 0f,
    val category: PatternCategory = PatternCategory.NARRATIVE_STRUCTURE,
    @Contextual val icon: ImageVector? = null
)

@Serializable
data class PlatformSettings(
    val platformName: String,
    val exportFormat: ExportFormat,
    val aspectRatio: AspectRatio,
    val resolution: Resolution,
    val maxDuration: Int? = null,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
enum class Resolution {
    SD_480p,
    HD_720p,
    HD_1080p,
    UHD_4K,
    UHD_8K
}

@Serializable
enum class PatternCategory {
    NARRATIVE_STRUCTURE,
    CHARACTER_ARC,
    DIALOGUE_STYLE,
    PACING,
    THEME,
    CONFLICT
}

@Serializable
data class PatternStructure(
    val type: String,
    val beats: List<StoryBeat>
)

@Serializable
data class StoryBeat(
    val name: String,
    val position: Float,
    val description: String
)

@Serializable
data class StoryAnalysis(
    val storyId: String,
    val pacing: PacingAnalysis,
    val themeConsistency: Float,
    val structureScore: Float,
    val suggestions: List<String>
)

@Serializable
data class PacingAnalysis(
    val overallPace: String,
    val actPacing: Map<Int, String>,
    val suggestions: List<String>,
    val sceneLengths: List<Float> = emptyList(),
)

@Serializable
data class DialogueAnalysis(
    val scriptId: String,
    val characterDialogueCount: Map<String, Int>,
    val averageLineLength: Float,
    val dialoguePacing: String,
    val suggestions: List<String>
)

@Serializable
data class ScriptVersion(
    val id: String,
    val scriptId: String,
    val version: String,
    val changes: String,
    val author: String,
    val timestamp: Long
)


@Serializable
data class QualityReview(
    val id: String,
    val contentId: String,
    val contentType: ContentType,
    val projectId: String,
    val reviewer: String?,
    val score: Float,
    val status: ReviewStatus,
    val feedback: String,
    val metrics: QualityMetrics,
    val createdAt: Long,
    val completedAt: Long?
)

@Serializable
data class ContentItem(
    val id: String,
    val type: ContentType,
    val title: String,
    val projectId: String,
    val thumbnailUrl: String?,
    val lastModified: Long,
    val status: ContentStatus
)

@Serializable
data class ContentFilters(
    val types: List<ContentType>? = null,
    val status: List<ContentStatus>? = null,
    val dateRange: DateRange? = null,
    val tags: List<String>? = null
)

@Serializable
data class DateRange(
    val start: Long,
    val end: Long
)

@Serializable
enum class ContentType {
    // Story/Script types
    STORY,
    SCRIPT,
    STORYBOARD,
    SCENE,
    IMAGE,
    VIDEO,
    DIALOGUE,
    STORYBOARD_FRAME,
    CHARACTER_AVATAR,
    CHARACTER,
    AVATAR,
    THUMBNAIL,
    AUDIO,
    PROP,
    SCENE_PREVIEW,
    VFX_PREVIEW,
    BACKGROUND,
    DOCUMENT,
    MODEL_3D,
    FONT,
    EFFECT,
    TEMPLATE,
    STYLE,
    OTHER
}

@Serializable
enum class ContentStatus {
    DRAFT,
    FINAL_DRAFT,
    IN_PROGRESS,
    IN_REVIEW,
    APPROVED,
    IN_PRODUCTION,
    ARCHIVED,
    COMPLETED
}

@Serializable
enum class ReviewStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    REJECTED
}


@Serializable
data class QualityMetric(
    val category: String,
    val score: Float, // 0-100
    val status: QualityStatus,
    val issues: List<QualityIssue>
)

@Serializable
data class QualityIssue(
    val severity: IssueSeverity,
    val description: String,
    val affectedScenes: List<String>,
    val recommendation: String
)

@Serializable
enum class QualityStatus {
    EXCELLENT,
    GOOD,
    NEEDS_ATTENTION,
    CRITICAL
}

@Serializable
enum class IssueSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}


@Serializable
data class ConsistencyChecks(
    val characterConsistency: List<ConsistencyCheck>,
    val narrativeConsistency: List<ConsistencyCheck>,
    val soundConsistency: List<ConsistencyCheck>,
    val visualConsistency: List<ConsistencyCheck>
)

@Serializable
data class ConsistencyCheck(
    val element: String,
    val description: String,
    val status: ConsistencyStatus,
    val affectedCount: Int,
    val details: List<String> = emptyList()
)

@Serializable
enum class ConsistencyStatus {
    CONSISTENT,
    MINOR_ISSUES,
    MAJOR_ISSUES
}

@Serializable
data class PlatformCompliance(
    val name: String,
    @Contextual val icon: ImageVector,
    val isCompliant: Boolean,
    val requirements: List<ComplianceRequirement>
)

@Serializable
data class ComplianceRequirement(
    val description: String,
    val met: Boolean
)

@Serializable
data class Recommendation(
    val title: String,
    val description: String,
    val priority: Priority,
    val estimatedImpact: Int? = null
)

@Serializable
data class UserReview(
    val id: String,
    val userName: String,
    val rating: Float, // 1-5 stars
    val comment: String,
    val timestamp: Long,
    val platform: String? = null,
    val isVerifiedPurchase: Boolean = false
)

/**
 * Data classes for production metadata
 */
@Serializable
data class ProductionMetadata(
    val episodeNumber: Int,
    val seasonNumber: Int,
    val seriesName: String,
    val episodeTitle: String,
    val genre: List<String>,
    val keyThemes: List<String>,
    val productionNotes: List<String>,
    val vfxRequirements: VFXRequirements? = null,
    val locations: List<String> = emptyList<String>(),
    val stuntRequirements: List<String> = emptyList<String>(),
    val musicRequirements: List<String> = emptyList<String>()
)

@Serializable
data class VFXRequirements(
    val totalShots: Int,
    val categories: Map<String, Int>,
    val priorityLevel: String,
    val estimatedRenderTime: Int
)
