// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/PublishingRepository.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing publishing and export operations
 * ALL MODELS ARE IMPORTED FROM domain.model PACKAGE
 */
interface PublishingRepository {

    // Publishing Project Management
    suspend fun getPublishingProjects(): List<PublishingProject>
    suspend fun getPublishingProject(projectId: String): PublishingProject?
    suspend fun createPublishingProject(project: PublishingProject): PublishingProject
    suspend fun updatePublishingProject(project: PublishingProject): PublishingProject
    suspend fun deletePublishingProject(projectId: String): Boolean

    // Export Operations
    suspend fun getExportPresets(): List<ExportPreset>
    suspend fun createExportPreset(preset: ExportPreset): ExportPreset
    suspend fun exportProject(request: ExportRequest): ExportResult
    suspend fun getExportHistory(projectId: String): List<ExportHistory>
    suspend fun getExportStatus(exportId: String): ExportStatus
    suspend fun cancelExport(exportId: String): Boolean

    // Platform Publishing
    suspend fun getConnectedPlatforms(): List<ConnectedPlatform>
    suspend fun connectPlatform(connection: PlatformConnection): ConnectedPlatform
    suspend fun disconnectPlatform(platformId: String): Boolean
    suspend fun publishToPlatform(request: PublishRequest): PublishResult
    suspend fun getPublishingStatus(publishId: String): PublishStatus
    suspend fun schedulePublish(request: PublishRequest, schedule: PublishSchedule): String
    suspend fun getScheduledPosts(projectId: String): List<PublishSchedule>
    suspend fun cancelScheduledPost(scheduleId: String): Boolean

    // Collaboration and Sharing
    suspend fun createShareLink(projectId: String, settings: ShareSettings): ShareLink
    suspend fun getShareLinks(projectId: String): List<ShareLink>
    suspend fun revokeShareLink(linkId: String): Boolean
    suspend fun getCollaborators(projectId: String): List<Collaborator>
    suspend fun inviteCollaborator(projectId: String, email: String, role: CollaboratorRole): Collaborator
    suspend fun updateCollaboratorRole(collaboratorId: String, role: CollaboratorRole): Boolean
    suspend fun removeCollaborator(collaboratorId: String): Boolean

    // Analytics and Insights
    suspend fun getPublishingAnalytics(projectId: String, platformId: String? = null): PublishingAnalytics
    suspend fun getAudienceInsights(platformId: String): AudienceInsights
    suspend fun getScheduleAnalytics(projectId: String): ScheduleAnalytics

    // Real-time updates
    fun observePublishingProjects(): Flow<List<PublishingProject>>
    fun observeExportProgress(exportId: String): Flow<ExportResult>
    fun observePublishingStatus(publishId: String): Flow<PublishStatus>
}