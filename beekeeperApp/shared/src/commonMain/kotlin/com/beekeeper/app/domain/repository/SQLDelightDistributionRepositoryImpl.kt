// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/SQLDelightDistributionRepositoryImpl.kt
package com.beekeeper.app.domain.repository

import PublishableContent
import com.beekeeper.app.data.sqldelight.CineFillerDatabase
import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * SQLDelight-backed implementation of DistributionRepository
 * Persists:
 * - Distribution Analytics (distribution_analytics table)
 * - Platform Connections (platform_connection table)
 * - Scheduled Posts (scheduled_post table)
 *
 * Uses mock implementations for complex platform integration (OAuth, publishing APIs)
 */
class SQLDelightDistributionRepositoryImpl(
    private val database: CineFillerDatabase
) : DistributionRepository {

    // Fallback to in-memory implementation for complex features
    private val fallback = DistributionRepositoryImpl()

    private val analyticsFlow = MutableStateFlow<DistributionAnalytics?>(null)
    private val connectedPlatformsFlow = MutableStateFlow<List<ConnectedPlatform>>(emptyList())

    // ===== ANALYTICS OPERATIONS (using database) =====

    override suspend fun getDistributionAnalytics(projectId: String): DistributionAnalytics? {
        return try {
            database.distributionAnalyticsQueries.selectLatestByProjectId(projectId)
                .executeAsOneOrNull()
                ?.let { row ->
                    // Deserialize JSON to DistributionAnalytics
                    kotlinx.serialization.json.Json.decodeFromString(
                        DistributionAnalytics.serializer(),
                        row.analytics_data
                    )
                }
        } catch (e: Exception) {
            println("❌ Error loading distribution analytics: ${e.message}")
            null
        }
    }

    override suspend fun updateDistributionAnalytics(analytics: DistributionAnalytics): Boolean {
        return try {
            val json = kotlinx.serialization.json.Json.encodeToString(
                DistributionAnalytics.serializer(),
                analytics
            )

            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

            database.distributionAnalyticsQueries.insertOrReplace(
                id = "${analytics.projectId}_$now",
                project_id = analytics.projectId,
                analytics_data = json,
                period_start = analytics.periodStart,
                period_end = analytics.periodEnd,
                generated_at = analytics.generatedAt,
                total_revenue = analytics.totalRevenue.toDouble(),
                total_views = analytics.totalViews.toLong(),
                average_engagement = analytics.averageEngagement.toDouble()
            )

            analyticsFlow.value = analytics
            true
        } catch (e: Exception) {
            println("❌ Error updating distribution analytics: ${e.message}")
            false
        }
    }

    override fun observeDistributionAnalytics(projectId: String): Flow<DistributionAnalytics?> = flow {
        emit(getDistributionAnalytics(projectId))
        analyticsFlow.collect { analytics ->
            if (analytics?.projectId == projectId) {
                emit(analytics)
            }
        }
    }

    // ===== PLATFORM CONNECTION OPERATIONS (using database) =====

    override suspend fun getConnectedPlatformIds(projectId: String): Set<String> {
        return try {
            database.platformConnectionQueries.selectByProjectId(projectId)
                .executeAsList()
                .filter { it.connection_status == "ACTIVE" }
                .map { it.platform }
                .toSet()
        } catch (e: Exception) {
            println("❌ Error loading connected platforms: ${e.message}")
            emptySet()
        }
    }

    override suspend fun getPlatformConnectionInfo(
        projectId: String,
        platform: SocialPlatform
    ): PlatformConnectionInfo? {
        return try {
            database.platformConnectionQueries.selectByProjectAndPlatform(projectId, platform.name)
                .executeAsOneOrNull()
                ?.let { row ->
                    val connectionInfoJson = row.connection_info_json
                    val metadata = if (connectionInfoJson != null) {
                        try {
                            kotlinx.serialization.json.Json.decodeFromString<Map<String, String>>(connectionInfoJson)
                        } catch (e: Exception) {
                            emptyMap()
                        }
                    } else {
                        emptyMap()
                    }

                    PlatformConnectionInfo(
                        platformId = row.id,
                        platform = platform,
                        username = row.account_name,
                        accountId = row.account_id,
                        connectionDate = row.created_at,
                        accessToken = row.access_token,
                        refreshToken = row.refresh_token,
                        tokenExpiry = row.expires_at,
                        metadata = metadata
                    )
                }
        } catch (e: Exception) {
            println("❌ Error loading platform connection info: ${e.message}")
            null
        }
    }

    override suspend fun connectPlatform(projectId: String, platform: SocialPlatform): Boolean {
        return try {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

            database.platformConnectionQueries.insertOrReplace(
                id = "${projectId}_${platform.name}",
                project_id = projectId,
                platform = platform.name,
                account_name = null,
                account_id = null,
                access_token = "mock_token", // TODO: Real OAuth implementation
                refresh_token = null,
                token_type = "Bearer",
                expires_at = now + (3600 * 1000), // 1 hour
                scopes = "read,write",
                connection_status = "ACTIVE",
                last_sync_at = now,
                connection_info_json = null,
                created_at = now,
                updated_at = now
            )

            true
        } catch (e: Exception) {
            println("❌ Error connecting platform: ${e.message}")
            false
        }
    }

    override suspend fun disconnectPlatform(projectId: String, platform: SocialPlatform): Boolean {
        return try {
            database.platformConnectionQueries.deleteByProjectAndPlatform(projectId, platform.name)
            true
        } catch (e: Exception) {
            println("❌ Error disconnecting platform: ${e.message}")
            false
        }
    }

    override suspend fun updatePlatformConnection(
        projectId: String,
        platform: SocialPlatform,
        connectionInfo: PlatformConnectionInfo
    ): Boolean {
        return try {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val metadataJson = kotlinx.serialization.json.Json.encodeToString(
                kotlinx.serialization.serializer<Map<String, Any>>(),
                connectionInfo.metadata
            )

            database.platformConnectionQueries.insertOrReplace(
                id = connectionInfo.platformId,
                project_id = projectId,
                platform = platform.name,
                account_name = connectionInfo.username,
                account_id = connectionInfo.accountId,
                access_token = connectionInfo.accessToken ?: "mock_token",
                refresh_token = connectionInfo.refreshToken,
                token_type = "Bearer",
                expires_at = connectionInfo.tokenExpiry,
                scopes = connectionInfo.permissions.joinToString(","),
                connection_status = "ACTIVE",
                last_sync_at = now,
                connection_info_json = metadataJson,
                created_at = connectionInfo.connectionDate,
                updated_at = now
            )

            true
        } catch (e: Exception) {
            println("❌ Error updating platform connection: ${e.message}")
            false
        }
    }

    // ===== SCHEDULED POSTS (using database) =====

    override suspend fun schedulePublish(
        projectId: String,
        platform: SocialPlatform,
        contentId: String,
        scheduledTime: Long,
        publishRequest: PublishRequest
    ): String {
        val scheduleId = "scheduled_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}"

        try {
            val requestJson = kotlinx.serialization.json.Json.encodeToString(
                PublishRequest.serializer(),
                publishRequest
            )

            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

            database.scheduledPostQueries.insertOrReplace(
                id = scheduleId,
                project_id = projectId,
                platform = platform.name,
                content_id = contentId,
                title = publishRequest.title,
                description = publishRequest.description,
                scheduled_time = scheduledTime,
                publish_request_json = requestJson,
                status = "SCHEDULED",
                published_at = null,
                publish_result_json = null,
                error_message = null,
                retry_count = 0,
                created_at = now,
                updated_at = now
            )

            return scheduleId
        } catch (e: Exception) {
            println("❌ Error scheduling post: ${e.message}")
            return scheduleId
        }
    }

    override suspend fun getScheduledPosts(projectId: String): List<ScheduledPost> {
        return try {
            database.scheduledPostQueries.selectByProjectId(projectId)
                .executeAsList()
                .map { row ->
                    val publishRequest = kotlinx.serialization.json.Json.decodeFromString(
                        PublishRequest.serializer(),
                        row.publish_request_json
                    )

                    ScheduledPost(
                        scheduleId = row.id,
                        projectId = row.project_id,
                        platform = SocialPlatform.valueOf(row.platform),
                        contentId = row.content_id,
                        scheduledTime = row.scheduled_time,
                        publishRequest = publishRequest,
                        status = ScheduleStatus.valueOf(row.status),
                        createdAt = row.created_at,
                        updatedAt = row.updated_at
                    )
                }
        } catch (e: Exception) {
            println("❌ Error loading scheduled posts: ${e.message}")
            emptyList()
        }
    }

    override suspend fun cancelScheduledPost(scheduleId: String): Boolean {
        return try {
            database.scheduledPostQueries.deleteById(scheduleId)
            true
        } catch (e: Exception) {
            println("❌ Error canceling scheduled post: ${e.message}")
            false
        }
    }

    override fun observeConnectedPlatforms(projectId: String): Flow<List<ConnectedPlatform>> = flow {
        // Get current connected platforms from database
        val platforms = getConnectedPlatformIds(projectId).map { platformId ->
            val platform = SocialPlatform.valueOf(platformId)
            val info = getPlatformConnectionInfo(projectId, platform)

            ConnectedPlatform(
                platform = platform,
                connectionInfo = info ?: PlatformConnectionInfo(
                    platformId = platformId,
                    platform = platform,
                    connectionDate = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                ),
                isActive = true,
                capabilities = emptyList()
            )
        }

        emit(platforms)

        // Subscribe to updates
        connectedPlatformsFlow.collect { updated ->
            emit(updated.filter { it.connectionInfo.platformId.startsWith(projectId) })
        }
    }

    // ===== DELEGATED METHODS (using fallback for complex features) =====

    override suspend fun getTitleRevenue(titleId: String): TitleRevenue? {
        return fallback.getTitleRevenue(titleId)
    }

    override suspend fun exportAnalytics(
        projectId: String,
        analytics: DistributionAnalytics?,
        format: DocExportFormat
    ): DocExportResult {
        return fallback.exportAnalytics(projectId, analytics, format)
    }

    override suspend fun getAllPlatforms(): List<SocialPlatform> {
        return fallback.getAllPlatforms()
    }

    override suspend fun publishContent(
        projectId: String,
        platform: SocialPlatform,
        contentId: String,
        title: String,
        description: String,
        mediaUrls: List<String>,
        tags: List<String>,
        metadata: Map<String, Any>
    ): PublishResult {
        return fallback.publishContent(projectId, platform, contentId, title, description, mediaUrls, tags, metadata)
    }

    override suspend fun publishToMultiplePlatforms(
        projectId: String,
        platforms: List<SocialPlatform>,
        contentId: String,
        title: String,
        description: String,
        mediaUrls: List<String>
    ): Map<SocialPlatform, PublishResult> {
        return fallback.publishToMultiplePlatforms(projectId, platforms, contentId, title, description, mediaUrls)
    }

    override suspend fun getPlatformRequirements(platform: SocialPlatform): PlatformRequirements {
        return fallback.getPlatformRequirements(platform)
    }

    override suspend fun validateContent(
        platform: SocialPlatform,
        content: PublishableContent
    ): ValidationResult {
        return fallback.validateContent(platform, content)
    }

    override suspend fun syncPlatformData(projectId: String, platform: SocialPlatform): Boolean {
        return fallback.syncPlatformData(projectId, platform)
    }

    override suspend fun migratePlatformData(
        fromPlatform: SocialPlatform,
        toPlatform: SocialPlatform,
        projectId: String
    ): Boolean {
        return fallback.migratePlatformData(fromPlatform, toPlatform, projectId)
    }

    override fun observePublishingStatus(publishId: String): Flow<PublishStatus> {
        return fallback.observePublishingStatus(publishId)
    }
}
