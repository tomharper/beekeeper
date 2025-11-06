// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/ai/services/AIVAService.kt
package com.beekeeper.app.data.ai.services
/*
import com.beekeeper.app.domain.ai.interfaces.*
import com.beekeeper.app.domain.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AIVAService(
    private val apiKey: String,
    private val baseUrl: String = "https://api.aiva.ai"
) : MusicGenerationService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    override suspend fun generateMusic(request: MusicGenerationRequest): Result<MusicGenerationResult> {
        return try {
            val response = client.post("$baseUrl/v1/compose") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(AIVARequest(
                    style = request.style,
                    mood = request.mood,
                    duration = request.durationSeconds,
                    instruments = request.instruments,
                    tempo = request.tempo,
                    key = request.key
                ))
            }

            val aivaResponse = response.body<AIVAResponse>()
            Result.success(MusicGenerationResult(
                compositionId = aivaResponse.composition_id,
                status = MusicStatus.PROCESSING,
                audioUrl = null,
                duration = request.durationSeconds,
                metadata = mapOf(
                    "style" to request.style,
                    "mood" to request.mood,
                    "tempo" to (request.tempo ?: "moderate")
                )
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMusicStatus(compositionId: String): Result<MusicGenerationResult> {
        return try {
            val response = client.get("$baseUrl/v1/compositions/$compositionId") {
                header("Authorization", "Bearer $apiKey")
            }

            val statusResponse = response.body<AIVAStatusResponse>()
            val status = when (statusResponse.status) {
                "completed" -> MusicStatus.COMPLETED
                "processing" -> MusicStatus.PROCESSING
                "failed" -> MusicStatus.FAILED
                else -> MusicStatus.PENDING
            }

            Result.success(MusicGenerationResult(
                compositionId = compositionId,
                status = status,
                audioUrl = statusResponse.audio_url,
                duration = statusResponse.duration ?: 0,
                metadata = statusResponse.metadata ?: emptyMap()
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadMusic(compositionId: String): Result<ByteArray> {
        return try {
            val statusResult = getMusicStatus(compositionId)
            if (statusResult.isSuccess && statusResult.getOrNull()?.status == MusicStatus.COMPLETED) {
                val audioUrl = statusResult.getOrNull()?.audioUrl
                    ?: throw Exception("Audio URL not available")
                val response = client.get(audioUrl)
                Result.success(response.body())
            } else {
                Result.failure(Exception("Music not ready for download"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getServiceName(): String = "AIVA"
    override fun isAvailable(): Boolean = apiKey.isNotEmpty()
}

@Serializable
data class AIVARequest(
    val style: String,
    val mood: String,
    val duration: Int,
    val instruments: List<String>? = null,
    val tempo: String? = null,
    val key: String? = null
)

@Serializable
data class AIVAResponse(
    val composition_id: String,
    val status: String
)

@Serializable
data class AIVAStatusResponse(
    val status: String,
    val audio_url: String? = null,
    val duration: Int? = null,
    val metadata: Map<String, String>? = null
)
*/