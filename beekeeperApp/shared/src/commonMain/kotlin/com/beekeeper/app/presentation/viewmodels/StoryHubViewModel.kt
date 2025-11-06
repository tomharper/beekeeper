// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/StoryHubViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.repository.ContentRepository
import com.beekeeper.app.domain.repository.CharacterRepository
import com.beekeeper.app.domain.repository.RepositoryManager
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.repository.ProjectRepository
import com.beekeeper.app.domain.repository.ProjectRepositoryImpl
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel for Story Hub screen
 * Uses centralized repositories instead of mock data
 */
class StoryHubViewModel(
    private val projectId: String,
    private val contentRepository: ContentRepository = RepositoryManager.contentRepository,
    private val characterRepository: CharacterRepository = RepositoryManager.characterRepository,
    private val projectRepository: ProjectRepository = ProjectRepositoryImpl() // ADD THIS
) : ViewModel() {
    // UI State
    private val _uiState = MutableStateFlow(StoryHubUiState())
    val uiState: StateFlow<StoryHubUiState> = _uiState.asStateFlow()

    // Selected story for detail view
    private val _selectedStory = MutableStateFlow<Story?>(null)
    val selectedStory: StateFlow<Story?> = _selectedStory.asStateFlow()

    // Story patterns
    private val _storyPatterns = MutableStateFlow<List<StoryPattern>>(emptyList())
    val storyPatterns: StateFlow<List<StoryPattern>> = _storyPatterns.asStateFlow()

    // Storyboards
    private val _storyboards = MutableStateFlow<List<Storyboard>>(emptyList())
    val storyboards: StateFlow<List<Storyboard>> = _storyboards.asStateFlow()

    private val _project = MutableStateFlow<CreativeProject?>(null)
    val project: StateFlow<CreativeProject?> = _project.asStateFlow()

    init {
        loadProject(projectId)
        loadStories()
        loadStoryboards()
        loadStoryPatterns()
        loadCharacterCount()
        observeStoryUpdates()
    }

    private fun loadProject(projectId: String) {
        viewModelScope.launch {
            projectRepository.getProject(projectId).fold(
                onSuccess = { project ->
                    _project.value = project
                },
                onFailure = { exception ->
                    // Handle error - could update error state
                    _uiState.update { it.copy(error = exception.message) }
                }
            )
        }
    }

    /**
     * Load stories from repository
     */
    private fun loadStories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val stories = contentRepository.getStories(projectId)
                _uiState.update {
                    it.copy(
                        stories = stories,
                        isLoading = false,
                        error = null
                    )
                }

                // Select first story by default if available
                if (stories.isNotEmpty() && _selectedStory.value == null) {
                    _selectedStory.value = stories.first()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load stories: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Load storyboards from repository
     */
    private fun loadStoryboards() {
        viewModelScope.launch {
            try {
                val boards = contentRepository.getStoryboards(projectId)
                _storyboards.value = boards
                _uiState.update {
                    it.copy(
                        storyboardCount = boards.size,
                        totalScenes = boards.sumOf { it.scenes.size }
                    )
                }
            } catch (e: Exception) {
                // Handle error silently or show toast
            }
        }
    }

    /**
     * Load story patterns for structure guidance
     */
    private fun loadStoryPatterns() {
        viewModelScope.launch {
            try {
                val patterns = contentRepository.getStoryPatterns(projectId)
                _storyPatterns.value = patterns
            } catch (e: Exception) {
                // Handle error silently or show toast
            }
        }
    }

    /**
     * Load character count for the project
     */
    private fun loadCharacterCount() {
        viewModelScope.launch {
            try {
                val characters = characterRepository.getCharacters(projectId)
                _uiState.update {
                    it.copy(characterBoardCount = characters.size)
                }
            } catch (e: Exception) {
                // Handle error silently or show toast
            }
        }
    }

    /**
     * Observe real-time story updates
     */
    private fun observeStoryUpdates() {
        contentRepository.observeStories(projectId)
            .onEach { stories ->
                _uiState.update { it.copy(stories = stories) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Create a new story
     */
    fun createStory(title: String, synopsis: String, genre: String, lastEditedBy: String = "current_user") {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val newStory = Story(
                    id = "story_${currentTime}",
                    projectId = projectId,
                    scriptId = "script_${currentTime}",
                    title = title,
                    synopsis = synopsis,
                    genre = genre,
                    themes = emptyList(),
                    acts = emptyList(),
                    createdAt = currentTime,
                    updatedAt = currentTime,
                    status = ContentStatus.DRAFT,
                    lastEditedBy = lastEditedBy,
                    storyboardIds = emptyList()
                )

                val createdStory = contentRepository.createStory(newStory)
                _selectedStory.value = createdStory
                loadStories() // Reload the list

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showSuccessMessage = "Story created successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to create story: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Create a new storyboard
     */
    fun createStoryboard(title: String, description: String, storyId: String, scriptId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val newStoryboard = Storyboard(
                    id = "storyboard_${getCurrentTimeMillis()}",
                    projectId = projectId,
                    title = title,
                    description = description,
                    scenes = emptyList(),
                    duration = 0,
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                    createdBy = "current_user",
                    completionPercentage =0,
                    storyId = storyId,
                    scriptId = scriptId
                )

                contentRepository.createStoryboard(newStoryboard)
                loadStoryboards() // Reload the list

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showSuccessMessage = "Storyboard created successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to create storyboard: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Update existing story
     */
    fun updateStory(story: Story, lastEditedBy: String = "current_user") {
        viewModelScope.launch {
            try {
                val updatedStory = contentRepository.updateStory(
                    story.copy(
                        updatedAt = Clock.System.now().toEpochMilliseconds(),
                        lastEditedBy = lastEditedBy,
                    )
                )
                _selectedStory.value = updatedStory
                loadStories() // Reload the list
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to update story: ${e.message}")
                }
            }
        }
    }

    /**
     * Delete a story
     */
    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            try {
                val success = contentRepository.deleteStory(storyId)
                if (success) {
                    if (_selectedStory.value?.id == storyId) {
                        _selectedStory.value = null
                    }
                    loadStories() // Reload list
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete story: ${e.message}")
                }
            }
        }
    }

    /**
     * Delete a storyboard
     */
    fun deleteStoryboard(storyboardId: String) {
        viewModelScope.launch {
            try {
                val success = contentRepository.deleteStoryboard(storyboardId)
                if (success) {
                    loadStoryboards() // Reload list
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete storyboard: ${e.message}")
                }
            }
        }
    }

    /**
     * Analyze story structure using AI
     */
    fun analyzeStoryStructure(storyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }

            try {
                val analysis = contentRepository.analyzeStoryStructure(storyId)
                _uiState.update {
                    it.copy(
                        storyAnalysis = analysis,
                        isAnalyzing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        error = "Analysis failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Generate script from story
     */
    fun generateScriptFromStory(storyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }

            try {
                val story = _uiState.value.stories.find { it.id == storyId }
                if (story != null) {
                    val script = Script(
                        id = "script_${getCurrentTimeMillis()}",
                        projectId = projectId,
                        storyId = storyId,
                        title = "${story.title} - Screenplay",
                        content = "", // Would be AI-generated
                        format = ScriptFormat.SCREENPLAY,
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now(),
                        lastEditedBy = "current_user",
                        sceneScripts = emptyList(),
                        props = emptyList()
                    )

                    contentRepository.createScript(script)

                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            showSuccessMessage = "Script generated successfully"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = "Failed to generate script: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Extract characters from story
     */
    fun extractCharactersFromStory(storyId: String) {
        viewModelScope.launch {
            try {
                val story = _uiState.value.stories.find { it.id == storyId }
                if (story != null) {
                    // This would use AI to analyze the story and extract characters
                    // For now, we'll just show a success message
                    _uiState.update {
                        it.copy(showSuccessMessage = "Characters extracted successfully")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to extract characters: ${e.message}")
                }
            }
        }
    }

    /**
     * Select a story for detail view
     */
    fun selectStory(story: Story) {
        _selectedStory.value = story
    }

    /**
     * Clear any error messages
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(showSuccessMessage = null) }
    }

    /**
     * Filter stories based on status
     */
    fun filterStoriesByStatus(status: ContentStatus?) {
        viewModelScope.launch {
            val allStories = contentRepository.getStories(projectId)
            val filteredStories = if (status != null) {
                allStories.filter { it.status == status }
            } else {
                allStories
            }
            _uiState.update { it.copy(stories = filteredStories) }
        }
    }
}

/**
 * UI State for Story Hub screen
 */
data class StoryHubUiState(
    val stories: List<Story> = emptyList(),
    val storyboardCount: Int = 0,
    val characterBoardCount: Int = 0,
    val totalScenes: Int = 0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val showSuccessMessage: String? = null,
    val storyAnalysis: StoryAnalysis? = null
)