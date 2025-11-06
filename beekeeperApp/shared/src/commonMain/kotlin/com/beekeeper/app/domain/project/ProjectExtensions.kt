// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/project/ProjectExtensions.kt
package com.beekeeper.app.domain.project
import kotlinx.datetime.Clock
import com.beekeeper.app.domain.model.*

// Extension functions for creating default instances
fun createDefaultProjectTimeline(
    startDate: kotlinx.datetime.Instant,
    projectType: ProjectType
): ProjectTimeline {
    val phases = mapOf(
        ProductionPhase.DEVELOPMENT to createDefaultPhaseTimeline(ProductionPhase.DEVELOPMENT),
        ProductionPhase.PRE_PRODUCTION to createDefaultPhaseTimeline(ProductionPhase.PRE_PRODUCTION),
        ProductionPhase.PRODUCTION to createDefaultPhaseTimeline(ProductionPhase.PRODUCTION),
        ProductionPhase.POST_PRODUCTION to createDefaultPhaseTimeline(ProductionPhase.POST_PRODUCTION),
        ProductionPhase.DISTRIBUTION to createDefaultPhaseTimeline(ProductionPhase.DISTRIBUTION)
    )

    return ProjectTimeline(
        startDate = startDate,
        plannedEndDate = calculateEndDate(startDate, projectType),
        milestones = generateDefaultMilestones(projectType),
        phases = phases
    )
}

fun createDefaultPhaseTimeline(phase: ProductionPhase): PhaseTimeline {
    val now = Clock.System.now()
    return PhaseTimeline(
        phase = phase,
        startDate = now,
        plannedEndDate = now,
        tasks = generateDefaultTasks(phase)
    )
}

fun createDefaultProjectBudget(totalAmount: Float): ProjectBudget {
    val categories = mapOf(
        BudgetCategory.AI_MODEL_USAGE to CategoryBudget(totalAmount * 0.3f),
        BudgetCategory.TEAM_COSTS to CategoryBudget(totalAmount * 0.4f),
        BudgetCategory.RENDERING_RESOURCES to CategoryBudget(totalAmount * 0.15f),
        BudgetCategory.PLATFORM_FEES to CategoryBudget(totalAmount * 0.05f),
        BudgetCategory.LICENSING to CategoryBudget(totalAmount * 0.05f),
        BudgetCategory.MISCELLANEOUS to CategoryBudget(totalAmount * 0.05f)
    )

    return ProjectBudget(
        totalBudget = totalAmount,
        categories = categories
    )
}

fun createEmptyDashboardData(): DashboardData {
    return DashboardData(
        totalProjects = 0,
        activeProjects = 0,
        projectsByStatus = emptyMap(),
        projectsByType = emptyMap(),
        projectsByPhase = emptyMap(),
        urgentProjects = emptyList(),
        recentProjects = emptyList(),
        totalBudget = 0f,
        spentBudget = 0f
    )
}

// Helper functions
fun calculateEndDate(
    startDate: kotlinx.datetime.Instant,
    projectType: ProjectType
): kotlinx.datetime.Instant {
    val daysToAdd = when (projectType) {
        ProjectType.SOCIAL_MEDIA_CONTENT -> 3
        ProjectType.MARKETING_CAMPAIGN -> 7
        ProjectType.MINI_SERIES -> 21
        ProjectType.TV_EPISODE -> 45
        ProjectType.TV_SERIES -> 180
        ProjectType.FEATURE_LENGTH -> 365
        ProjectType.DOCUMENTARY -> 90
        ProjectType.INTERACTIVE_CONTENT -> 120
        ProjectType.LOCALIZED_CONTENT -> 30
        ProjectType.WEB_SERIES -> 10
        ProjectType.FEATURE_FILM -> 180
        ProjectType.SHORT_FILM -> 90
        ProjectType.ANIMATION -> 180
        ProjectType.COMMERCIAL -> 15
        ProjectType.MUSIC_VIDEO -> 15
        ProjectType.EDUCATIONAL -> 15
        ProjectType.EXPERIMENTAL -> 15
        else -> 15
    }

    return kotlinx.datetime.Instant.fromEpochMilliseconds(
        startDate.toEpochMilliseconds() + (daysToAdd * 24 * 60 * 60 * 1000L)
    )
}

fun generateDefaultMilestones(projectType: ProjectType): List<Milestone> {
    return when (projectType) {
        ProjectType.SOCIAL_MEDIA_CONTENT -> listOf(
            Milestone("m1", "Script Complete", "Initial script ready for review", Clock.System.now(), phase = ProductionPhase.DEVELOPMENT),
            Milestone("m2", "Content Generated", "AI content generation complete", Clock.System.now(), phase = ProductionPhase.PRODUCTION),
            Milestone("m3", "Ready for Upload", "Content ready for platform delivery", Clock.System.now(), phase = ProductionPhase.DISTRIBUTION)
        )
        ProjectType.TV_EPISODE -> listOf(
            Milestone("m1", "Script Locked", "Final script approved", Clock.System.now(), phase = ProductionPhase.DEVELOPMENT),
            Milestone("m2", "Assets Created", "All character and environment assets ready", Clock.System.now(), phase = ProductionPhase.PRE_PRODUCTION),
            Milestone("m3", "Rough Cut", "Initial edit complete", Clock.System.now(), phase = ProductionPhase.PRODUCTION),
            Milestone("m4", "Final Cut", "Final edit approved", Clock.System.now(), phase = ProductionPhase.POST_PRODUCTION),
            Milestone("m5", "Platform Delivery", "Content delivered to streaming platforms", Clock.System.now(), phase = ProductionPhase.DISTRIBUTION)
        )
        else -> emptyList()
    }
}

fun generateDefaultTasks(phase: ProductionPhase): List<PhaseTask> {
    return when (phase) {
        ProductionPhase.DEVELOPMENT -> listOf(
            PhaseTask("t1", "Create Initial Concept", "Develop core story concept", status = TaskStatus.NOT_STARTED, estimatedHours = 8f, aiAssistanceRequired = true),
            PhaseTask("t2", "Write Script", "Complete script writing", status = TaskStatus.NOT_STARTED, estimatedHours = 16f, aiAssistanceRequired = true),
            PhaseTask("t3", "Character Development", "Develop main characters", status = TaskStatus.NOT_STARTED, estimatedHours = 12f, aiAssistanceRequired = true)
        )
        ProductionPhase.PRE_PRODUCTION -> listOf(
            PhaseTask("t4", "Create Storyboard", "Visual storyboard creation", status = TaskStatus.NOT_STARTED, estimatedHours = 20f, aiAssistanceRequired = true),
            PhaseTask("t5", "Asset Planning", "Plan all required assets", status = TaskStatus.NOT_STARTED, estimatedHours = 8f),
            PhaseTask("t6", "Casting", "Select voice actors and digital avatars", status = TaskStatus.NOT_STARTED, estimatedHours = 10f, aiAssistanceRequired = true)
        )
        ProductionPhase.PRODUCTION -> listOf(
            PhaseTask("t7", "Generate Scenes", "AI scene generation", status = TaskStatus.NOT_STARTED, estimatedHours = 30f, aiAssistanceRequired = true),
            PhaseTask("t8", "Quality Review", "Review generated content quality", status = TaskStatus.NOT_STARTED, estimatedHours = 15f),
            PhaseTask("t9", "Audio Generation", "Generate voice and sound effects", status = TaskStatus.NOT_STARTED, estimatedHours = 20f, aiAssistanceRequired = true)
        )
        ProductionPhase.POST_PRODUCTION -> listOf(
            PhaseTask("t10", "Video Editing", "Edit and assemble final video", status = TaskStatus.NOT_STARTED, estimatedHours = 25f),
            PhaseTask("t11", "Color Grading", "Apply final color grading", status = TaskStatus.NOT_STARTED, estimatedHours = 8f, aiAssistanceRequired = true),
            PhaseTask("t12", "Audio Mixing", "Final audio mix and master", status = TaskStatus.NOT_STARTED, estimatedHours = 12f)
        )
        ProductionPhase.DISTRIBUTION -> listOf(
            PhaseTask("t13", "Platform Optimization", "Optimize for target platforms", status = TaskStatus.NOT_STARTED, estimatedHours = 10f),
            PhaseTask("t14", "Upload and Delivery", "Deliver to streaming platforms", status = TaskStatus.NOT_STARTED, estimatedHours = 4f),
            PhaseTask("t15", "Performance Monitoring", "Monitor content performance", status = TaskStatus.NOT_STARTED, estimatedHours = 8f)
        )
    }
}