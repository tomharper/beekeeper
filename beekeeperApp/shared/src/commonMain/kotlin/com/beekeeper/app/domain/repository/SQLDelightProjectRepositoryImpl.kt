// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/SQLDelightProjectRepositoryImpl.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.project.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * SQLDelight-backed implementation of ProjectRepository
 * Reads data from SQLDelight database (ProjectFactory JSON blobs)
 * Uses in-memory cache for performance
 */
class SQLDelightProjectRepositoryImpl(
    private val factoryRepository: SQLDelightProjectFactoryRepository
) : ProjectRepository {

    // Cache for performance
    private val projectsCache = mutableMapOf<String, CreativeProject>()
    private val projectsFlow = MutableStateFlow<List<CreativeProject>>(emptyList())

    init {
        // Load initial data from SQLDelight
        CoroutineScope(Dispatchers.Default).launch {
            refreshCache()
        }
    }

    /**
     * Refresh cache from SQLDelight
     */
    private suspend fun refreshCache() {
        try {
            val factories = factoryRepository.getAllFactories()
            projectsCache.clear()
            factories.forEach { factory ->
                projectsCache[factory.projectId] = factory.project
            }
            projectsFlow.value = projectsCache.values.toList()
        } catch (e: Exception) {
            println("❌ Error refreshing project cache: ${e.message}")
        }
    }

    // ===== CORE PROJECT CRUD OPERATIONS =====

    override suspend fun createProject(project: CreativeProject): Result<CreativeProject> {
        return try {
            // Create a new ProjectFactory and save it
            val factory = com.beekeeper.app.domain.factory.ProjectFactory.createEmpty(project)
            factoryRepository.saveFactory(factory, factoryType = "user")

            // Update cache
            projectsCache[project.id] = project
            projectsFlow.value = projectsCache.values.toList()

            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProject(projectId: String): Result<CreativeProject?> {
        return try {
            // Try cache first
            val cached = projectsCache[projectId]
            if (cached != null) {
                return Result.success(cached)
            }

            // Load from SQLDelight
            val factory = factoryRepository.getFactoryByProjectId(projectId)
            val project = factory?.project

            // Update cache
            project?.let { projectsCache[projectId] = it }

            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProject(project: CreativeProject): Result<CreativeProject> {
        return try {
            // Load existing factory to preserve other data
            val existingFactory = factoryRepository.getFactoryByProjectId(project.id)

            if (existingFactory != null) {
                // Update project but keep characters, stories, etc.
                val updatedFactory = existingFactory.copy(project = project)
                factoryRepository.saveFactory(updatedFactory, factoryType = existingFactory.metadata.let {
                    when {
                        it.isSample -> "sample"
                        it.isTemplate -> "template"
                        else -> "user"
                    }
                })
            } else {
                // Create new factory
                val factory = com.beekeeper.app.domain.factory.ProjectFactory.createEmpty(project)
                factoryRepository.saveFactory(factory, factoryType = "user")
            }

            // Update cache
            projectsCache[project.id] = project
            projectsFlow.value = projectsCache.values.toList()

            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> {
        return try {
            factoryRepository.deleteFactory(projectId)
            projectsCache.remove(projectId)
            projectsFlow.value = projectsCache.values.toList()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== PROJECT LISTING AND FILTERING =====

    override fun getAllProjects(): Flow<List<CreativeProject>> {
        return projectsFlow
    }

    override fun getProjectsByStatus(status: ProjectStatus): Flow<List<CreativeProject>> {
        return projectsFlow.map { projects ->
            projects.filter { it.status == status }
        }
    }

    override fun getProjectsByType(type: ProjectType): Flow<List<CreativeProject>> {
        return projectsFlow.map { projects ->
            projects.filter { it.type == type }
        }
    }

    override fun getProjectsByClient(clientId: String): Flow<List<CreativeProject>> {
        return projectsFlow.map { projects ->
            projects.filter { it.clientId == clientId }
        }
    }

    override fun getProjectsByPhase(phase: ProductionPhase): Flow<List<CreativeProject>> {
        return projectsFlow.map { projects ->
            projects.filter { project ->
                project.timeline?.phases?.get(phase)?.let { true } ?: false
            }
        }
    }

    override fun getProjectsByPriority(priority: ProjectPriority): Flow<List<CreativeProject>> {
        return projectsFlow.map { projects ->
            projects.filter { it.priority == priority }
        }
    }

    // ===== PROJECT SEARCH AND ANALYTICS =====

    override suspend fun searchProjects(query: String): Result<List<CreativeProject>> {
        return try {
            val results = projectsCache.values.filter { project ->
                project.title.contains(query, ignoreCase = true) ||
                project.description.contains(query, ignoreCase = true)
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProjectAnalytics(projectId: String): Result<ProjectAnalytics?> {
        // For now, return mock analytics
        // TODO: Store analytics in SQLDelight or calculate from project data
        return Result.success(null)
    }

    override suspend fun getDashboardMetrics(): Result<DashboardMetrics> {
        return try {
            val projects = projectsCache.values.toList()
            val metrics = DashboardMetrics(
                totalProjects = projects.size,
                activeProjects = projects.count { it.status == ProjectStatus.IN_PRODUCTION || it.status == ProjectStatus.IN_DEVELOPMENT },
                completedProjects = projects.count { it.status == ProjectStatus.COMPLETED },
                completedThisMonth = 0, // TODO: Calculate based on completion dates
                averageProjectDuration = 0f, // TODO: Calculate from project timelines
                averageCostPerMinute = 0f, // TODO: Calculate from budget and duration
                totalRevenue = projects.sumOf { it.budget?.totalBudget?.toDouble() ?: 0.0 }.toFloat(),
                averageCompletionRate = 0f, // TODO: Calculate completion percentage
                clientSatisfactionScore = 0f, // TODO: Calculate from reviews
                aiEfficiencyScore = 0f, // TODO: Calculate from AI metrics
                platformPerformance = emptyMap(), // TODO: Calculate from platform metrics
                upcomingDeadlines = emptyList() // TODO: Extract from milestones
            )
            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== TEAM AND COLLABORATION =====
    // For now, these return mock/stub responses
    // TODO: Implement team management in SQLDelight

    override suspend fun addTeamMember(projectId: String, member: TeamMember): Result<Unit> {
        // Stub - would need to update project.team in factory
        return Result.success(Unit)
    }

    override suspend fun removeTeamMember(projectId: String, memberId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun updateTeamMember(projectId: String, member: TeamMember): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getTeamMembers(projectId: String): Result<List<TeamMember>> {
        return try {
            val project = projectsCache[projectId]
            val members = project?.team?.let { team ->
                listOfNotNull(
                    team.projectManager,
                    team.creativeDirector,
                    team.technicalLead,
                    *team.writers.toTypedArray(),
                    *team.designers.toTypedArray(),
                    *team.qaTeam.toTypedArray(),
                    *team.clientContacts.toTypedArray(),
                    *team.aiSpecialists.toTypedArray()
                )
            } ?: emptyList()
            Result.success(members)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== TASKS AND MILESTONES =====

    override suspend fun addTask(projectId: String, task: PhaseTask): Result<PhaseTask> {
        return Result.success(task)
    }

    override suspend fun updateTask(projectId: String, task: PhaseTask): Result<PhaseTask> {
        return Result.success(task)
    }

    override suspend fun deleteTask(projectId: String, taskId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getTasks(projectId: String): Result<List<PhaseTask>> {
        return try {
            val project = projectsCache[projectId]
            val tasks = project?.timeline?.phases?.values?.flatMap { it.tasks } ?: emptyList()
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMilestone(projectId: String, milestone: Milestone): Result<Milestone> {
        return Result.success(milestone)
    }

    override suspend fun updateMilestone(projectId: String, milestone: Milestone): Result<Milestone> {
        return Result.success(milestone)
    }

    override suspend fun getMilestones(projectId: String): Result<List<Milestone>> {
        return try {
            val project = projectsCache[projectId]
            val milestones = project?.timeline?.milestones ?: emptyList()
            Result.success(milestones)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== DELIVERABLES =====

    override suspend fun addDeliverable(projectId: String, deliverable: Deliverable): Result<Deliverable> {
        return Result.success(deliverable)
    }

    override suspend fun updateDeliverable(projectId: String, deliverable: Deliverable): Result<Deliverable> {
        return Result.success(deliverable)
    }

    override suspend fun markDeliverableComplete(projectId: String, deliverableId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getDeliverables(projectId: String): Result<List<Deliverable>> {
        return try {
            val project = projectsCache[projectId]
            val deliverables = project?.deliverables ?: emptyList()
            Result.success(deliverables)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== BUDGET MANAGEMENT =====

    override suspend fun updateBudget(projectId: String, budget: ProjectBudget): Result<ProjectBudget> {
        return Result.success(budget)
    }

    override suspend fun addExpense(projectId: String, category: BudgetCategory, amount: Float, description: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getBudgetBreakdown(projectId: String): Result<ProjectBudget?> {
        return try {
            val project = projectsCache[projectId]
            Result.success(project?.budget)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== PLATFORM INTEGRATION =====

    override suspend fun updatePlatformTargets(projectId: String, platforms: List<StreamingPlatform>): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun trackPlatformPerformance(projectId: String, platformId: String, metrics: PlatformMetrics): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getPlatformMetrics(projectId: String, platformId: String?): Result<List<PlatformMetrics>> {
        return Result.success(emptyList())
    }

    // ===== REAL-TIME UPDATES =====

    override fun observeProject(projectId: String): Flow<CreativeProject?> {
        return projectsFlow.map { projects -> projects.find { it.id == projectId } }
    }

    override fun observeTeamMembers(projectId: String): Flow<List<TeamMember>> {
        return flow { emit(emptyList()) }
    }

    override fun observeTasks(projectId: String): Flow<List<PhaseTask>> {
        return flow { emit(emptyList()) }
    }

    // ===== DISTRIBUTION AND REVENUE =====

    override suspend fun getDistributionAnalytics(projectId: String): Result<DistributionAnalytics> {
        // Return stub/empty analytics for now
        // TODO: Implement actual analytics calculation
        return Result.failure(Exception("Analytics not yet implemented in SQLDelight backend"))
    }

    override suspend fun getTitleRevenue(projectId: String, titleId: String): Result<TitleRevenue> {
        return Result.success(TitleRevenue(
            titleId = titleId,
            title = "",
            totalRevenue = 0f,
            channelBreakdown = emptyMap(),
            viewershipRetention = emptyList(),
            recommendations = emptyList(),
            contentClassification = ContentClassification(
                format = ContentFormat.SHORT_FORM,
                duration = DurationBucket.ONE_TO_THREE_MIN,
                orientation = ContentOrientation.LANDSCAPE,
                style = ContentStyle.ENTERTAINMENT
            ),
            publishDate = Clock.System.now().toEpochMilliseconds(),
            lastUpdated = Clock.System.now().toEpochMilliseconds()
        ))
    }

    override suspend fun updateRevenueData(projectId: String, revenueData: RevenueUpdate): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getContentTitles(projectId: String): Result<List<TitleRevenue>> {
        return Result.success(emptyList())
    }

    override fun observeDistributionMetrics(projectId: String): Flow<DistributionAnalytics> {
        // Return empty flow for now
        // TODO: Implement actual analytics observation
        return flow { }
    }
}
