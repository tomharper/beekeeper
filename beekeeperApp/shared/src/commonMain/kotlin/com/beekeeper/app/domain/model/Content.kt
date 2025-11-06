// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/Content.kt
package com.beekeeper.app.domain.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * Unified domain entity representing Content (media assets)
 * Superset of all content-related properties
 */
@Serializable
data class Content(
    val id: String,
    val projectId: String? = null,
    val title: String,
    val type: ContentType,
    val format: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val size: Long,
    val duration: Int? = null,
    val dimensions: Dimensions? = null,
    val metadata: ContentMetadata? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
    val uploadedBy: String,
    val isProcessed: Boolean = false,
    val isPublic: Boolean = false,
    // Additional fields for superset
    val description: String? = null,
    val version: String? = null,
    val checksum: String? = null,
    val processingStatus: ContentProcessingStatus = ContentProcessingStatus.NONE
)

@Serializable
data class Dimensions(
    val width: Int,
    val height: Int,
    val aspectRatio: String? = null,
    val depth: Int? = null // For 3D content
)

@Serializable
data class ContentMetadata(
    val originalFileName: String? = null,
    val mimeType: String? = null,
    val codec: String? = null,
    val bitrate: Int? = null,
    val frameRate: Float? = null,
    val colorSpace: String? = null,
    val hasAlpha: Boolean = false,
    val isHDR: Boolean = false,
    // Additional metadata fields
    val author: String? = null,
    val copyright: String? = null,
    val location: LocationData? = null,
    val capturedAt: Instant? = null,
    val device: String? = null,
    val software: String? = null,
    val keywords: List<String> = emptyList(),
    val customData: Map<String, String> = emptyMap()
)

@Serializable
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val placeName: String? = null,
    val country: String? = null,
    val city: String? = null
)

@Serializable
data class ImageAsset(
    val id: String,
    val projectId: String?,
    val url: String,
    val thumbnailUrl: String,
    val width: Int,
    val height: Int,
    val format: String,
    val size: Long,
    val tags: List<String>,
    val metadata: ImageMetadata,
    val createdAt: Long
)

@Serializable
data class ImageMetadata(
    val colorPalette: List<String>,
    val dominantColor: String,
    val hasTransparency: Boolean,
    val dpi: Int?,
    val exifData: Map<String, String>? = null
)

@Serializable
data class VideoAsset(
    val id: String,
    val projectId: String?,
    val url: String,
    val thumbnailUrl: String,
    val duration: Int,
    val format: String,
    val resolution: String,
    val frameRate: Float,
    val size: Long,
    val metadata: VideoMetadata,
    val createdAt: Long
)

@Serializable
data class VideoMetadata(
    val codec: String,
    val bitrate: Long,
    val hasAudio: Boolean,
    val aspectRatio: String,
    val videoTracks: Int? = null,
    val audioTracks: Int? = null,
    val subtitleTracks: Int? = null
)

@Serializable
data class AudioAsset(
    val id: String,
    val projectId: String?,
    val url: String,
    val duration: Int,
    val format: String,
    val bitrate: Int,
    val sampleRate: Int,
    val size: Long,
    val metadata: AudioMetadata,
    val createdAt: Long
)

@Serializable
data class AudioMetadata(
    val channels: Int,
    val codec: String,
    val hasLyrics: Boolean,
    val genre: String?,
    val artist: String? = null,
    val album: String? = null,
    val year: Int? = null,
    val bpm: Int? = null
)

@Serializable
data class VisualStyle(
    val id: String,
    val projectId: String,
    val name: String,
    val description: String,
    val previewUrl: String?,
    val parameters: StyleParameters,
    val applicableTypes: List<AssetType>,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class StyleParameters(
    val colorScheme: ColorScheme?,
    val typography: Typography?,
    val effects: List<VisualEffect>,
    val filters: List<Filter>,
    val customCss: String?,
    val layout: LayoutSettings? = null
)

@Serializable
data class ColorScheme(
    val primary: String,
    val secondary: String,
    val accent: String,
    val background: String,
    val text: String,
    val additionalColors: List<String> = emptyList()
)

@Serializable
data class Typography(
    val fontFamily: String,
    val fontSize: Int,
    val fontWeight: String,
    val lineHeight: Float,
    val letterSpacing: Float,
    val textAlign: String? = null
)

@Serializable
data class VisualEffect(
    val type: String,
    val intensity: Float,
    val parameters: Map<String, String>
)

@Serializable
data class Filter(
    val name: String,
    val value: String,
    val unit: String? = null
)

@Serializable
data class LayoutSettings(
    val columns: Int? = null,
    val spacing: Float? = null,
    val padding: Float? = null,
    val alignment: String? = null
)

@Serializable
data class UploadProgress(
    val assetId: String,
    val progress: Float,
    val status: UploadStatus,
    val error: String?,
    val bytesUploaded: Long? = null,
    val totalBytes: Long? = null,
    val estimatedTimeRemaining: Int? = null
)

// Content Processing Status enum (to avoid conflict with ProcessingStatus data class)
@Serializable
enum class ContentProcessingStatus {
    NONE,
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED,
    PAUSED,
    QUEUED,
    PROCESSING,
    FINALIZING,
    UPLOADING,
    TRANSCODING,
    ANALYZING,
    OPTIMIZING
}


@Serializable
data class ImageUploadRequest(
    val projectId: String,
    val file: ByteArray,
    val filename: String,
    val tags: List<String>,
    val metadata: Map<String, String>? = null
)

@Serializable
data class VideoUploadRequest(
    val projectId: String,
    val file: ByteArray,
    val filename: String,
    val tags: List<String>,
    val autoTranscode: Boolean = true
)

@Serializable
data class AudioUploadRequest(
    val projectId: String,
    val file: ByteArray,
    val filename: String,
    val tags: List<String>,
    val normalize: Boolean = false
)

@Serializable
data class LibraryItem(
    val id: String,
    val assetId: String,
    val assetType: AssetType,
    val title: String,
    val thumbnailUrl: String,
    val tags: List<String>,
    val collections: List<String>,
    val addedAt: Long,
    val lastUsed: Long?
)

@Serializable
data class AssetCollection(
    val id: String,
    val name: String,
    val description: String?,
    val coverImageUrl: String?,
    val assetCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)


@Serializable
enum class AssetType {
    AVATAR, IMAGE, VIDEO, AUDIO, STYLE, FONT, TEMPLATE, DOCUMENT, MODEL_3D
}

@Serializable
enum class UploadStatus {
    PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED, PAUSED
}




@Serializable
data class VideoEditState(
    val frameId: String? = null,
    val sceneId: String? = null,
    val projectId: String,
    val videoUrl: String? = null,
    val duration: Float = 0f,
    val currentTime: Float = 0f,
    val isPlaying: Boolean = false,
    val volume: Float = 1f,
    val hasAudio: Boolean = false,
    // Effects and overlays
    val visualEffect: String? = null,
    val stylePreset: String? = null,
    val musicTrack: MusicTrack? = null,
    val hashtags: List<String> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val emojiOverlays: List<EmojiOverlay> = emptyList(),
    // Edit history
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val hasChanges: Boolean = false
)

@Serializable
data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String? = null,
    val url: String,
    val duration: Float,
    val startTime: Float = 0f,
    val volume: Float = 1f,
    val fadeIn: Boolean = false,
    val fadeOut: Boolean = false
)

@Serializable
data class TextOverlay(
    val id: String,
    val text: String,
    val position: Float = 0f,
    val x: Float,  // Changed from Offset to Float coordinates
    val y: Float,
    val fontSize: Float = 24f,
    val fontFamily: String = "System",
    val color: String = "#FFFFFF",
    val backgroundColor: String? = null,
    val startTime: Float,
    val endTime: Float = 5f,
    val duration: Float,
    val animation: TextAnimation? = null
)

/**
 * Emoji overlay
 */
@Serializable
data class EmojiOverlay(
    val id: String,
    val emoji: String,
    val x: Float,
    val y: Float,
    val size: Float = 48f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val startTime: Float,
    val endTime: Float = 5f,
    val duration: Float
)

@Serializable
enum class EditTool {
    TRIM, CROP, ROTATE, DRAW, TEXT, STICKER, FILTER, EFFECT
}


@Serializable
data class ColorGrading(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val warmth: Float = 0f,
    val tint: Float = 0f,
    val vignette: Float = 0f
)


@Serializable
data class LUT(
    val id: String,
    val name: String,
    val url: String,
    val intensity: Float = 1f
)

/**
 * Text animation types
 */
@Serializable
enum class TextAnimation {
    FADE_IN, FADE_OUT, SLIDE_IN, SLIDE_OUT, BOUNCE, TYPEWRITER
}


/**
 * Sticker overlay
 */
@Serializable
data class Sticker(
    val id: String,
    val url: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rotation: Float = 0f,
    val startTime: Float,
    val duration: Float
)

/**
 * Caption for accessibility
 */
@Serializable
data class Caption(
    val id: String,
    val text: String,
    val startTime: Float,
    val endTime: Float,
    val language: String = "en"
)

/**
 * Watermark settings
 */
@Serializable
data class Watermark(
    val id: String,
    val imageUrl: String? = null,
    val text: String? = null,
    val x: Float,
    val y: Float,
    val opacity: Float = 0.5f,
    val scale: Float = 1f
)

/**
 * Avatar video for AI generation
 */
@Serializable
data class AvatarVideo(
    val id: String,
    val avatarId: String,
    val videoUrl: String,
    val duration: Float,
    val platform: String? = null
)

/**
 * Generated scene from AI
 */
@Serializable
data class GeneratedScene(
    val id: String,
    val sceneId: String,
    val prompt: String,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val duration: Float,
    val generatedAt: Long
)

/**
 * Voiceover settings
 */
@Serializable
data class Voiceover(
    val id: String,
    val text: String,
    val voiceId: String,
    val audioUrl: String? = null,
    val language: String = "en",
    val speed: Float = 1f,
    val pitch: Float = 1f,
    val volume: Float = 1f
)

/**
 * Animation settings
 */
@Serializable
data class Animation(
    val id: String,
    val type: AnimationType,
    val duration: Float,
    val delay: Float = 0f,
    val easing: String = "ease-in-out"
)

@Serializable
enum class AnimationType {
    FADE, SLIDE, SCALE, ROTATE, BOUNCE, FLIP
}

/**
 * Motion effect
 */
@Serializable
data class MotionEffect(
    val id: String,
    val type: MotionType,
    val intensity: Float = 1f,
    val startTime: Float,
    val duration: Float
)

@Serializable
enum class MotionType {
    ZOOM_IN, ZOOM_OUT, PAN_LEFT, PAN_RIGHT, TILT_UP, TILT_DOWN, SHAKE, SPIN
}

/**
 * Platform-specific export format

@Serializable
data class ExportFormat(
    val platform: String,
    val maxDuration: Int? = null,
    val aspectRatio: AspectRatio,
    val resolution: Resolution,
    val maxFileSize: Long? = null,
    val recommendedBitrate: Int,
    val requiresWatermark: Boolean = false
)
 */

/**
 * Edit action for undo/redo
 */
@Serializable
sealed class EditAction {
    @Serializable
    data class AddOverlay(val overlayType: String, val overlayId: String) : EditAction()

    @Serializable
    data class RemoveOverlay(val overlayId: String) : EditAction()

    @Serializable
    data class ApplyEffect(val effect: String) : EditAction()

    @Serializable
    data class ChangeTiming(val oldTime: Float, val newTime: Float) : EditAction()

    @Serializable
    data class ModifyFrame(val frameId: String, val property: String, val value: String) : EditAction()

    @Serializable
    data class AddTransition(val transition: TransitionType) : EditAction()

    @Serializable
    data class ChangeVolume(val oldVolume: Float, val newVolume: Float) : EditAction()
}

/**
 * Media information
 */
@Serializable
data class MediaInfo(
    val duration: Float,
    val width: Int,
    val height: Int,
    val hasAudio: Boolean,
    val frameRate: Float,
    val bitrate: Int? = null,
    val codec: String? = null
)

/**
 * Generated content from frame generation
 */
@Serializable
data class GeneratedFrameContent(
    val frameId: String,
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val thumbnailUrl: String? = null,
    val duration: Float
)

/**
 * Stitched video result
 */
@Serializable
data class StitchedVideo(
    val url: String,
    val duration: Float,
    val frameCount: Int
)
