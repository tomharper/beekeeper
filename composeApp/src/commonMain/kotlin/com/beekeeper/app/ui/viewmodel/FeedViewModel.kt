package com.beekeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.data.repository.FeedRepository
import com.beekeeper.app.data.repository.FollowRepository
import com.beekeeper.app.domain.model.FeedItem
import com.beekeeper.app.domain.model.FollowUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FeedUiState(
    val feed: List<FeedItem> = emptyList(),
    val following: List<FollowUser> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<FollowUser> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null
)

private const val FEED_PAGE_SIZE = 20

class FeedViewModel(
    private val feedRepository: FeedRepository,
    private val followRepository: FollowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
        loadFollowing()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            feedRepository.getFeed(limit = FEED_PAGE_SIZE).fold(
                onSuccess = { feed ->
                    _uiState.value = _uiState.value.copy(feed = feed, isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load feed"
                    )
                }
            )
        }
    }

    /** Load the next page using the oldest currently-shown item as the cursor. */
    fun loadMore() {
        val current = _uiState.value.feed
        if (current.isEmpty() || _uiState.value.isLoading) return
        val cursor = current.last().occurredAt.toString()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            feedRepository.getFeed(limit = FEED_PAGE_SIZE, before = cursor).fold(
                onSuccess = { older ->
                    _uiState.value = _uiState.value.copy(
                        feed = current + older,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load more"
                    )
                }
            )
        }
    }

    fun loadFollowing() {
        viewModelScope.launch {
            followRepository.getFollowing().fold(
                onSuccess = { following ->
                    _uiState.value = _uiState.value.copy(following = following)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to load following"
                    )
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun search() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)

            followRepository.searchUsers(query).fold(
                onSuccess = { results ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = results,
                        isSearching = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = error.message ?: "Search failed"
                    )
                }
            )
        }
    }

    fun follow(userId: String) {
        viewModelScope.launch {
            followRepository.followUser(userId).fold(
                onSuccess = {
                    loadFollowing()
                    loadFeed()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to follow"
                    )
                }
            )
        }
    }

    fun unfollow(userId: String) {
        viewModelScope.launch {
            followRepository.unfollowUser(userId).fold(
                onSuccess = {
                    loadFollowing()
                    loadFeed()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to unfollow"
                    )
                }
            )
        }
    }

    fun isFollowing(userId: String): Boolean =
        _uiState.value.following.any { it.id == userId }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
