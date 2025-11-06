// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/ProjectModels.kt
package com.beekeeper.app.domain.model

import com.beekeeper.app.domain.repository.PlatformCapability
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CreativeProject(
    val id: String,
    val title: String,
    val description: String,
    val type: ProjectType,
    val status: ProjectStatus,
    val timeline: ProjectTimeline? = null,
    val team: ProjectTeam? = null,
    val budget: ProjectBudget? = null,
    val deliverables: List<Deliverable> = emptyList(),
    val platformTargets: List<StreamingPlatform> = emptyList(),
    val currentPhase: ProductionPhase,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val clientId: String = "",
    val priority: ProjectPriority = ProjectPriority.MEDIUM,
    val metadata: String = "",
    val projectBible: ProjectBible? = null, // this is the story arc
    val analytics: ProjectAnalytics? = null,
    val userReviews: List<UserReview> = emptyList()
)

@Serializable
enum class Genre {
    ACTION,
    ADVENTURE,
    COMEDY,
    DRAMA,
    FANTASY,
    HORROR,
    MYSTERY,
    ROMANCE,
    SCI_FI,
    THRILLER,
    WESTERN,
    DOCUMENTARY,
    ANIMATION,
    MUSICAL,
    MORALITY_TALE
}

@Serializable
enum class ProjectStatus {
    ACTIVE,
    DRAFT,
    IN_DEVELOPMENT,
    PRE_PRODUCTION,
    IN_PRODUCTION,
    POST_PRODUCTION,
    COMPLETED,
    PUBLISHED,
    ARCHIVED,
    ON_HOLD,
    PLANNING,
    IN_REVIEW,
    READY_FOR_DISTRIBUTION,
    DELIVERED,
    CANCELLED
}

@Serializable
data class Credit(
    val role: String,
    val name: String,
    val link: String? = null
)


@Serializable
data class ProjectMetadata(
    val genre: String,
    val tags: List<String>,
    val targetAudience: String,
    val rating: String?,
    val credits: List<Credit>,
    val language: String = "English",
    // Additional fields for superset
    val description: String? = null,
    val keywords: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val budget: Double? = null,
    val actualCost: Double? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val location: String? = null,
    val aspectRatio: String = "16:9",
    val frameRate: String = "24fps",
    val resolution: String = "1920x1080"
)


@Serializable
enum class ProjectPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@Serializable
enum class ProductionPhase {
    DEVELOPMENT,        // Story creation & script development
    PRE_PRODUCTION,     // Planning & asset creation
    PRODUCTION,         // Content generation & quality control
    POST_PRODUCTION,    // Assembly & refinement
    DISTRIBUTION       // Platform delivery & analytics
}
@Serializable
enum class ProjectType {
    // Short-Form Track (CineFiller Phase 1-2)
    SOCIAL_MEDIA_CONTENT,    // 1-5 minute videos for social platforms
    MARKETING_CAMPAIGN,      // Promotional content for existing IP
    MINI_SERIES,            // 5-10 minute episodic content
    WEB_SERIES,

    // Episodic Track (CineFiller Phase 2-4)
    TV_EPISODE,             // 30-60 minute episodes
    TV_SERIES,              // Multi-episode series management
    FEATURE_LENGTH,         // 90+ minute content

    // Specialized Content
    DOCUMENTARY,            // Non-fiction content
    INTERACTIVE_CONTENT,    // Branching narratives
    LOCALIZED_CONTENT,      // Multi-language adaptations

    FEATURE_FILM,
    SHORT_FILM,
    ANIMATION,
    COMMERCIAL,
    MUSIC_VIDEO,
    EDUCATIONAL,
    EXPERIMENTAL
}

@Serializable
data class ProjectTimeline(
    val startDate: Instant,
    val plannedEndDate: Instant,
    val actualEndDate: Instant? = null,
    val milestones: List<Milestone>,
    val phases: Map<ProductionPhase, PhaseTimeline>
)

@Serializable
data class Milestone(
    val id: String,
    val title: String,
    val description: String,
    val targetDate: Instant,
    val completedDate: Instant? = null,
    val isCompleted: Boolean = false,
    val dependencies: List<String> = emptyList(),
    val phase: ProductionPhase
)

@Serializable
data class PhaseTimeline(
    val phase: ProductionPhase,
    val startDate: Instant,
    val plannedEndDate: Instant,
    val actualEndDate: Instant? = null,
    val progress: Float = 0f, // 0.0 to 1.0
    val tasks: List<PhaseTask>
)

@Serializable
data class PhaseTask(
    val id: String,
    val title: String,
    val description: String,
    val assignedTo: String? = null,
    val status: TaskStatus,
    val estimatedHours: Float,
    val actualHours: Float = 0f,
    val dependencies: List<String> = emptyList(),
    val aiAssistanceRequired: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM
)

@Serializable
enum class TaskStatus {
    NOT_STARTED,
    IN_PROGRESS,
    BLOCKED,
    IN_REVIEW,
    COMPLETED,
    CANCELLED
}

@Serializable
enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@Serializable
data class ProjectTeam(
    val projectManager: TeamMember,
    val creativeDirector: TeamMember? = null,
    val writers: List<TeamMember> = emptyList(),
    val designers: List<TeamMember> = emptyList(),
    val technicalLead: TeamMember? = null,
    val qaTeam: List<TeamMember> = emptyList(),
    val clientContacts: List<TeamMember> = emptyList(),
    val aiSpecialists: List<TeamMember> = emptyList()
)

@Serializable
data class TeamMember(
    val id: String,
    val name: String,
    val email: String,
    val role: TeamRole,
    val permissions: List<Permission>,
    val hourlyRate: Float? = null,
    val availability: Float = 1.0f // 0.0 to 1.0 (percentage available)
)

@Serializable
enum class TeamRole {
    PROJECT_MANAGER,
    CREATIVE_DIRECTOR,
    WRITER,
    SCRIPT_WRITER,
    CHARACTER_DESIGNER,
    STORYBOARD_ARTIST,
    ENVIRONMENT_DESIGNER,
    TECHNICAL_LEAD,
    AI_SPECIALIST,
    QA_ANALYST,
    CLIENT_CONTACT,
    PRODUCER,
    DIRECTOR,
    EDITOR,
    SOUND_DESIGNER
}

@Serializable
enum class Permission {
    READ_PROJECT,
    EDIT_PROJECT,
    MANAGE_TEAM,
    APPROVE_CONTENT,
    MANAGE_BUDGET,
    ACCESS_AI_TOOLS,
    EXPORT_CONTENT,
    VIEW_ANALYTICS,
    ADMIN_ACCESS
}


enum class PermissionType {
    VIEW, COMMENT, EDIT, DELETE, PUBLISH, SHARE, MANAGE_USERS, ADMIN
}

@Serializable
data class ProjectBudget(
    val totalBudget: Float,
    val spentAmount: Float = 0f,
    val categories: Map<BudgetCategory, CategoryBudget>,
    val currency: String = "USD"
)

@Serializable
data class CategoryBudget(
    val allocated: Float,
    val spent: Float = 0f,
    val remaining: Float = allocated - spent
)

@Serializable
enum class BudgetCategory {
    TEAM_COSTS,
    AI_MODEL_USAGE,
    RENDERING_RESOURCES,
    PLATFORM_FEES,
    LICENSING,
    EQUIPMENT,
    MARKETING,
    MISCELLANEOUS
}

@Serializable
data class Deliverable(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String,
    val type: DeliverableType,
    val format: String,
    val duration: Float? = null, // in minutes
    val targetPlatforms: List<StreamingPlatform>,
    val status: DeliverableStatus,
    val dueDate: Instant,
    val deliveredDate: Instant? = null,
    val filePath: String? = null,
    val fileSize: Long? = null,
    val qualityMetrics: QualityMetrics? = null
)

@Serializable
enum class DeliverableType {
    SCRIPT,
    STORYBOARD,
    CHARACTER_DESIGN,
    ENVIRONMENT_DESIGN,
    VIDEO_CONTENT,
    AUDIO_TRACK,
    MARKETING_ASSET,
    THUMBNAIL,
    METADATA_PACKAGE,
    FINAL_EXPORT
}

@Serializable
enum class DeliverableStatus {
    PENDING,
    IN_PROGRESS,
    READY_FOR_REVIEW,
    IN_REVIEW,
    APPROVED,
    REQUIRES_REVISION,
    DELIVERED,
    REJECTED
}

@Serializable
data class QualityMetrics(
    val overallScore: Float, // 0.0 to 1.0
    val technicalQuality: Float,
    val creativityScore: Float,
    val platformCompliance: Float,
    val audienceEngagement: Float? = null,
    val aiConfidenceScore: Float? = null,
    val reviewNotes: List<String> = emptyList(),
    val narrativeCoherence: Float,
    val characterConsistency: Float,
    val dialogueQuality: Float,
    val pacingScore: Float,
    val visualComposition: Float?
)


@Serializable
data class StreamingPlatform(
    val id: String,
    val name: String,
    val type: PlatformType,
    val requirements: PlatformRequirements,
    val apiEndpoint: String? = null,
    val credentials: String? = null, // Encrypted
    val isActive: Boolean = true
)

@Serializable
enum class PlatformType {
    STREAMING_SERVICE,  // Netflix, Prime Video, Disney+
    SOCIAL_MEDIA,      // YouTube, TikTok, Instagram
    BROADCASTING,      // Traditional TV networks
    CORPORATE,         // Internal company platforms
    EDUCATIONAL,       // Learning platforms
    GAMING            // Game-integrated content
}

enum class PlatformName {
    YOUTUBE, INSTAGRAM, TIKTOK, FACEBOOK, TWITTER, LINKEDIN,
    VIMEO, TWITCH, SNAPCHAT, PINTEREST, REDDIT, DISCORD,
    THREADS, MASTODON, BLUESKY, CUSTOM
}

@Serializable
data class PlatformRequirements(
    val supportedFormats: List<String>,
    val maxFileSize: Long,
    val maxDuration: Float? = null,
    val minResolution: String,
    val maxResolution: String,
    val requiredMetadata: List<String>,
    val contentGuidelines: List<String> = emptyList(),
    val uploadSchedule: UploadSchedule? = null,
    val platform: SocialPlatform = SocialPlatform.NONE,
    val maxVideoSize: Long? = null,
    val maxImageSize: Long? = null,
    val supportedVideoFormats: List<String> = emptyList(),
    val supportedImageFormats: List<String> = emptyList(),
    val maxVideoDuration: Int? = null, // in seconds
    val minVideoDuration: Int? = null,
    val maxDescriptionLength: Int? = null,
    val maxTitleLength: Int? = null,
    val maxTags: Int? = null,
    val aspectRatios: List<AspectRatio> = emptyList(),
    val features: List<PlatformCapability> = emptyList()
)

@Serializable
data class UploadSchedule(
    val preferredTimeSlots: List<String>,
    val timezone: String,
    val advanceNoticeRequired: Int // hours
)

// Project Analytics and Performance
@Serializable
data class ProjectAnalytics(
    val projectId: String,
    val performanceMetrics: PerformanceMetrics,
    val costAnalysis: CostAnalysis,
    val timelineAnalysis: TimelineAnalysis,
    val qualityAnalysis: QualityAnalysis,
    val platformPerformance: Map<String, PlatformMetrics>
)

@Serializable
data class PerformanceMetrics(
    val totalViews: Long = 0,
    val engagementRate: Float = 0f,
    val completionRate: Float = 0f,
    val shareRate: Float = 0f,
    val likeRate: Float = 0f,
    val commentRate: Float = 0f,
    val revenueGenerated: Float = 0f
)

@Serializable
data class CostAnalysis(
    val costPerMinute: Float,
    val costPerView: Float,
    val budgetUtilization: Float,
    val costEfficiency: Float, // Compared to traditional production
    val aiCostBreakdown: Map<String, Float>,
    val teamCostBreakdown: Map<TeamRole, Float>
)

@Serializable
data class TimelineAnalysis(
    val plannedDuration: Float, // in days
    val actualDuration: Float,
    val timeEfficiency: Float,
    val phaseBreakdown: Map<ProductionPhase, Float>,
    val bottlenecks: List<String>,
    val accelerators: List<String>
)

@Serializable
data class QualityAnalysis(
    val averageQualityScore: Float,
    val qualityTrend: List<QualityDataPoint>,
    val platformApprovalRate: Float,
    val clientSatisfactionScore: Float,
    val aiEffectivenessScore: Float
)

@Serializable
data class QualityDataPoint(
    val timestamp: Instant,
    val score: Float,
    val phase: ProductionPhase
)

@Serializable
data class PlatformMetrics(
    val platformId: String,
    val uploadSuccess: Boolean,
    val processingTime: Float, // hours
    val approvalStatus: String,
    val performanceScore: Float,
    val audienceReach: Long,
    val engagementMetrics: PerformanceMetrics
)
