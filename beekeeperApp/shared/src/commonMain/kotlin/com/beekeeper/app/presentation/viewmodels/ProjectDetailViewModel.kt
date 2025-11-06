// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/ProjectDetailViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.beekeeper.app.domain.project.*
import com.beekeeper.app.domain.repository.ProjectRepository
import com.beekeeper.app.presentation.screens.ProjectDetailUiState

class ProjectDetailViewModel(
    private val repository: ProjectRepository,
    private val projectId: String
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    
    private val _uiState = MutableStateFlow<ProjectDetailUiState>(ProjectDetailUiState.Loading)
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()
    
    private val _project = MutableStateFlow<CreativeProject?>(null)
    val project: StateFlow<CreativeProject?> = _project.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadProject(projectId: String) {
        viewModelScope.launch {
            _uiState.value = ProjectDetailUiState.Loading
            _isLoading.value = true
            
            try {
                repository.getProject(projectId).fold(
                    onSuccess = { project ->
                        if (project != null) {
                            _project.value = project
                            _uiState.value = ProjectDetailUiState.Success(project)
                        } else {
                            _uiState.value = ProjectDetailUiState.Error("Project not found")
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            error.message ?: "Failed to load project"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    e.message ?: "Unknown error occurred"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateProject(updatedProject: CreativeProject) {
        viewModelScope.launch {
            try {
                repository.updateProject(updatedProject).fold(
                    onSuccess = { project ->
                        _project.value = project
                        _uiState.value = ProjectDetailUiState.Success(project)
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            "Failed to update project: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    "Update error: ${e.message}"
                )
            }
        }
    }
    
    fun updateProjectPhase(newPhase: ProductionPhase) {
        _project.value?.let { currentProject ->
            val updatedProject = currentProject.copy(
                status = mapPhaseToStatus(newPhase),
                currentPhase = newPhase,
                updatedAt = kotlinx.datetime.Clock.System.now()
            )
            updateProject(updatedProject)
        }
    }
    
    fun updateProjectPriority(newPriority: ProjectPriority) {
        _project.value?.let { currentProject ->
            val updatedProject = currentProject.copy(
                updatedAt = kotlinx.datetime.Clock.System.now(),
                priority = newPriority
            )
            updateProject(updatedProject)
        }
    }
    
    fun updateTask(task: PhaseTask) {
        viewModelScope.launch {
            try {
                repository.updateTask(projectId, task).fold(
                    onSuccess = { updatedTask ->
                        // Update the local project state
                        _project.value?.let { currentProject ->
                            currentProject.timeline?.let { timeline ->
                                val updatedPhases = timeline.phases.mapValues { (phase, phaseTimeline) ->
                                    if (phaseTimeline.tasks.any { it.id == task.id }) {
                                        val updatedTasks = phaseTimeline.tasks.map { t ->
                                            if (t.id == task.id) updatedTask else t
                                        }
                                        phaseTimeline.copy(tasks = updatedTasks)
                                    } else {
                                        phaseTimeline
                                    }
                                }

                                val updatedProject = currentProject.copy(
                                    timeline = timeline.copy(phases = updatedPhases),
                                    updatedAt = kotlinx.datetime.Clock.System.now()
                                )

                                _project.value = updatedProject
                                _uiState.value = ProjectDetailUiState.Success(updatedProject)
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            "Failed to update task: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    "Task update error: ${e.message}"
                )
            }
        }
    }
    
    fun addTask(
        title: String,
        description: String,
        phase: ProductionPhase,
        assignedTo: String? = null,
        estimatedHours: Float,
        priority: TaskPriority = TaskPriority.MEDIUM,
        aiAssistanceRequired: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val newTask = PhaseTask(
                    id = generateTaskId(),
                    title = title,
                    description = description,
                    assignedTo = assignedTo,
                    status = TaskStatus.NOT_STARTED,
                    estimatedHours = estimatedHours,
                    priority = priority,
                    aiAssistanceRequired = aiAssistanceRequired
                )
                
                repository.addTask(projectId, newTask).fold(
                    onSuccess = { task ->
                        // Update local state
                        _project.value?.let { currentProject ->
                            currentProject.timeline?.let { timeline ->
                                val updatedPhases = timeline.phases.toMutableMap()
                                val phaseTimeline = updatedPhases[phase]

                                if (phaseTimeline != null) {
                                    val updatedTasks = phaseTimeline.tasks + task
                                    updatedPhases[phase] = phaseTimeline.copy(tasks = updatedTasks)

                                    val updatedProject = currentProject.copy(
                                        timeline = timeline.copy(phases = updatedPhases),
                                        updatedAt = kotlinx.datetime.Clock.System.now()
                                    )

                                    _project.value = updatedProject
                                    _uiState.value = ProjectDetailUiState.Success(updatedProject)
                                }
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            "Failed to add task: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    "Add task error: ${e.message}"
                )
            }
        }
    }
    
    fun updateBudget(budget: ProjectBudget) {
        viewModelScope.launch {
            try {
                repository.updateBudget(projectId, budget).fold(
                    onSuccess = { updatedBudget ->
                        _project.value?.let { currentProject ->
                            val updatedProject = currentProject.copy(
                                budget = updatedBudget,
                                updatedAt = kotlinx.datetime.Clock.System.now()
                            )
                            
                            _project.value = updatedProject
                            _uiState.value = ProjectDetailUiState.Success(updatedProject)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            "Failed to update budget: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    "Budget update error: ${e.message}"
                )
            }
        }
    }
    
    fun addExpense(
        category: BudgetCategory,
        amount: Float,
        description: String
    ) {
        viewModelScope.launch {
            try {
                repository.addExpense(projectId, category, amount, description).fold(
                    onSuccess = {
                        // Refresh the project to get updated budget
                        loadProject(projectId)
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            "Failed to add expense: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    "Add expense error: ${e.message}"
                )
            }
        }
    }
    
    fun addMilestone(milestone: Milestone) {
        viewModelScope.launch {
            try {
                repository.addMilestone(projectId, milestone).fold(
                    onSuccess = { addedMilestone ->
                        _project.value?.let { currentProject ->
                            currentProject.timeline?.let { timeline ->
                                val updatedMilestones = timeline.milestones + addedMilestone
                                val updatedTimeline = timeline.copy(milestones = updatedMilestones)
                                val updatedProject = currentProject.copy(
                                    timeline = updatedTimeline,
                                    updatedAt = kotlinx.datetime.Clock.System.now()
                                )

                                _project.value = updatedProject
                                _uiState.value = ProjectDetailUiState.Success(updatedProject)
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            "Failed to add milestone: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    "Add milestone error: ${e.message}"
                )
            }
        }
    }
    
    fun updateMilestone(milestone: Milestone) {
        viewModelScope.launch {
            try {
                repository.updateMilestone(projectId, milestone).fold(
                    onSuccess = { updatedMilestone ->
                        _project.value?.let { currentProject ->
                            currentProject.timeline?.let { timeline ->
                                val updatedMilestones = timeline.milestones.map { m ->
                                    if (m.id == milestone.id) updatedMilestone else m
                                }
                                val updatedTimeline = timeline.copy(milestones = updatedMilestones)
                                val updatedProject = currentProject.copy(
                                    timeline = updatedTimeline,
                                    updatedAt = kotlinx.datetime.Clock.System.now()
                                )
                            _project.value = updatedProject
                            _uiState.value = ProjectDetailUiState.Success(updatedProject)
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            "Failed to update milestone: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    "Update milestone error: ${e.message}"
                )
            }
        }
    }
    
    fun addTeamMember(member: TeamMember) {
        viewModelScope.launch {
            try {
                repository.addTeamMember(projectId, member).fold(
                    onSuccess = {
                        // Refresh the project to get updated team
                        loadProject(projectId)
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            "Failed to add team member: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    "Add team member error: ${e.message}"
                )
            }
        }
    }
    
    fun removeTeamMember(memberId: String) {
        viewModelScope.launch {
            try {
                repository.removeTeamMember(projectId, memberId).fold(
                    onSuccess = {
                        // Refresh the project to get updated team
                        loadProject(projectId)
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            "Failed to remove team member: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    "Remove team member error: ${e.message}"
                )
            }
        }
    }
    
    fun updateDeliverable(deliverable: Deliverable) {
        viewModelScope.launch {
            try {
                repository.updateDeliverable(projectId, deliverable).fold(
                    onSuccess = { updatedDeliverable ->
                        _project.value?.let { currentProject ->
                            val updatedDeliverables = currentProject.deliverables.map { d ->
                                if (d.id == deliverable.id) updatedDeliverable else d
                            }
                            val updatedProject = currentProject.copy(
                                deliverables = updatedDeliverables,
                                updatedAt = kotlinx.datetime.Clock.System.now()
                            )
                            
                            _project.value = updatedProject
                            _uiState.value = ProjectDetailUiState.Success(updatedProject)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ProjectDetailUiState.Error(
                            "Failed to update deliverable: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProjectDetailUiState.Error(
                    "Update deliverable error: ${e.message}"
                )
            }
        }
    }
    
    private fun mapPhaseToStatus(phase: ProductionPhase): ProjectStatus {
        return when (phase) {
            ProductionPhase.DEVELOPMENT -> ProjectStatus.IN_DEVELOPMENT
            ProductionPhase.PRE_PRODUCTION -> ProjectStatus.PRE_PRODUCTION
            ProductionPhase.PRODUCTION -> ProjectStatus.IN_PRODUCTION
            ProductionPhase.POST_PRODUCTION -> ProjectStatus.POST_PRODUCTION
            ProductionPhase.DISTRIBUTION -> ProjectStatus.READY_FOR_DISTRIBUTION
        }
    }
    
    private fun generateTaskId(): String {
        return "task_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}_${(100..999).random()}"
    }
}
