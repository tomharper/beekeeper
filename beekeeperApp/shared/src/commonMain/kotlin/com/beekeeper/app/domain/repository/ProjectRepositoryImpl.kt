// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/ProjectRepositoryImpl.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.data.api.ApiConfig
import com.beekeeper.app.data.api.ApiService
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.project.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

/**
 * Implementation of unified ProjectRepository with backend API integration
 * Falls back to local factories in offline mode
 * Optionally persists API responses to SQLite database for offline access
 */
class ProjectRepositoryImpl(
    private val apiService: ApiService = ApiService(),
    private val databaseRepository: SQLDelightProjectFactoryRepository? = null
) : ProjectRepository {

    // In-memory storage for mock data
    private val projectsCache = mutableMapOf<String, CreativeProject>()
    private val teamMembersCache = mutableMapOf<String, MutableList<TeamMember>>()
    private val tasksCache = mutableMapOf<String, MutableList<PhaseTask>>()
    private val milestonesCache = mutableMapOf<String, MutableList<Milestone>>()
    private val deliverablesCache = mutableMapOf<String, MutableList<Deliverable>>()
    private val analyticsCache = mutableMapOf<String, ProjectAnalytics>()
    private val platformMetricsCache = mutableMapOf<String, MutableList<PlatformMetrics>>()

    // Flow for real-time updates
    private val projectsFlow = MutableStateFlow<List<CreativeProject>>(emptyList())
    private val projectUpdateFlow = mutableMapOf<String, MutableStateFlow<CreativeProject?>>()
    private val teamMembersFlow = mutableMapOf<String, MutableStateFlow<List<TeamMember>>>()
    private val tasksFlow = mutableMapOf<String, MutableStateFlow<List<PhaseTask>>>()

    init {
        // Initialize with data from backend or local factories
        initializeSampleData()
    }

    /**
     * Suspend function to sync projects from server
     * Can be called explicitly by AuthViewModel after login
     */
    suspend fun syncFromServer(): Result<Int> = withContext(Dispatchers.Default) {
        if (ApiConfig.offlineMode) {
            return@withContext Result.failure(Exception("Offline mode enabled"))
        }

        try {
            // Step 1: Load from database first (fast cold start)
            loadFromDatabase()

            // Step 2: Fetch from API
            apiService.getProjects(limit = 100).fold(
                onSuccess = { response ->
                    if (response == null) {
                        println("✅ Projects list unchanged (304 Not Modified)")
                        if (projectsCache.isNotEmpty()) {
                            projectsFlow.value = projectsCache.values.toList()
                        }
                        return@withContext Result.success(projectsCache.size)
                    }

                    // Load full details for each project
                    response.projects.forEach { summary ->
                        apiService.getProjectDetails(summary.id).fold(
                            onSuccess = { details ->
                                if (details == null) {
                                    println("✅ Project ${summary.id} unchanged (304)")
                                    return@fold
                                }

                                val project = details.project
                                projectsCache[project.id] = project
                                teamMembersCache[project.id] = extractTeamMembers(project.team).toMutableList()
                                tasksCache[project.id] = extractTasks(project.timeline).toMutableList()
                                milestonesCache[project.id] = (project.timeline?.milestones ?: emptyList()).toMutableList()
                                deliverablesCache[project.id] = project.deliverables.toMutableList()
                                analyticsCache[project.id] = createSampleAnalytics(project)
                                platformMetricsCache[project.id] = createSamplePlatformMetrics(project)
                                saveProjectToDatabase(details)
                            },
                            onFailure = { error ->
                                println("Failed to load project ${summary.id}: ${error.message}")
                            }
                        )
                    }

                    // Update the flow
                    projectsFlow.value = projectsCache.values.toList()
                    println("✅ Synced ${projectsCache.size} projects from API")
                    Result.success(projectsCache.size)
                },
                onFailure = { error ->
                    println("❌ API sync failed: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            println("❌ Error syncing from API: ${e.message}")
            Result.failure(e)
        }
    }

    private fun initializeSampleData() {
        // Try to load from API first (async)
        if (!ApiConfig.offlineMode) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    // Step 1: Try loading from database first (fast cold start)
                    loadFromDatabase()

                    // Step 2: Fetch from API in background (with ETags)
                    apiService.getProjects(limit = 100).fold(
                        onSuccess = { response ->
                            if (response == null) {
                                println("✅ Projects list unchanged (304 Not Modified) - using existing cache")
                                if (projectsCache.isNotEmpty()) {
                                    projectsFlow.value = projectsCache.values.toList()
                                    println("Updated flow with ${projectsCache.size} cached projects")
                                }
                                return@fold
                            }

                            // Load full details for each project
                            response.projects.forEach { summary ->
                                apiService.getProjectDetails(summary.id).fold(
                                    onSuccess = { details ->
                                        if (details == null) {
                                            println("✅ Project ${summary.id} unchanged (304 Not Modified)")
                                            return@fold
                                        }

                                        val project = details.project
                                        projectsCache[project.id] = project

                                        // Extract team members from ProjectTeam structure
                                        val teamMembers = extractTeamMembers(project.team)
                                        teamMembersCache[project.id] = teamMembers.toMutableList()

                                        // Extract tasks from timeline phases
                                        val tasks = extractTasks(project.timeline)
                                        tasksCache[project.id] = tasks.toMutableList()

                                        // Extract milestones from timeline
                                        milestonesCache[project.id] = (project.timeline?.milestones ?: emptyList()).toMutableList()

                                        // Initialize deliverables
                                        deliverablesCache[project.id] = project.deliverables.toMutableList()

                                        // Create sample analytics
                                        analyticsCache[project.id] = createSampleAnalytics(project)

                                        // Create sample platform metrics
                                        platformMetricsCache[project.id] = createSamplePlatformMetrics(project)

                                        // Save to database for offline access
                                        saveProjectToDatabase(details)
                                    },
                                    onFailure = { error ->
                                        println("Failed to load project ${summary.id}: ${error.message}")
                                    }
                                )
                            }

                            // Update the flow with API data
                            projectsFlow.value = projectsCache.values.toList()
                            println("Loaded ${projectsCache.size} projects from API")
                            println("🔄 [ProjectRepositoryImpl] Updated projectsFlow after API load with ${projectsFlow.value.size} projects")
                        },
                        onFailure = { error ->
                            println("❌ API call failed: ${error.message}")
                            // If cache is empty, user will see empty state
                            // Database should have cached data from previous successful fetch
                            if (projectsCache.isEmpty()) {
                                println("⚠️ No cached data available - user will see empty state")
                            }
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Error loading from API: ${e.message}")
                    // Rely on database cache loaded at startup
                    if (projectsCache.isEmpty()) {
                        println("⚠️ No cached data available - user will see empty state")
                    }
                }
            }
        } else {
            // Offline mode - should use SQLDelightProjectRepositoryImpl instead
            println("⚠️ Offline mode enabled but using ProjectRepositoryImpl - no data will be loaded")
        }
    }

    // ===== HELPER FUNCTIONS =====

    private fun extractTeamMembers(team: ProjectTeam?): List<TeamMember> {
        if (team == null) return emptyList()
        val members = mutableListOf<TeamMember>()
        members.add(team.projectManager)
        team.creativeDirector?.let { members.add(it) }
        members.addAll(team.writers)
        members.addAll(team.designers)
        team.technicalLead?.let { members.add(it) }
        members.addAll(team.qaTeam)
        members.addAll(team.clientContacts)
        members.addAll(team.aiSpecialists)
        return members
    }

    private fun extractTasks(timeline: ProjectTimeline?): List<PhaseTask> {
        if (timeline == null) return emptyList()
        return timeline.phases.values.flatMap { phaseTimeline ->
            phaseTimeline.tasks
        }
    }

    /**
     * Load projects from SQLite database
     * Provides fast cold start by loading cached data immediately
     */
    private suspend fun loadFromDatabase() {
        databaseRepository?.let { dbRepo ->
            try {
                val factories = dbRepo.getAllFactories()
                if (factories.isNotEmpty()) {
                    println("💾 Loading ${factories.size} projects from database...")
                    factories.forEach { factory ->
                        val project = factory.project
                        projectsCache[project.id] = project

                        // Extract and cache related data
                        teamMembersCache[project.id] = extractTeamMembers(project.team).toMutableList()
                        tasksCache[project.id] = extractTasks(project.timeline).toMutableList()
                        milestonesCache[project.id] = (project.timeline?.milestones ?: emptyList()).toMutableList()
                        deliverablesCache[project.id] = project.deliverables.toMutableList()
                        analyticsCache[project.id] = createSampleAnalytics(project)
                        platformMetricsCache[project.id] = createSamplePlatformMetrics(project)
                    }
                    projectsFlow.value = projectsCache.values.toList()
                    println("✅ Loaded ${factories.size} projects from database")
                    println("🔄 [ProjectRepositoryImpl] Updated projectsFlow with ${projectsFlow.value.size} projects")
                } else {
                    println("💾 Database is empty, will fetch from API")
                }
            } catch (e: Exception) {
                println("⚠️ Failed to load from database: ${e.message}")
            }
        }
    }

    /**
     * Save project to SQLite database
     * Converts ProjectDetailsResponse (from API) to ProjectFactory and persists as JSON
     */
    private suspend fun saveProjectToDatabase(projectDetails: com.beekeeper.app.data.api.ProjectDetailsResponse) {
        databaseRepository?.let { dbRepo ->
            try {
                // Convert to ProjectFactory using the existing factory structure
                val factory = com.beekeeper.app.domain.factory.ProjectFactory(
                    project = projectDetails.project,
                    characters = projectDetails.characters,
                    stories = projectDetails.stories,
                    scripts = projectDetails.scripts,
                    storyboards = projectDetails.storyboards,
                    publishingProject = projectDetails.publishingProject,
                    projectBible = projectDetails.projectBible,
                    metadata = projectDetails.metadata
                )

                dbRepo.saveFactory(factory, factoryType = "api")
                println("💾 Saved project ${projectDetails.project.title} to database")
            } catch (e: Exception) {
                println("⚠️ Failed to save project to database: ${e.message}")
            }
        }
    }

    // ===== CORE PROJECT CRUD OPERATIONS =====

    override suspend fun createProject(project: CreativeProject): Result<CreativeProject> {
        return try {
            projectsCache[project.id] = project

            // Extract and cache related data
            teamMembersCache[project.id] = extractTeamMembers(project.team).toMutableList()
            tasksCache[project.id] = extractTasks(project.timeline).toMutableList()
            milestonesCache[project.id] = (project.timeline?.milestones ?: emptyList()).toMutableList()
            deliverablesCache[project.id] = project.deliverables.toMutableList()

            projectsFlow.value = projectsCache.values.toList()
            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProject(projectId: String): Result<CreativeProject?> {
        return try {
            Result.success(projectsCache[projectId])
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProject(project: CreativeProject): Result<CreativeProject> {
        return try {
            projectsCache[project.id] = project

            // Update related caches
            teamMembersCache[project.id] = extractTeamMembers(project.team).toMutableList()
            tasksCache[project.id] = extractTasks(project.timeline).toMutableList()
            milestonesCache[project.id] = (project.timeline?.milestones ?: emptyList()).toMutableList()
            deliverablesCache[project.id] = project.deliverables.toMutableList()

            projectsFlow.value = projectsCache.values.toList()
            projectUpdateFlow[project.id]?.value = project

            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> {
        return try {

            projectsCache.remove(projectId)
            teamMembersCache.remove(projectId)
            tasksCache.remove(projectId)
            milestonesCache.remove(projectId)
            deliverablesCache.remove(projectId)
            analyticsCache.remove(projectId)
            platformMetricsCache.remove(projectId)

            projectsFlow.value = projectsCache.values.toList()
            projectUpdateFlow[projectId]?.value = null

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== PROJECT LISTING AND FILTERING =====

    override fun getAllProjects(): Flow<List<CreativeProject>> {
        println("🔍 [ProjectRepositoryImpl] getAllProjects() called - current flow has ${projectsFlow.value.size} projects")
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
            projects.filter { it.currentPhase == phase }
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
        return try {

            val project = projectsCache[projectId]
            if (project != null) {
                val analytics = analyticsCache[projectId] ?: createSampleAnalytics(project)
                Result.success(analytics)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDashboardMetrics(): Result<DashboardMetrics> {
        return try {
            val allProjects = projectsCache.values.toList()
            val metrics = DashboardMetrics(
                totalProjects = allProjects.size,
                activeProjects = allProjects.count {
                    it.status in listOf(
                        ProjectStatus.IN_DEVELOPMENT,
                        ProjectStatus.IN_PRODUCTION,
                        ProjectStatus.POST_PRODUCTION
                    )
                },
                completedProjects = allProjects.count { it.status == ProjectStatus.COMPLETED },
                totalRevenue = allProjects.sumOf { it.budget?.totalBudget?.toDouble() ?: 0.0 }.toFloat(),
                averageCompletionRate = calculateAverageCompletionRate(allProjects),
                upcomingDeadlines = getUpcomingDeadlines(allProjects),
                completedThisMonth = 0, // Calculate based on actual dates
                averageProjectDuration = 90f, // Mock value
                averageCostPerMinute = 100f, // Mock value
                clientSatisfactionScore = 4.5f, // Mock value
                aiEfficiencyScore = 88f, // Mock value
                platformPerformance = mapOf(
                    "YouTube" to 92f,
                    "TikTok" to 85f,
                    "Instagram" to 88f
                )
            )

            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== TEAM AND COLLABORATION =====

    override suspend fun addTeamMember(projectId: String, member: TeamMember): Result<Unit> {
        return try {

            teamMembersCache[projectId]?.add(member)
            teamMembersFlow[projectId]?.value = teamMembersCache[projectId] ?: emptyList()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeTeamMember(projectId: String, memberId: String): Result<Unit> {
        return try {

            teamMembersCache[projectId]?.removeAll { it.id == memberId }
            teamMembersFlow[projectId]?.value = teamMembersCache[projectId] ?: emptyList()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTeamMember(projectId: String, member: TeamMember): Result<Unit> {
        return try {

            val members = teamMembersCache[projectId]
            members?.let {
                val index = it.indexOfFirst { m -> m.id == member.id }
                if (index != -1) {
                    it[index] = member
                    teamMembersFlow[projectId]?.value = it
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTeamMembers(projectId: String): Result<List<TeamMember>> {
        return try {
            Result.success(teamMembersCache[projectId] ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== TASKS AND MILESTONES =====

    override suspend fun addTask(projectId: String, task: PhaseTask): Result<PhaseTask> {
        return try {
            tasksCache[projectId]?.add(task)
            tasksFlow[projectId]?.value = tasksCache[projectId] ?: emptyList()

            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(projectId: String, task: PhaseTask): Result<PhaseTask> {
        return try {
            val tasks = tasksCache[projectId]
            tasks?.let {
                val index = it.indexOfFirst { t -> t.id == task.id }
                if (index != -1) {
                    it[index] = task
                    tasksFlow[projectId]?.value = it
                }
            }

            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(projectId: String, taskId: String): Result<Unit> {
        return try {
            tasksCache[projectId]?.removeAll { it.id == taskId }
            tasksFlow[projectId]?.value = tasksCache[projectId] ?: emptyList()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTasks(projectId: String): Result<List<PhaseTask>> {
        return try {
            Result.success(tasksCache[projectId] ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMilestone(projectId: String, milestone: Milestone): Result<Milestone> {
        return try {
            milestonesCache[projectId]?.add(milestone)

            Result.success(milestone)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMilestone(projectId: String, milestone: Milestone): Result<Milestone> {
        return try {

            val milestones = milestonesCache[projectId]
            milestones?.let {
                val index = it.indexOfFirst { m -> m.id == milestone.id }
                if (index != -1) {
                    it[index] = milestone
                }
            }

            Result.success(milestone)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMilestones(projectId: String): Result<List<Milestone>> {
        return try {
            Result.success(milestonesCache[projectId] ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== DELIVERABLES =====

    override suspend fun addDeliverable(projectId: String, deliverable: Deliverable): Result<Deliverable> {
        return try {
            deliverablesCache[projectId]?.add(deliverable)

            Result.success(deliverable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDeliverable(projectId: String, deliverable: Deliverable): Result<Deliverable> {
        return try {
            val deliverables = deliverablesCache[projectId]
            deliverables?.let {
                val index = it.indexOfFirst { d -> d.id == deliverable.id }
                if (index != -1) {
                    it[index] = deliverable
                }
            }

            Result.success(deliverable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markDeliverableComplete(projectId: String, deliverableId: String): Result<Unit> {
        return try {
            val deliverables = deliverablesCache[projectId]
            deliverables?.let {
                val index = it.indexOfFirst { d -> d.id == deliverableId }
                if (index != -1) {
                    val deliverable = it[index]
                    it[index] = deliverable.copy(
                        status = DeliverableStatus.DELIVERED,
                        deliveredDate = Clock.System.now()
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDeliverables(projectId: String): Result<List<Deliverable>> {
        return try {
            Result.success(deliverablesCache[projectId] ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== BUDGET MANAGEMENT =====

    override suspend fun updateBudget(projectId: String, budget: ProjectBudget): Result<ProjectBudget> {
        return try {
            projectsCache[projectId]?.let { project ->
                val updatedProject = project.copy(budget = budget)
                projectsCache[projectId] = updatedProject
                projectsFlow.value = projectsCache.values.toList()
            }

            Result.success(budget)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addExpense(
        projectId: String,
        category: BudgetCategory,
        amount: Float,
        description: String
    ): Result<Unit> {
        return try {
            projectsCache[projectId]?.let { project ->
                project.budget?.let { budget ->
                    val currentCategory = budget.categories[category]
                    if (currentCategory != null) {
                        val updatedCategory = currentCategory.copy(
                            spent = currentCategory.spent + amount
                        )
                        val updatedCategories = budget.categories.toMutableMap()
                        updatedCategories[category] = updatedCategory

                        val updatedBudget = budget.copy(
                            categories = updatedCategories,
                            spentAmount = budget.spentAmount + amount
                        )

                        val updatedProject = project.copy(budget = updatedBudget)
                        projectsCache[projectId] = updatedProject
                        projectsFlow.value = projectsCache.values.toList()
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBudgetBreakdown(projectId: String): Result<ProjectBudget?> {
        return try {
            Result.success(projectsCache[projectId]?.budget)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== PLATFORM INTEGRATION =====

    override suspend fun updatePlatformTargets(
        projectId: String,
        platforms: List<StreamingPlatform>
    ): Result<Unit> {
        return try {
            projectsCache[projectId]?.let { project ->
                val updatedProject = project.copy(platformTargets = platforms)
                projectsCache[projectId] = updatedProject
                projectsFlow.value = projectsCache.values.toList()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun trackPlatformPerformance(
        projectId: String,
        platformId: String,
        metrics: PlatformMetrics
    ): Result<Unit> {
        return try {
            platformMetricsCache[projectId]?.add(metrics)
                ?: run {
                    platformMetricsCache[projectId] = mutableListOf(metrics)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlatformMetrics(
        projectId: String,
        platformId: String?
    ): Result<List<PlatformMetrics>> {
        return try {
            val metrics = platformMetricsCache[projectId] ?: emptyList()
            val filtered = if (platformId != null) {
                metrics.filter { it.platformId == platformId }
            } else {
                metrics
            }

            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== REAL-TIME UPDATES =====

    override fun observeProject(projectId: String): Flow<CreativeProject?> {
        return projectUpdateFlow.getOrPut(projectId) {
            MutableStateFlow(projectsCache[projectId])
        }
    }

    override fun observeTeamMembers(projectId: String): Flow<List<TeamMember>> {
        return teamMembersFlow.getOrPut(projectId) {
            MutableStateFlow(teamMembersCache[projectId] ?: emptyList())
        }
    }

    override fun observeTasks(projectId: String): Flow<List<PhaseTask>> {
        return tasksFlow.getOrPut(projectId) {
            MutableStateFlow(tasksCache[projectId] ?: emptyList())
        }
    }

    // ===== ANALYTICS HELPER FUNCTIONS =====

    private fun createSampleAnalytics(project: CreativeProject): ProjectAnalytics {
        val tasks = extractTasks(project.timeline)
        val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
        val totalTasks = tasks.size

        return ProjectAnalytics(
            projectId = project.id,
            performanceMetrics = PerformanceMetrics(
                totalViews = (10000..1000000).random().toLong(),
                engagementRate = (2.5f..15.0f).random(),
                completionRate = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) * 100 else 0f,
                shareRate = (0.5f..5.0f).random(),
                likeRate = (5f..25f).random(),
                commentRate = (1f..10f).random(),
                revenueGenerated = (1000f..50000f).random()
            ),
            costAnalysis = CostAnalysis(
                costPerMinute = 100f,
                costPerView = 0.01f,
                budgetUtilization = project.budget?.let { (it.spentAmount / it.totalBudget) * 100 } ?: 0f,
                costEfficiency = 85f,
                aiCostBreakdown = mapOf(
                    "GPT-4" to 5000f,
                    "DALL-E" to 3000f,
                    "Stable Diffusion" to 2000f
                ),
                teamCostBreakdown = mapOf(
                    TeamRole.PROJECT_MANAGER to 10000f,
                    TeamRole.CREATIVE_DIRECTOR to 8000f,
                    TeamRole.WRITER to 5000f
                )
            ),
            timelineAnalysis = TimelineAnalysis(
                plannedDuration = 90f,
                actualDuration = 85f,
                timeEfficiency = 94.4f,
                phaseBreakdown = mapOf(
                    ProductionPhase.DEVELOPMENT to 30f,
                    ProductionPhase.PRE_PRODUCTION to 20f,
                    ProductionPhase.PRODUCTION to 25f,
                    ProductionPhase.POST_PRODUCTION to 10f
                ),
                bottlenecks = listOf("Script approval", "Asset generation"),
                accelerators = listOf("AI assistance", "Parallel processing")
            ),
            qualityAnalysis = QualityAnalysis(
                averageQualityScore = 92f,
                qualityTrend = listOf(
                    QualityDataPoint(Clock.System.now() - 30.days, 85f, ProductionPhase.DEVELOPMENT),
                    QualityDataPoint(Clock.System.now() - 20.days, 88f, ProductionPhase.PRE_PRODUCTION),
                    QualityDataPoint(Clock.System.now() - 10.days, 92f, ProductionPhase.PRODUCTION)
                ),
                platformApprovalRate = 95f,
                clientSatisfactionScore = 4.5f,
                aiEffectivenessScore = 88f
            ),
            platformPerformance = emptyMap()
        )
    }

    private fun createSamplePlatformMetrics(project: CreativeProject): MutableList<PlatformMetrics> {
        return project.platformTargets.map { platform ->
            PlatformMetrics(
                platformId = platform.id,
                uploadSuccess = true,
                processingTime = (1f..5f).random(),
                approvalStatus = "APPROVED",
                performanceScore = (80f..100f).random(),
                audienceReach = (10000..1000000).random().toLong(),
                engagementMetrics = PerformanceMetrics(
                    totalViews = (10000..500000).random().toLong(),
                    engagementRate = (2f..15f).random(),
                    completionRate = (60f..95f).random(),
                    shareRate = (0.5f..5f).random(),
                    likeRate = (5f..20f).random(),
                    commentRate = (1f..8f).random(),
                    revenueGenerated = (100f..10000f).random()
                )
            )
        }.toMutableList()
    }

    private fun calculateAverageCompletionRate(projects: List<CreativeProject>): Float {
        if (projects.isEmpty()) return 0f

        val completionRates = projects.map { project ->
            val tasks = extractTasks(project.timeline)
            val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
            val totalTasks = tasks.size
            if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) * 100 else 0f
        }

        return completionRates.average().toFloat()
    }

    private fun getUpcomingDeadlines(projects: List<CreativeProject>): List<UpcomingDeadline> {
        val now = Clock.System.now()
        val deadlines = mutableListOf<UpcomingDeadline>()

        projects.forEach { project ->
            project.timeline?.milestones
                ?.filter { it.targetDate > now && !it.isCompleted }
                ?.sortedBy { it.targetDate }
                ?.take(3)
                ?.forEach { milestone ->
                    deadlines.add(
                        UpcomingDeadline(
                            projectId = project.id,
                            projectTitle = project.title,
                            milestoneTitle = milestone.title,
                            dueDate = milestone.targetDate,
                            daysRemaining = ((milestone.targetDate - now).inWholeDays).toInt()
                        )
                    )
                }
        }

        return deadlines.sortedBy { it.dueDate }.take(10)
    }

    override suspend fun getDistributionAnalytics(projectId: String): Result<DistributionAnalytics> {
        return try {
            // Mock implementation - replace with actual data source
            val analytics = DistributionAnalytics(
                projectId = projectId,
                totalRevenue = 125000f,
                revenueByChannel = mapOf(
                    SocialPlatform.YOUTUBE to 45000f,
                    SocialPlatform.TIKTOK to 35000f,
                    SocialPlatform.INSTAGRAM to 25000f,
                    SocialPlatform.FACEBOOK to 20000f
                ),
                revenueByGeo = mapOf(
                    "US" to 50000f,
                    "UK" to 25000f,
                    "CA" to 15000f,
                    "AU" to 10000f,
                    "Other" to 25000f
                ),
                revenueByContentType = mapOf(),
                revenueByDuration = mapOf(
                    DurationBucket.THIRTY_SECONDS to 30000f,
                    DurationBucket.ONE_TO_THREE_MIN to 45000f,
                    DurationBucket.THREE_TO_TEN_MIN to 50000f
                ),
                performanceMetrics = DistributionPerformanceMetrics(
                    totalViews = 2500000,
                    engagementRate = 4.5f,
                    completionRate = 65f,
                    shareRate = 2.3f,
                    likeRate = 5.2f,
                    commentRate = 1.8f,
                    revenueGenerated = 125000f,
                    avgViewDuration = 180f,
                    clickThroughRate = 3.2f
                ),
                optimizationSuggestions = listOf(
                    OptimizationSuggestion(
                        id = "opt1",
                        type = RecommendationType.THUMBNAIL_OPTIMIZATION,
                        title = "Improve Thumbnail CTR",
                        description = "Your thumbnails could be more engaging",
                        estimatedRevenueImpact = 15000f,
                        priority = Priority.HIGH,
                        actionItems = listOf(
                            "Use brighter colors",
                            "Add text overlay",
                            "Test A/B variations"
                        )
                    )
                ),
                totalViews = 0,
                averageEngagement = 0.0f,
                periodStart = 0,
                periodEnd = 0,
                generatedAt = 0
            )
            Result.success(analytics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTitleRevenue(projectId: String, titleId: String): Result<TitleRevenue> {
        return try {
            val titleRevenue = TitleRevenue(
                titleId = titleId,
                title = "Sample Title",
                totalRevenue = 25000f,
                channelBreakdown = mapOf(
                    SocialPlatform.YOUTUBE to ChannelPerformance(
                        platform = SocialPlatform.YOUTUBE,
                        revenue = 15000f,
                        views = 500000,
                        engagement = 4.5f,
                        averageWatchTime = 240f,
                        shareCount = 5000,
                        commentCount = 2000,
                        likeCount = 25000
                    )
                ),
                viewershipRetention = listOf(
                    RetentionPoint(0f, 100f),
                    RetentionPoint(10f, 95f),
                    RetentionPoint(25f, 85f),
                    RetentionPoint(50f, 70f),
                    RetentionPoint(75f, 60f),
                    RetentionPoint(100f, 55f)
                ),
                recommendations = listOf(),
                contentClassification = ContentClassification(
                    format = ContentFormat.SHORT_FORM,
                    duration = DurationBucket.ONE_TO_THREE_MIN,
                    orientation = ContentOrientation.PORTRAIT,
                    style = ContentStyle.ENTERTAINMENT
                ),
                publishDate = Clock.System.now().toEpochMilliseconds(),
                lastUpdated = Clock.System.now().toEpochMilliseconds()
            )
            Result.success(titleRevenue)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRevenueData(projectId: String, revenueData: RevenueUpdate): Result<Unit> {
        return try {
            // Update revenue data in your data source
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getContentTitles(projectId: String): Result<List<TitleRevenue>> {
        return try {
            // Return list of titles with revenue data
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeDistributionMetrics(projectId: String): Flow<DistributionAnalytics> {
        return flow {
            // Emit distribution metrics updates
            getDistributionAnalytics(projectId).fold(
                onSuccess = { emit(it) },
                onFailure = { /* Handle error */ }
            )
        }
    }
}

// Helper extension functions for Float ranges
fun ClosedFloatingPointRange<Float>.random(): Float {
    return start + Random.nextFloat() * (endInclusive - start)
}