package com.beekeeper.app.data.repository

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.api.CreateInspectionRequest
import com.beekeeper.app.data.api.UpdateInspectionRequest
import com.beekeeper.app.data.database.Database
import com.beekeeper.app.data.database.toDbInspection
import com.beekeeper.app.data.database.toDomainInspection
import com.beekeeper.app.domain.model.Inspection

class InspectionRepository(
    private val apiClient: ApiClient,
    private val database: Database
) {

    suspend fun getInspections(
        hiveId: String? = null,
        limit: Int? = null
    ): Result<List<Inspection>> {
        return try {
            // Try to fetch from API
            val inspections = apiClient.getInspections(hiveId, limit)

            // Cache in database
            inspections.forEach { inspection ->
                database.inspectionQueries.insertOrReplace(inspection.toDbInspection())
            }

            Result.success(inspections)
        } catch (e: Exception) {
            // If API fails, try to return cached data
            try {
                val cachedInspections = if (hiveId != null) {
                    database.inspectionQueries.selectByHive(hiveId).executeAsList()
                } else {
                    database.inspectionQueries.selectAll().executeAsList()
                }

                val result = cachedInspections.map { it.toDomainInspection() }
                    .let { if (limit != null) it.take(limit) else it }

                Result.success(result)
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getInspection(id: String): Result<Inspection> {
        return try {
            // Try to fetch from API
            val inspection = apiClient.getInspection(id)

            // Cache in database
            database.inspectionQueries.insertOrReplace(inspection.toDbInspection())

            Result.success(inspection)
        } catch (e: Exception) {
            // If API fails, try to return cached data
            try {
                val cachedInspection = database.inspectionQueries.selectById(id).executeAsOneOrNull()
                if (cachedInspection != null) {
                    Result.success(cachedInspection.toDomainInspection())
                } else {
                    Result.failure(e)
                }
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getRecentInspections(limit: Int = 10): Result<List<Inspection>> {
        return try {
            // Try to fetch from API
            val inspections = apiClient.getRecentInspections(limit)

            // Cache in database
            inspections.forEach { inspection ->
                database.inspectionQueries.insertOrReplace(inspection.toDbInspection())
            }

            Result.success(inspections)
        } catch (e: Exception) {
            // If API fails, try to return cached data
            try {
                val cachedInspections = database.inspectionQueries.selectRecent(limit.toLong()).executeAsList()
                Result.success(cachedInspections.map { it.toDomainInspection() })
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getLatestHiveInspection(hiveId: String): Result<Inspection> {
        return try {
            // Try to fetch from API
            val inspection = apiClient.getLatestHiveInspection(hiveId)

            // Cache in database
            database.inspectionQueries.insertOrReplace(inspection.toDbInspection())

            Result.success(inspection)
        } catch (e: Exception) {
            // If API fails, try to return cached data
            try {
                val cachedInspection = database.inspectionQueries.selectLatestByHive(hiveId).executeAsOneOrNull()
                if (cachedInspection != null) {
                    Result.success(cachedInspection.toDomainInspection())
                } else {
                    Result.failure(e)
                }
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createInspection(request: CreateInspectionRequest): Result<Inspection> {
        return try {
            // Try to create via API
            val inspection = apiClient.createInspection(request)

            // Cache in database
            database.inspectionQueries.insertOrReplace(inspection.toDbInspection())

            Result.success(inspection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInspection(id: String, request: UpdateInspectionRequest): Result<Inspection> {
        return try {
            // Try to update via API
            val inspection = apiClient.updateInspection(id, request)

            // Update cache in database
            database.inspectionQueries.insertOrReplace(inspection.toDbInspection())

            Result.success(inspection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInspection(id: String): Result<Unit> {
        return try {
            // Try to delete via API
            apiClient.deleteInspection(id)

            // Delete from cache
            database.inspectionQueries.deleteById(id)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
