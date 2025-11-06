package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.repository.ContentRepository
import com.beekeeper.app.domain.repository.ContentRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoryboardDetailUiState(
    val storyboard: Storyboard? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSceneIndex: Int = 0,
    val isPlaying: Boolean = false,
    val selectedSceneId: String? = null
)

class StoryboardDetailViewModel(
    private val projectId: String,
    private val storyboardId: String,
    private val contentRepository: ContentRepository = ContentRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryboardDetailUiState())
    val uiState: StateFlow<StoryboardDetailUiState> = _uiState.asStateFlow()

    init {
        loadStoryboardData()
    }

    // Public method for retry functionality
    fun retry() {
        loadStoryboardData()
    }

    private fun loadStoryboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load storyboard with its scenes
                val storyboard = contentRepository.getStoryboard(storyboardId)

                if (storyboard != null) {
                    _uiState.update {
                        it.copy(
                            storyboard = storyboard,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Storyboard not found",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "An error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setCurrentSceneIndex(index: Int) {
        val scenes = _uiState.value.storyboard?.scenes ?: return
        if (index in scenes.indices) {
            _uiState.update {
                it.copy(
                    currentSceneIndex = index,
                    selectedSceneId = scenes[index].id
                )
            }
        }
    }

    fun selectScene(sceneId: String) {
        val scenes = _uiState.value.storyboard?.scenes ?: return
        val index = scenes.indexOfFirst { it.id == sceneId }
        if (index != -1) {
            _uiState.update {
                it.copy(
                    currentSceneIndex = index,
                    selectedSceneId = sceneId
                )
            }
        }
    }

    fun togglePlayback() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }

        if (_uiState.value.isPlaying) {
            startPlayback()
        }
    }

    private fun startPlayback() {
        viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                val scenes = _uiState.value.storyboard?.scenes ?: break
                val currentIndex = _uiState.value.currentSceneIndex

                if (currentIndex < scenes.size - 1) {
                    // Wait for the duration of the current scene
                    val currentScene = scenes[currentIndex]

                    // Move to next scene
                    _uiState.update {
                        it.copy(
                            currentSceneIndex = currentIndex + 1,
                            selectedSceneId = scenes[currentIndex + 1].id
                        )
                    }
                } else {
                    // Stop at the end
                    _uiState.update {
                        it.copy(
                            isPlaying = false,
                            currentSceneIndex = 0,
                            selectedSceneId = scenes.firstOrNull()?.id
                        )
                    }
                }
            }
        }
    }

    fun nextScene() {
        val scenes = _uiState.value.storyboard?.scenes ?: return
        val currentIndex = _uiState.value.currentSceneIndex

        if (currentIndex < scenes.size - 1) {
            _uiState.update {
                it.copy(
                    currentSceneIndex = currentIndex + 1,
                    selectedSceneId = scenes[currentIndex + 1].id
                )
            }
        }
    }

    fun previousScene() {
        val scenes = _uiState.value.storyboard?.scenes ?: return
        val currentIndex = _uiState.value.currentSceneIndex

        if (currentIndex > 0) {
            _uiState.update {
                it.copy(
                    currentSceneIndex = currentIndex - 1,
                    selectedSceneId = scenes[currentIndex - 1].id
                )
            }
        }
    }

    fun addScene(scene: Scene) {
        viewModelScope.launch {
            try {
                val currentStoryboard = _uiState.value.storyboard ?: return@launch
                val updatedScenes = currentStoryboard.scenes + scene
                val updatedStoryboard = currentStoryboard.copy(scenes = updatedScenes)

                // Save to repository
                contentRepository.updateStoryboard(updatedStoryboard)

                // Update UI state
                _uiState.update {
                    it.copy(storyboard = updatedStoryboard)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to add scene: ${e.message}")
                }
            }
        }
    }

    fun updateScene(sceneId: String, updatedScene: Scene) {
        viewModelScope.launch {
            try {
                val currentStoryboard = _uiState.value.storyboard ?: return@launch
                val updatedScenes = currentStoryboard.scenes.map { scene ->
                    if (scene.id == sceneId) updatedScene else scene
                }
                val updatedStoryboard = currentStoryboard.copy(scenes = updatedScenes)

                // Save to repository
                contentRepository.updateStoryboard(updatedStoryboard)

                // Update UI state
                _uiState.update {
                    it.copy(storyboard = updatedStoryboard)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to update scene: ${e.message}")
                }
            }
        }
    }

    fun deleteScene(sceneId: String) {
        viewModelScope.launch {
            try {
                val currentStoryboard = _uiState.value.storyboard ?: return@launch
                val updatedScenes = currentStoryboard.scenes.filter { it.id != sceneId }
                val updatedStoryboard = currentStoryboard.copy(scenes = updatedScenes)

                // Save to repository
                contentRepository.updateStoryboard(updatedStoryboard)

                // Update UI state and adjust current index if needed
                val newIndex = if (_uiState.value.currentSceneIndex >= updatedScenes.size) {
                    (updatedScenes.size - 1).coerceAtLeast(0)
                } else {
                    _uiState.value.currentSceneIndex
                }

                _uiState.update {
                    it.copy(
                        storyboard = updatedStoryboard,
                        currentSceneIndex = newIndex,
                        selectedSceneId = updatedScenes.getOrNull(newIndex)?.id
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete scene: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun getTotalDuration(): Int {
        return _uiState.value.storyboard?.scenes?.sumOf { it.duration } ?: 0
    }

    fun getSceneCount(): Int {
        return _uiState.value.storyboard?.scenes?.size ?: 0
    }
}