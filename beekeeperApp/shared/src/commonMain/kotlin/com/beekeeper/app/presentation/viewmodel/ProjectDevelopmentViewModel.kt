// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodel/ProjectDevelopmentViewModel.kt
package com.beekeeper.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.repository.CharacterRepository
import com.beekeeper.app.domain.repository.ContentRepository
import com.beekeeper.app.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectDevelopmentUiState(
    val project: CreativeProject? = null,
    val projectBible: ProjectBible? = null,
    val characters: List<CharacterProfile> = emptyList(),
    val stories: List<Story> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProjectDevelopmentViewModel(
    private val projectId: String,
    private val projectRepository: ProjectRepository,
    private val characterRepository: CharacterRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectDevelopmentUiState())
    val uiState: StateFlow<ProjectDevelopmentUiState> = _uiState.asStateFlow()

    init {
        println("🔵 ProjectDevelopmentViewModel INIT - projectId: $projectId")
        loadProjectData()
    }

    private fun loadProjectData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load project
                val projectResult = projectRepository.getProject(projectId)
                val project = projectResult.getOrNull()

                println("🔍 ProjectDevelopmentViewModel - Loaded project: ${project?.id}")
                println("🔍 ProjectDevelopmentViewModel - ProjectBible present: ${project?.projectBible != null}")
                if (project?.projectBible != null) {
                    println("🔍 ProjectBible episodeBlueprints count: ${project.projectBible.episodeBlueprints.size}")
                    println("🔍 ProjectBible worldLogic present: ${project.projectBible.worldLogic != null}")
                    println("🔍 ProjectBible plotLogic present: ${project.projectBible.plotLogic != null}")
                }

                // Load characters
                val characters = characterRepository.getCharacters(projectId)

                // Load stories
                val stories = contentRepository.getStories(projectId)

                _uiState.update {
                    it.copy(
                        project = project,
                        projectBible = project?.projectBible,
                        characters = characters,
                        stories = stories,
                        isLoading = false
                    )
                }

                println("🔍 Updated uiState - projectBible: ${_uiState.value.projectBible != null}")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load project data"
                    )
                }
            }
        }
    }

    fun refresh() {
        loadProjectData()
    }
}
