package com.beekeeper.app.data.repository

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.database.Database
import com.beekeeper.app.data.database.toDbFeedItem
import com.beekeeper.app.data.database.toDomain
import com.beekeeper.app.data.database.toDomainFeedItem
import com.beekeeper.app.domain.model.FeedItem
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class FeedRepository(
    private val apiClient: ApiClient,
    private val database: Database
) {

    /**
     * API-first read of the activity feed. On success the results are cached
     * into the FeedItem table; on failure we fall back to the cache (same
     * pattern as InspectionRepository). [before] is an occurredAt ISO cursor.
     */
    suspend fun getFeed(limit: Int = 20, before: String? = null): Result<List<FeedItem>> {
        return try {
            val dtos = apiClient.getFeed(limit, before)

            // First page (no cursor) replaces the cache; pages append.
            if (before == null) {
                database.feedItemQueries.deleteAll()
            }
            dtos.forEach { dto ->
                database.feedItemQueries.insertOrReplace(dto.toDbFeedItem())
            }

            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            // Fall back to cached data on error.
            try {
                val cached = if (before != null) {
                    // `before` is an ISO LocalDateTime cursor (occurredAt of last item seen).
                    val cursor = LocalDateTime.parse(before)
                        .toInstant(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds()
                    database.feedItemQueries.selectBefore(cursor).executeAsList()
                } else {
                    database.feedItemQueries.selectAll().executeAsList()
                }
                val result = cached.map { it.toDomainFeedItem() }.take(limit)
                Result.success(result)
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }
}
