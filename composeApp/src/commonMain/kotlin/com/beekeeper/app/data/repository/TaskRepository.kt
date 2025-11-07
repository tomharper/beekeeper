package com.beekeeper.app.data.repository

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.api.CreateTaskRequest
import com.beekeeper.app.data.api.UpdateTaskRequest
import com.beekeeper.app.data.database.Database
import com.beekeeper.app.data.database.toDbTask
import com.beekeeper.app.data.database.toDomainTask
import com.beekeeper.app.domain.model.Task
import com.beekeeper.app.domain.model.TaskStatus

class TaskRepository(
    private val apiClient: ApiClient,
    private val database: Database
) {

    suspend fun getTasks(
        status: TaskStatus? = null,
        hiveId: String? = null,
        apiaryId: String? = null,
        upcomingDays: Int? = null
    ): Result<List<Task>> {
        return try {
            // Try to fetch from API
            val tasks = apiClient.getTasks(status, hiveId, apiaryId, upcomingDays)

            // Cache in database
            tasks.forEach { task ->
                database.taskQueries.insertOrReplace(task.toDbTask())
            }

            Result.success(tasks)
        } catch (e: Exception) {
            // If API fails, try to return cached data
            try {
                val cachedTasks = when {
                    status != null -> database.taskQueries.selectByStatus(status.name).executeAsList()
                    hiveId != null -> database.taskQueries.selectByHive(hiveId).executeAsList()
                    apiaryId != null -> database.taskQueries.selectByApiary(apiaryId).executeAsList()
                    else -> database.taskQueries.selectAll().executeAsList()
                }
                Result.success(cachedTasks.map { it.toDomainTask() })
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getPendingTasks(): Result<List<Task>> {
        return try {
            val tasks = apiClient.getPendingTasks()
            tasks.forEach { task ->
                database.taskQueries.insertOrReplace(task.toDbTask())
            }
            Result.success(tasks)
        } catch (e: Exception) {
            try {
                val cached = database.taskQueries.selectPending().executeAsList()
                Result.success(cached.map { it.toDomainTask() })
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getOverdueTasks(): Result<List<Task>> {
        return try {
            val tasks = apiClient.getOverdueTasks()
            tasks.forEach { task ->
                database.taskQueries.insertOrReplace(task.toDbTask())
            }
            Result.success(tasks)
        } catch (e: Exception) {
            try {
                val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                val cached = database.taskQueries.selectOverdue(now).executeAsList()
                Result.success(cached.map { it.toDomainTask() })
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getTask(id: String): Result<Task> {
        return try {
            val task = apiClient.getTask(id)
            database.taskQueries.insertOrReplace(task.toDbTask())
            Result.success(task)
        } catch (e: Exception) {
            try {
                val cached = database.taskQueries.selectById(id).executeAsOneOrNull()
                cached?.let { Result.success(it.toDomainTask()) } ?: Result.failure(e)
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createTask(request: CreateTaskRequest): Result<Task> {
        return try {
            val task = apiClient.createTask(request)
            database.taskQueries.insertOrReplace(task.toDbTask())
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(id: String, request: UpdateTaskRequest): Result<Task> {
        return try {
            val task = apiClient.updateTask(id, request)
            database.taskQueries.insertOrReplace(task.toDbTask())
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeTask(id: String): Result<Task> {
        return try {
            val task = apiClient.completeTask(id)
            database.taskQueries.insertOrReplace(task.toDbTask())
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(id: String): Result<Unit> {
        return try {
            apiClient.deleteTask(id)
            database.taskQueries.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
