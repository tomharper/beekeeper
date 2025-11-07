package com.beekeeper.app.data.repository

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.api.CreateInspectionRequest
import com.beekeeper.app.data.api.UpdateInspectionRequest
import com.beekeeper.app.data.database.Database
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
            Result.success(apiClient.getInspections(hiveId, limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInspection(id: String): Result<Inspection> {
        return try {
            Result.success(apiClient.getInspection(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentInspections(limit: Int = 10): Result<List<Inspection>> {
        return try {
            Result.success(apiClient.getRecentInspections(limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLatestHiveInspection(hiveId: String): Result<Inspection> {
        return try {
            Result.success(apiClient.getLatestHiveInspection(hiveId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createInspection(request: CreateInspectionRequest): Result<Inspection> {
        return try {
            Result.success(apiClient.createInspection(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInspection(id: String, request: UpdateInspectionRequest): Result<Inspection> {
        return try {
            Result.success(apiClient.updateInspection(id, request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInspection(id: String): Result<Unit> {
        return try {
            apiClient.deleteInspection(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
