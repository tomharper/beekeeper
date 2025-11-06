// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/ProjectsViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import com.beekeeper.app.domain.repository.ProjectRepository
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.screens.ProjectTab
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ProjectsUiState(
    val projects: List<CreativeProject> = emptyList(),
    val filteredProjects: List<CreativeProject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: ProjectTab = ProjectTab.ALL,
    val searchQuery: String = "",
    val sortBy: ProjectSortOption = ProjectSortOption.UPDATED_DATE
)

enum class ProjectSortOption {
    UPDATED_DATE,
    CREATED_DATE,
    TITLE,
    PROGRESS,
    PRIORITY
}

class ProjectsViewModel(
    private val repository: ProjectRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    private val _projects = MutableStateFlow<List<CreativeProject>>(emptyList())
    val projects: StateFlow<List<CreativeProject>> = _projects.asStateFlow()

    init {
        loadProjects()
        observeProjects()
    }

    private fun observeProjects() {
        viewModelScope.launch {
            repository.getAllProjects()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            error = exception.message,
                            isLoading = false
                        )
                    }
                }
                .collect { projectList ->
                    println("🎯 [ProjectsViewModel] Received ${projectList.size} projects from repository")
                    projectList.forEach { project ->
                        println("  - ${project.title}")
                    }
                    _projects.value = projectList
                    _uiState.update {
                        it.copy(
                            projects = projectList,
                            filteredProjects = filterAndSortProjects(
                                projectList,
                                it.selectedTab,
                                it.searchQuery,
                                it.sortBy
                            ),
                            isLoading = false,
                            error = null
                        )
                    }
                    println("🎯 [ProjectsViewModel] UI state updated with ${projectList.size} projects, filtered: ${_uiState.value.filteredProjects.size}")
                }
        }
    }

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // The observeProjects flow will handle the actual loading
        }
    }

    fun selectTab(tab: ProjectTab) {
        _uiState.update { state ->
            state.copy(
                selectedTab = tab,
                filteredProjects = filterAndSortProjects(
                    state.projects,
                    tab,
                    state.searchQuery,
                    state.sortBy
                )
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredProjects = filterAndSortProjects(
                    state.projects,
                    state.selectedTab,
                    query,
                    state.sortBy
                )
            )
        }
    }

    fun updateSortOption(sortBy: ProjectSortOption) {
        _uiState.update { state ->
            state.copy(
                sortBy = sortBy,
                filteredProjects = filterAndSortProjects(
                    state.projects,
                    state.selectedTab,
                    state.searchQuery,
                    sortBy
                )
            )
        }
    }

    private fun filterAndSortProjects(
        projects: List<CreativeProject>,
        tab: ProjectTab,
        searchQuery: String,
        sortBy: ProjectSortOption
    ): List<CreativeProject> {
        // First filter by tab
        val tabFiltered = when (tab) {
            ProjectTab.ALL -> projects
            ProjectTab.IN_PROGRESS -> projects.filter { project ->
                project.status in listOf(
                    ProjectStatus.IN_DEVELOPMENT,
                    ProjectStatus.PRE_PRODUCTION,
                    ProjectStatus.IN_PRODUCTION,
                    ProjectStatus.POST_PRODUCTION
                )
            }
            ProjectTab.COMPLETED -> projects.filter { project ->
                project.status == ProjectStatus.COMPLETED
            }
        }

        // Then filter by search query
        val searchFiltered = if (searchQuery.isBlank()) {
            tabFiltered
        } else {
            tabFiltered.filter { project ->
                project.title.contains(searchQuery, ignoreCase = true) ||
                        project.description.contains(searchQuery, ignoreCase = true)
            }
        }

        // Finally sort
        return when (sortBy) {
            ProjectSortOption.UPDATED_DATE -> searchFiltered.sortedByDescending { it.updatedAt }
            ProjectSortOption.CREATED_DATE -> searchFiltered.sortedByDescending { it.createdAt }
            ProjectSortOption.TITLE -> searchFiltered.sortedBy { it.title }
            ProjectSortOption.PROGRESS -> searchFiltered.sortedByDescending {
                calculateProjectProgress(it)
            }
            ProjectSortOption.PRIORITY -> searchFiltered.sortedBy {
                when (it.priority) {
                    ProjectPriority.CRITICAL -> 0
                    ProjectPriority.HIGH -> 1
                    ProjectPriority.MEDIUM -> 2
                    ProjectPriority.LOW -> 3
                }
            }
        }
    }

    private fun calculateProjectProgress(project: CreativeProject): Float {
        // Calculate overall progress based on current phase and timeline
        val phases = listOf(
            ProductionPhase.DEVELOPMENT,
            ProductionPhase.PRE_PRODUCTION,
            ProductionPhase.PRODUCTION,
            ProductionPhase.POST_PRODUCTION,
            ProductionPhase.DISTRIBUTION
        )

        val currentPhaseIndex = phases.indexOf(project.currentPhase)
        if (currentPhaseIndex == -1) return 0f

        val baseProgress = (currentPhaseIndex * 20f) // Each phase is 20%

        // Add progress within current phase
        val currentPhaseTimeline = project.timeline?.phases?.get(project.currentPhase)
        val phaseProgress = (currentPhaseTimeline?.progress ?: 0f) * 20f

        return baseProgress + phaseProgress
    }

    fun getProjectMetrics(): ProjectMetrics {
        val allProjects = _projects.value
        return ProjectMetrics(
            total = allProjects.size,
            inProgress = allProjects.count {
                it.status in listOf(
                    ProjectStatus.IN_DEVELOPMENT,
                    ProjectStatus.PRE_PRODUCTION,
                    ProjectStatus.IN_PRODUCTION,
                    ProjectStatus.POST_PRODUCTION
                )
            },
            completed = allProjects.count { it.status == ProjectStatus.COMPLETED },
            onHold = allProjects.count { it.status == ProjectStatus.ON_HOLD }
        )
    }

    fun refresh() {
        loadProjects()
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val result = repository.deleteProject(projectId)
                result.fold(
                    onSuccess = {
                        // Project deletion successful - projects will be updated via observeProjects flow
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to delete project: ${exception.message}"
                            )
                        }
                    }
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to delete project: ${exception.message}"
                    )
                }
            }
        }
    }

    fun archiveProject(projectId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Find the project and update its status to ARCHIVED
                val project = _projects.value.find { it.id == projectId }
                if (project != null) {
                    val updatedProject = project.copy(status = ProjectStatus.ARCHIVED)
                    val result = repository.updateProject(updatedProject)

                    result.fold(
                        onSuccess = {
                            // Project archival successful - projects will be updated via observeProjects flow
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { exception ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Failed to archive project: ${exception.message}"
                                )
                            }
                        }
                    )
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Project not found"
                        )
                    }
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to archive project: ${exception.message}"
                    )
                }
            }
        }
    }
}

data class ProjectMetrics(
    val total: Int,
    val inProgress: Int,
    val completed: Int,
    val onHold: Int
)