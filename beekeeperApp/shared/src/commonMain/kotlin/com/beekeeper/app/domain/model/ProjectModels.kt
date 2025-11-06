// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/project/ProjectModels.kt
package com.beekeeper.app.domain.project

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import com.beekeeper.app.domain.model.*

/**
 * Models specific to ProjectUseCases that don't exist in domain.model
 * These are NOT duplicates - only models that are truly missing
 */

// Dashboard related models
@Serializable
data class DashboardMetrics(
    val totalProjects: Int,
    val activeProjects: Int,
    val completedProjects: Int,
    val completedThisMonth: Int,
    val averageProjectDuration: Float,
    val averageCostPerMinute: Float,
    val totalRevenue: Float,
    val averageCompletionRate: Float,
    val clientSatisfactionScore: Float,
    val aiEfficiencyScore: Float,
    val platformPerformance: Map<String, Float>,
    val upcomingDeadlines: List<UpcomingDeadline>
)

@Serializable
data class UpcomingDeadline(
    val projectId: String,
    val projectTitle: String,
    val milestoneTitle: String,
    val dueDate: Instant,
    val daysRemaining: Int
)

@Serializable
data class DashboardData(
    val totalProjects: Int,
    val activeProjects: Int,
    val projectsByStatus: Map<ProjectStatus, Int>,
    val projectsByType: Map<ProjectType, Int>,
    val projectsByPhase: Map<ProductionPhase, Int>,
    val urgentProjects: List<CreativeProject>,
    val recentProjects: List<CreativeProject>,
    val totalBudget: Float,
    val spentBudget: Float
)

@Serializable
data class BudgetSummary(
    val totalBudget: Float,
    val totalSpent: Float,
    val remaining: Float,
    val utilizationPercentage: Float,
    val categoryBreakdown: Map<BudgetCategory, CategoryBudget>,
    val isOverBudget: Boolean
)

@Serializable
data class PlatformOptimization(
    val platformId: String,
    val recommendedFormat: String,
    val recommendedResolution: String,
    val compressionSettings: CompressionSettings,
    val metadataOptimization: MetadataOptimization,
    val estimatedProcessingTime: Float
)

@Serializable
data class CompressionSettings(
    val videoCodec: String = "h265",
    val audioCodec: String = "mp4",
    val audioBitrate: Int = 256,
    val videoBitrate: Int = 1500,
    val keyframeInterval: Int = 15,
    val quality: Int = 0,
    val maxSize: Long?
)



@Serializable
data class MetadataOptimization(
    val titleOptimization: String,
    val descriptionOptimization: String,
    val tagsOptimization: List<String>,
    val thumbnailOptimization: String
)

enum class WorkflowAutomationType {
    AUTO_ASSIGN_TASKS,
    GENERATE_TIMELINE,
    OPTIMIZE_BUDGET,
    PREDICT_BOTTLENECKS
}

@Serializable
data class WorkflowAutomationResult(
    val type: WorkflowAutomationType,
    val success: Boolean,
    val message: String,
    val affectedItems: List<String>
)