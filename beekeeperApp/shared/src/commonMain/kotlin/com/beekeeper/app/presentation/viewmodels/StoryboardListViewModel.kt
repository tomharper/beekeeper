// File: StoryboardListViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.repository.ContentRepository
import com.beekeeper.app.domain.repository.ContentRepositoryImpl
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class StoryboardListViewModel(
    private val projectId: String,
    private val storyId: String,
    private val contentRepository: ContentRepository = ContentRepositoryImpl()
) : ViewModel() {
    
    val _uiState = MutableStateFlow(StoryboardListUiState())
    val uiState: StateFlow<StoryboardListUiState> = _uiState.asStateFlow()
    
    private val _story = MutableStateFlow<Story?>(null)
    val story: StateFlow<Story?> = _story.asStateFlow()
    
    init {
        loadStory()
        loadStoryboards()
    }
    
    private fun loadStory() {
        viewModelScope.launch {
            try {
                val storyData = contentRepository.getStory(storyId)
                _story.value = storyData
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to load story: ${e.message}")
                }
            }
        }
    }
    
    private fun loadStoryboards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Get all storyboards for the project
                val allStoryboards = contentRepository.getStoryboards(projectId)
                
                // Filter storyboards for this specific story
                val storyStoryboards = allStoryboards.filter { it.storyId == storyId }
                
                // Calculate aggregate stats
                val totalScenes = storyStoryboards.sumOf { it.sceneCount }
                val totalDuration = storyStoryboards.sumOf { it.duration }
                val averageCompletion = if (storyStoryboards.isNotEmpty()) {
                    storyStoryboards.sumOf { it.completionPercentage } / storyStoryboards.size
                } else {
                    0
                }
                
                _uiState.update {
                    it.copy(
                        storyboards = storyStoryboards,
                        totalScenes = totalScenes,
                        totalDuration = totalDuration,
                        averageCompletion = averageCompletion,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load storyboards: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun createStoryboard(title: String, description: String, type: StoryboardType, scriptId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isCreating = true) }
                
                val storyboard = Storyboard(
                    id = "storyboard_${getCurrentTimeMillis()}",
                    projectId = projectId,
                    title = title,
                    description = description.ifBlank { null },
                    scenes = emptyList(),
                    sceneCount = 0,
                    duration = 0,
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                    createdBy = "current_user",
                    version = 1,
                    isLocked = false,
                    thumbnailUrl = null,
                    completionPercentage = 0,
                    storyId = storyId,
                    scriptId = scriptId, // Can be linked later
                    storyboardType = type
                )
                
                contentRepository.createStoryboard(storyboard)
                
                // Reload storyboards
                loadStoryboards()
                
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        showSuccessMessage = "Storyboard created successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        error = "Failed to create storyboard: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun deleteStoryboard(storyboardId: String) {
        viewModelScope.launch {
            try {
                contentRepository.deleteStoryboard(storyboardId)
                loadStoryboards()
                
                _uiState.update {
                    it.copy(showSuccessMessage = "Storyboard deleted")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete storyboard: ${e.message}")
                }
            }
        }
    }
    
    fun duplicateStoryboard(storyboardId: String) {
        viewModelScope.launch {
            try {
                val original = _uiState.value.storyboards.find { it.id == storyboardId }
                if (original != null) {
                    val duplicate = original.copy(
                        id = "storyboard_${getCurrentTimeMillis()}",
                        title = "${original.title} (Copy)",
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now(),
                        version = 1,
                        isLocked = false,
                        completionPercentage = 0
                    )
                    
                    contentRepository.createStoryboard(duplicate)
                    loadStoryboards()
                    
                    _uiState.update {
                        it.copy(showSuccessMessage = "Storyboard duplicated")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to duplicate storyboard: ${e.message}")
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(showSuccessMessage = null) }
    }
}

data class StoryboardListUiState(
    val storyboards: List<Storyboard> = emptyList(),
    val totalScenes: Int = 0,
    val totalDuration: Int = 0,
    val averageCompletion: Int = 0,
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val showSuccessMessage: String? = null
)