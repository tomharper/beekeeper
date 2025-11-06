// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/api/ApiService.kt
package com.beekeeper.app.data.api

import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.factory.ProjectFactory
import com.beekeeper.app.domain.factory.ProjectFactoryMetadata
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

/**
 * Simple in-memory ETag cache
 */
object ETagCache {
    private val cache = mutableMapOf<String, String>()

    fun get(url: String): String? = cache[url]
    fun set(url: String, etag: String) {
        cache[url] = etag
    }
    fun clear() {
        cache.clear()
    }
}

/**
 * API service for backend communication
 * Handles all HTTP requests to the CineFiller backend
 * Implements ETag-based caching for efficient data fetching
 * Supports JWT authentication with Bearer tokens
 */
class ApiService(
    private val client: HttpClient = HttpClientFactory.create(),
    private var authToken: String? = null
) {

    /**
     * Set authentication token for all subsequent requests
     */
    fun setAuthToken(token: String?) {
        authToken = token
    }

    /**
     * Get current authentication token
     */
    fun getAuthToken(): String? = authToken

    /**
     * Add Authorization header to request if token is available
     */
    private fun HttpRequestBuilder.addAuthHeader() {
        authToken?.let {
            header("Authorization", "Bearer $it")
        }
    }

    // ========== Authentication ==========

    /**
     * Login with username and password
     * Returns access token on success
     */
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = client.post("${ApiConfig.API_BASE_URL}/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }
            val loginResponse: LoginResponse = response.body()
            // Automatically set the token for future requests
            setAuthToken(loginResponse.access_token)
            Result.success(loginResponse)
        } catch (e: Exception) {
            println("❌ Login failed: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Verify current authentication token
     * Returns true if token is valid, false otherwise
     */
    suspend fun verifyToken(): Result<Boolean> {
        return try {
            val response: HttpResponse = client.get("${ApiConfig.API_BASE_URL}/auth/verify") {
                addAuthHeader()
            }
            Result.success(response.status == HttpStatusCode.OK)
        } catch (e: Exception) {
            println("❌ Token verification failed: ${e.message}")
            Result.success(false)
        }
    }

    /**
     * Get user profile information
     */
    suspend fun getUserProfile(): Result<UserProfileResponse> {
        return try {
            val response = client.get("${ApiConfig.API_BASE_URL}/auth/profile") {
                addAuthHeader()
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Projects ==========

    /**
     * Get list of projects with ETag caching
     * Returns ProjectsResponse on success (200), null on 304 Not Modified
     */
    suspend fun getProjects(
        limit: Int = 50,
        offset: Int = 0,
        factoryType: String? = null
    ): Result<ProjectsResponse?> {
        return try {
            val url = "${ApiConfig.API_BASE_URL}/projects/"
            val cachedETag = ETagCache.get(url)

            val response: HttpResponse = client.get(url) {
                addAuthHeader()
                parameter("limit", limit)
                parameter("offset", offset)
                factoryType?.let { parameter("factory_type", it) }

                // Only send If-None-Match if we have a cached ETag
                cachedETag?.let {
                    header("If-None-Match", it)
                    println("📤 Sending If-None-Match: $it")
                }
            }

            when (response.status) {
                HttpStatusCode.NotModified -> {
                    println("✅ ETag cache hit for projects list - data unchanged (304)")
                    Result.success(null)
                }
                HttpStatusCode.OK -> {
                    val projectsResponse = response.body<ProjectsResponse>()
                    response.headers["ETag"]?.let { etag ->
                        ETagCache.set(url, etag)
                        println("✅ Cached new ETag: $etag (${projectsResponse.projects.size} projects)")
                    }
                    Result.success(projectsResponse)
                }
                else -> Result.failure(Exception("Unexpected status: ${response.status}"))
            }
        } catch (e: Exception) {
            println("❌ API call failed: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get full project details including all content with ETag caching
     * Returns ProjectDetailsResponse on success (200), null on 304 Not Modified
     */
    suspend fun getProjectDetails(projectId: String): Result<ProjectDetailsResponse?> {
        return try {
            val url = "${ApiConfig.API_BASE_URL}/projects/$projectId"
            val cachedETag = ETagCache.get(url)

            val response: HttpResponse = client.get(url) {
                addAuthHeader()
                // Only send If-None-Match if we have a cached ETag
                cachedETag?.let {
                    header("If-None-Match", it)
                    println("📤 Sending If-None-Match for $projectId: $it")
                }
            }

            when (response.status) {
                HttpStatusCode.NotModified -> {
                    println("✅ ETag cache hit for project $projectId - data unchanged (304)")
                    Result.success(null)
                }
                HttpStatusCode.OK -> {
                    val projectDetails = response.body<ProjectDetailsResponse>()
                    response.headers["ETag"]?.let { etag ->
                        ETagCache.set(url, etag)
                        println("✅ Cached new ETag for $projectId: $etag")
                    }
                    Result.success(projectDetails)
                }
                else -> Result.failure(Exception("Unexpected status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update project metadata
     */
    suspend fun updateProject(projectId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            client.put("${ApiConfig.API_BASE_URL}/projects/$projectId") {
                addAuthHeader()
                contentType(ContentType.Application.Json)
                setBody(updates)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a project
     */
    suspend fun deleteProject(projectId: String): Result<Unit> {
        return try {
            client.delete("${ApiConfig.API_BASE_URL}/projects/$projectId") {
                addAuthHeader()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Characters ==========

    /**
     * Get all characters for a project with ETag caching
     * Returns CharactersResponse on success (200), null on 304 Not Modified
     */
    suspend fun getCharacters(projectId: String): Result<CharactersResponse?> {
        return try {
            val url = "${ApiConfig.API_BASE_URL}/projects/$projectId/characters"
            val cachedETag = ETagCache.get(url)

            val response: HttpResponse = client.get(url) {
                addAuthHeader()
                // Only send If-None-Match if we have a cached ETag
                cachedETag?.let {
                    header("If-None-Match", it)
                    println("📤 Sending If-None-Match for characters $projectId: $it")
                }
            }

            when (response.status) {
                HttpStatusCode.NotModified -> {
                    println("✅ ETag cache hit for characters $projectId - data unchanged (304)")
                    Result.success(null)
                }
                HttpStatusCode.OK -> {
                    val charactersResponse = response.body<CharactersResponse>()
                    response.headers["ETag"]?.let { etag ->
                        ETagCache.set(url, etag)
                        println("✅ Cached new ETag for characters $projectId: $etag (${charactersResponse.characters.size} characters)")
                    }
                    Result.success(charactersResponse)
                }
                else -> Result.failure(Exception("Unexpected status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a specific character
     */
    suspend fun getCharacter(projectId: String, characterId: String): Result<CharacterResponse> {
        return try {
            val response = client.get("${ApiConfig.API_BASE_URL}/projects/$projectId/characters/$characterId") {
                addAuthHeader()
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new character
     */
    suspend fun createCharacter(projectId: String, character: CharacterProfile): Result<CharacterProfile> {
        return try {
            val response = client.post("${ApiConfig.API_BASE_URL}/projects/$projectId/characters") {
                addAuthHeader()
                contentType(ContentType.Application.Json)
                setBody(character)
            }
            val result: CharacterCreateResponse = response.body()
            Result.success(result.character)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update a character
     */
    suspend fun updateCharacter(projectId: String, characterId: String, character: CharacterProfile): Result<CharacterProfile> {
        return try {
            val response = client.put("${ApiConfig.API_BASE_URL}/projects/$projectId/characters/$characterId") {
                addAuthHeader()
                contentType(ContentType.Application.Json)
                setBody(character)
            }
            val result: CharacterUpdateResponse = response.body()
            Result.success(result.character)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a character
     */
    suspend fun deleteCharacter(projectId: String, characterId: String): Result<Unit> {
        return try {
            client.delete("${ApiConfig.API_BASE_URL}/projects/$projectId/characters/$characterId") {
                addAuthHeader()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Project Bible ==========

    /**
     * Get project bible
     */
    suspend fun getProjectBible(projectId: String): Result<ProjectBibleResponse> {
        return try {
            val response = client.get("${ApiConfig.API_BASE_URL}/projects/$projectId/bible") {
                addAuthHeader()
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update project bible
     */
    suspend fun updateProjectBible(projectId: String, bible: ProjectBible): Result<Unit> {
        return try {
            client.post("${ApiConfig.API_BASE_URL}/projects/$projectId/bible") {
                addAuthHeader()
                contentType(ContentType.Application.Json)
                setBody(bible)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Episode Blueprints ==========

    /**
     * Get all episode blueprints for a project from episode_blueprints table
     */
    suspend fun getEpisodeBlueprints(projectId: String): Result<EpisodeBlueprintsResponse> {
        return try {
            val response = client.get("${ApiConfig.API_BASE_URL}/projects/$projectId/blueprints") {
                addAuthHeader()
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Stories ==========

    /**
     * Get all stories for a project
     */
    suspend fun getStories(projectId: String): Result<StoriesResponse> {
        return try {
            val response = client.get("${ApiConfig.API_BASE_URL}/projects/$projectId/stories") {
                addAuthHeader()
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get story with script and storyboards
     */
    suspend fun getStoryDetails(projectId: String, storyId: String): Result<StoryDetailsResponse> {
        return try {
            val response = client.get("${ApiConfig.API_BASE_URL}/projects/$projectId/stories/$storyId") {
                addAuthHeader()
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Props ==========

    /**
     * Get all props for a project
     */
    suspend fun getProps(projectId: String): Result<PropsResponse> {
        return try {
            val response = client.get("${ApiConfig.API_BASE_URL}/projects/$projectId/props") {
                addAuthHeader()
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ========== Response DTOs ==========

@Serializable
data class ProjectsResponse(
    val projects: List<ProjectSummary>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

@Serializable
data class ProjectSummary(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val factory_type: String,
    val created_at: Long,
    val updated_at: Long
)

@Serializable
data class ProjectDetailsResponse(
    val project: CreativeProject,
    val characters: List<CharacterProfile> = emptyList(),
    val stories: List<Story> = emptyList(),
    val scripts: List<Script> = emptyList(),
    val storyboards: List<Storyboard> = emptyList(),
    val publishingProject: PublishingProject? = null,
    val projectBible: ProjectBible? = null,
    val metadata: ProjectFactoryMetadata = ProjectFactoryMetadata()
)

@Serializable
data class CharactersResponse(
    val characters: List<CharacterProfile>
)

@Serializable
data class CharacterResponse(
    val character: CharacterProfile
)

@Serializable
data class CharacterCreateResponse(
    val success: Boolean,
    val character: CharacterProfile
)

@Serializable
data class CharacterUpdateResponse(
    val success: Boolean,
    val character: CharacterProfile
)

@Serializable
data class ProjectBibleResponse(
    val projectBible: ProjectBible
)

@Serializable
data class StoriesResponse(
    val stories: List<Story>
)

@Serializable
data class StoryDetailsResponse(
    val story: Story,
    val script: Script? = null,
    val storyboards: List<Storyboard> = emptyList()
)

@Serializable
data class PropsResponse(
    val props: List<Prop>
)

@Serializable
data class EpisodeBlueprintsResponse(
    val project_id: String,
    val blueprints: List<EpisodeBlueprintFromTable>,
    val count: Int
)

@Serializable
data class EpisodeBlueprintFromTable(
    val id: String,
    val project_id: String,
    val episode_number: Int,
    val title: String?,
    val synopsis: String?,
    val blueprint: kotlinx.serialization.json.JsonObject? = null,  // V2 blueprint (legacy)
    val blueprint_v3_json: kotlinx.serialization.json.JsonObject? = null,  // V3 blueprint (current)
    val archetype: String?,
    val archetype_id: String?,
    val genres: List<String>? = null,
    val tones: List<String>? = null,
    val structure_pattern: String?,
    val pacing_style: String?,
    val duration: String?,
    val created_at: String?,
    val updated_at: String?
) {
    /**
     * Parse the V3 blueprint JSON into an EpisodeBlueprintV3 object
     * Returns null if blueprint_v3_json is not available
     */
    fun toEpisodeBlueprintV3(): EpisodeBlueprintV3? {
        val blueprintJson = blueprint_v3_json ?: return null
        return try {
            kotlinx.serialization.json.Json.decodeFromJsonElement(
                EpisodeBlueprintV3.serializer(),
                blueprintJson
            )
        } catch (e: Exception) {
            println("Error parsing V3 blueprint: ${e.message}")
            null
        }
    }
}

// ========== Authentication DTOs ==========

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val access_token: String,
    val token_type: String = "bearer"
)

@Serializable
data class UserProfileResponse(
    val username: String,
    val email: String? = null,
    val created_at: Long? = null
)
