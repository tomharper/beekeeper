// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/ai/models/AIModels.kt
package com.beekeeper.app.domain.ai

// Script Generation Models
data class ScriptGenerationRequest(
    val topic: String,
    val style: String? = null,
    val duration: Int = 60, // seconds
    val tone: String = "professional",
    val language: String = "en"
)

data class ScriptGenerationResult(
    val script: String,
    val estimatedDuration: Int,
    val segments: List<ScriptSegment> = emptyList()
)

data class ScriptSegment(
    val text: String,
    val startTime: Float,
    val endTime: Float,
    val emotion: String? = null
)

// Avatar Generation Models
data class AvatarCreationRequest(
    val imageUrl: String? = null,
    val imageBase64: String? = null,
    val style: AvatarStylePreset = AvatarStylePreset.REALISTIC,
    val name: String? = null
)

enum class AvatarStylePreset {
    REALISTIC, CARTOON, ANIME, PROFESSIONAL
}

data class AvatarCreationResult(
    val avatarId: String,
    val previewUrl: String,
    val metadata: Map<String, Any> = emptyMap()
)

// Video Generation Models
data class VideoGenerationRequest(
    val avatarId: String,
    val script: String,
    val voiceId: String? = null,
    val background: String? = null,
    val visualEffects: List<String> = emptyList(),
    val music: String? = null,
    val outputFormat: VideoFormat = VideoFormat.MP4_720P
)

enum class VideoFormat {
    MP4_720P, MP4_1080P, MP4_4K, WEBM
}

data class VideoGenerationResult(
    val videoId: String,
    val videoUrl: String,
    val duration: Int,
    val status: VideoStatus,
    val thumbnailUrl: String? = null
)

enum class VideoStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}

