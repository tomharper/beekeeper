// File: fillerApp/shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/StoryboardScenesViewModel.kt

package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.repository.ContentRepositoryImpl
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

data class StoryboardScenesUiState(
    val isLoading: Boolean = false,
    val storyboard: Storyboard? = null,
    val scenes: List<Scene> = emptyList(),
    val totalDuration: Int = 0,
    val completionPercentage: Int = 0,
    val approvedScenes: Int = 0,
    val availableCharacters: List<String> = emptyList(),
    val availableLocations: List<String> = emptyList(),
    val error: String? = null
)

class StoryboardScenesViewModel(
    private val storyboardId: String,
    private val projectId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryboardScenesUiState())
    val uiState: StateFlow<StoryboardScenesUiState> = _uiState.asStateFlow()

    init {
        loadStoryboardScenes()
    }

    private fun loadStoryboardScenes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Generate sample storyboard data
                val factory = ContentRepositoryImpl()
                val sampleStoryboards = factory.getStoryboards(projectId)
                val storyboard = sampleStoryboards.find { sb -> sb.id == storyboardId }
                    ?: sampleStoryboards.firstOrNull()

                if (storyboard != null) {
                    val scenes = storyboard.scenes
                    val allCharacters = scenes.flatMap { scene ->
                        scene.getCharactersList()
                    }.distinct()

                    val allLocations = scenes.mapNotNull { it.location }.distinct()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            storyboard = storyboard,
                            scenes = scenes,
                            totalDuration = scenes.sumOf { scene -> scene.duration },
                            completionPercentage = calculateCompletionPercentage(scenes),
                            approvedScenes = calculateApprovedScenes(scenes),
                            availableCharacters = allCharacters,
                            availableLocations = allLocations,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun createScene(title: String, description: String, duration: Int) {
        viewModelScope.launch {
            val currentScenes = _uiState.value.scenes
            val newSceneNumber = (currentScenes.maxOfOrNull { it.sceneNumber } ?: 0) + 1

            val newScene = Scene(
                id = "scene_${getCurrentTimeMillis()}",
                storyboardId = storyboardId,
                sceneNumber = newSceneNumber,
                title = title,
                description = description,
                duration = duration,
                frames = emptyList(),
                location = null,
                timeOfDay = null,
                characterIds = emptyList(),
                soundEffects = emptyList(),
                musicCues = emptyList(),
                dialogueSnippet = null,
                cameraDirection = null,
                notes = null,
                transitionType = TransitionType.FADE_IN,
                aiSuggestions = emptyList(),
                isKeyScene = false,
                sceneScriptId = "scenescriptid_${getCurrentTimeMillis()}",
            )

            // Update UI state
            _uiState.update { state ->
                state.copy(
                    scenes = state.scenes + newScene,
                    totalDuration = state.totalDuration + duration
                )
            }
        }
    }

    fun deleteScene(sceneId: String) {
        viewModelScope.launch {
            val sceneToDelete = _uiState.value.scenes.find { it.id == sceneId }

            if (sceneToDelete != null) {
                // Update UI state
                _uiState.update { state ->
                    state.copy(
                        scenes = state.scenes.filter { it.id != sceneId },
                        totalDuration = state.totalDuration - sceneToDelete.duration
                    )
                }

                // Reorder scene numbers
                reorderSceneNumbers()
            }
        }
    }

    fun updateScene(sceneId: String, updatedScene: Scene) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    scenes = state.scenes.map { scene ->
                        if (scene.id == sceneId) updatedScene else scene
                    }
                )
            }
        }
    }

    fun duplicateScene(sceneId: String) {
        viewModelScope.launch {
            val sceneToDuplicate = _uiState.value.scenes.find { it.id == sceneId }

            if (sceneToDuplicate != null) {
                val duplicatedScene = sceneToDuplicate.copy(
                    id = "scene_${getCurrentTimeMillis()}",
                    title = "${sceneToDuplicate.title} (Copy)",
                    sceneNumber = sceneToDuplicate.sceneNumber + 1
                )

                // Update UI state and reorder
                val updatedScenes = _uiState.value.scenes.toMutableList()
                val insertIndex = updatedScenes.indexOfFirst { it.id == sceneId } + 1
                updatedScenes.add(insertIndex, duplicatedScene)

                _uiState.update { state ->
                    state.copy(
                        scenes = updatedScenes,
                        totalDuration = state.totalDuration + duplicatedScene.duration
                    )
                }

                reorderSceneNumbers()
            }
        }
    }

    fun reorderScenes(sceneIds: List<String>) {
        viewModelScope.launch {
            val reorderedScenes = sceneIds.mapIndexedNotNull { index, id ->
                _uiState.value.scenes.find { it.id == id }?.copy(sceneNumber = index + 1)
            }

            if (reorderedScenes.size == _uiState.value.scenes.size) {
                _uiState.update { state ->
                    state.copy(scenes = reorderedScenes)
                }
            }
        }
    }

    fun addFrameToScene(sceneId: String, frame: Frame) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    scenes = state.scenes.map { scene ->
                        if (scene.id == sceneId) {
                            scene.copy(frames = scene.frames + frame)
                        } else scene
                    }
                )
            }
        }
    }

    fun removeFrameFromScene(sceneId: String, frameId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    scenes = state.scenes.map { scene ->
                        if (scene.id == sceneId) {
                            scene.copy(frames = scene.frames.filter { it.id != frameId })
                        } else scene
                    }
                )
            }
        }
    }

    fun updateSceneMetadata(
        sceneId: String,
        location: String? = null,
        timeOfDay: String? = null,
        characters: List<String>? = null
    ) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    scenes = state.scenes.map { scene ->
                        if (scene.id == sceneId) {
                            scene.copy(
                                location = location ?: scene.location,
                                timeOfDay = timeOfDay ?: scene.timeOfDay,
                                characterIds = characters ?: scene.getCharactersList()
                            )
                        } else scene
                    }
                )
            }
        }
    }

    fun generateAISuggestions(sceneId: String) {
        viewModelScope.launch {

            val suggestions = listOf(
                "Consider adding a close-up shot for emotional impact",
                "The lighting could be more dramatic for this scene",
                "Add ambient sounds to enhance atmosphere",
                "Character blocking could be improved for better composition",
                "Consider a slower pacing for this dramatic moment"
            )

            _uiState.update { state ->
                state.copy(
                    scenes = state.scenes.map { scene ->
                        if (scene.id == sceneId) {
                            scene.copy(aiSuggestions = suggestions)
                        } else scene
                    }
                )
            }
        }
    }

    private fun reorderSceneNumbers() {
        val reorderedScenes = _uiState.value.scenes
            .sortedBy { it.sceneNumber }
            .mapIndexed { index, scene ->
                scene.copy(sceneNumber = index + 1)
            }

        _uiState.update { state ->
            state.copy(scenes = reorderedScenes)
        }
    }

    private fun calculateCompletionPercentage(scenes: List<Scene>): Int {
        if (scenes.isEmpty()) return 0

        // Calculate based on scenes with frames
        val scenesWithFrames = scenes.count { it.frames.isNotEmpty() }
        return (scenesWithFrames * 100) / scenes.size
    }

    private fun calculateApprovedScenes(scenes: List<Scene>): Int {
        // For now, count scenes with more than 3 frames as "approved"
        return scenes.count { it.frames.size >= 3 }
    }
}

// Extension function to safely get characters from Scene
fun Scene.getCharactersList(): List<String> {
    return this.characterIds ?: emptyList()
}