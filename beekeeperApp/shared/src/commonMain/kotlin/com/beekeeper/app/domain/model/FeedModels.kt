// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/FeedModels.kt
package com.beekeeper.app.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

// Feed Post Models (moved from FeedScreen for reusability)
@Serializable
data class FeedPost(
    val id: String,
    val author: FeedAuthor,
    val content: FeedContent,
    val stats: PostStats,
    val timestamp: Instant,
    val isSponsored: Boolean = false,
    val hasBeenLiked: Boolean = false,
    val hasBeenBookmarked: Boolean = false
)

@Serializable
data class FeedAuthor(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val isVerified: Boolean = false,
    val isFollowing: Boolean = false
)

@Serializable
data class FeedContent(
    val text: String? = null,
    val media: List<MediaItem> = emptyList(),
    val projectReference: ProjectReference? = null,
    val aiGenerated: Boolean = false
)

@Serializable
data class MediaItem(
    val id: String,
    val url: String,
    val type: MediaType,
    val thumbnailUrl: String? = null,
    val aspectRatio: Float = 1f,
    val duration: Int? = null // in seconds for videos
)

@Serializable
enum class MediaType {
    IMAGE, VIDEO, AVATAR, SCENE
}

@Serializable
data class ProjectReference(
    val projectId: String,
    val projectName: String,
    val projectType: String
)

@Serializable
data class PostStats(
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val views: Int
)

// Notification Models
@Serializable
data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val targetId: String, // ID of the post, user, or other entity
    val targetType: String, // "post", "user", "project", etc.
    val timestamp: Instant,
    val isRead: Boolean = false,
    val actor: FeedAuthor? = null, // Who triggered the notification
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    MENTION,
    SHARE,
    PROJECT_UPDATE,
    AI_GENERATION_COMPLETE,
    SYSTEM
}

// User Repository Extensions
interface UserRepository {
    suspend fun getCurrentUser(): User
    suspend fun getUser(userId: String): User?
    suspend fun followUser(targetUserId: String, currentUserId: String)
    suspend fun unfollowUser(targetUserId: String, currentUserId: String)
    suspend fun bookmarkPost(postId: String, userId: String)
    suspend fun unbookmarkPost(postId: String, userId: String)
    suspend fun reportContent(contentId: String, reason: String, reporterId: String)
    fun getUnreadNotificationCount(userId: String): Flow<Int>
    suspend fun getNotifications(userId: String, limit: Int = 20, offset: Int = 0): List<Notification>
    suspend fun markNotificationAsRead(notificationId: String)
    suspend fun markAllNotificationsAsRead(userId: String)
}

// Media Repository Extensions  
interface MediaRepository {
    suspend fun createPost(
        authorId: String,
        content: FeedContent,
        projectId: String? = null
    ): FeedPost
    
    suspend fun deletePost(postId: String)
    
    suspend fun getPost(postId: String): FeedPost?
    
    suspend fun getFeedPosts(
        feedType: FeedType,
        userId: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<FeedPost>
    
    suspend fun uploadMedia(
        file: ByteArray,
        fileName: String,
        mimeType: String
    ): MediaItem
}

@Serializable
enum class FeedType {
    FOR_YOU,
    FOLLOWING,
    TRENDING,
    USER_PROFILE,
    PROJECT_UPDATES
}

// Analytics Repository Extensions
interface AnalyticsRepository {
    suspend fun recordEngagement(
        contentId: String,
        engagementType: String,
        userId: String,
        metadata: Map<String, Any> = emptyMap()
    )
    
    suspend fun recordView(
        contentId: String,
        userId: String,
        duration: Long? = null
    )
    
    suspend fun getEngagementStats(contentId: String): PostStats
}
