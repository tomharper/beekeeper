// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/HomeViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import com.beekeeper.app.domain.repository.ProjectRepository
import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

data class HomeUiState(
    val recentProjects: List<CreativeProject> = emptyList(),
    val upcomingDeadlines: List<DeadlineInfo> = emptyList(),
    val metrics: Any? = null, // Generic metrics, can be customized
    val quickStats: QuickStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DeadlineInfo(
    val projectId: String,
    val projectTitle: String,
    val taskTitle: String,
    val dueDate: Instant,
    val priority: TaskPriority,
    val phase: ProductionPhase
)

data class QuickStats(
    val totalProjects: Int,
    val activeProjects: Int,
    val completedThisMonth: Int,
    val teamMembers: Int,
    val averageProgress: Float,
    val totalBudget: Float,
    val budgetSpent: Float
)

class HomeViewModel(
    private val repository: ProjectRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load recent projects
                repository.getAllProjects()
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                error = exception.message,
                                isLoading = false
                            )
                        }
                    }
                    .collect { projects ->
                        val sortedProjects = projects
                            .sortedByDescending { it.updatedAt }
                            .take(5)

                        val upcomingDeadlines = extractUpcomingDeadlines(projects)
                        val quickStats = calculateQuickStats(projects)

                        _uiState.update {
                            it.copy(
                                recentProjects = sortedProjects,
                                upcomingDeadlines = upcomingDeadlines,
                                quickStats = quickStats,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                // Load dashboard metrics
                loadMetrics()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load dashboard data",
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun loadMetrics() {
        // Since getDashboardMetrics might not exist in ProjectRepository,
        // we'll skip this for now or calculate metrics from projects
        try {
            // Optional: If the repository has analytics methods, use them
            // For now, we'll just use the quick stats as our metrics
            _uiState.update { it.copy(metrics = it.quickStats) }
        } catch (e: Exception) {
            println("Metrics loading skipped: ${e.message}")
        }
    }

    private fun extractUpcomingDeadlines(projects: List<CreativeProject>): List<DeadlineInfo> {
        val now = Clock.System.now()
        val deadlines = mutableListOf<DeadlineInfo>()

        projects.forEach { project ->
            // Check phase deadlines only - tasks don't have deadline field
            project.timeline?.phases?.forEach { (phase, timeline) ->
                timeline.plannedEndDate?.let { endDate ->
                    if (endDate > now && endDate < (now + 14.days) && timeline.progress < 1.0f) {
                        deadlines.add(
                            DeadlineInfo(
                                projectId = project.id,
                                projectTitle = project.title,
                                taskTitle = "Complete ${phase.name}",
                                dueDate = endDate,
                                priority = TaskPriority.HIGH,
                                phase = phase
                            )
                        )
                    }
                }
            }

            // Add high priority uncompleted tasks as pseudo-deadlines
            project.timeline?.phases?.forEach { (phase, timeline) ->
                timeline.tasks
                    .filter { task ->
                        task.priority == TaskPriority.CRITICAL || task.priority == TaskPriority.HIGH
                    }
                    .filter { task ->
                        task.status != TaskStatus.COMPLETED && task.status != TaskStatus.CANCELLED
                    }
                    .forEach { task ->
                        // Estimate deadline based on phase end date and task progress
                        timeline.plannedEndDate?.let { phaseEndDate ->
                            if (phaseEndDate > now && phaseEndDate < (now + 30.days)) {
                                deadlines.add(
                                    DeadlineInfo(
                                        projectId = project.id,
                                        projectTitle = project.title,
                                        taskTitle = task.title,
                                        dueDate = phaseEndDate, // Use phase end date as task deadline
                                        priority = task.priority,
                                        phase = phase
                                    )
                                )
                            }
                        }
                    }
            }
        }

        return deadlines
            .sortedBy { it.dueDate }
            .take(5)
    }

    private fun calculateQuickStats(projects: List<CreativeProject>): QuickStats {
        val activeProjects = projects.filter { project ->
            project.status in listOf(
                ProjectStatus.IN_DEVELOPMENT,
                ProjectStatus.PRE_PRODUCTION,
                ProjectStatus.IN_PRODUCTION,
                ProjectStatus.POST_PRODUCTION
            )
        }

        val completedThisMonth = projects.filter { project ->
            project.status == ProjectStatus.COMPLETED &&
                    project.updatedAt?.let { it > Clock.System.now() - 30.days } == true
        }

        val allTeamMembers = projects.flatMap { project ->
            val team = mutableListOf<String>()
            // Add all team members from writers and designers
            project.team?.writers?.map { it.id }?.let { team.addAll(it) }
            project.team?.designers?.map { it.id }?.let { team.addAll(it) }
            team
        }.distinct()

        val totalBudget = projects.sumOf { it.budget?.totalBudget?.toDouble() ?: 0.0 }.toFloat()
        val budgetSpent = projects.sumOf { it.budget?.spentAmount?.toDouble() ?: 0.0 }.toFloat()

        val averageProgress = if (activeProjects.isNotEmpty()) {
            activeProjects.map { calculateProjectProgress(it) }.average().toFloat()
        } else 0f

        return QuickStats(
            totalProjects = projects.size,
            activeProjects = activeProjects.size,
            completedThisMonth = completedThisMonth.size,
            teamMembers = allTeamMembers.size,
            averageProgress = averageProgress,
            totalBudget = totalBudget,
            budgetSpent = budgetSpent
        )
    }

    private fun calculateProjectProgress(project: CreativeProject): Float {
        val phases = listOf(
            ProductionPhase.DEVELOPMENT,
            ProductionPhase.PRE_PRODUCTION,
            ProductionPhase.PRODUCTION,
            ProductionPhase.POST_PRODUCTION,
            ProductionPhase.DISTRIBUTION
        )

        val currentPhaseIndex = phases.indexOf(project.currentPhase)
        if (currentPhaseIndex == -1) return 0f

        val baseProgress = (currentPhaseIndex * 20f)
        val currentPhaseTimeline = project.timeline?.phases?.get(project.currentPhase)
        val phaseProgress = (currentPhaseTimeline?.progress ?: 0f) * 20f

        return (baseProgress + phaseProgress).coerceIn(0f, 100f)
    }

    fun refresh() {
        loadDashboardData()
    }

    fun getProjectById(projectId: String): CreativeProject? {
        return _uiState.value.recentProjects.find { it.id == projectId }
    }
}