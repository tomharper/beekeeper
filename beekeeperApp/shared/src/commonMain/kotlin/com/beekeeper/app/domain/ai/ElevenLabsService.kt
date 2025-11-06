// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/ai/ElevenLabsService.kt
package com.beekeeper.app.domain.ai
/*
import com.beekeeper.app.domain.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

class ElevenLabsService(
    private val apiKey: String,
    private val baseUrl: String = "https://api.elevenlabs.io"
) : VoiceSynthesisService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    override suspend fun synthesizeVoice(request: VoiceSynthesisRequest): Result<VoiceSynthesisResult> {
        return try {
            val response = client.post("$baseUrl/v1/text-to-speech/${request.voiceId}") {
                header("xi-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(ElevenLabsRequest(
                    text = request.text,
                    voice_settings = VoiceSettings(
                        stability = request.stability ?: 0.75f,
                        similarity_boost = request.similarityBoost ?: 0.85f,
                        style = request.style ?: 0.0f,
                        use_speaker_boost = request.useSpeakerBoost ?: true
                    ),
                    model_id = request.modelId ?: "eleven_multilingual_v2"
                ))
            }

            val audioBytes = response.body<ByteArray>()
            Result.success(VoiceSynthesisResult(
                audioData = audioBytes,
                format = "mp3",
                duration = estimateAudioDuration(request.text),
                voiceId = request.voiceId
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun listVoices(): Result<List<VoiceProfile>> {
        return try {
            val response = client.get("$baseUrl/v1/voices") {
                header("xi-api-key", apiKey)
            }

            val voicesResponse = response.body<ElevenLabsVoicesResponse>()
            val voices = voicesResponse.voices.map { voice ->
                VoiceProfile(
                    id = voice.voice_id,
                    name = voice.name,
                    category = voice.category ?: "general",
                    language = "en", // ElevenLabs supports multiple but defaults to English
                    gender = voice.labels?.gender ?: "unspecified",
                    age = voice.labels?.age ?: "adult",
                    accent = voice.labels?.accent ?: "american",
                    description = voice.description,
                    previewUrl = voice.preview_url
                )
            }
            Result.success(voices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cloneVoice(request: VoiceCloneRequest): Result<VoiceCloneResult> {
        return try {
            // Implementation for voice cloning
            // This is a premium feature requiring audio samples
            Result.failure(Exception("Voice cloning not implemented yet"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getServiceName(): String = "ElevenLabs"
    override fun isAvailable(): Boolean = apiKey.isNotEmpty()

    private fun estimateAudioDuration(text: String): Float {
        // Rough estimation: ~150 words per minute
        val wordCount = text.split("\\s+".toRegex()).size
        return (wordCount / 150f) * 60f
    }
}

@Serializable
data class ElevenLabsRequest(
    val text: String,
    val voice_settings: VoiceSettings,
    val model_id: String
)

@Serializable
data class VoiceSettings(
    val stability: Float,
    val similarity_boost: Float,
    val style: Float? = null,
    val use_speaker_boost: Boolean? = null
)

@Serializable
data class ElevenLabsVoicesResponse(
    val voices: List<ElevenLabsVoice>
)

@Serializable
data class ElevenLabsVoice(
    val voice_id: String,
    val name: String,
    val category: String? = null,
    val description: String? = null,
    val preview_url: String? = null,
    val labels: VoiceLabels? = null
)

@Serializable
data class VoiceLabels(
    val gender: String? = null,
    val age: String? = null,
    val accent: String? = null
)

 */