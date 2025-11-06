// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/DistributionRepository.kt
package com.beekeeper.app.domain.repository

import PublishableContent
import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Repository interface for managing distribution analytics and platform publishing
 * Superset of all distribution-related functionality
 */
interface DistributionRepository {
    // Analytics Operations
    suspend fun getDistributionAnalytics(projectId: String): DistributionAnalytics?
    suspend fun getTitleRevenue(titleId: String): TitleRevenue?
    suspend fun updateDistributionAnalytics(analytics: DistributionAnalytics): Boolean
    suspend fun exportAnalytics(
        projectId: String,
        analytics: DistributionAnalytics?,
        format: DocExportFormat
    ): DocExportResult
    fun observeDistributionAnalytics(projectId: String): Flow<DistributionAnalytics?>

    // Platform Management - Core methods
    suspend fun getAllPlatforms(): List<SocialPlatform>
    suspend fun getConnectedPlatformIds(projectId: String): Set<String>
    suspend fun getPlatformConnectionInfo(
        projectId: String,
        platform: SocialPlatform
    ): PlatformConnectionInfo?

    // Platform Connection Operations
    suspend fun connectPlatform(projectId: String, platform: SocialPlatform): Boolean
    suspend fun disconnectPlatform(projectId: String, platform: SocialPlatform): Boolean
    suspend fun updatePlatformConnection(
        projectId: String,
        platform: SocialPlatform,
        connectionInfo: PlatformConnectionInfo
    ): Boolean

    // Publishing Operations
    suspend fun publishContent(
        projectId: String,
        platform: SocialPlatform,
        contentId: String,
        title: String,
        description: String,
        mediaUrls: List<String>,
        tags: List<String> = emptyList(),
        metadata: Map<String, Any> = emptyMap()
    ): PublishResult

    suspend fun schedulePublish(
        projectId: String,
        platform: SocialPlatform,
        contentId: String,
        scheduledTime: Long,
        publishRequest: PublishRequest
    ): String

    suspend fun getScheduledPosts(projectId: String): List<ScheduledPost>
    suspend fun cancelScheduledPost(scheduleId: String): Boolean

    // Bulk Operations
    suspend fun publishToMultiplePlatforms(
        projectId: String,
        platforms: List<SocialPlatform>,
        contentId: String,
        title: String,
        description: String,
        mediaUrls: List<String>
    ): Map<SocialPlatform, PublishResult>

    // Platform-specific Features
    suspend fun getPlatformRequirements(platform: SocialPlatform): PlatformRequirements
    suspend fun validateContent(
        platform: SocialPlatform,
        content: PublishableContent
    ): ValidationResult

    // Sync and Migration
    suspend fun syncPlatformData(projectId: String, platform: SocialPlatform): Boolean
    suspend fun migratePlatformData(
        fromPlatform: SocialPlatform,
        toPlatform: SocialPlatform,
        projectId: String
    ): Boolean

    // Observables for real-time updates
    fun observeConnectedPlatforms(projectId: String): Flow<List<ConnectedPlatform>>
    fun observePublishingStatus(publishId: String): Flow<PublishStatus>
}

// Supporting data classes
data class PlatformConnectionInfo(
    val platformId: String,
    val platform: SocialPlatform,
    val username: String? = null,
    val accountId: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val followers: Int? = null,
    val following: Int? = null,
    val verified: Boolean = false,
    val lastPublished: Long? = null,
    val connectionDate: Long,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenExpiry: Long? = null,
    val permissions: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

data class DocExportResult(
    val success: Boolean,
    val exportId: String? = null,
    val filePath: String? = null,
    val downloadUrl: String? = null,
    val format: DocExportFormat,
    val sizeBytes: Long? = null,
    val error: String? = null,
    val exportedAt: Long? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class PublishResult(
    val success: Boolean,
    val publishId: String? = null,
    val publishedPlatforms: List<String> = emptyList(),
    val platform: SocialPlatform,
    val postUrl: String? = null,
    val postId: String? = null,
    val error: String? = null,
    val publishedAt: Long? = null,
    val metrics: PublishMetrics? = null,
    val message: String? = null,
)

data class PublishMetrics(
    val views: Long = 0,
    val likes: Long = 0,
    val comments: Long = 0,
    val shares: Long = 0,
    val engagement: Float = 0f,
    val reach: Long = 0,
    val impressions: Long = 0
)

data class ConnectedPlatform(
    val platform: SocialPlatform,
    val connectionInfo: PlatformConnectionInfo,
    val isActive: Boolean = true,
    val capabilities: List<PlatformCapability> = emptyList()
)

enum class PlatformCapability {
    VIDEO_UPLOAD,
    IMAGE_UPLOAD,
    STORY_UPLOAD,
    LIVE_STREAMING,
    SCHEDULING,
    ANALYTICS,
    COMMENTS,
    DIRECT_MESSAGING,
    REELS,
    SHORTS,
    CAROUSEL,
    THREADS,
    POLLS,
    MONETIZATION
}

@Serializable
data class PublishRequest(
    val title: String,
    val description: String,
    val mediaUrls: List<String>,
    val tags: List<String> = emptyList(),
    val visibility: ContentVisibility = ContentVisibility.PUBLIC,
    val allowComments: Boolean = true,
    val allowSharing: Boolean = true,
    val metadata: Map<String, String> = emptyMap() // Changed from Map<String, Any> for serialization
)

@Serializable
enum class ContentVisibility {
    PUBLIC,
    PRIVATE,
    UNLISTED,
    FOLLOWERS_ONLY,
    FRIENDS_ONLY
}

@Serializable
data class ScheduledPost(
    val scheduleId: String,
    val projectId: String,
    val platform: SocialPlatform,
    val contentId: String,
    val scheduledTime: Long,
    val publishRequest: PublishRequest,
    val status: ScheduleStatus,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
enum class ScheduleStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED,
    CANCELLED
}


@Serializable
data class ContentDimensions(
    val width: Int,
    val height: Int
)

@Serializable
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList(),
    val warnings: List<ValidationWarning> = emptyList(),
    val suggestions: List<String> = emptyList()
)

@Serializable
data class ValidationError(
    val field: String,
    val message: String,
    val code: String = "NA",
    val severity: ValidationSeverity = ValidationSeverity.ERROR
)

@Serializable
enum class ValidationSeverity {
    ERROR,
    WARNING,
    INFO
}
@Serializable
data class ValidationWarning(
    val field: String,
    val message: String,
    val severity: WarningSeverity
)

@Serializable
enum class WarningSeverity {
    LOW,
    MEDIUM,
    HIGH
}

@Serializable
enum class PublishStatus {
    PENDING,
    UPLOADING,
    PROCESSING,
    PUBLISHED,
    FAILED,
    CANCELLED
}

// Supporting data classes
data class PlatformConnection(
    val id: String,
    val platform: SocialPlatform,
    val name: String,
    val username: String,
    val followers: Int,
    val isConnected: Boolean,
    val lastPublished: Long?
)

