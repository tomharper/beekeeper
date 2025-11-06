// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/ProjectRepository.kt
package com.beekeeper.app.domain.repository


import kotlinx.coroutines.flow.Flow
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.project.DashboardMetrics

/**
 * Unified repository interface for project operations
 * Combines all functionality from both previous interfaces (superset approach)
 * ALL MODELS ARE IMPORTED FROM domain.model PACKAGE
 */
interface ProjectRepository {
    
    // ===== CORE PROJECT CRUD OPERATIONS =====
    /**
     * Create a new creative project
     * @return Result containing the created project or error
     */
    suspend fun createProject(project: CreativeProject): Result<CreativeProject>
    
    /**
     * Get a specific project by ID
     * @return Result containing the project or null if not found
     */
    suspend fun getProject(projectId: String): Result<CreativeProject?>
    
    /**
     * Update an existing project
     * @return Result containing the updated project or error
     */
    suspend fun updateProject(project: CreativeProject): Result<CreativeProject>
    
    /**
     * Delete a project by ID
     * @return Result indicating success or failure
     */
    suspend fun deleteProject(projectId: String): Result<Unit>
    
    // ===== PROJECT LISTING AND FILTERING =====
    /**
     * Get all projects as a reactive Flow
     * @return Flow emitting list of all projects
     */
    fun getAllProjects(): Flow<List<CreativeProject>>
    
    /**
     * Get projects filtered by status
     * @return Flow emitting filtered projects
     */
    fun getProjectsByStatus(status: ProjectStatus): Flow<List<CreativeProject>>
    
    /**
     * Get projects filtered by type
     * @return Flow emitting filtered projects
     */
    fun getProjectsByType(type: ProjectType): Flow<List<CreativeProject>>
    
    /**
     * Get projects for a specific client
     * @return Flow emitting client's projects
     */
    fun getProjectsByClient(clientId: String): Flow<List<CreativeProject>>
    
    /**
     * Get projects in a specific production phase
     * @return Flow emitting projects in the specified phase
     */
    fun getProjectsByPhase(phase: ProductionPhase): Flow<List<CreativeProject>>
    
    /**
     * Get projects filtered by priority level
     * @return Flow emitting projects with specified priority
     */
    fun getProjectsByPriority(priority: ProjectPriority): Flow<List<CreativeProject>>
    
    // ===== PROJECT SEARCH AND ANALYTICS =====
    /**
     * Search projects by query string
     * @param query Search query
     * @return Result containing matching projects
     */
    suspend fun searchProjects(query: String): Result<List<CreativeProject>>
    
    /**
     * Get analytics data for a specific project
     * @return Result containing project analytics or null
     */
    suspend fun getProjectAnalytics(projectId: String): Result<ProjectAnalytics?>
    
    /**
     * Get overall dashboard metrics across all projects
     * @return Result containing dashboard metrics
     */
    suspend fun getDashboardMetrics(): Result<DashboardMetrics>
    
    // ===== TEAM AND COLLABORATION =====
    /**
     * Add a team member to a project
     * @return Result indicating success or failure
     */
    suspend fun addTeamMember(projectId: String, member: TeamMember): Result<Unit>
    
    /**
     * Remove a team member from a project
     * @return Result indicating success or failure
     */
    suspend fun removeTeamMember(projectId: String, memberId: String): Result<Unit>
    
    /**
     * Update team member details in a project
     * @return Result indicating success or failure
     */
    suspend fun updateTeamMember(projectId: String, member: TeamMember): Result<Unit>
    
    /**
     * Get all team members for a project
     * @return Result containing list of team members
     */
    suspend fun getTeamMembers(projectId: String): Result<List<TeamMember>>
    
    // ===== TASKS AND MILESTONES =====
    /**
     * Add a new task to a project phase
     * @return Result containing the created task
     */
    suspend fun addTask(projectId: String, task: PhaseTask): Result<PhaseTask>
    
    /**
     * Update an existing task
     * @return Result containing the updated task
     */
    suspend fun updateTask(projectId: String, task: PhaseTask): Result<PhaseTask>
    
    /**
     * Delete a task from a project
     * @return Result indicating success or failure
     */
    suspend fun deleteTask(projectId: String, taskId: String): Result<Unit>
    
    /**
     * Get all tasks for a project
     * @return Result containing list of tasks
     */
    suspend fun getTasks(projectId: String): Result<List<PhaseTask>>
    
    /**
     * Add a milestone to a project
     * @return Result containing the created milestone
     */
    suspend fun addMilestone(projectId: String, milestone: Milestone): Result<Milestone>
    
    /**
     * Update an existing milestone
     * @return Result containing the updated milestone
     */
    suspend fun updateMilestone(projectId: String, milestone: Milestone): Result<Milestone>
    
    /**
     * Get all milestones for a project
     * @return Result containing list of milestones
     */
    suspend fun getMilestones(projectId: String): Result<List<Milestone>>
    
    // ===== DELIVERABLES =====
    /**
     * Add a deliverable to a project
     * @return Result containing the created deliverable
     */
    suspend fun addDeliverable(projectId: String, deliverable: Deliverable): Result<Deliverable>
    
    /**
     * Update an existing deliverable
     * @return Result containing the updated deliverable
     */
    suspend fun updateDeliverable(projectId: String, deliverable: Deliverable): Result<Deliverable>
    
    /**
     * Mark a deliverable as complete
     * @return Result indicating success or failure
     */
    suspend fun markDeliverableComplete(projectId: String, deliverableId: String): Result<Unit>
    
    /**
     * Get all deliverables for a project
     * @return Result containing list of deliverables
     */
    suspend fun getDeliverables(projectId: String): Result<List<Deliverable>>
    
    // ===== BUDGET MANAGEMENT =====
    /**
     * Update project budget
     * @return Result containing the updated budget
     */
    suspend fun updateBudget(projectId: String, budget: ProjectBudget): Result<ProjectBudget>
    
    /**
     * Add an expense to a project budget
     * @return Result indicating success or failure
     */
    suspend fun addExpense(
        projectId: String, 
        category: BudgetCategory, 
        amount: Float, 
        description: String
    ): Result<Unit>
    
    /**
     * Get budget breakdown for a project
     * @return Result containing budget details
     */
    suspend fun getBudgetBreakdown(projectId: String): Result<ProjectBudget?>
    
    // ===== PLATFORM INTEGRATION =====
    /**
     * Update target platforms for a project
     * @return Result indicating success or failure
     */
    suspend fun updatePlatformTargets(
        projectId: String, 
        platforms: List<StreamingPlatform>
    ): Result<Unit>
    
    /**
     * Track performance metrics for a specific platform
     * @return Result indicating success or failure
     */
    suspend fun trackPlatformPerformance(
        projectId: String, 
        platformId: String, 
        metrics: PlatformMetrics
    ): Result<Unit>
    
    /**
     * Get platform performance data
     * @return Result containing platform metrics
     */
    suspend fun getPlatformMetrics(
        projectId: String, 
        platformId: String? = null
    ): Result<List<PlatformMetrics>>
    
    // ===== REAL-TIME UPDATES =====
    /**
     * Observe project updates in real-time
     * @return Flow emitting project updates
     */
    fun observeProject(projectId: String): Flow<CreativeProject?>
    
    /**
     * Observe team member changes
     * @return Flow emitting team member updates
     */
    fun observeTeamMembers(projectId: String): Flow<List<TeamMember>>
    
    /**
     * Observe task updates
     * @return Flow emitting task updates
     */
    fun observeTasks(projectId: String): Flow<List<PhaseTask>>

    suspend fun getDistributionAnalytics(projectId: String): Result<DistributionAnalytics>
    suspend fun getTitleRevenue(projectId: String, titleId: String): Result<TitleRevenue>
    suspend fun updateRevenueData(projectId: String, revenueData: RevenueUpdate): Result<Unit>
    suspend fun getContentTitles(projectId: String): Result<List<TitleRevenue>>
    fun observeDistributionMetrics(projectId: String): Flow<DistributionAnalytics>
}
