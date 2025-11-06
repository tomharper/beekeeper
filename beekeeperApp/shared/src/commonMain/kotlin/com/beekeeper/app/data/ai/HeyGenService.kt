// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/ai/HeyGenService.kt
package com.beekeeper.app.data.ai
/*
import com.beekeeper.app.domain.ai.interfaces.*
import com.beekeeper.app.domain.ai.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class HeyGenService(
    private val apiKey: String
) : AvatarGenerationService, VideoGenerationService {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    // AvatarGenerationService Implementation
    override suspend fun createAvatar(request: AvatarCreationRequest): Result<AvatarCreationResult> {
        return try {
            val response = client.post("https://api.heygen.com/v2/avatars") {
                header("X-Api-Key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(HeyGenAvatarRequest(
                    avatar_name = request.name ?: "Custom Avatar",
                    avatar_style = mapAvatarStyle(request.style),
                    preview_image_url = request.imageUrl,
                    preview_image_base64 = request.imageBase64
                ))
            }
            
            val heygenResponse = response.body<HeyGenAvatarResponse>()
            Result.success(AvatarCreationResult(
                avatarId = heygenResponse.data.avatar_id,
                previewUrl = heygenResponse.data.preview_image_url,
                metadata = mapOf("heygen_id" to heygenResponse.data.avatar_id)
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun listAvatars(): Result<List<AvatarCreationResult>> {
        return try {
            val response = client.get("https://api.heygen.com/v2/avatars") {
                header("X-Api-Key", apiKey)
            }
            
            val heygenResponse = response.body<HeyGenAvatarListResponse>()
            val avatars = heygenResponse.data.avatars.map { avatar ->
                AvatarCreationResult(
                    avatarId = avatar.avatar_id,
                    previewUrl = avatar.preview_image_url,
                    metadata = mapOf("name" to avatar.avatar_name)
                )
            }
            Result.success(avatars)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAvatar(avatarId: String): Result<Unit> {
        return try {
            client.delete("https://api.heygen.com/v2/avatars/$avatarId") {
                header("X-Api-Key", apiKey)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAvatarDetails(avatarId: String): Result<AvatarCreationResult> {
        return try {
            val response = client.get("https://api.heygen.com/v2/avatars/$avatarId") {
                header("X-Api-Key", apiKey)
            }
            
            val avatar = response.body<HeyGenAvatar>()
            Result.success(AvatarCreationResult(
                avatarId = avatar.avatar_id,
                previewUrl = avatar.preview_image_url,
                metadata = mapOf("name" to avatar.avatar_name)
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // VideoGenerationService Implementation
    override suspend fun generateVideo(request: VideoGenerationRequest): Result<VideoGenerationResult> {
        return try {
            val response = client.post("https://api.heygen.com/v2/video/generate") {
                header("X-Api-Key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(HeyGenVideoRequest(
                    video_inputs = listOf(
                        VideoInput(
                            character = Character(
                                type = "avatar",
                                avatar_id = request.avatarId
                            ),
                            voice = Voice(
                                type = "text",
                                input_text = request.script,
                                voice_id = request.voiceId
                            )
                        )
                    ),
                    dimension = mapVideoFormat(request.outputFormat)
                ))
            }
            
            val heygenResponse = response.body<HeyGenVideoResponse>()
            Result.success(VideoGenerationResult(
                videoId = heygenResponse.data.video_id,
                videoUrl = "", // URL will be available after processing
                duration = 0, // Will be updated when checking status
                status = VideoStatus.PROCESSING
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getVideoStatus(videoId: String): Result<VideoGenerationResult> {
        return try {
            val response = client.get("https://api.heygen.com/v1/video_status.get") {
                header("X-Api-Key", apiKey)
                parameter("video_id", videoId)
            }
            
            val statusResponse = response.body<HeyGenVideoStatusResponse>()
            val status = when (statusResponse.data.status) {
                "completed" -> VideoStatus.COMPLETED
                "processing" -> VideoStatus.PROCESSING
                "failed" -> VideoStatus.FAILED
                else -> VideoStatus.PENDING
            }
            
            Result.success(VideoGenerationResult(
                videoId = videoId,
                videoUrl = statusResponse.data.video_url ?: "",
                duration = statusResponse.data.duration ?: 0,
                status = status,
                thumbnailUrl = statusResponse.data.thumbnail_url
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun listVoices(language: String?): Result<List<com.beekeeper.app.domain.ai.interfaces.Voice>> {
        return try {
            val response = client.get("https://api.heygen.com/v2/voices") {
                header("X-Api-Key", apiKey)
            }
            
            val voicesResponse = response.body<HeyGenVoicesResponse>()
            val voices = voicesResponse.data.voices
                .filter { voice -> language == null || voice.language == language }
                .map { voice ->
                    Voice(
                        id = voice.voice_id,
                        name = voice.name,
                        language = voice.language,
                        gender = voice.gender,
                        previewUrl = voice.preview_audio
                    )
                }
            Result.success(voices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun downloadVideo(videoId: String): Result<ByteArray> {
        return try {
            val statusResult = getVideoStatus(videoId)
            if (statusResult.isSuccess && statusResult.getOrNull()?.status == VideoStatus.COMPLETED) {
                val videoUrl = statusResult.getOrNull()?.videoUrl ?: throw Exception("Video URL not available")
                val response = client.get(videoUrl)
                Result.success(response.body())
            } else {
                Result.failure(Exception("Video not ready for download"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getServiceName(): String = "HeyGen"
    
    override fun isAvailable(): Boolean = apiKey.isNotEmpty()
    
    private fun mapAvatarStyle(style: AvatarStylePreset): String {
        return when (style) {
            AvatarStylePreset.REALISTIC -> "realistic"
            AvatarStylePreset.CARTOON -> "cartoon"
            AvatarStylePreset.ANIME -> "anime"
            AvatarStylePreset.PROFESSIONAL -> "business"
        }
    }
    
    private fun mapVideoFormat(format: VideoFormat): Dimension {
        return when (format) {
            VideoFormat.MP4_720P -> Dimension(width = 1280, height = 720)
            VideoFormat.MP4_1080P -> Dimension(width = 1920, height = 1080)
            VideoFormat.MP4_4K -> Dimension(width = 3840, height = 2160)
            VideoFormat.WEBM -> Dimension(width = 1280, height = 720)
        }
    }
}

// HeyGen API Data Classes
@Serializable
data class HeyGenAvatarRequest(
    val avatar_name: String,
    val avatar_style: String,
    val preview_image_url: String? = null,
    val preview_image_base64: String? = null
)

@Serializable
data class HeyGenAvatarResponse(
    val data: HeyGenAvatar
)

@Serializable
data class HeyGenAvatar(
    val avatar_id: String,
    val avatar_name: String,
    val preview_image_url: String
)

@Serializable
data class HeyGenAvatarListResponse(
    val data: AvatarList
)

@Serializable
data class AvatarList(
    val avatars: List<HeyGenAvatar>
)

@Serializable
data class HeyGenVideoRequest(
    val video_inputs: List<VideoInput>,
    val dimension: Dimension? = null
)

@Serializable
data class VideoInput(
    val character: Character,
    val voice: Voice
)

@Serializable
data class Character(
    val type: String,
    val avatar_id: String
)

@Serializable
data class Voice(
    val type: String,
    val input_text: String,
    val voice_id: String? = null
)

@Serializable
data class Dimension(
    val width: Int,
    val height: Int
)

@Serializable
data class HeyGenVideoResponse(
    val data: VideoData
)

@Serializable
data class VideoData(
    val video_id: String
)

@Serializable
data class HeyGenVideoStatusResponse(
    val data: VideoStatusData
)

@Serializable
data class VideoStatusData(
    val status: String,
    val video_url: String? = null,
    val duration: Int? = null,
    val thumbnail_url: String? = null
)

@Serializable
data class HeyGenVoicesResponse(
    val data: VoicesData
)

@Serializable
data class VoicesData(
    val voices: List<HeyGenVoice>
)

@Serializable
data class HeyGenVoice(
    val voice_id: String,
    val name: String,
    val language: String,
    val gender: String,
    val preview_audio: String
)

*/