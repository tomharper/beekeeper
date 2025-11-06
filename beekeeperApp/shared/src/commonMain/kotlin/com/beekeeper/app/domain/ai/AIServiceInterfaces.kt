// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/ai/interfaces/AIServiceInterfaces.kt
package com.beekeeper.app.domain.ai.interfaces

import com.beekeeper.app.domain.ai.AvatarCreationRequest
import com.beekeeper.app.domain.ai.AvatarCreationResult
import com.beekeeper.app.domain.ai.ScriptGenerationRequest
import com.beekeeper.app.domain.ai.ScriptGenerationResult
import com.beekeeper.app.domain.ai.VideoGenerationRequest
import com.beekeeper.app.domain.ai.VideoGenerationResult

/**
 * Interface for script generation services (Claude, GPT, Gemini, etc.)
 */
interface ScriptGenerationService {
    suspend fun generateScript(request: ScriptGenerationRequest): Result<ScriptGenerationResult>
    suspend fun improveScript(script: String, improvements: List<String>): Result<String>
    suspend fun translateScript(script: String, targetLanguage: String): Result<String>
    fun getServiceName(): String
    fun isAvailable(): Boolean
}

/**
 * Interface for avatar creation services (HeyGen, D-ID, Synthesia, etc.)
 */
interface AvatarGenerationService {
    suspend fun createAvatar(request: AvatarCreationRequest): Result<AvatarCreationResult>
    suspend fun listAvatars(): Result<List<AvatarCreationResult>>
    suspend fun deleteAvatar(avatarId: String): Result<Unit>
    suspend fun getAvatarDetails(avatarId: String): Result<AvatarCreationResult>
    fun getServiceName(): String
    fun isAvailable(): Boolean
}

/**
 * Interface for video generation services
 */
interface VideoGenerationService {
    suspend fun generateVideo(request: VideoGenerationRequest): Result<VideoGenerationResult>
    suspend fun getVideoStatus(videoId: String): Result<VideoGenerationResult>
    suspend fun listVoices(language: String? = null): Result<List<Voice>>
    suspend fun downloadVideo(videoId: String): Result<ByteArray>
    fun getServiceName(): String
    fun isAvailable(): Boolean
}

data class Voice(
    val id: String,
    val name: String,
    val language: String,
    val gender: String,
    val previewUrl: String? = null
)

/**
 * Configuration for AI services
 */
data class AIServiceConfig(
    val scriptService: ScriptServiceProvider = ScriptServiceProvider.CLAUDE,
    val avatarService: AvatarServiceProvider = AvatarServiceProvider.HEYGEN,
    val videoService: VideoServiceProvider = VideoServiceProvider.HEYGEN
)

enum class ScriptServiceProvider {
    CLAUDE, BEDROCK_CLAUDE, GPT4, GEMINI, LOCAL
}

enum class AvatarServiceProvider {
    HEYGEN, DID, SYNTHESIA, LOCAL
}

enum class VideoServiceProvider {
    HEYGEN, DID, SYNTHESIA, LOCAL
}

