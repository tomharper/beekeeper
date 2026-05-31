package com.beekeeper.app.data.repository

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.domain.model.FollowUser

/**
 * Follow graph access. Follow state lives server-side (small/known audience),
 * so this is a thin passthrough over the API with no local cache.
 */
class FollowRepository(
    private val apiClient: ApiClient
) {

    suspend fun getFollowing(): Result<List<FollowUser>> {
        return try {
            val users = apiClient.getFollowing().map { FollowUser(it.id, it.fullName) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<FollowUser>> {
        return try {
            val users = apiClient.searchUsers(query).map { FollowUser(it.id, it.fullName) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun followUser(id: String): Result<Unit> {
        return try {
            apiClient.followUser(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(id: String): Result<Unit> {
        return try {
            apiClient.unfollowUser(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
