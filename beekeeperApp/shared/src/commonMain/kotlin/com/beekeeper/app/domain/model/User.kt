//User.kt

package com.beekeeper.app.domain.model

import androidx.compose.ui.graphics.Color
import com.beekeeper.app.domain.ai.interfaces.AvatarServiceProvider
import com.beekeeper.app.domain.ai.interfaces.ScriptServiceProvider
import com.beekeeper.app.domain.ai.interfaces.VideoServiceProvider
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

enum class SubscriptionType(val displayName: String, val color: Color) {
    FREE("Free", Color(0xFF757575)),
    PRO("Pro", Color(0xFF7C3AED)),
    ENTERPRISE("Enterprise", Color(0xFFFFD700))
}


// Additional User model fields
@Serializable
data class User(
    val id: String,
    val username: String,
    val name: String = "Cinefiller Demo",
    val email: String = "dth@cinefiller.com",
    val role: String = "Content Creator",
    val joinDate: String = "Jan 2024",
    val customerAlias: String = "cinefiller-demo",
    val avatarUrl: String? = null,
    val subscription: SubscriptionType = SubscriptionType.PRO,
    val projectsCreated: Int = 42,
    val videosPublished: Int = 128,
    val totalViews: String = "1.2M",
    val storageUsed: String = "45.3 GB",
    val displayName: String,
    val bio: String? = null,
    val isVerified: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val enableNotifications: Boolean = true,
    val privateAccount: Boolean = false,
    val showAiContent: Boolean = true,
    val autoplayVideos: Boolean = true,
    val feedPreference: FeedType = FeedType.FOR_YOU
)


// Comment model (for repository)
@Serializable
data class Comment(
    val id: String,
    val postId: String,
    val author: FeedAuthor,
    val text: String,
    val timestamp: Instant,
    val likes: Int = 0,
    val hasLiked: Boolean = false,
    val parentCommentId: String? = null, // For replies
    val replies: List<Comment> = emptyList()
)
// Extension for UserPreferences (add to your preferences model)
data class UserPreferences(
    // Existing preferences...
    val claudeApiKey: String? = null,
    val bedrockConfig: BedrockConfigPrefs? = null,
    val heygenApiKey: String? = null,
    val didApiKey: String? = null,
    val synthesiaApiKey: String? = null,
    val preferredScriptProvider: ScriptServiceProvider = ScriptServiceProvider.CLAUDE,
    val preferredAvatarProvider: AvatarServiceProvider = AvatarServiceProvider.HEYGEN,
    val preferredVideoProvider: VideoServiceProvider = VideoServiceProvider.HEYGEN
)

data class BedrockConfigPrefs(
    val region: String,
    val accessKeyId: String,
    val secretAccessKey: String
)
