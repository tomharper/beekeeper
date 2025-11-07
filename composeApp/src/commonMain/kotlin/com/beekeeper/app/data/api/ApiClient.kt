package com.beekeeper.app.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.beekeeper.app.domain.model.*

class ApiClient(private val baseUrl: String = "http://localhost:2020/api") {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    private var authToken: String? = null

    fun setAuthToken(token: String) {
        authToken = token
    }

    private fun HttpRequestBuilder.addAuthHeader() {
        authToken?.let {
            header("Authorization", "Bearer $it")
        }
    }

    // Apiaries
    suspend fun getApiaries(): List<Apiary> {
        return httpClient.get("$baseUrl/apiaries") {
            addAuthHeader()
        }.body()
    }

    suspend fun getApiary(id: String): Apiary {
        return httpClient.get("$baseUrl/apiaries/$id") {
            addAuthHeader()
        }.body()
    }

    // Hives
    suspend fun getHives(apiaryId: String? = null): List<Hive> {
        return httpClient.get("$baseUrl/hives") {
            apiaryId?.let { parameter("apiary_id", it) }
            addAuthHeader()
        }.body()
    }

    suspend fun getHive(id: String): Hive {
        return httpClient.get("$baseUrl/hives/$id") {
            addAuthHeader()
        }.body()
    }

    // Tasks
    suspend fun getTasks(
        status: TaskStatus? = null,
        hiveId: String? = null,
        apiaryId: String? = null,
        upcomingDays: Int? = null
    ): List<Task> {
        return httpClient.get("$baseUrl/tasks") {
            status?.let { parameter("task_status", it.name) }
            hiveId?.let { parameter("hive_id", it) }
            apiaryId?.let { parameter("apiary_id", it) }
            upcomingDays?.let { parameter("upcoming_days", it) }
            addAuthHeader()
        }.body()
    }

    suspend fun getPendingTasks(): List<Task> {
        return httpClient.get("$baseUrl/tasks/pending") {
            addAuthHeader()
        }.body()
    }

    suspend fun getOverdueTasks(): List<Task> {
        return httpClient.get("$baseUrl/tasks/overdue") {
            addAuthHeader()
        }.body()
    }

    suspend fun getTask(id: String): Task {
        return httpClient.get("$baseUrl/tasks/$id") {
            addAuthHeader()
        }.body()
    }

    suspend fun createTask(task: CreateTaskRequest): Task {
        return httpClient.post("$baseUrl/tasks") {
            addAuthHeader()
            contentType(ContentType.Application.Json)
            setBody(task)
        }.body()
    }

    suspend fun updateTask(id: String, task: UpdateTaskRequest): Task {
        return httpClient.put("$baseUrl/tasks/$id") {
            addAuthHeader()
            contentType(ContentType.Application.Json)
            setBody(task)
        }.body()
    }

    suspend fun completeTask(id: String): Task {
        return httpClient.post("$baseUrl/tasks/$id/complete") {
            addAuthHeader()
        }.body()
    }

    suspend fun deleteTask(id: String) {
        httpClient.delete("$baseUrl/tasks/$id") {
            addAuthHeader()
        }
    }

    // Inspections
    suspend fun getInspections(
        hiveId: String? = null,
        limit: Int? = null
    ): List<Inspection> {
        return httpClient.get("$baseUrl/inspections") {
            hiveId?.let { parameter("hive_id", it) }
            limit?.let { parameter("limit", it) }
            addAuthHeader()
        }.body()
    }

    suspend fun getInspection(id: String): Inspection {
        return httpClient.get("$baseUrl/inspections/$id") {
            addAuthHeader()
        }.body()
    }

    suspend fun getRecentInspections(limit: Int = 10): List<Inspection> {
        return httpClient.get("$baseUrl/inspections/recent") {
            parameter("limit", limit)
            addAuthHeader()
        }.body()
    }

    suspend fun getLatestHiveInspection(hiveId: String): Inspection {
        return httpClient.get("$baseUrl/inspections/hive/$hiveId/latest") {
            addAuthHeader()
        }.body()
    }

    suspend fun createInspection(inspection: CreateInspectionRequest): Inspection {
        return httpClient.post("$baseUrl/inspections") {
            addAuthHeader()
            contentType(ContentType.Application.Json)
            setBody(inspection)
        }.body()
    }

    suspend fun updateInspection(id: String, inspection: UpdateInspectionRequest): Inspection {
        return httpClient.put("$baseUrl/inspections/$id") {
            addAuthHeader()
            contentType(ContentType.Application.Json)
            setBody(inspection)
        }.body()
    }

    suspend fun deleteInspection(id: String) {
        httpClient.delete("$baseUrl/inspections/$id") {
            addAuthHeader()
        }
    }

    // Alerts
    suspend fun getActiveAlerts(): List<Alert> {
        return httpClient.get("$baseUrl/alerts/active") {
            addAuthHeader()
        }.body()
    }

    // Weather
    suspend fun getWeather(): Weather {
        return httpClient.get("$baseUrl/weather") {
            addAuthHeader()
        }.body()
    }

    // Recommendations
    suspend fun getRecommendations(hiveId: String): List<Recommendation> {
        return httpClient.get("$baseUrl/recommendations") {
            parameter("hive_id", hiveId)
            addAuthHeader()
        }.body()
    }
}
