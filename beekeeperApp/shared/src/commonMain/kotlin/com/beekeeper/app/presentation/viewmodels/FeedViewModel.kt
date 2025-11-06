// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/FeedViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

// UI State
data class FeedUiState(
    val forYouPosts: List<FeedPost> = emptyList(),
    val followingPosts: List<FeedPost> = emptyList(),
    val trendingPosts: List<FeedPost> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMoreForYou: Boolean = true,
    val hasMoreFollowing: Boolean = true,
    val hasMoreTrending: Boolean = true,
    val error: String? = null,
    val currentUserId: String = "",
    val unreadNotifications: Int = 0
)

// Events
sealed class FeedEvent {
    data class LikePost(val postId: String) : FeedEvent()
    data class UnlikePost(val postId: String) : FeedEvent()
    data class BookmarkPost(val postId: String) : FeedEvent()
    data class UnbookmarkPost(val postId: String) : FeedEvent()
    data class SharePost(val postId: String) : FeedEvent()
    data class FollowUser(val userId: String) : FeedEvent()
    data class UnfollowUser(val userId: String) : FeedEvent()
    data class DeletePost(val postId: String) : FeedEvent()
    data class ReportPost(val postId: String, val reason: String) : FeedEvent()
    object RefreshForYou : FeedEvent()
    object RefreshFollowing : FeedEvent()
    object RefreshTrending : FeedEvent()
    object LoadMoreForYou : FeedEvent()
    object LoadMoreFollowing : FeedEvent()
    object LoadMoreTrending : FeedEvent()
}

object feed {
    // Simple composable function to remember the ViewModel
    @Composable
    fun rememberFeedViewModel(): FeedViewModel {
        return viewModel {
            FeedViewModel(
                feedRepository = com.beekeeper.app.domain.repository.RepositoryManager.feedRepository
            )
        }
    }
}
// The actual ViewModel
class FeedViewModel(
    private val feedRepository: com.beekeeper.app.domain.repository.SQLDelightFeedRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var forYouPage = 0
    private var followingPage = 0
    private var trendingPage = 0
    private val pageSize = 20

    init {
        loadInitialData()
    }

    fun handleEvent(event: FeedEvent) {
        when (event) {
            is FeedEvent.LikePost -> likePost(event.postId)
            is FeedEvent.UnlikePost -> unlikePost(event.postId)
            is FeedEvent.BookmarkPost -> bookmarkPost(event.postId)
            is FeedEvent.UnbookmarkPost -> unbookmarkPost(event.postId)
            is FeedEvent.SharePost -> sharePost(event.postId)
            is FeedEvent.FollowUser -> followUser(event.userId)
            is FeedEvent.UnfollowUser -> unfollowUser(event.userId)
            is FeedEvent.DeletePost -> deletePost(event.postId)
            is FeedEvent.ReportPost -> reportPost(event.postId, event.reason)
            FeedEvent.RefreshForYou -> refreshForYou()
            FeedEvent.RefreshFollowing -> refreshFollowing()
            FeedEvent.RefreshTrending -> refreshTrending()
            FeedEvent.LoadMoreForYou -> loadMoreForYou()
            FeedEvent.LoadMoreFollowing -> loadMoreFollowing()
            FeedEvent.LoadMoreTrending -> loadMoreTrending()
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Try to load from database first
            val forYouPosts = if (feedRepository != null) {
                val dbPosts = feedRepository.getFeedPosts(FeedType.FOR_YOU, pageSize)
                // If database is empty, generate and save mock data
                if (dbPosts.isEmpty()) {
                    val mockPosts = generateMockPosts(0, pageSize, "for_you")
                    feedRepository.saveFeedPosts(mockPosts, FeedType.FOR_YOU)
                    mockPosts
                } else {
                    dbPosts
                }
            } else {
                generateMockPosts(0, pageSize, "for_you")
            }

            val followingPosts = if (feedRepository != null) {
                val dbPosts = feedRepository.getFeedPosts(FeedType.FOLLOWING, pageSize)
                if (dbPosts.isEmpty()) {
                    val mockPosts = generateMockPosts(0, pageSize, "following")
                    feedRepository.saveFeedPosts(mockPosts, FeedType.FOLLOWING)
                    mockPosts
                } else {
                    dbPosts
                }
            } else {
                generateMockPosts(0, pageSize, "following")
            }

            val trendingPosts = if (feedRepository != null) {
                val dbPosts = feedRepository.getFeedPosts(FeedType.TRENDING, pageSize)
                if (dbPosts.isEmpty()) {
                    val mockPosts = generateMockPosts(0, pageSize, "trending")
                    feedRepository.saveFeedPosts(mockPosts, FeedType.TRENDING)
                    mockPosts
                } else {
                    dbPosts
                }
            } else {
                generateMockPosts(0, pageSize, "trending")
            }

            _uiState.update {
                it.copy(
                    forYouPosts = forYouPosts,
                    followingPosts = followingPosts,
                    trendingPosts = trendingPosts,
                    isLoading = false,
                    currentUserId = "current_user",
                    unreadNotifications = 3
                )
            }
        }
    }

    private fun refreshForYou() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            forYouPage = 0
            val posts = generateMockPosts(0, pageSize, "for_you")
            _uiState.update {
                it.copy(
                    forYouPosts = posts,
                    isRefreshing = false
                )
            }
        }
    }

    private fun refreshFollowing() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            followingPage = 0
            val posts = generateMockPosts(0, pageSize, "following")
            _uiState.update {
                it.copy(
                    followingPosts = posts,
                    isRefreshing = false
                )
            }
        }
    }

    private fun refreshTrending() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            trendingPage = 0
            val posts = generateMockPosts(0, pageSize, "trending")
            _uiState.update {
                it.copy(
                    trendingPosts = posts,
                    isRefreshing = false
                )
            }
        }
    }

    private fun loadMoreForYou() {
        if (_uiState.value.hasMoreForYou && !_uiState.value.isLoading) {
            viewModelScope.launch {
                forYouPage++
                val morePosts = generateMockPosts(forYouPage * pageSize, pageSize, "for_you")
                _uiState.update {
                    it.copy(
                        forYouPosts = it.forYouPosts + morePosts,
                        hasMoreForYou = morePosts.size == pageSize
                    )
                }
            }
        }
    }

    private fun loadMoreFollowing() {
        if (_uiState.value.hasMoreFollowing && !_uiState.value.isLoading) {
            viewModelScope.launch {
                followingPage++
                val morePosts = generateMockPosts(followingPage * pageSize, pageSize, "following")
                _uiState.update {
                    it.copy(
                        followingPosts = it.followingPosts + morePosts,
                        hasMoreFollowing = morePosts.size == pageSize
                    )
                }
            }
        }
    }

    private fun loadMoreTrending() {
        if (_uiState.value.hasMoreTrending && !_uiState.value.isLoading) {
            viewModelScope.launch {
                trendingPage++
                val morePosts = generateMockPosts(trendingPage * pageSize, pageSize, "trending")
                _uiState.update {
                    it.copy(
                        trendingPosts = it.trendingPosts + morePosts,
                        hasMoreTrending = morePosts.size == pageSize
                    )
                }
            }
        }
    }

    private fun likePost(postId: String) {
        viewModelScope.launch {
            feedRepository?.likePost(postId)
        }
        updatePostInAllFeeds(postId) { post ->
            post.copy(
                hasBeenLiked = true,
                stats = post.stats.copy(likes = post.stats.likes + 1)
            )
        }
    }

    private fun unlikePost(postId: String) {
        viewModelScope.launch {
            feedRepository?.unlikePost(postId)
        }
        updatePostInAllFeeds(postId) { post ->
            post.copy(
                hasBeenLiked = false,
                stats = post.stats.copy(likes = post.stats.likes - 1)
            )
        }
    }

    private fun bookmarkPost(postId: String) {
        viewModelScope.launch {
            feedRepository?.bookmarkPost(postId)
        }
        updatePostInAllFeeds(postId) { post ->
            post.copy(hasBeenBookmarked = true)
        }
    }

    private fun unbookmarkPost(postId: String) {
        viewModelScope.launch {
            feedRepository?.unbookmarkPost(postId)
        }
        updatePostInAllFeeds(postId) { post ->
            post.copy(hasBeenBookmarked = false)
        }
    }

    private fun sharePost(postId: String) {
        viewModelScope.launch {
            feedRepository?.sharePost(postId)
        }
        updatePostInAllFeeds(postId) { post ->
            post.copy(stats = post.stats.copy(shares = post.stats.shares + 1))
        }
    }

    private fun followUser(userId: String) {
        viewModelScope.launch {
            feedRepository?.followUser(userId)
        }
        updateAuthorInAllFeeds(userId) { author ->
            author.copy(isFollowing = true)
        }
    }

    private fun unfollowUser(userId: String) {
        viewModelScope.launch {
            feedRepository?.unfollowUser(userId)
        }
        updateAuthorInAllFeeds(userId) { author ->
            author.copy(isFollowing = false)
        }
    }

    private fun deletePost(postId: String) {
        viewModelScope.launch {
            feedRepository?.deletePost(postId)
        }
        _uiState.update { state ->
            state.copy(
                forYouPosts = state.forYouPosts.filter { it.id != postId },
                followingPosts = state.followingPosts.filter { it.id != postId },
                trendingPosts = state.trendingPosts.filter { it.id != postId }
            )
        }
    }

    private fun reportPost(postId: String, reason: String) {
        // In a real app, this would send the report to the server
        deletePost(postId) // For now, just hide the post
    }

    private fun updatePostInAllFeeds(postId: String, update: (FeedPost) -> FeedPost) {
        _uiState.update { state ->
            state.copy(
                forYouPosts = state.forYouPosts.map { if (it.id == postId) update(it) else it },
                followingPosts = state.followingPosts.map { if (it.id == postId) update(it) else it },
                trendingPosts = state.trendingPosts.map { if (it.id == postId) update(it) else it }
            )
        }
    }

    private fun updateAuthorInAllFeeds(userId: String, update: (FeedAuthor) -> FeedAuthor) {
        _uiState.update { state ->
            state.copy(
                forYouPosts = state.forYouPosts.map { post ->
                    if (post.author.id == userId) {
                        post.copy(author = update(post.author))
                    } else post
                },
                followingPosts = state.followingPosts.map { post ->
                    if (post.author.id == userId) {
                        post.copy(author = update(post.author))
                    } else post
                },
                trendingPosts = state.trendingPosts.map { post ->
                    if (post.author.id == userId) {
                        post.copy(author = update(post.author))
                    } else post
                }
            )
        }
    }

    // Mock data generator
    private fun generateMockPosts(offset: Int, limit: Int, feedType: String): List<FeedPost> {
        val currentTime = Clock.System.now()
        val mockAuthors = listOf(
            FeedAuthor("user_1", "watanabefilmmaker", "Watanabe", null, true, false),
            FeedAuthor("user_2", "sarahcreates", "Sarah Martinez", null, false, true),
            FeedAuthor("user_3", "mikevfx", "Mike Johnson", null, true, false),
            FeedAuthor("user_4", "emilydirector", "Emily Wong", null, false, false),
            FeedAuthor("user_5", "cinefiller", "CineFiller", null, true, true)
        )

        val postTexts = listOf(
            "Just wrapped up an incredible shoot using AI-generated backgrounds. The future of filmmaking is here! 🎬",
            "Behind the scenes of our latest project. Can't wait to share the final cut with you all!",
            "Experimenting with new visual effects techniques. What do you think of this style?",
            "The power of AI in content creation never ceases to amaze me. Check out what we created in just 2 hours!",
            "Collaboration is key in this industry. Grateful for my amazing team! 🙏",
            "New tutorial dropping tomorrow on advanced character animation. Stay tuned!",
            "Sometimes the best shots happen by accident. This was completely unplanned but turned out perfect.",
            "Working on something special for next month. Here's a little sneak peek...",
            "Remember: story always comes first. Technology is just a tool to tell it better.",
            "Friday mood: Creating magic with CineFiller's latest features! ✨"
        )

        return (offset until (offset + limit).coerceAtMost(50)).map { index ->
            val author = mockAuthors[index % mockAuthors.size]
            val hasMedia = index % 2 == 0
            val hasProject = index % 4 == 0
            val isVideo = index % 3 == 0

            val stats = when (feedType) {
                "trending" -> PostStats(
                    likes = (1000..10000).random(),
                    comments = (50..500).random(),
                    shares = (10..200).random(),
                    views = (10000..100000).random()
                )
                else -> PostStats(
                    likes = (10..1000).random(),
                    comments = (0..50).random(),
                    shares = (0..20).random(),
                    views = (100..5000).random()
                )
            }

            FeedPost(
                id = "${feedType}_post_$index",
                author = author,
                content = FeedContent(
                    text = postTexts[index % postTexts.size],
                    media = if (hasMedia) {
                        if (isVideo) {
                            listOf(
                                MediaItem(
                                    id = "media_${index}_1",
                                    url = "mock_video_url",
                                    type = MediaType.VIDEO,
                                    aspectRatio = 16f/9f,
                                    duration = (30..300).random()
                                )
                            )
                        } else {
                            (1..(1..4).random()).map { i ->
                                MediaItem(
                                    id = "media_${index}_$i",
                                    url = "mock_image_url_$i",
                                    type = MediaType.IMAGE,
                                    aspectRatio = 1f
                                )
                            }
                        }
                    } else emptyList(),
                    projectReference = if (hasProject) {
                        ProjectReference(
                            projectId = "proj_$index",
                            projectName = listOf(
                                "The Digital Frontier",
                                "Urban Legends Series",
                                "AI Avatar Showcase",
                                "Cinematic Landscapes"
                            )[index % 4],
                            projectType = listOf("Short Film", "Web Series", "Demo Reel", "Art Project")[index % 4]
                        )
                    } else null,
                    aiGenerated = index % 5 == 0
                ),
                stats = stats,
                timestamp = currentTime - (index * 2).hours,
                isSponsored = index % 10 == 0 && author.id == "user_5",
                hasBeenLiked = index % 3 == 0,
                hasBeenBookmarked = index % 7 == 0
            )
        }
    }
}
