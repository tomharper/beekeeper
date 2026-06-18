package com.beekeeper.app.data.repository

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.database.Database
import com.beekeeper.app.data.database.toDbApiary
import com.beekeeper.app.data.database.toDomainApiary
import com.beekeeper.app.domain.model.Apiary

class ApiaryRepository(
    private val apiClient: ApiClient,
    private val database: Database
) {

    suspend fun getApiaries(): Result<List<Apiary>> {
        return try {
            // Try to fetch from API
            val apiaries = apiClient.getApiaries()

            // Cache in database
            apiaries.forEach { apiary ->
                database.apiaryQueries.insertOrReplace(apiary.toDbApiary())
            }

            Result.success(apiaries)
        } catch (e: Exception) {
            // If API fails, try to return cached data
            try {
                val cachedApiaries = database.apiaryQueries.selectAll().executeAsList()
                Result.success(cachedApiaries.map { it.toDomainApiary() })
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getApiary(id: String): Result<Apiary> {
        return try {
            // Try to fetch from API
            val apiary = apiClient.getApiary(id)

            // Cache in database
            database.apiaryQueries.insertOrReplace(apiary.toDbApiary())

            Result.success(apiary)
        } catch (e: Exception) {
            // If API fails, try to return cached data
            try {
                val cachedApiary = database.apiaryQueries.selectById(id).executeAsOneOrNull()
                if (cachedApiary != null) {
                    Result.success(cachedApiary.toDomainApiary())
                } else {
                    Result.failure(e)
                }
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }
}
