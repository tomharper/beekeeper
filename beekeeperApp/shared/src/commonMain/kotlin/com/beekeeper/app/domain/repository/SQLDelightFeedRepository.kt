// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/SQLDelightFeedRepository.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.data.sqldelight.CineFillerDatabase
import com.beekeeper.app.domain.model.*
import kotlinx.datetime.Instant

/**
 * SQLDelight-backed Feed Repository
 * Persists feed posts, media, and user interactions
 */
class SQLDelightFeedRepository(
    private val database: CineFillerDatabase,
    private val currentUserId: String = "current_user"
) {

    // ===== FEED POST OPERATIONS =====

    suspend fun saveFeedPost(post: FeedPost, feedType: FeedType) {
        try {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

            // Save post
            database.feedPostQueries.insertOrReplace(
                id = post.id,
                author_id = post.author.id,
                author_username = post.author.username,
                author_display_name = post.author.displayName,
                author_avatar_url = post.author.avatarUrl,
                author_is_verified = if (post.author.isVerified) 1 else 0,
                author_is_following = if (post.author.isFollowing) 1 else 0,
                content_text = post.content.text,
                content_project_id = post.content.projectReference?.projectId,
                content_project_name = post.content.projectReference?.projectName,
                content_project_type = post.content.projectReference?.projectType,
                content_ai_generated = if (post.content.aiGenerated) 1 else 0,
                stats_likes = post.stats.likes.toLong(),
                stats_comments = post.stats.comments.toLong(),
                stats_shares = post.stats.shares.toLong(),
                stats_views = post.stats.views.toLong(),
                timestamp = post.timestamp.toEpochMilliseconds(),
                is_sponsored = if (post.isSponsored) 1 else 0,
                has_been_liked = if (post.hasBeenLiked) 1 else 0,
                has_been_bookmarked = if (post.hasBeenBookmarked) 1 else 0,
                feed_type = feedType.name,
                created_at = now
            )

            // Save media
            post.content.media.forEachIndexed { index, media ->
                database.feedMediaQueries.insertOrReplace(
                    id = media.id,
                    post_id = post.id,
                    url = media.url,
                    type = media.type.name,
                    thumbnail_url = media.thumbnailUrl,
                    aspect_ratio = media.aspectRatio.toDouble(),
                    duration = media.duration?.toLong(),
                    display_order = index.toLong()
                )
            }
        } catch (e: Exception) {
            println("❌ Error saving feed post: ${e.message}")
        }
    }

    suspend fun saveFeedPosts(posts: List<FeedPost>, feedType: FeedType) {
        posts.forEach { saveFeedPost(it, feedType) }
    }

    suspend fun getFeedPosts(feedType: FeedType, limit: Int = 20): List<FeedPost> {
        return try {
            database.feedPostQueries.selectByFeedType(feedType.name, limit.toLong())
                .executeAsList()
                .map { row ->
                    val media = database.feedMediaQueries.selectByPostId(row.id)
                        .executeAsList()
                        .map { mediaRow ->
                            MediaItem(
                                id = mediaRow.id,
                                url = mediaRow.url,
                                type = MediaType.valueOf(mediaRow.type),
                                thumbnailUrl = mediaRow.thumbnail_url,
                                aspectRatio = mediaRow.aspect_ratio.toFloat(),
                                duration = mediaRow.duration?.toInt()
                            )
                        }

                    FeedPost(
                        id = row.id,
                        author = FeedAuthor(
                            id = row.author_id,
                            username = row.author_username,
                            displayName = row.author_display_name,
                            avatarUrl = row.author_avatar_url,
                            isVerified = row.author_is_verified == 1L,
                            isFollowing = row.author_is_following == 1L
                        ),
                        content = FeedContent(
                            text = row.content_text,
                            media = media,
                            projectReference = if (row.content_project_id != null) {
                                ProjectReference(
                                    projectId = row.content_project_id,
                                    projectName = row.content_project_name ?: "",
                                    projectType = row.content_project_type ?: ""
                                )
                            } else null,
                            aiGenerated = row.content_ai_generated == 1L
                        ),
                        stats = PostStats(
                            likes = row.stats_likes.toInt(),
                            comments = row.stats_comments.toInt(),
                            shares = row.stats_shares.toInt(),
                            views = row.stats_views.toInt()
                        ),
                        timestamp = Instant.fromEpochMilliseconds(row.timestamp),
                        isSponsored = row.is_sponsored == 1L,
                        hasBeenLiked = row.has_been_liked == 1L,
                        hasBeenBookmarked = row.has_been_bookmarked == 1L
                    )
                }
        } catch (e: Exception) {
            println("❌ Error loading feed posts: ${e.message}")
            emptyList()
        }
    }

    // ===== USER INTERACTION OPERATIONS =====

    suspend fun likePost(postId: String) {
        try {
            // Update interaction table
            val interactionId = "like_${currentUserId}_$postId"
            database.userInteractionQueries.insertOrIgnore(
                id = interactionId,
                user_id = currentUserId,
                target_id = postId,
                interaction_type = "LIKE",
                created_at = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            )

            // Update post stats and like status
            val post = database.feedPostQueries.selectById(postId).executeAsOneOrNull()
            if (post != null) {
                database.feedPostQueries.updateStats(
                    stats_likes = post.stats_likes + 1,
                    stats_comments = post.stats_comments,
                    stats_shares = post.stats_shares,
                    stats_views = post.stats_views,
                    id = postId
                )
                database.feedPostQueries.updateInteraction(
                    has_been_liked = 1,
                    has_been_bookmarked = post.has_been_bookmarked,
                    id = postId
                )
            }
        } catch (e: Exception) {
            println("❌ Error liking post: ${e.message}")
        }
    }

    suspend fun unlikePost(postId: String) {
        try {
            // Remove interaction
            database.userInteractionQueries.delete(
                user_id = currentUserId,
                target_id = postId,
                interaction_type = "LIKE"
            )

            // Update post stats and like status
            val post = database.feedPostQueries.selectById(postId).executeAsOneOrNull()
            if (post != null && post.stats_likes > 0) {
                database.feedPostQueries.updateStats(
                    stats_likes = post.stats_likes - 1,
                    stats_comments = post.stats_comments,
                    stats_shares = post.stats_shares,
                    stats_views = post.stats_views,
                    id = postId
                )
                database.feedPostQueries.updateInteraction(
                    has_been_liked = 0,
                    has_been_bookmarked = post.has_been_bookmarked,
                    id = postId
                )
            }
        } catch (e: Exception) {
            println("❌ Error unliking post: ${e.message}")
        }
    }

    suspend fun bookmarkPost(postId: String) {
        try {
            val interactionId = "bookmark_${currentUserId}_$postId"
            database.userInteractionQueries.insertOrIgnore(
                id = interactionId,
                user_id = currentUserId,
                target_id = postId,
                interaction_type = "BOOKMARK",
                created_at = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            )

            database.feedPostQueries.selectById(postId).executeAsOneOrNull()?.let { post ->
                database.feedPostQueries.updateInteraction(
                    has_been_liked = post.has_been_liked,
                    has_been_bookmarked = 1,
                    id = postId
                )
            }
        } catch (e: Exception) {
            println("❌ Error bookmarking post: ${e.message}")
        }
    }

    suspend fun unbookmarkPost(postId: String) {
        try {
            database.userInteractionQueries.delete(
                user_id = currentUserId,
                target_id = postId,
                interaction_type = "BOOKMARK"
            )

            database.feedPostQueries.selectById(postId).executeAsOneOrNull()?.let { post ->
                database.feedPostQueries.updateInteraction(
                    has_been_liked = post.has_been_liked,
                    has_been_bookmarked = 0,
                    id = postId
                )
            }
        } catch (e: Exception) {
            println("❌ Error unbookmarking post: ${e.message}")
        }
    }

    suspend fun followUser(userId: String) {
        try {
            val interactionId = "follow_${currentUserId}_$userId"
            database.userInteractionQueries.insertOrIgnore(
                id = interactionId,
                user_id = currentUserId,
                target_id = userId,
                interaction_type = "FOLLOW",
                created_at = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            )

            // Update all posts by this author
            database.feedPostQueries.updateAuthorFollowing(
                author_is_following = 1,
                author_id = userId
            )
        } catch (e: Exception) {
            println("❌ Error following user: ${e.message}")
        }
    }

    suspend fun unfollowUser(userId: String) {
        try {
            database.userInteractionQueries.delete(
                user_id = currentUserId,
                target_id = userId,
                interaction_type = "FOLLOW"
            )

            // Update all posts by this author
            database.feedPostQueries.updateAuthorFollowing(
                author_is_following = 0,
                author_id = userId
            )
        } catch (e: Exception) {
            println("❌ Error unfollowing user: ${e.message}")
        }
    }

    suspend fun deletePost(postId: String) {
        try {
            database.feedMediaQueries.deleteByPostId(postId)
            database.feedPostQueries.deleteById(postId)
        } catch (e: Exception) {
            println("❌ Error deleting post: ${e.message}")
        }
    }

    suspend fun sharePost(postId: String) {
        try {
            val post = database.feedPostQueries.selectById(postId).executeAsOneOrNull()
            if (post != null) {
                database.feedPostQueries.updateStats(
                    stats_likes = post.stats_likes,
                    stats_comments = post.stats_comments,
                    stats_shares = post.stats_shares + 1,
                    stats_views = post.stats_views,
                    id = postId
                )
            }
        } catch (e: Exception) {
            println("❌ Error sharing post: ${e.message}")
        }
    }

    // ===== CLEANUP =====

    suspend fun deleteOldPosts(olderThanMillis: Long) {
        try {
            database.feedPostQueries.deleteOldPosts(olderThanMillis)
        } catch (e: Exception) {
            println("❌ Error deleting old posts: ${e.message}")
        }
    }
}
