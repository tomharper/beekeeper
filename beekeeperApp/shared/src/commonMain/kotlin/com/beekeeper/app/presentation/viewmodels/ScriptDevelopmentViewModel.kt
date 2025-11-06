// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/ScriptDevelopmentViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.repository.ContentRepository
import com.beekeeper.app.domain.repository.ProjectRepository
import com.beekeeper.app.domain.repository.RepositoryManager
import com.beekeeper.app.domain.repository.StoryPatternsRepository
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ScriptDevelopmentUiState(
    val currentStory: Story? = null,
    val characters: List<CharacterProfile> = emptyList(),
    val scenes: List<SceneScript> = emptyList(),
    val selectedScene: SceneScript? = null,
    val beats: List<StoryBeat> = emptyList(),
    val availablePatterns: List<StoryPattern> = emptyList(),
    val appliedPatterns: List<StoryPattern> = emptyList(),
    val contentType: ContentType? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ScriptDevelopmentViewModel(
    private val projectId: String,
    private val storyId: String?,
    private val contentRepository: ContentRepository,
    private val patternsRepository: StoryPatternsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScriptDevelopmentUiState())
    val uiState: StateFlow<ScriptDevelopmentUiState> = _uiState.asStateFlow()

    init {
        loadStoryData()
        loadPatterns()
    }

    private fun loadStoryData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load story if ID provided
                if (storyId != null) {
                    val story = contentRepository.getStory(storyId)
                    _uiState.update { it.copy(currentStory = story) }

                    // Load associated script and scenes
                    story?.scriptId?.let { scriptId ->
                        val script = contentRepository.getScript(scriptId)
                        script?.let {
                            val scenes = it.sceneScripts ?: it.acts?.flatMap { act -> act.sceneScripts } ?: emptyList()
                            _uiState.update { state ->
                                state.copy(
                                    scenes = scenes,
                                    beats = detectNarrativeBeats(scenes)
                                )
                            }
                        }
                    }

                    // Load characters from script
                    story?.scriptId?.let { scriptId ->
                        val characters = contentRepository.extractCharactersFromScript(scriptId)
                        _uiState.update { state -> state.copy(characters = characters) }
                    }
                }

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load story data: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadPatterns() {
        viewModelScope.launch {
            try {
                val patterns = patternsRepository.getPatterns(projectId)
                _uiState.update { it.copy(availablePatterns = patterns) }
            } catch (e: Exception) {
                // Handle error silently for patterns
            }
        }
    }

    fun setContentType(type: ContentType) {
        _uiState.update { it.copy(contentType = type) }
    }

    fun createStoryWithPattern(pattern: StoryPattern) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Create new story based on pattern
                val story = Story(
                    id = "story_${getCurrentTimeMillis()}",
                    projectId = projectId,
                    scriptId = "", // Will be updated after script creation
                    title = "New ${_uiState.value.contentType?.name ?: "Story"}",
                    synopsis = "Story based on ${pattern.name} pattern",
                    genre = pattern.structure.type,
                    themes = pattern.examples,
                    setting = null,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                    status = ContentStatus.DRAFT,
                    storyboardIds = emptyList()
                )

                contentRepository.createStory(story)

                // Create initial script with scenes based on pattern
                val script = Script(
                    id = "script_${getCurrentTimeMillis()}",
                    projectId = projectId,
                    storyId = story.id,
                    title = "${story.title} - Draft 1",
                    content = "",
                    version = "1.0",
                    format = ScriptFormat.SCREENPLAY,
                    sceneScripts = createScenesFromPattern(pattern),
                    lastEditedBy = "current_user",
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                    props = emptyList()
                )

                contentRepository.createScript(script)

                // Update story with script reference
                val updatedStory = story.copy(scriptId = script.id)
                contentRepository.updateStory(updatedStory)

                _uiState.update {
                    it.copy(
                        currentStory = updatedStory,
                        scenes = script.sceneScripts ?: emptyList(),
                        appliedPatterns = listOf(pattern),
                        beats = pattern.structure.beats,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to create story: ${e.message}"
                    )
                }
            }
        }
    }

    fun editStory(story: Story) {
        viewModelScope.launch {
            try {
                contentRepository.updateStory(story)
                _uiState.update { it.copy(currentStory = story) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to update story: ${e.message}")
                }
            }
        }
    }

    fun selectScene(scene: SceneScript) {
        _uiState.update { it.copy(selectedScene = scene) }
    }

    fun addScene(position: Float) {
        viewModelScope.launch {
            try {
                val scenes = _uiState.value.scenes
                val insertIndex = (position * scenes.size).toInt().coerceIn(0, scenes.size)
                val scriptId = _uiState.value.currentStory?.scriptId ?: ""

                val newScene = SceneScript(
                    id = "scene_${getCurrentTimeMillis()}",
                    scriptId = scriptId,
                    sceneNumber = (insertIndex + 1).toString(),
                    title = "New Scene",
                    dialogue = emptyList(),
                    characterIds = emptyList(),
                    emotionalTone = EmotionalTone.NEUTRAL,
                    narrativeFunction = NarrativeFunction.RISING_ACTION
                )

                val updatedScenes = scenes.toMutableList().apply {
                    add(insertIndex, newScene)
                    // Update scene numbers
                    forEachIndexed { index, scene ->
                        this[index] = scene.copy(sceneNumber = (index + 1).toString())
                    }
                }

                updateScenes(updatedScenes)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to add scene: ${e.message}")
                }
            }
        }
    }

    fun reorderScenes(from: Int, to: Int) {
        viewModelScope.launch {
            try {
                val scenes = _uiState.value.scenes.toMutableList()
                val scene = scenes.removeAt(from)
                scenes.add(to, scene)

                // Update scene numbers
                val updatedScenes = scenes.mapIndexed { index, scene ->
                    scene.copy(sceneNumber = (index + 1).toString())
                }

                updateScenes(updatedScenes)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to reorder scenes: ${e.message}")
                }
            }
        }
    }

    fun editScene(scene: SceneScript) {
        viewModelScope.launch {
            try {
                val updatedScenes = _uiState.value.scenes.map {
                    if (it.id == scene.id) scene else it
                }
                updateScenes(updatedScenes)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to edit scene: ${e.message}")
                }
            }
        }
    }

    fun addDialogue(sceneId: String, dialogue: DialogueLine) {
        viewModelScope.launch {
            try {
                val updatedScenes = _uiState.value.scenes.map { scene ->
                    if (scene.id == sceneId) {
                        scene.copy(dialogue = scene.dialogue + dialogue)
                    } else {
                        scene
                    }
                }
                updateScenes(updatedScenes)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to add dialogue: ${e.message}")
                }
            }
        }
    }

    fun addCharacter(character: CharacterProfile) {
        viewModelScope.launch {
            try {
                val updatedCharacters = _uiState.value.characters + character
                _uiState.update { it.copy(characters = updatedCharacters) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to add character: ${e.message}")
                }
            }
        }
    }

    fun removeCharacter(characterId: String) {
        viewModelScope.launch {
            try {
                val updatedCharacters = _uiState.value.characters.filter { it.id != characterId }
                _uiState.update { it.copy(characters = updatedCharacters) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to remove character: ${e.message}")
                }
            }
        }
    }

    fun editCharacter(character: CharacterProfile) {
        viewModelScope.launch {
            try {
                // Note: ContentRepository doesn't have updateCharacter method
                // This would need to be implemented or use a different approach
                val updatedCharacters = _uiState.value.characters.map {
                    if (it.id == character.id) character else it
                }
                _uiState.update { it.copy(characters = updatedCharacters) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to edit character: ${e.message}")
                }
            }
        }
    }

    fun applyStoryPattern(pattern: StoryPattern) {
        viewModelScope.launch {
            try {
                // Apply pattern to existing story
                val updatedPatterns = _uiState.value.appliedPatterns + pattern
                _uiState.update {
                    it.copy(
                        appliedPatterns = updatedPatterns,
                        beats = pattern.structure.beats
                    )
                }

                // Optionally reorganize scenes based on pattern
                reorganizeScenesForPattern(pattern)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to apply pattern: ${e.message}")
                }
            }
        }
    }

    fun removeStoryPattern(pattern: StoryPattern) {
        viewModelScope.launch {
            val updatedPatterns = _uiState.value.appliedPatterns.filter { it.id != pattern.id }
            _uiState.update { it.copy(appliedPatterns = updatedPatterns) }
        }
    }

    private suspend fun updateScenes(scenes: List<SceneScript>) {
        _uiState.update { it.copy(scenes = scenes) }

        // Update script in repository
        _uiState.value.currentStory?.scriptId?.let { scriptId ->
            val script = contentRepository.getScript(scriptId)
            script?.let {
                val updatedScript = it.copy(
                    sceneScripts = scenes,
                    updatedAt = Clock.System.now()
                )
                contentRepository.updateScript(updatedScript)
            }
        }
    }

    private fun detectNarrativeBeats(scenes: List<SceneScript>): List<StoryBeat> {
        // Simple detection logic - can be enhanced
        return if (scenes.size >= 3) {
            listOf(
                StoryBeat("Setup", 0.0f, "Introduction"),
                StoryBeat("Rising Action", 0.25f, "Conflict builds"),
                StoryBeat("Climax", 0.75f, "Peak tension"),
                StoryBeat("Resolution", 0.9f, "Conclusion")
            )
        } else {
            listOf(
                StoryBeat("Beginning", 0.0f, "Start"),
                StoryBeat("Middle", 0.5f, "Development"),
                StoryBeat("End", 1.0f, "Conclusion")
            )
        }
    }

    private fun createScenesFromPattern(pattern: StoryPattern): List<SceneScript> {
        val scriptId = _uiState.value.currentStory?.scriptId ?: ""
        return pattern.structure.beats.mapIndexed { index, beat ->
            SceneScript(
                id = "scene_${getCurrentTimeMillis()}_$index",
                scriptId = scriptId,
                sceneNumber = (index + 1).toString(),
                title = beat.name,
                description = beat.description,
                narrativeFunction = when (beat.name.lowercase()) {
                    "setup", "opening" -> NarrativeFunction.OPENING
                    "climax" -> NarrativeFunction.CLIMAX
                    "resolution", "ending" -> NarrativeFunction.RESOLUTION
                    else -> NarrativeFunction.RISING_ACTION
                },
                dialogue = emptyList(),
                characterIds = emptyList(),
                emotionalTone = EmotionalTone.NEUTRAL
            )
        }
    }

    private suspend fun reorganizeScenesForPattern(pattern: StoryPattern) {
        // This would reorganize existing scenes to match the pattern structure
        // For now, just update the narrative structure
        _uiState.update {
            it.copy(
                beats = pattern.structure.beats
            )
        }
    }
}

// Factory function for creating the ViewModel
@Composable
fun rememberScriptDevelopmentViewModel(
    projectId: String,
    storyId: String?
): ScriptDevelopmentViewModel {
    return remember {
        ScriptDevelopmentViewModel(
            projectId = projectId,
            storyId = storyId,
            contentRepository = RepositoryManager.contentRepository,
            patternsRepository = RepositoryManager.storyPatternsRepository
        )
    }
}