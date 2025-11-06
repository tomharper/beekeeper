// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/SceneDetailViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.repository.ContentRepository
import com.beekeeper.app.domain.repository.FrameRepository
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SceneDetailUiState(
    val scene: Scene? = null,
    val frames: List<Frame> = emptyList(),
    val storyboard: Storyboard? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFrameId: String? = null,
    val isPlaying: Boolean = false,
    val currentPlaybackFrame: Int = 0,
    val sortOption: FrameSortOption = FrameSortOption.FRAME_NUMBER,
    val viewType: FrameViewType = FrameViewType.GRID
)

enum class FrameSortOption {
    FRAME_NUMBER, TIMESTAMP, SHOT_TYPE, CAMERA_ANGLE, DATE_MODIFIED
}

enum class FrameViewType {
    GRID, LIST, TIMELINE, FILMSTRIP
}

class SceneDetailViewModel(
    private val projectId: String,
    private val storyboardId: String,
    private val sceneId: String,
    private val contentRepository: ContentRepository,
    private val frameRepository: FrameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SceneDetailUiState())
    val uiState: StateFlow<SceneDetailUiState> = _uiState.asStateFlow()

    init {
        loadSceneData()
        observeSceneUpdates()
    }

    // Public retry method for SceneDetailScreen
    fun retry() {
        loadSceneData()
    }

    private fun loadSceneData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load storyboard data
                val storyboard = contentRepository.getStoryboard(storyboardId)

                // Find the specific scene
                val scene = storyboard?.scenes?.find { it.id == sceneId }

                if (scene != null) {
                    // Use frames from the scene object first (superset approach)
                    var frames = scene.frames

                    // If scene has no frames, try to get from repository
                    if (frames.isEmpty()) {
                        frames = frameRepository.getFramesBySceneId(sceneId)
                    } else {
                        // Merge with repository frames to get superset
                        try {
                            val repoFrames = frameRepository.getFramesBySceneId(sceneId)
                            if (repoFrames.isNotEmpty()) {
                                val frameMap = frames.associateBy { it.id }.toMutableMap()
                                repoFrames.forEach { repoFrame ->
                                    if (!frameMap.containsKey(repoFrame.id)) {
                                        frameMap[repoFrame.id] = repoFrame
                                    }
                                }
                                frames = frameMap.values.sortedBy { it.frameNumber }
                            }
                        } catch (e: Exception) {
                            // If repository fails, just use scene frames
                        }
                    }

                    _uiState.update {
                        it.copy(
                            scene = scene,
                            frames = sortFrames(frames, it.sortOption),
                            storyboard = storyboard,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Scene not found",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load scene data",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeSceneUpdates() {
        // Observe frame updates from repository
        viewModelScope.launch {
            frameRepository.observeFramesBySceneId(sceneId)
                .collect { repoFrames ->
                    val currentScene = _uiState.value.scene
                    if (currentScene != null && currentScene.frames.isEmpty() && repoFrames.isNotEmpty()) {
                        _uiState.update { state ->
                            state.copy(frames = sortFrames(repoFrames, state.sortOption))
                        }
                    }
                }
        }
    }

    fun selectFrame(frameId: String?) {
        _uiState.update { it.copy(selectedFrameId = frameId) }
    }

    fun addFrame(
        description: String,
        shotType: ShotType? = null,
        cameraAngle: CameraAngle? = null,
        cameraMovement: CameraMovement? = null
    ) {
        viewModelScope.launch {
            try {
                val currentScene = _uiState.value.scene ?: return@launch
                val currentFrames = _uiState.value.frames
                val newFrameNumber = currentFrames.size + 1

                val newFrame = Frame(
                    id = "frame_${sceneId}_${getCurrentTimeMillis()}",
                    frameNumber = newFrameNumber,
                    sceneId = sceneId,
                    description = description,
                    timestamp = calculateTimestamp(newFrameNumber),
                    shotType = shotType ?: ShotType.MEDIUM_SHOT,
                    cameraAngle = cameraAngle ?: CameraAngle.EYE_LEVEL,
                    cameraMovement = cameraMovement,
                    duration = 2.0f,
                    transitionIn = TransitionType.CUT,
                    transitionOut = TransitionType.CUT,
                    dialogueLineId = null
                )

                // Add to repository
                frameRepository.addFrame(newFrame)

                // Update the scene object with new frame
                val updatedScene = currentScene.copy(
                    frames = currentScene.frames + newFrame
                )

                // Update the storyboard with updated scene
                val currentStoryboard = _uiState.value.storyboard
                if (currentStoryboard != null) {
                    val updatedStoryboard = currentStoryboard.copy(
                        scenes = currentStoryboard.scenes.map { s ->
                            if (s.id == sceneId) updatedScene else s
                        }
                    )

                    // Save updated storyboard
                    contentRepository.updateStoryboard(updatedStoryboard)
                }

                // Reload to get consistent data
                loadSceneData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to add frame: ${e.message}")
                }
            }
        }
    }

    fun updateFrame(
        frameId: String,
        description: String? = null,
        shotType: ShotType? = null,
        cameraAngle: CameraAngle? = null,
        cameraMovement: CameraMovement? = null
    ) {
        viewModelScope.launch {
            try {
                val currentScene = _uiState.value.scene ?: return@launch
                val frame = _uiState.value.frames.find { it.id == frameId }

                if (frame != null) {
                    val updatedFrame = frame.copy(
                        description = description ?: frame.description,
                        shotType = shotType ?: frame.shotType,
                        cameraAngle = cameraAngle ?: frame.cameraAngle,
                        cameraMovement = cameraMovement ?: frame.cameraMovement
                    )

                    // Update in repository
                    frameRepository.updateFrame(updatedFrame)

                    // Update the scene object's frames
                    val updatedScene = currentScene.copy(
                        frames = currentScene.frames.map { f ->
                            if (f.id == frameId) updatedFrame else f
                        }
                    )

                    // Update the storyboard
                    val currentStoryboard = _uiState.value.storyboard
                    if (currentStoryboard != null) {
                        val updatedStoryboard = currentStoryboard.copy(
                            scenes = currentStoryboard.scenes.map { s ->
                                if (s.id == sceneId) updatedScene else s
                            }
                        )

                        // Save updated storyboard
                        contentRepository.updateStoryboard(updatedStoryboard)
                    }

                    // Update local state immediately
                    _uiState.update { state ->
                        state.copy(
                            scene = updatedScene,
                            frames = sortFrames(updatedScene.frames, state.sortOption)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to update frame: ${e.message}")
                }
            }
        }
    }

    fun deleteFrame(frameId: String) {
        viewModelScope.launch {
            try {
                val currentScene = _uiState.value.scene ?: return@launch

                // Delete from repository
                frameRepository.deleteFrame(frameId)

                // Update the scene object's frames
                val updatedFrames = currentScene.frames
                    .filter { it.id != frameId }
                    .mapIndexed { index, frame ->
                        // Renumber remaining frames
                        frame.copy(
                            frameNumber = index + 1,
                            timestamp = calculateTimestamp(index + 1)
                        )
                    }

                val updatedScene = currentScene.copy(frames = updatedFrames)

                // Update the storyboard
                val currentStoryboard = _uiState.value.storyboard
                if (currentStoryboard != null) {
                    val updatedStoryboard = currentStoryboard.copy(
                        scenes = currentStoryboard.scenes.map { s ->
                            if (s.id == sceneId) updatedScene else s
                        }
                    )

                    // Save updated storyboard
                    contentRepository.updateStoryboard(updatedStoryboard)
                }

                // Reload to get consistent data
                loadSceneData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete frame: ${e.message}")
                }
            }
        }
    }

    fun reorderFrames(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                val currentScene = _uiState.value.scene ?: return@launch
                val frames = _uiState.value.frames.toMutableList()

                if (fromIndex in frames.indices && toIndex in frames.indices) {
                    val frame = frames.removeAt(fromIndex)
                    frames.add(toIndex, frame)

                    // Update frame numbers and timestamps
                    val reorderedFrames = frames.mapIndexed { index, f ->
                        f.copy(
                            frameNumber = index + 1,
                            timestamp = calculateTimestamp(index + 1)
                        )
                    }

                    // Update frames in repository
                    reorderedFrames.forEach { frame ->
                        frameRepository.updateFrame(frame)
                    }

                    // Update the scene object
                    val updatedScene = currentScene.copy(frames = reorderedFrames)

                    // Update the storyboard
                    val currentStoryboard = _uiState.value.storyboard
                    if (currentStoryboard != null) {
                        val updatedStoryboard = currentStoryboard.copy(
                            scenes = currentStoryboard.scenes.map { s ->
                                if (s.id == sceneId) updatedScene else s
                            }
                        )

                        // Save updated storyboard
                        contentRepository.updateStoryboard(updatedStoryboard)
                    }

                    // Update local state
                    _uiState.update { state ->
                        state.copy(
                            scene = updatedScene,
                            frames = reorderedFrames
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to reorder frames: ${e.message}")
                }
            }
        }
    }

    fun setSortOption(option: FrameSortOption) {
        _uiState.update { state ->
            state.copy(
                sortOption = option,
                frames = sortFrames(state.frames, option)
            )
        }
    }

    fun setViewType(type: FrameViewType) {
        _uiState.update { it.copy(viewType = type) }
    }

    fun playScene() {
        _uiState.update { it.copy(isPlaying = true, currentPlaybackFrame = 0) }
        startPlayback()
    }

    fun pauseScene() {
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun stopScene() {
        _uiState.update { it.copy(isPlaying = false, currentPlaybackFrame = 0) }
    }

    private fun startPlayback() {
        viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                val currentFrame = _uiState.value.currentPlaybackFrame
                val frames = _uiState.value.frames

                if (frames.isNotEmpty() && currentFrame < frames.size - 1) {
                    _uiState.update { it.copy(currentPlaybackFrame = currentFrame + 1) }
                    // Wait for frame duration
                } else {
                    // Loop back to start or stop
                    _uiState.update { it.copy(currentPlaybackFrame = 0, isPlaying = false) }
                }
            }
        }
    }

    private fun sortFrames(frames: List<Frame>, sortOption: FrameSortOption): List<Frame> {
        return when (sortOption) {
            FrameSortOption.FRAME_NUMBER -> frames.sortedBy { it.frameNumber }
            FrameSortOption.TIMESTAMP -> frames.sortedBy { it.timestamp }
            FrameSortOption.SHOT_TYPE -> frames.sortedBy { it.shotType.name }
            FrameSortOption.CAMERA_ANGLE -> frames.sortedBy { it.cameraAngle.name }
            FrameSortOption.DATE_MODIFIED -> frames.sortedBy { it.id }
        }
    }

    private fun calculateTimestamp(frameNumber: Int): String {
        // Assuming 2 seconds per frame for simplicity
        val totalSeconds = (frameNumber - 1) * 2
        val hours = totalSeconds / 3600
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return when {
            hours > 0 -> "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
            else -> "${minutes}:${seconds.toString().padStart(2, '0')}"
        }
    }
}