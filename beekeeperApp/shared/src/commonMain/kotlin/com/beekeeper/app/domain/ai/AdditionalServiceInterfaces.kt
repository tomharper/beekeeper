// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/ai/interfaces/AdditionalServiceInterfaces.kt
package com.beekeeper.app.domain.ai.interfaces

import com.beekeeper.app.domain.model.*
import kotlinx.serialization.Serializable
/**
 * Interface for voice synthesis services (ElevenLabs, Azure TTS, etc.)
 */
interface VoiceSynthesisService {
    suspend fun synthesizeVoice(request: VoiceSynthesisRequest): Result<VoiceSynthesisResult>
    suspend fun listVoices(): Result<List<VoiceProfile>>
    suspend fun cloneVoice(request: VoiceCloneRequest): Result<VoiceCloneResult>
    fun getServiceName(): String
    fun isAvailable(): Boolean
}

/**
 * Interface for music generation services (AIVA, Mubert, etc.)
 */
interface MusicGenerationService {
    suspend fun generateMusic(request: MusicGenerationRequest): Result<MusicGenerationResult>
    suspend fun getMusicStatus(compositionId: String): Result<MusicGenerationResult>
    suspend fun downloadMusic(compositionId: String): Result<ByteArray>
    fun getServiceName(): String
    fun isAvailable(): Boolean
}

/**
 * Interface for video editing and stitching services (Shotstack, etc.)
 */
interface VideoEditingService {
    suspend fun stitchFrames(request: VideoStitchRequest): Result<VideoStitchResult>
    suspend fun getStitchStatus(renderId: String): Result<VideoStitchResult>
    fun getServiceName(): String
    fun isAvailable(): Boolean
}

/**
 * Interface for image generation services (DALL-E, Midjourney, etc.)
 */
interface ImageGenerationService {
    suspend fun generateImage(request: ImageGenerationRequest): Result<ImageGenerationResult>
    suspend fun generateImageVariations(request: ImageVariationRequest): Result<List<ImageGenerationResult>>
    suspend fun upscaleImage(request: ImageUpscaleRequest): Result<ImageGenerationResult>
    fun getServiceName(): String
    fun isAvailable(): Boolean
}

/**
 * Interface for subtitle and transcription services
 */
interface TranscriptionService {
    suspend fun transcribeAudio(request: TranscriptionRequest): Result<TranscriptionResult>
    suspend fun generateSubtitles(request: SubtitleRequest): Result<SubtitleResult>
    fun getServiceName(): String
    fun isAvailable(): Boolean
}

/**
 * Interface for translation services
 */
interface TranslationService {
    suspend fun translateText(request: TranslationRequest): Result<TranslationResult>
    suspend fun translateSubtitles(request: SubtitleTranslationRequest): Result<SubtitleResult>
    suspend fun detectLanguage(text: String): Result<String>
    fun getServiceName(): String
    fun isAvailable(): Boolean
}


// Voice Synthesis Models
@Serializable
data class VoiceSynthesisRequest(
    val text: String,
    val voiceId: String,
    val language: String = "en",
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val stability: Float? = null,
    val similarityBoost: Float? = null,
    val style: Float? = null,
    val useSpeakerBoost: Boolean? = null,
    val modelId: String? = null,
    val outputFormat: String = "mp3"
)

@Serializable
data class VoiceSynthesisResult(
    val audioData: ByteArray,
    val format: String,
    val duration: Float,
    val voiceId: String,
    val metadata: Map<String, String> = emptyMap()
)


@Serializable
data class VoiceCloneRequest(
    val name: String,
    val description: String,
    val audioSamples: List<String>, // URLs to audio samples
    val language: String = "en"
)

@Serializable
data class VoiceCloneResult(
    val voiceId: String,
    val status: String,
    val previewUrl: String? = null
)

// Music Generation Models
@Serializable
data class MusicGenerationRequest(
    val style: String, // "cinematic", "ambient", "orchestral", etc.
    val mood: String, // "dramatic", "peaceful", "energetic", etc.
    val durationSeconds: Int,
    val tempo: String? = null, // "slow", "moderate", "fast"
    val key: String? = null, // "C major", "A minor", etc.
    val instruments: List<String>? = null,
    val tags: List<String> = emptyList(),
    val referenceTrack: String? = null
)

@Serializable
data class MusicGenerationResult(
    val compositionId: String,
    val status: MusicStatus,
    val audioUrl: String? = null,
    val duration: Int,
    val metadata: Map<String, String> = emptyMap()
)

enum class MusicStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}

// Video Editing Models
@Serializable
data class VideoStitchRequest(
    val frames: List<FrameData>,
    val outputFormat: String = "mp4",
    val resolution: String = "1920x1080",
    val fps: Int = 24,
    val quality: String = "high",
    val transitions: List<TransitionData> = emptyList(),
    val backgroundColor: String? = null
)

@Serializable
data class FrameData(
    val videoUrl: String,
    val audioUrl: String? = null,
    val duration: Float,
    val startTime: Float? = null,
    val effects: List<EffectData> = emptyList()
)

@Serializable
data class TransitionData(
    val type: String, // "fade", "dissolve", "wipe", etc.
    val duration: Float,
    val position: Int // Between which frames
)

@Serializable
data class EffectData(
    val type: String, // "blur", "color_correction", etc.
    val parameters: Map<String, String>
)

@Serializable
data class VideoStitchResult(
    val renderId: String,
    val status: VideoStitchStatus,
    val videoUrl: String? = null,
    val duration: Float,
    val thumbnailUrl: String? = null
)

enum class VideoStitchStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}

// Image Generation Models
@Serializable
data class ImageGenerationRequest(
    val prompt: String,
    val negativePrompt: String? = null,
    val style: String? = null,
    val width: Int = 1024,
    val height: Int = 1024,
    val steps: Int = 50,
    val guidance: Float = 7.5f,
    val seed: Long? = null,
    val model: String? = null
)

@Serializable
data class ImageGenerationResult(
    val imageUrl: String,
    val imageData: ByteArray? = null,
    val prompt: String,
    val seed: Long? = null,
    val dimensions: Pair<Int, Int>,
    val model: String? = null
)

@Serializable
data class ImageVariationRequest(
    val sourceImageUrl: String,
    val variations: Int = 4,
    val strength: Float = 0.8f
)

@Serializable
data class ImageUpscaleRequest(
    val imageUrl: String,
    val scaleFactor: Int = 4, // 2x, 4x, etc.
    val enhanceDetails: Boolean = true
)

// Transcription Models
@Serializable
data class TranscriptionRequest(
    val audioUrl: String,
    val language: String? = null,
    val speakerLabels: Boolean = false,
    val punctuation: Boolean = true,
    val profanityFilter: Boolean = false
)

@Serializable
data class TranscriptionResult(
    val text: String,
    val segments: List<TranscriptionSegment>,
    val confidence: Float,
    val language: String? = null,
    val speakers: List<Speaker>? = null
)

@Serializable
data class TranscriptionSegment(
    val text: String,
    val startTime: Float,
    val endTime: Float,
    val confidence: Float,
    val speaker: String? = null
)

@Serializable
data class Speaker(
    val id: String,
    val name: String? = null
)

@Serializable
data class SubtitleRequest(
    val transcription: TranscriptionResult,
    val maxLength: Int = 42, // Characters per line
    val maxDuration: Float = 3.0f, // Seconds per subtitle
    val format: String = "srt" // srt, vtt, etc.
)

@Serializable
data class SubtitleResult(
    val subtitles: List<SubtitleEntry>,
    val format: String,
    val content: String // Raw subtitle file content
)

@Serializable
data class SubtitleEntry(
    val index: Int,
    val startTime: Float,
    val endTime: Float,
    val text: String
)

// Translation Models
@Serializable
data class TranslationRequest(
    val text: String,
    val sourceLanguage: String? = null, // Auto-detect if null
    val targetLanguage: String,
    val formality: String? = null, // "formal", "informal"
    val context: String? = null
)

@Serializable
data class TranslationResult(
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val confidence: Float? = null
)

@Serializable
data class SubtitleTranslationRequest(
    val subtitles: SubtitleResult,
    val targetLanguage: String,
    val preserveTiming: Boolean = true
)

@Serializable
enum class ServicePriority {
    HIGH, MEDIUM, LOW
}

@Serializable
data class ServiceEndpoint(
    val url: String,
    val priority: ServicePriority = ServicePriority.MEDIUM,
    val timeout: Long = 30000,
    val retries: Int = 3
)

@Serializable
data class RateLimitConfig(
    val requestsPerMinute: Int,
    val requestsPerHour: Int? = null,
    val requestsPerDay: Int? = null,
    val burstLimit: Int? = null
)

@Serializable
data class ExtendedServiceConfig(
    val apiKey: String = "",
    val baseUrl: String,
    val endpoints: Map<String, ServiceEndpoint> = emptyMap(),
    val rateLimit: RateLimitConfig? = null,
    val features: List<String> = emptyList(),
    val priority: ServicePriority = ServicePriority.MEDIUM,
    val fallbackService: String? = null
)

@Serializable
data class SceneGenerationPipeline(
    val sceneId: String,
    val scriptContent: String,
    val frames: List<FrameGenerationRequest>,
    val audioRequirements: AudioGenerationRequirements,
    val videoRequirements: VideoGenerationRequirements,
    val deliverySettings: DeliverySettings
)

@Serializable
data class FrameGenerationRequest(
    val frameId: String,
    val order: Int,
    val visualPrompt: String,
    val duration: Float,
    val cameraAngle: String,
    val shotType: String,
    val dialogueLineId: String? = null,
    val audioPrompt: String? = null,
    val effects: List<String> = emptyList()
)

@Serializable
data class AudioGenerationRequirements(
    val backgroundMusic: MusicGenerationRequest? = null,
    val voiceovers: List<VoiceGenerationRequest> = emptyList(),
    val soundEffects: List<SoundEffectRequest> = emptyList()
)

@Serializable
data class VoiceGenerationRequest(
    val dialogueLineId: String,
    val characterName: String,
    val text: String,
    val voiceId: String,
    val emotion: String? = null,
    val timing: VoiceTiming
)

@Serializable
data class VoiceTiming(
    val startTime: Float,
    val endTime: Float,
    val fadeIn: Float = 0.1f,
    val fadeOut: Float = 0.1f
)

@Serializable
data class SoundEffectRequest(
    val type: String, // "ambient", "foley", "music_sting"
    val prompt: String,
    val timing: VoiceTiming,
    val volume: Float = 1.0f
)

@Serializable
data class VideoGenerationRequirements(
    val resolution: String = "1920x1080",
    val fps: Int = 24,
    val format: String = "mp4",
    val quality: String = "high",
    val stabilization: Boolean = true,
    val colorGrading: String? = null
)

@Serializable
data class DeliverySettings(
    val platforms: List<String>, // "youtube", "tiktok", "instagram", etc.
    val aspectRatios: List<String> = listOf("16:9"),
    val compressionSettings: Map<String, String> = emptyMap(),
    val deliveryFormat: String = "mp4"
)

@Serializable
data class SceneGenerationResult(
    val sceneId: String,
    val status: GenerationStatus,
    val videoUrl: String? = null,
    val duration: Float,
    val generatedFrames: List<GeneratedFrameResult>,
    val audioTracks: List<GeneratedAudioResult>,
    val deliverables: List<DeliverableResult>,
    val metadata: GenerationMetadata
)

@Serializable
data class GeneratedFrameResult(
    val frameId: String,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val duration: Float,
    val generationTime: Float,
    val model: String,
    val quality: Float? = null
)

@Serializable
data class GeneratedAudioResult(
    val type: String, // "dialogue", "music", "sfx"
    val audioUrl: String,
    val duration: Float,
    val volume: Float = 1.0f,
    val startTime: Float,
    val endTime: Float
)

@Serializable
data class DeliverableResult(
    val platform: String,
    val url: String,
    val resolution: String,
    val format: String,
    val size: Long? = null
)

@Serializable
data class GenerationMetadata(
    val totalGenerationTime: Float,
    val costs: Map<String, Float>, // Service name to cost
    val modelsUsed: List<String>,
    val qualityScores: Map<String, Float>,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)

enum class GenerationStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, PARTIALLY_COMPLETED
}
