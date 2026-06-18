package com.beekeeper.app.data.repository

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.database.Database
import com.beekeeper.app.data.database.toDbHive
import com.beekeeper.app.data.database.toDomainHive
import com.beekeeper.app.domain.model.Hive

class HiveRepository(
    private val apiClient: ApiClient,
    private val database: Database
) {

    suspend fun getHives(apiaryId: String? = null): Result<List<Hive>> {
        return try {
            // Try to fetch from API
            val hives = apiClient.getHives(apiaryId)

            // Cache in database
            hives.forEach { hive ->
                database.hiveQueries.insertOrReplace(hive.toDbHive())
            }

            Result.success(hives)
        } catch (e: Exception) {
            // If API fails, try to return cached data
            try {
                val cachedHives = if (apiaryId != null) {
                    database.hiveQueries.selectByApiary(apiaryId).executeAsList()
                } else {
                    database.hiveQueries.selectAll().executeAsList()
                }
                Result.success(cachedHives.map { it.toDomainHive() })
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getHive(id: String): Result<Hive> {
        return try {
            // Try to fetch from API
            val hive = apiClient.getHive(id)

            // Cache in database
            database.hiveQueries.insertOrReplace(hive.toDbHive())

            Result.success(hive)
        } catch (e: Exception) {
            // If API fails, try to return cached data
            try {
                val cachedHive = database.hiveQueries.selectById(id).executeAsOneOrNull()
                if (cachedHive != null) {
                    Result.success(cachedHive.toDomainHive())
                } else {
                    Result.failure(e)
                }
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }
}
