package com.beekeeper.app.data.repository

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.api.CreateTaskRequest
import com.beekeeper.app.data.api.UpdateTaskRequest
import com.beekeeper.app.domain.model.Task
import com.beekeeper.app.domain.model.TaskStatus

class TaskRepository(private val apiClient: ApiClient) {

    suspend fun getTasks(
        status: TaskStatus? = null,
        hiveId: String? = null,
        apiaryId: String? = null,
        upcomingDays: Int? = null
    ): Result<List<Task>> {
        return try {
            Result.success(apiClient.getTasks(status, hiveId, apiaryId, upcomingDays))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingTasks(): Result<List<Task>> {
        return try {
            Result.success(apiClient.getPendingTasks())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOverdueTasks(): Result<List<Task>> {
        return try {
            Result.success(apiClient.getOverdueTasks())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTask(id: String): Result<Task> {
        return try {
            Result.success(apiClient.getTask(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTask(request: CreateTaskRequest): Result<Task> {
        return try {
            Result.success(apiClient.createTask(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(id: String, request: UpdateTaskRequest): Result<Task> {
        return try {
            Result.success(apiClient.updateTask(id, request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeTask(id: String): Result<Task> {
        return try {
            Result.success(apiClient.completeTask(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(id: String): Result<Unit> {
        return try {
            apiClient.deleteTask(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
