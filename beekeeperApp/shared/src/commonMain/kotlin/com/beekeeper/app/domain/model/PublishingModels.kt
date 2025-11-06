// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/PublishingModels.kt
package com.beekeeper.app.domain.model

import com.beekeeper.app.domain.project.CompressionSettings
import kotlinx.serialization.Serializable

/**
 * Unified domain models for Publishing Repository
 * Superset of all publishing-related models
 */

// Publishing Project Models
@Serializable
data class PublishingProject(
    val id: String,
    val title: String,
    val description: String,
    val contentIds: List<String>,
    val platforms: List<PlatformConfig>,
    val exportSettings: ExportSettings,
    val metadata: ProjectMetadata,
    val status: PublishingStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val publishedAt: Long?
)

@Serializable
data class PlatformConfig(
    val platformId: String,
    val platformName: String,
    val settings: Map<String, String?>,
    val isEnabled: Boolean
)
@Serializable

data class ExportSettings(
    val format: ExportFormat,
    val quality: QualityLevel,
    val resolution: String?,
    val frameRate: Int?,
    val audioSettings: AudioExportSettings?,
    val watermark: WatermarkSettings?,
    val compression: CompressionSettings?
)

@Serializable
data class ExportPreset(
    val id: String,
    val name: String,
    val description: String,
    val format: ExportFormat,
    val settings: ExportSettings,
    val platforms: List<String>,
    val isCustom: Boolean
)

@Serializable
data class ExportRequest(
    val projectId: String,
    val contentIds: List<String>,
    val presetId: String?,
    val customSettings: ExportSettings?,
    val destination: ExportDestination
)

@Serializable
data class DocExportResult(
    val exportId: String,
    val status: ExportStatus,
    val format: DocExportFormat,
    val outputFiles: List<OutputFile>,
    val duration: Long,
    val errors: List<String>?,
    val downloadUrl: String? = null,
    val filePath: String? = null,
    val fileSize: Long? = null,
    val recordCount: Int? = null,
    val message: String? = null
)

@Serializable
data class ExportResult(
    val success: Boolean,
    val format: com.beekeeper.app.presentation.viewmodels.ExportFormat,
    val downloadUrl: String? = null,
    val filePath: String? = null,
    val fileSize: Long? = null,
    val recordCount: Int? = null,
    val message: String? = null
)

@Serializable
data class ExportHistory(
    val id: String,
    val projectId: String,
    val exportDate: Long,
    val format: ExportFormat,
    val size: Long,
    val downloadUrl: String?,
    val expiresAt: Long?
)

@Serializable
data class OutputFile(
    val filename: String,
    val url: String,
    val size: Long,
    val format: String
)


@Serializable
data class ConnectedPlatform(
    val id: String,
    val type: PlatformType,
    val name: PlatformName,
    val icon: String,
    val accountName: String,
    val isConnected: Boolean,
    val capabilities: List<PlatformCapability>,
    val lastSync: Long?
)

@Serializable
data class PlatformConnection(
    val platformType: PlatformType,
    val name: PlatformName,
    val credentials: Map<String, String>,
    val permissions: List<String>
)

data class PublishRequest(
    val projectId: String,
    val platformId: String,
    val content: PublishContent,
    val settings: PublishSettings,
    val schedule: PublishSchedule?
)

@Serializable
data class PublishContent(
    val title: String,
    val description: String,
    val files: List<String>,
    val thumbnail: String?,
    val tags: List<String>,
    val category: String?
)

@Serializable
data class PublishSettings(
    val privacy: PrivacyLevel,
    val comments: Boolean,
    val notifications: Boolean,
    val monetization: MonetizationSettings?,
    val customSettings: Map<String, String>
)

@Serializable
data class PublishResult(
    val publishId: String,
    val platformId: String,
    val status: PublishStatus,
    val url: String?,
    val platformPostId: String?,
    val errors: List<String>?
)

@Serializable
data class PublishStatus(
    val publishId: String,
    val status: PublishingStatus,
    val progress: Float,
    val message: String?,
    val completedAt: Long?
)

@Serializable
data class PublishSchedule(
    val scheduledAt: Long,
    val timezone: String,
    val repeat: RepeatInterval?
)


@Serializable
data class ShareLink(
    val id: String,
    val projectId: String,
    val url: String,
    val type: ShareType,
    val expiresAt: Long?,
    val password: String?,
    val viewCount: Int,
    val createdAt: Long
)

@Serializable
data class Collaborator(
    val id: String,
    val userId: String,
    val projectId: String,
    val name: String,
    val email: String,
    val role: CollaboratorRole,
    val permissions: List<Permission>,
    val invitedAt: Long,
    val joinedAt: Long?
)

@Serializable
data class PublishingAnalytics(
    val projectId: String,
    val platformId: String,
    val period: String,
    val views: Long,
    val likes: Long,
    val shares: Long,
    val comments: Long,
    val engagement: Float,
    val reach: Long,
    val impressions: Long,
    val ctr: Float,
    val demographics: Demographics?,
    val topContent: List<ContentMetric>,
    val growthRate: Float,
    val trends: List<TrendPoint>
)

@Serializable
data class AudienceInsights(
    val totalFollowers: Long,
    val newFollowers: Long,
    val demographics: Demographics,
    val interests: List<Interest>,
    val peakTimes: List<PeakTime>,
    val geographic: List<Geographic>
)

@Serializable
data class ScheduleAnalytics(
    val scheduledPosts: Int,
    val publishedPosts: Int,
    val failedPosts: Int,
    val avgEngagement: Float,
    val bestTimes: List<String>,
    val performanceByDay: Map<String, Float>
)


@Serializable
data class AudioExportSettings(
    val codec: String,
    val bitrate: Int,
    val sampleRate: Int,
    val channels: Int
)

@Serializable
data class WatermarkSettings(
    val imageUrl: String,
    val position: WatermarkPosition,
    val opacity: Float,
    val size: Float
)

@Serializable
data class MonetizationSettings(
    val enabled: Boolean,
    val adBreaks: List<AdBreak>?,
    val sponsorships: List<Sponsorship>?
)

@Serializable
data class ContentMetric(
    val contentId: String,
    val title: String,
    val metric: Long
)

@Serializable
data class GrowthPoint(
    val date: Long,
    val value: Long
)

@Serializable
data class TrendPoint(
    val timestamp: Long,
    val value: Float
)

@Serializable
data class Demographics(
    val ageGroups: Map<String, Float>,
    val genderDistribution: Map<String, Float>
)

@Serializable
data class Interest(
    val category: String,
    val percentage: Float
)

@Serializable
data class PeakTime(
    val dayOfWeek: String,
    val hour: Int,
    val engagement: Float
)

@Serializable
data class Geographic(
    val country: String,
    val percentage: Float
)
@Serializable
data class AdBreak(
    val timestamp: Long,
    val duration: Int
)

@Serializable
data class Sponsorship(
    val sponsor: String,
    val placement: String
)

@Serializable
enum class DocExportFormat {
    // Document formats
    PDF, DOCX, TXT, EXCEL,
    // Data formats
    HTML, JSON, XML, CSV,
}


@Serializable
enum class ExportFormat {
    // Video formats
    MP4, MOV, AVI, MKV, WEBM, WMV, FLV, M4V,
    // Image formats
    GIF, PNG, JPG, JPEG, WEBP, BMP, TIFF,
    // Audio formats
    MP3, WAV, AAC, OGG, FLAC
}

@Serializable
enum class QualityLevel {
    LOW, MEDIUM, HIGH, ULTRA, CUSTOM
}

@Serializable
enum class ExportDestination {
    LOCAL, CLOUD, PLATFORM, EMAIL, FTP, SFTP
}

@Serializable
enum class ExportStatus {
    QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED, PAUSED
}

@Serializable
enum class PlatformCapability {
    VIDEO, IMAGE, STORY, LIVE, SHORTS, REELS, CAROUSEL,
    ARTICLE, POLL, THREAD, SPACE, FLEET
}

@Serializable
enum class PublishingStatus {
    DRAFT, SCHEDULED, IN_PROGRESS, PUBLISHING, PUBLISHED, FAILED, REMOVED, PAUSED
}

@Serializable
enum class PrivacyLevel {
    PUBLIC, UNLISTED, PRIVATE, FOLLOWERS_ONLY, FRIENDS_ONLY, CUSTOM
}

@Serializable
enum class ScheduleStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
}


@Serializable
enum class SocialPlatform {
    // Major platforms
    FACEBOOK, INSTAGRAM, TWITTER, LINKEDIN, TIKTOK, YOUTUBE, YOUTUBE_SHORTS,
    // Additional platforms
    PINTEREST, REDDIT, SNAPCHAT, DISCORD, TWITCH, VIMEO,
    // Newer platforms
    THREADS, MASTODON, BLUESKY, BEREAL, CLUBHOUSE,
    // Regional platforms
    WECHAT, WEIBO, LINE, TELEGRAM, WHATSAPP,
    // Custom
    CUSTOM, NONE, MEDIUM, SUBSTACK, PATREON, ONLYFANS
}

@Serializable
enum class ShareType {
    VIEW_ONLY, COMMENT, EDIT, FULL_ACCESS
}

@Serializable
enum class CollaboratorRole {
    VIEWER, COMMENTER, EDITOR, ADMIN, OWNER
}

@Serializable
enum class RepeatInterval {
    HOURLY, DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY
}

@Serializable
enum class WatermarkPosition {
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    MIDDLE_LEFT, CENTER, MIDDLE_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
}