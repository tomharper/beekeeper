// 4. Create ContentQualityReviewViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.repository.*
import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ContentQualityReviewViewModel(
    private val projectId: String,
    private val projectRepository: ProjectRepository = ProjectRepositoryImpl(),
    private val contentRepository: ContentRepository = RepositoryManager.contentRepository
) : ViewModel() {
    
    private val _project = MutableStateFlow<CreativeProject?>(null)
    val project: StateFlow<CreativeProject?> = _project.asStateFlow()
    
    private val _contentItems = MutableStateFlow<List<ContentItem>>(emptyList())
    val contentItems: StateFlow<List<ContentItem>> = _contentItems.asStateFlow()
    
    private val _selectedContent = MutableStateFlow<ContentItem?>(null)
    val selectedContent: StateFlow<ContentItem?> = _selectedContent.asStateFlow()
    
    private val _qualityScore = MutableStateFlow(0f)
    val qualityScore: StateFlow<Float> = _qualityScore.asStateFlow()
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    
    init {
        loadProject()
        loadContent()
    }

    private fun loadProject() {
        viewModelScope.launch {
            projectRepository.getProject(projectId).fold(
                onSuccess = { project ->
                    _project.value = project
                },
                onFailure = { exception ->
                }
            )
        }
    }
    
    private fun loadContent() {
        viewModelScope.launch {
            try {
                // Load all content items for review
                val stories = contentRepository.getStories(projectId)
                val scripts = contentRepository.getScripts(projectId)
                val storyboards = contentRepository.getStoryboards(projectId)
                
                _contentItems.value = stories.map { story ->
                    ContentItem(
                        id = story.id,
                        type = ContentType.STORY,
                        title = story.title,
                        projectId = projectId,
                        thumbnailUrl = null,
                        lastModified = story.updatedAt,
                        status = story.status
                    )
                } + scripts.map { script ->
                    ContentItem(
                        id = script.id,
                        type = ContentType.SCRIPT,
                        title = script.title,
                        projectId = projectId,
                        thumbnailUrl = null,
                        lastModified = script.updatedAt.toEpochMilliseconds(),
                        status = ContentStatus.DRAFT
                    )
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun selectContent(content: ContentItem) {
        _selectedContent.value = content
        analyzeQuality(content)
    }
    
    private fun analyzeQuality(content: ContentItem) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                // Simulate quality analysis
                _qualityScore.value = (70..95).random().toFloat() / 100f
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
}

