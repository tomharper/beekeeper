import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.AspectRatio
import com.beekeeper.app.domain.model.SocialPlatform
import com.beekeeper.app.domain.repository.ContentDimensions
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * Comprehensive ViewModel for Video Editor Screen
 * Supports cross-platform content creation and export to multiple social media platforms
 */
class VideoEditorViewModel : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(VideoEditorUiState())
    val uiState: StateFlow<VideoEditorUiState> = _uiState.asStateFlow()

    // Project ds
    private val _project = MutableStateFlow<VideoProject?>(null)
    val project: StateFlow<VideoProject?> = _project.asStateFlow()

    // Export State
    private val _exportState = MutableStateFlow(ExportState())
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    // Timeline State
    private val _timelineState = MutableStateFlow(TimelineState())
    val timelineState: StateFlow<TimelineState> = _timelineState.asStateFlow()

    // Playback State
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // Effects and Filters State
    private val _effectsState = MutableStateFlow(EffectsState())
    val effectsState: StateFlow<EffectsState> = _effectsState.asStateFlow()

    // Platform-specific configurations
    private val _platformConfigs = MutableStateFlow(PlatformConfigurations())
    val platformConfigs: StateFlow<PlatformConfigurations> = _platformConfigs.asStateFlow()

    // AI Features State
    private val _aiState = MutableStateFlow(AIFeaturesState())
    val aiState: StateFlow<AIFeaturesState> = _aiState.asStateFlow()

    // Project Management
    fun createNewProject(config: ProjectConfig) {
        viewModelScope.launch {
            val newProject = VideoProject(
                id = generateId(),
                name = config.name,
                createdAt = Clock.System.now(),
                modifiedAt = Clock.System.now(),
                config = config,
                timeline = Timeline(),
                metadata = ProjectMetadata()
            )
            _project.value = newProject
            _uiState.value = _uiState.value.copy(
                isProjectLoaded = true,
                currentProjectId = newProject.id
            )
        }
    }

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Load project implementation
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun saveProject() {
        viewModelScope.launch {
            _project.value?.let { proj ->
                // Save project implementation
                _uiState.value = _uiState.value.copy(
                    lastSaveTime = Clock.System.now(),
                    hasUnsavedChanges = false
                )
            }
        }
    }

    // Media Management
    fun importMedia(mediaItems: List<MediaItem>) {
        viewModelScope.launch {
            val currentTimeline = _timelineState.value
            val updatedMediaPool = currentTimeline.mediaPool + mediaItems
            _timelineState.value = currentTimeline.copy(
                mediaPool = updatedMediaPool,
                totalMediaCount = updatedMediaPool.size
            )
        }
    }

    fun addMediaToTimeline(mediaItem: MediaItem, trackId: String, position: Long) {
        viewModelScope.launch {
            val clip = TimelineClip(
                id = generateId(),
                mediaId = mediaItem.id,
                trackId = trackId,
                startTime = position,
                duration = mediaItem.duration,
                inPoint = 0L,
                outPoint = mediaItem.duration,
                effects = mutableListOf(),
                transitions = mutableListOf()
            )

            val currentTimeline = _timelineState.value
            val updatedClips = currentTimeline.clips + clip
            _timelineState.value = currentTimeline.copy(
                clips = updatedClips,
                totalDuration = calculateTotalDuration(updatedClips)
            )

            _uiState.value = _uiState.value.copy(hasUnsavedChanges = true)
        }
    }

    // Editing Operations
    fun splitClip(clipId: String, splitTime: Long) {
        viewModelScope.launch {
            // Split clip implementation
            _uiState.value = _uiState.value.copy(hasUnsavedChanges = true)
        }
    }

    fun trimClip(clipId: String, newInPoint: Long, newOutPoint: Long) {
        viewModelScope.launch {
            val currentTimeline = _timelineState.value
            val updatedClips = currentTimeline.clips.map { clip ->
                if (clip.id == clipId) {
                    clip.copy(inPoint = newInPoint, outPoint = newOutPoint)
                } else clip
            }
            _timelineState.value = currentTimeline.copy(clips = updatedClips)
            _uiState.value = _uiState.value.copy(hasUnsavedChanges = true)
        }
    }

    fun deleteClip(clipId: String) {
        viewModelScope.launch {
            val currentTimeline = _timelineState.value
            val updatedClips = currentTimeline.clips.filter { it.id != clipId }
            _timelineState.value = currentTimeline.copy(
                clips = updatedClips,
                totalDuration = calculateTotalDuration(updatedClips)
            )
            _uiState.value = _uiState.value.copy(hasUnsavedChanges = true)
        }
    }

    // Effects and Filters
    fun applyEffect(clipId: String, effect: VideoEffect) {
        viewModelScope.launch {
            val currentTimeline = _timelineState.value
            val updatedClips = currentTimeline.clips.map { clip ->
                if (clip.id == clipId) {
                    clip.copy(effects = clip.effects + effect)
                } else clip
            }
            _timelineState.value = currentTimeline.copy(clips = updatedClips)
            _uiState.value = _uiState.value.copy(hasUnsavedChanges = true)
        }
    }

    fun applyGlobalFilter(filter: VideoFilter) {
        viewModelScope.launch {
            val currentEffects = _effectsState.value
            _effectsState.value = currentEffects.copy(
                globalFilters = currentEffects.globalFilters + filter
            )
            _uiState.value = _uiState.value.copy(hasUnsavedChanges = true)
        }
    }

    // AI Features
    fun generateCaptions() {
        viewModelScope.launch {
            _aiState.value = _aiState.value.copy(isGeneratingCaptions = true)
            // AI caption generation implementation
            _aiState.value = _aiState.value.copy(isGeneratingCaptions = false)
        }
    }

    fun autoEditVideo() {
        viewModelScope.launch {
            _aiState.value = _aiState.value.copy(isAutoEditing = true)
            // AI auto-edit implementation
            _aiState.value = _aiState.value.copy(isAutoEditing = false)
        }
    }

    fun generateMusic() {
        viewModelScope.launch {
            _aiState.value = _aiState.value.copy(isGeneratingMusic = true)
            // AI music generation implementation
            _aiState.value = _aiState.value.copy(isGeneratingMusic = false)
        }
    }

    // Playback Control
    fun play() {
        _playbackState.value = _playbackState.value.copy(
            isPlaying = true,
            isPaused = false
        )
    }

    fun pause() {
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isPaused = true
        )
    }

    fun seek(position: Long) {
        _playbackState.value = _playbackState.value.copy(
            currentPosition = position
        )
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(
            playbackSpeed = speed
        )
    }

    // Export Functions
    fun prepareExport(platform: SocialPlatform) {
        viewModelScope.launch {
            val config = getOptimalExportConfig(platform)
            _exportState.value = _exportState.value.copy(
                selectedPlatform = platform,
                exportConfig = config,
                isConfigured = true
            )
        }
    }

    fun startExport() {
        viewModelScope.launch {
            _exportState.value = _exportState.value.copy(
                isExporting = true,
                exportProgress = 0f
            )
            // Export implementation
        }
    }

    fun cancelExport() {
        _exportState.value = _exportState.value.copy(
            isExporting = false,
            exportProgress = 0f
        )
    }

    // Undo/Redo
    fun undo() {
        viewModelScope.launch {
            // Undo implementation
            _uiState.value = _uiState.value.copy(
                canUndo = false, // Update based on history
                canRedo = true
            )
        }
    }

    fun redo() {
        viewModelScope.launch {
            // Redo implementation
            _uiState.value = _uiState.value.copy(
                canUndo = true,
                canRedo = false // Update based on history
            )
        }
    }

    // Helper Functions
    private fun generateId(): String {
        return "id_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(100000)}"
    }

    private fun calculateTotalDuration(clips: List<TimelineClip>): Long {
        return clips.maxOfOrNull { it.startTime + (it.outPoint - it.inPoint) } ?: 0L
    }

    private fun getOptimalExportConfig(platform: SocialPlatform): ExportConfig {
        return when (platform) {
            SocialPlatform.YOUTUBE -> ExportConfig(
                resolution = Resolution(1920, 1080),
                frameRate = 30,
                bitrate = 8000000,
                codec = "H.264",
                audioCodec = "AAC",
                audioBitrate = 256000,
                format = "MP4",
                aspectRatio = AspectRatio.RATIO_16_9
            )
            SocialPlatform.TIKTOK -> ExportConfig(
                resolution = Resolution(1080, 1920),
                frameRate = 30,
                bitrate = 6000000,
                codec = "H.264",
                audioCodec = "AAC",
                audioBitrate = 128000,
                format = "MP4",
                aspectRatio = AspectRatio.RATIO_9_16
            )
            SocialPlatform.INSTAGRAM -> ExportConfig(
                resolution = Resolution(1080, 1920),
                frameRate = 30,
                bitrate = 5000000,
                codec = "H.264",
                audioCodec = "AAC",
                audioBitrate = 128000,
                format = "MP4",
                aspectRatio = AspectRatio.RATIO_9_16
            )
            SocialPlatform.TIKTOK -> ExportConfig(
                resolution = Resolution(1080, 1080),
                frameRate = 30,
                bitrate = 5000000,
                codec = "H.264",
                audioCodec = "AAC",
                audioBitrate = 128000,
                format = "MP4",
                aspectRatio = AspectRatio.RATIO_1_1
            )
            else -> ExportConfig() // Default config
        }
    }
}

// Data Classes and States

data class VideoEditorUiState(
    val isLoading: Boolean = false,
    val isProjectLoaded: Boolean = false,
    val currentProjectId: String? = null,
    val hasUnsavedChanges: Boolean = false,
    val lastSaveTime: Instant? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val selectedTool: EditingTool = EditingTool.SELECTION,
    val zoomLevel: Float = 1.0f,
    val showGrid: Boolean = false,
    val showWaveforms: Boolean = true,
    val showThumbnails: Boolean = true,
    val errorMessage: String? = null
)

data class VideoProject(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val modifiedAt: Instant,
    val config: ProjectConfig,
    val timeline: Timeline,
    val metadata: ProjectMetadata,
    val version: String = "1.0.0"
)

data class ProjectConfig(
    val name: String,
    val resolution: Resolution,
    val frameRate: Int,
    val aspectRatio: AspectRatio,
    val colorSpace: ColorSpace = ColorSpace.REC_709,
    val audioSampleRate: Int = 48000,
    val audioChannels: Int = 2
)

data class Timeline(
    val tracks: List<Track> = emptyList(),
    val markers: List<Marker> = emptyList(),
    val duration: Long = 0L
)

data class TimelineState(
    val clips: List<TimelineClip> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val currentTime: Long = 0L,
    val totalDuration: Long = 0L,
    val selectedClips: Set<String> = emptySet(),
    val mediaPool: List<MediaItem> = emptyList(),
    val totalMediaCount: Int = 0,
    val markers: List<Marker> = emptyList()
)

data class TimelineClip(
    val id: String,
    val mediaId: String,
    val trackId: String,
    val startTime: Long,
    val duration: Long,
    val inPoint: Long,
    val outPoint: Long,
    val effects: List<VideoEffect>,
    val transitions: List<Transition>,
    val opacity: Float = 1.0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val position: Position = Position(0f, 0f),
    val keyframes: List<Keyframe> = emptyList()
)

data class Track(
    val id: String,
    val type: TrackType,
    val name: String,
    val isLocked: Boolean = false,
    val isVisible: Boolean = true,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
    val opacity: Float = 1.0f
)



@Serializable
data class PublishableContent(
    val contentId: String,
    val title: String,
    val description: String,
    val mediaUrls: List<String>,
    val mediaType: MediaType,
    val duration: Int? = null,
    val fileSize: Long? = null,
    val dimensions: ContentDimensions? = null
)


@Serializable
enum class MediaType {
    VIDEO,
    IMAGE,
    AUDIO,
    CAROUSEL,
    STORY,
    REEL,
    SHORT,
    LIVE,
    NONE
}


data class MediaItem(
    val id: String,
    val uri: String,
    val type: MediaType,
    val duration: Long,
    val thumbnail: String?,
    val metadata: MediaMetadata,
    val fileSize: Long,
    val importedAt: Instant
)

data class MediaMetadata(
    val width: Int = 0,
    val height: Int = 0,
    val frameRate: Float = 0f,
    val codec: String = "",
    val bitrate: Long = 0L,
    val audioCodec: String? = null,
    val audioBitrate: Long? = null,
    val audioChannels: Int? = null,
    val hasAlpha: Boolean = false
)

data class PlaybackState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentPosition: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isLooping: Boolean = false,
    val loopStart: Long = 0L,
    val loopEnd: Long = 0L,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false
)

data class ExportState(
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val selectedPlatform: SocialPlatform? = null,
    val exportConfig: ExportConfig? = null,
    val isConfigured: Boolean = false,
    val exportPath: String? = null,
    val estimatedFileSize: Long = 0L,
    val estimatedTime: Long = 0L,
    val exportErrors: List<String> = emptyList()
)

data class ExportConfig(
    val resolution: Resolution = Resolution(1920, 1080),
    val frameRate: Int = 30,
    val bitrate: Long = 8000000,
    val codec: String = "H.264",
    val audioCodec: String = "AAC",
    val audioBitrate: Long = 256000,
    val format: String = "MP4",
    val aspectRatio: AspectRatio = AspectRatio.RATIO_16_9,
    val quality: ExportQuality = ExportQuality.HIGH,
    val includeWatermark: Boolean = false,
    val watermarkPosition: Position? = null,
    val includeMetadata: Boolean = true,
    val customMetadata: Map<String, String> = emptyMap()
)

data class EffectsState(
    val availableEffects: List<VideoEffect> = emptyList(),
    val availableFilters: List<VideoFilter> = emptyList(),
    val availableTransitions: List<Transition> = emptyList(),
    val globalFilters: List<VideoFilter> = emptyList(),
    val luts: List<LUT> = emptyList(),
    val presets: List<EffectPreset> = emptyList()
)

data class AIFeaturesState(
    val isGeneratingCaptions: Boolean = false,
    val generatedCaptions: List<Caption> = emptyList(),
    val isAutoEditing: Boolean = false,
    val autoEditSuggestions: List<EditSuggestion> = emptyList(),
    val isGeneratingMusic: Boolean = false,
    val generatedMusic: List<AudioTrack> = emptyList(),
    val isEnhancingVideo: Boolean = false,
    val isRemovingBackground: Boolean = false,
    val isStabilizing: Boolean = false,
    val aiModel: String = "default"
)

data class PlatformConfigurations(
    val youtube: PlatformConfig = PlatformConfig.youtube(),
    val tiktok: PlatformConfig = PlatformConfig.tiktok(),
    val instagram: PlatformConfig = PlatformConfig.instagram(),
    val facebook: PlatformConfig = PlatformConfig.facebook(),
    val twitter: PlatformConfig = PlatformConfig.twitter(),
    val linkedin: PlatformConfig = PlatformConfig.linkedin(),
    val snapchat: PlatformConfig = PlatformConfig.snapchat(),
    val pinterest: PlatformConfig = PlatformConfig.pinterest(),
    val reddit: PlatformConfig = PlatformConfig.reddit(),
    val twitch: PlatformConfig = PlatformConfig.twitch()
)

data class PlatformConfig(
    val platform: SocialPlatform,
    val maxDuration: Long,
    val minDuration: Long,
    val maxFileSize: Long,
    val supportedFormats: List<String>,
    val supportedCodecs: List<String>,
    val supportedResolutions: List<Resolution>,
    val supportedAspectRatios: List<AspectRatio>,
    val requiredMetadata: List<String>,
    val features: PlatformFeatures
) {
    companion object {
        fun youtube() = PlatformConfig(
            platform = SocialPlatform.YOUTUBE,
            maxDuration = 12 * 60 * 60 * 1000L, // 12 hours
            minDuration = 0L,
            maxFileSize = 256L * 1024 * 1024 * 1024, // 256GB
            supportedFormats = listOf("MP4", "MOV", "AVI", "WMV", "FLV", "3GPP", "WebM"),
            supportedCodecs = listOf("H.264", "H.265", "VP8", "VP9", "AV1"),
            supportedResolutions = listOf(
                Resolution(3840, 2160),
                Resolution(2560, 1440),
                Resolution(1920, 1080),
                Resolution(1280, 720),
                Resolution(854, 480)
            ),
            supportedAspectRatios = listOf(
                AspectRatio.RATIO_16_9,
                AspectRatio.RATIO_4_3,
                AspectRatio.RATIO_21_9
            ),
            requiredMetadata = listOf("title", "description"),
            features = PlatformFeatures(
                supportsChapters = true,
                supportsEndScreen = true,
                supportsCaptions = true,
                supportsCards = true,
                supportsPremiere = true,
                supportsScheduling = true,
                supportsMonetization = true,
                supports360Video = true,
                supportsHDR = true,
                supportsLiveStreaming = true
            )
        )

        fun tiktok() = PlatformConfig(
            platform = SocialPlatform.TIKTOK,
            maxDuration = 10 * 60 * 1000L, // 10 minutes
            minDuration = 3 * 1000L, // 3 seconds
            maxFileSize = 287L * 1024 * 1024, // 287MB
            supportedFormats = listOf("MP4", "MOV"),
            supportedCodecs = listOf("H.264"),
            supportedResolutions = listOf(
                Resolution(1080, 1920)
            ),
            supportedAspectRatios = listOf(
                AspectRatio.RATIO_9_16
            ),
            requiredMetadata = listOf("caption"),
            features = PlatformFeatures(
                supportsEffects = true,
                supportsFilters = true,
                supportsMusic = true,
                supportsStickers = true,
                supportsDuet = true,
                supportsStitch = true,
                supportsLiveStreaming = true,
                supportsHashtags = true
            )
        )

        fun instagram() = PlatformConfig(
            platform = SocialPlatform.INSTAGRAM,
            maxDuration = 90 * 1000L, // 90 seconds for reels
            minDuration = 3 * 1000L,
            maxFileSize = 4L * 1024 * 1024 * 1024, // 4GB
            supportedFormats = listOf("MP4", "MOV"),
            supportedCodecs = listOf("H.264"),
            supportedResolutions = listOf(
                Resolution(1080, 1920),
                Resolution(1080, 1080)
            ),
            supportedAspectRatios = listOf(
                AspectRatio.RATIO_9_16,
                AspectRatio.RATIO_1_1,
                AspectRatio.RATIO_4_5
            ),
            requiredMetadata = listOf("caption"),
            features = PlatformFeatures(
                supportsEffects = true,
                supportsFilters = true,
                supportsMusic = true,
                supportsStickers = true,
                supportsLocation = true,
                supportsMentions = true,
                supportsHashtags = true,
                supportsShoppingTags = true
            )
        )

        fun facebook() = PlatformConfig(
            platform = SocialPlatform.FACEBOOK,
            maxDuration = 240 * 60 * 1000L, // 240 minutes
            minDuration = 1000L,
            maxFileSize = 10L * 1024 * 1024 * 1024, // 10GB
            supportedFormats = listOf("MP4", "MOV"),
            supportedCodecs = listOf("H.264"),
            supportedResolutions = listOf(
                Resolution(1920, 1080),
                Resolution(1280, 720)
            ),
            supportedAspectRatios = listOf(
                AspectRatio.RATIO_16_9,
                AspectRatio.RATIO_9_16,
                AspectRatio.RATIO_1_1
            ),
            requiredMetadata = listOf("title"),
            features = PlatformFeatures(
                supportsCaptions = true,
                supportsLiveStreaming = true,
                supports360Video = true,
                supportsCrossposting = true,
                supportsScheduling = true,
                supportsPremiere = true
            )
        )

        fun twitter() = PlatformConfig(
            platform = SocialPlatform.TWITTER,
            maxDuration = 140 * 1000L, // 2:20 minutes
            minDuration = 500L,
            maxFileSize = 512L * 1024 * 1024, // 512MB
            supportedFormats = listOf("MP4", "MOV"),
            supportedCodecs = listOf("H.264"),
            supportedResolutions = listOf(
                Resolution(1920, 1080),
                Resolution(1280, 720)
            ),
            supportedAspectRatios = listOf(
                AspectRatio.RATIO_16_9,
                AspectRatio.RATIO_1_1
            ),
            requiredMetadata = listOf("tweet"),
            features = PlatformFeatures(
                supportsCaptions = true,
                supportsGifs = true,
                supportsThreads = true,
                supportsHashtags = true,
                supportsMentions = true
            )
        )

        fun linkedin() = PlatformConfig(
            platform = SocialPlatform.LINKEDIN,
            maxDuration = 10 * 60 * 1000L, // 10 minutes
            minDuration = 3 * 1000L,
            maxFileSize = 5L * 1024 * 1024 * 1024, // 5GB
            supportedFormats = listOf("MP4"),
            supportedCodecs = listOf("H.264"),
            supportedResolutions = listOf(
                Resolution(1920, 1080),
                Resolution(1280, 720)
            ),
            supportedAspectRatios = listOf(
                AspectRatio.RATIO_16_9,
                AspectRatio.RATIO_1_1
            ),
            requiredMetadata = listOf("title", "description"),
            features = PlatformFeatures(
                supportsCaptions = true,
                supportsNativeVideo = true,
                supportsArticles = true,
                supportsHashtags = true
            )
        )

        fun snapchat() = PlatformConfig(
            platform = SocialPlatform.SNAPCHAT,
            maxDuration = 60 * 1000L, // 60 seconds
            minDuration = 3 * 1000L,
            maxFileSize = 1024L * 1024 * 1024, // 1GB
            supportedFormats = listOf("MP4", "MOV"),
            supportedCodecs = listOf("H.264"),
            supportedResolutions = listOf(
                Resolution(1080, 1920)
            ),
            supportedAspectRatios = listOf(
                AspectRatio.RATIO_9_16
            ),
            requiredMetadata = emptyList(),
            features = PlatformFeatures(
                supportsFilters = true,
                supportsLenses = true,
                supportsStickers = true,
                supportsText = true,
                supportsDrawing = true,
                supportsGeofilters = true
            )
        )

        fun pinterest() = PlatformConfig(
            platform = SocialPlatform.PINTEREST,
            maxDuration = 15 * 60 * 1000L, // 15 minutes
            minDuration = 4 * 1000L,
            maxFileSize = 2L * 1024 * 1024 * 1024, // 2GB
            supportedFormats = listOf("MP4", "MOV"),
            supportedCodecs = listOf("H.264"),
            supportedResolutions = listOf(
                Resolution(1080, 1920),
                Resolution(1080, 1080),
                Resolution(720, 1280)
            ),
            supportedAspectRatios = listOf(
                AspectRatio.RATIO_9_16,
                AspectRatio.RATIO_1_1,
                AspectRatio.RATIO_2_3
            ),
            requiredMetadata = listOf("title", "description"),
            features = PlatformFeatures(
                supportsIdeaPins = true,
                supportsStoryPins = true,
                supportsHashtags = true
            )
        )

        fun reddit() = PlatformConfig(
            platform = SocialPlatform.REDDIT,
            maxDuration = 15 * 60 * 1000L, // 15 minutes
            minDuration = 0L,
            maxFileSize = 1L * 1024 * 1024 * 1024, // 1GB
            supportedFormats = listOf("MP4", "MOV", "GIF"),
            supportedCodecs = listOf("H.264"),
            supportedResolutions = listOf(
                Resolution(1920, 1080),
                Resolution(1280, 720)
            ),
            supportedAspectRatios = listOf(
                AspectRatio.RATIO_16_9,
                AspectRatio.RATIO_9_16,
                AspectRatio.RATIO_1_1
            ),
            requiredMetadata = listOf("title"),
            features = PlatformFeatures(
                supportsGifs = true,
                supportsCrossposting = true,
                supportsNSFWContent = true
            )
        )

        fun twitch() = PlatformConfig(
            platform = SocialPlatform.TWITCH,
            maxDuration = 48 * 60 * 60 * 1000L, // 48 hours (VODs)
            minDuration = 0L,
            maxFileSize = Long.MAX_VALUE, // No specific limit for VODs
            supportedFormats = listOf("MP4", "MOV", "FLV"),
            supportedCodecs = listOf("H.264", "H.265"),
            supportedResolutions = listOf(
                Resolution(3840, 2160),
                Resolution(1920, 1080),
                Resolution(1280, 720)
            ),
            supportedAspectRatios = listOf(
                AspectRatio.RATIO_16_9
            ),
            requiredMetadata = listOf("title", "category"),
            features = PlatformFeatures(
                supportsLiveStreaming = true,
                supportsClips = true,
                supportsHighlights = true,
                supportsVOD = true,
                supportsRaids = true,
                supportsSubscriptions = true,
                supportsBitsAndDonations = true
            )
        )
    }
}

data class PlatformFeatures(
    val supportsLiveStreaming: Boolean = false,
    val supports360Video: Boolean = false,
    val supportsHDR: Boolean = false,
    val supportsCaptions: Boolean = false,
    val supportsChapters: Boolean = false,
    val supportsEndScreen: Boolean = false,
    val supportsCards: Boolean = false,
    val supportsPremiere: Boolean = false,
    val supportsScheduling: Boolean = false,
    val supportsMonetization: Boolean = false,
    val supportsEffects: Boolean = false,
    val supportsFilters: Boolean = false,
    val supportsMusic: Boolean = false,
    val supportsStickers: Boolean = false,
    val supportsDuet: Boolean = false,
    val supportsStitch: Boolean = false,
    val supportsHashtags: Boolean = false,
    val supportsLocation: Boolean = false,
    val supportsMentions: Boolean = false,
    val supportsShoppingTags: Boolean = false,
    val supportsCrossposting: Boolean = false,
    val supportsGifs: Boolean = false,
    val supportsThreads: Boolean = false,
    val supportsNativeVideo: Boolean = false,
    val supportsArticles: Boolean = false,
    val supportsLenses: Boolean = false,
    val supportsText: Boolean = false,
    val supportsDrawing: Boolean = false,
    val supportsGeofilters: Boolean = false,
    val supportsIdeaPins: Boolean = false,
    val supportsStoryPins: Boolean = false,
    val supportsNSFWContent: Boolean = false,
    val supportsClips: Boolean = false,
    val supportsHighlights: Boolean = false,
    val supportsVOD: Boolean = false,
    val supportsRaids: Boolean = false,
    val supportsSubscriptions: Boolean = false,
    val supportsBitsAndDonations: Boolean = false
)

// Supporting Classes
data class VideoEffect(
    val id: String,
    val type: EffectType,
    val name: String,
    val parameters: Map<String, Any>,
    val intensity: Float = 1.0f,
    val isEnabled: Boolean = true
)

data class VideoFilter(
    val id: String,
    val name: String,
    val type: FilterType,
    val parameters: Map<String, Any>,
    val intensity: Float = 1.0f
)

data class Transition(
    val id: String,
    val type: TransitionType,
    val duration: Long,
    val parameters: Map<String, Any>
)

data class Caption(
    val id: String,
    val text: String,
    val startTime: Long,
    val endTime: Long,
    val style: CaptionStyle,
    val position: Position
)

data class CaptionStyle(
    val fontFamily: String,
    val fontSize: Float,
    val fontColor: String,
    val backgroundColor: String?,
    val bold: Boolean,
    val italic: Boolean,
    val underline: Boolean,
    val alignment: TextAlignment
)

data class EditSuggestion(
    val id: String,
    val type: SuggestionType,
    val description: String,
    val confidence: Float,
    val clipIds: List<String>,
    val suggestedAction: () -> Unit
)

data class AudioTrack(
    val id: String,
    val name: String,
    val duration: Long,
    val uri: String,
    val genre: String?,
    val mood: String?,
    val tempo: Int?
)

data class LUT(
    val id: String,
    val name: String,
    val filePath: String,
    val thumbnail: String?
)

data class EffectPreset(
    val id: String,
    val name: String,
    val effects: List<VideoEffect>,
    val filters: List<VideoFilter>,
    val thumbnail: String?
)

data class Marker(
    val id: String,
    val time: Long,
    val name: String,
    val color: String,
    val type: MarkerType
)

data class Keyframe(
    val id: String,
    val time: Long,
    val property: String,
    val value: Any,
    val interpolation: InterpolationType
)

data class Resolution(
    val width: Int,
    val height: Int
)

data class Position(
    val x: Float,
    val y: Float
)

data class ProjectMetadata(
    val tags: List<String> = emptyList(),
    val description: String = "",
    val thumbnail: String? = null,
    val collaborators: List<String> = emptyList(),
    val version: Int = 1,
    val customData: Map<String, Any> = emptyMap()
)


enum class TrackType {
    VIDEO,
    AUDIO,
    SUBTITLE,
    GRAPHICS,
    EFFECT,
    MUSIC,
    VOICEOVER,
    SOUND_EFFECT
}

enum class EffectType {
    COLOR_CORRECTION,
    COLOR_GRADING,
    BLUR,
    SHARPEN,
    DISTORTION,
    GLOW,
    SHADOW,
    KEYING,
    MASKING,
    TRANSFORM,
    TIME_REMAP,
    STABILIZATION,
    NOISE_REDUCTION,
    ANIMATION,
    PARTICLE,
    TEXT_ANIMATION,
    CUSTOM
}

enum class FilterType {
    VINTAGE,
    BLACK_WHITE,
    SEPIA,
    CINEMATIC,
    VIVID,
    DRAMATIC,
    WARM,
    COOL,
    FILM,
    INSTAGRAM,
    CUSTOM
}

enum class TransitionType {
    CUT,
    DISSOLVE,
    FADE,
    WIPE,
    SLIDE,
    PUSH,
    ZOOM,
    ROTATE,
    MORPH,
    GLITCH,
    CUSTOM
}


enum class ColorSpace {
    REC_709,
    REC_2020,
    DCI_P3,
    SRGB,
    ADOBE_RGB,
    ACES
}

enum class ExportQuality {
    LOW,
    MEDIUM,
    HIGH,
    ULTRA,
    CUSTOM
}

enum class EditingTool {
    SELECTION,
    BLADE,
    TRIM,
    SLIP,
    SLIDE,
    RIPPLE,
    ROLL,
    RATE_STRETCH,
    PEN,
    HAND,
    ZOOM
}

enum class MarkerType {
    STANDARD,
    CHAPTER,
    COMMENT,
    TODO,
    SYNC_POINT,
    IN_POINT,
    OUT_POINT
}

enum class InterpolationType {
    LINEAR,
    BEZIER,
    HOLD,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT
}

enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT,
    JUSTIFY
}

enum class SuggestionType {
    CUT,
    TRANSITION,
    EFFECT,
    MUSIC_SYNC,
    PACE_ADJUSTMENT,
    COLOR_CORRECTION,
    AUDIO_ENHANCEMENT,
    REMOVE_SILENCE
}