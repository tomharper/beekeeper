// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/MediaViewModels.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.ExportFormat
import com.beekeeper.app.domain.model.Scene
import com.beekeeper.app.domain.model.StoryboardFrame
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Core Media ViewModel following the architecture decision - ViewModels only, no state classes
 * This is the main ViewModel for basic media operations
 */
class MediaViewModel : ViewModel() {
    // Image loading state as simple flow
    private val _imageLoadingState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val imageLoadingState: StateFlow<Map<String, Boolean>> = _imageLoadingState
    
    // Video playback state
    private val _currentVideoUrl = MutableStateFlow<String?>(null)
    val currentVideoUrl: StateFlow<String?> = _currentVideoUrl
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    /**
     * Load image - returns flow of loading state
     */
    fun loadImage(url: String): Flow<ImageLoadState> = flow {
        emit(ImageLoadState.Loading)
        _imageLoadingState.update { it + (url to true) }
        
        try {
            // Kamel handles the actual loading
            emit(ImageLoadState.Success(url))
        } catch (e: Exception) {
            emit(ImageLoadState.Error(e))
        } finally {
            _imageLoadingState.update { it + (url to false) }
        }
    }
    
    /**
     * Preload multiple images
     */
    fun preloadImages(urls: List<String>) {
        viewModelScope.launch {
            urls.forEach { url ->
                loadImage(url).collect()
            }
        }
    }
    
    /**
     * Load video
     */
    fun loadVideo(url: String) {
        _currentVideoUrl.value = url
        _isLoading.value = true
        _error.value = null
    }
    
    /**
     * Play/pause video
     */
    fun togglePlayback() {
        _isPlaying.value = !_isPlaying.value
    }
    
    fun play() {
        _isPlaying.value = true
    }
    
    fun pause() {
        _isPlaying.value = false
    }
    
    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        _isPlaying.value = false
        _currentVideoUrl.value = null
    }
}

/**
 * Image load state
 */
sealed class ImageLoadState {
    object Loading : ImageLoadState()
    data class Success(val url: String) : ImageLoadState()
    data class Error(val exception: Throwable) : ImageLoadState()
}

/**
 * Base Media ViewModel for reusable media functionality
 * Following the architecture decision - ViewModels only, no state classes
 */
abstract class BaseMediaViewModel : ViewModel() {
    // Current media URL
    protected val _currentMediaUrl = MutableStateFlow<String?>(null)
    val currentMediaUrl: StateFlow<String?> = _currentMediaUrl
    
    // Playback state
    protected val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    // Loading state
    protected val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Error state
    protected val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun play() {
        _isPlaying.value = true
    }
    
    fun pause() {
        _isPlaying.value = false
    }
    
    fun togglePlayback() {
        _isPlaying.value = !_isPlaying.value
    }
    
    open fun loadMedia(url: String) {
        _currentMediaUrl.value = url
        _isLoading.value = true
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        _isPlaying.value = false
        _currentMediaUrl.value = null
    }
}

/**
 * ViewModel for Storyboard frame playback
 */
class StoryboardMediaViewModel : BaseMediaViewModel() {
    // Storyboard-specific state
    private val _frames = MutableStateFlow<List<StoryboardFrame>>(emptyList())
    val frames: StateFlow<List<StoryboardFrame>> = _frames
    
    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex
    
    private val _autoPlayEnabled = MutableStateFlow(false)
    val autoPlayEnabled: StateFlow<Boolean> = _autoPlayEnabled
    
    private val _frameDelay = MutableStateFlow(2000L) // 2 seconds per frame
    val frameDelay: StateFlow<Long> = _frameDelay
    
    fun loadStoryboard(frames: List<StoryboardFrame>) {
        _frames.value = frames
        _currentFrameIndex.value = 0
        frames.firstOrNull()?.let { frame ->
            when {
                frame.sceneData.videoUrl != null -> loadMedia(frame.sceneData.videoUrl)
                frame.sceneData.imageUrl != null -> loadMedia(frame.sceneData.imageUrl)
            }
        }
    }
    
    fun nextFrame() {
        val frames = _frames.value
        val nextIndex = (_currentFrameIndex.value + 1).coerceAtMost(frames.size - 1)
        _currentFrameIndex.value = nextIndex
        
        frames.getOrNull(nextIndex)?.let { frame ->
            when {
                frame.sceneData.videoUrl != null -> loadMedia(frame.sceneData.videoUrl)
                frame.sceneData.imageUrl != null -> loadMedia(frame.sceneData.imageUrl)
            }
        }
    }
    
    fun previousFrame() {
        val frames = _frames.value
        val prevIndex = (_currentFrameIndex.value - 1).coerceAtLeast(0)
        _currentFrameIndex.value = prevIndex
        
        frames.getOrNull(prevIndex)?.let { frame ->
            when {
                frame.sceneData.videoUrl != null -> loadMedia(frame.sceneData.videoUrl)
                frame.sceneData.imageUrl != null -> loadMedia(frame.sceneData.imageUrl)
            }
        }
    }
    
    fun startAutoPlay() {
        _autoPlayEnabled.value = true
        viewModelScope.launch {
            while (_autoPlayEnabled.value && _currentFrameIndex.value < _frames.value.size - 1) {
                nextFrame()
            }
            _autoPlayEnabled.value = false
        }
    }
    
    fun stopAutoPlay() {
        _autoPlayEnabled.value = false
    }
    
    fun setFrameDelay(delayMs: Long) {
        _frameDelay.value = delayMs
    }
}

/**
 * ViewModel for Avatar video playback
 */
class AvatarMediaViewModel : BaseMediaViewModel() {
    // Avatar-specific state
    private val _avatarVideos = MutableStateFlow<List<AvatarVideo>>(emptyList())
    val avatarVideos: StateFlow<List<AvatarVideo>> = _avatarVideos
    
    private val _selectedAvatarId = MutableStateFlow<String?>(null)
    val selectedAvatarId: StateFlow<String?> = _selectedAvatarId
    
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating
    
    fun loadAvatarVideo(avatarId: String, videoUrl: String) {
        _selectedAvatarId.value = avatarId
        loadMedia(videoUrl)
    }
    
    fun generateAvatarVideo(prompt: String, platform: GenerationPlatform) {
        _isGenerating.value = true
        viewModelScope.launch {
            try {
                // In real app, call API
                val generatedUrl = "generated_avatar_video.mp4"
                loadMedia(generatedUrl)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isGenerating.value = false
            }
        }
    }
    
    fun saveAvatarToLibrary(avatarId: String, videoUrl: String) {
        val newAvatar = AvatarVideo(avatarId, videoUrl)
        _avatarVideos.value = _avatarVideos.value + newAvatar
    }
}

/**
 * ViewModel for Scene video playback
 */
class SceneMediaViewModel : BaseMediaViewModel() {
    // Scene-specific state
    private val _scenes = MutableStateFlow<List<Scene>>(emptyList())
    val scenes: StateFlow<List<Scene>> = _scenes
    
    private val _currentSceneId = MutableStateFlow<String?>(null)
    val currentSceneId: StateFlow<String?> = _currentSceneId
    
    private val _sceneTransitions = MutableStateFlow(true)
    val sceneTransitions: StateFlow<Boolean> = _sceneTransitions
    
    fun loadScene(sceneId: String, videoUrl: String) {
        _currentSceneId.value = sceneId
        loadMedia(videoUrl)
    }
    
    fun loadScenes(scenes: List<Scene>) {
        _scenes.value = scenes
        scenes.firstOrNull()?.let { scene ->
            loadScene(scene.id, scene.videoUrl.toString())
        }
    }
    
    fun playSceneSequence() {
        viewModelScope.launch {
            for (scene in _scenes.value) {
                loadScene(scene.id, scene.videoUrl.toString())
                play()
            }
            pause()
        }
    }
}

/**
 * ViewModel for Video Editor
 */
class VideoEditorViewModel : BaseMediaViewModel() {
    // Editor-specific state
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration
    
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed
    
    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume
    
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted
    
    private val _hasEdits = MutableStateFlow(false)
    val hasEdits: StateFlow<Boolean> = _hasEdits
    
    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress
    
    fun seekTo(position: Long) {
        _currentPosition.value = position.coerceIn(0, _duration.value)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed.coerceIn(0.25f, 2.0f)
    }
    
    fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
    }
    
    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }
    
    fun addCaption(text: String, startTime: Long, endTime: Long) {
        // Add caption logic
        _hasEdits.value = true
    }
    
    fun applyFilter(filter: VideoFilter) {
        // Apply filter logic
        _hasEdits.value = true
    }
    
    fun exportVideo(format: ExportFormat = ExportFormat.MP4) {
        viewModelScope.launch {
            _exportProgress.value = 0f
            for (i in 1..100) {
                _exportProgress.value = i / 100f
            }
            _hasEdits.value = false
        }
    }
}



data class AvatarVideo(
    val id: String,
    val videoUrl: String,
    val platform: GenerationPlatform = GenerationPlatform.HEYGEN,
    val createdAt: Long = getCurrentTimeMillis()
)

enum class GenerationPlatform {
    HEYGEN,
    DID,
    SYNTHESIA,
    STABLE_DIFFUSION,
    MIDJOURNEY,
    DALL_E,
    CUSTOM,
    KREA_AI
}

enum class VideoFilter {
    NONE,
    VINTAGE,
    BLACK_WHITE,
    CINEMATIC,
    WARM,
    COLD
}

