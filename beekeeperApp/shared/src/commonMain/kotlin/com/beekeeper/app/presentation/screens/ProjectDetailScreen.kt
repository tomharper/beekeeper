// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/ProjectDetailScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.rememberAppTheme
import com.beekeeper.app.presentation.viewmodels.ProjectDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    viewModel: ProjectDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPhase: (ProductionPhase) -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val project by viewModel.project.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(ProjectDetailTab.OVERVIEW) }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        ProjectDetailTopBar(
            project = project,
            onNavigateBack = onNavigateBack,
            onNavigateToAnalytics = onNavigateToAnalytics
        )

        when (uiState) {
            is ProjectDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProjectDetailUiState.Error -> {
                val errorState = uiState as ProjectDetailUiState.Error
                ErrorContent(
                    message = errorState.message,
                    onRetry = { viewModel.loadProject(projectId) }
                )
            }

            is ProjectDetailUiState.Success -> {
                project?.let { proj ->
                    ProjectDetailContent(
                        project = proj,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        onNavigateToPhase = onNavigateToPhase,
                        onNavigateToTeam = onNavigateToTeam,
                        onTaskUpdate = { task -> viewModel.updateTask(task) },
                        onBudgetUpdate = { budget -> viewModel.updateBudget(budget) },
                        onAddMilestone = { milestone -> viewModel.addMilestone(milestone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectDetailTopBar(
    project: CreativeProject?,
    onNavigateBack: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    SecondaryTopBar(
        title = project?.title ?: "Project Details",
        subtitle = project?.type?.toString(),
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = onNavigateToAnalytics) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = "Analytics",
                    tint = rememberAppTheme().colors.onSurface
                )
            }
        }
    )
}

@Composable
private fun ProjectDetailContent(
    project: CreativeProject,
    selectedTab: ProjectDetailTab,
    onTabSelected: (ProjectDetailTab) -> Unit,
    onNavigateToPhase: (ProductionPhase) -> Unit,
    onNavigateToTeam: () -> Unit,
    onTaskUpdate: (PhaseTask) -> Unit,
    onBudgetUpdate: (ProjectBudget) -> Unit,
    onAddMilestone: (Milestone) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Project Status Header
        ProjectStatusHeader(project = project)

        // Tab Bar
        ProjectDetailTabBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )

        // Tab Content
        when (selectedTab) {
            ProjectDetailTab.OVERVIEW -> {
                ProjectOverviewTab(
                    project = project,
                    onNavigateToPhase = onNavigateToPhase,
                    onNavigateToTeam = onNavigateToTeam
                )
            }
            ProjectDetailTab.PHASES -> {
                ProjectPhasesTab(
                    project = project,
                    onNavigateToPhase = onNavigateToPhase,
                    onTaskUpdate = onTaskUpdate
                )
            }
            ProjectDetailTab.TEAM -> {
                ProjectTeamTab(
                    project = project,
                    onNavigateToTeam = onNavigateToTeam
                )
            }
            ProjectDetailTab.BUDGET -> {
                ProjectBudgetTab(
                    project = project,
                    onBudgetUpdate = onBudgetUpdate
                )
            }
            ProjectDetailTab.DELIVERABLES -> {
                ProjectDeliverablesTab(project = project)
            }
        }
    }
}

@Composable
private fun ProjectStatusHeader(project: CreativeProject) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Phase",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = project.currentPhase.name.replace('_', ' '),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Priority Badge
                PriorityBadge(priority = project.priority)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            ProjectProgressIndicator(project = project)

            Spacer(modifier = Modifier.height(16.dp))

            // Key Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    title = "Budget Used",
                    value = project.budget?.let {
                        "${((it.spentAmount / it.totalBudget) * 100).toInt()}%"
                    } ?: "N/A"
                )

                MetricItem(
                    title = "Team Size",
                    value = "${getTeamSize(project.team)}"
                )

                MetricItem(
                    title = "Platforms",
                    value = "${project.platformTargets.size}"
                )

                MetricItem(
                    title = "Deliverables",
                    value = "${project.deliverables.count { it.status == DeliverableStatus.DELIVERED }}/${project.deliverables.size}"
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ProjectDetailTabBar(
    selectedTab: ProjectDetailTab,
    onTabSelected: (ProjectDetailTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        ProjectDetailTab.values().forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun ProjectOverviewTab(
    project: CreativeProject,
    onNavigateToPhase: (ProductionPhase) -> Unit,
    onNavigateToTeam: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Project Description
        item {
            OverviewCard(
                title = "Project Description",
                icon = Icons.Default.Description
            ) {
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Timeline Overview
        item {
            OverviewCard(
                title = "Timeline",
                icon = Icons.Default.Schedule
            ) {
                TimelineOverview(project = project)
            }
        }

        // Current Phase Tasks
        item {
            OverviewCard(
                title = "Current Phase Tasks",
                icon = Icons.Default.Task,
                actionText = "View All",
                onAction = { onNavigateToPhase(project.currentPhase) }
            ) {
                CurrentPhaseTasks(project = project)
            }
        }

        // Recent Activity
        item {
            OverviewCard(
                title = "Recent Activity",
                icon = Icons.Default.History
            ) {
                RecentActivity(project = project)
            }
        }

        // Platform Targets
        item {
            OverviewCard(
                title = "Target Platforms",
                icon = Icons.Default.Devices
            ) {
                PlatformTargets(platforms = project.platformTargets)
            }
        }
    }
}

@Composable
private fun OverviewCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                if (actionText != null && onAction != null) {
                    TextButton(onClick = onAction) {
                        Text(actionText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

// Placeholder components for the overview tab content
@Composable
private fun TimelineOverview(project: CreativeProject) {
    // Implementation for timeline overview
    Text("Timeline content will be implemented here")
}

@Composable
private fun CurrentPhaseTasks(project: CreativeProject) {
    // Implementation for current phase tasks
    Text("Current phase tasks will be implemented here")
}

@Composable
private fun RecentActivity(project: CreativeProject) {
    // Implementation for recent activity
    Text("Recent activity will be implemented here")
}

@Composable
private fun PlatformTargets(platforms: List<StreamingPlatform>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(platforms) { platform ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = platform.name,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

// Placeholder implementations for other tabs
@Composable
private fun ProjectPhasesTab(
    project: CreativeProject,
    onNavigateToPhase: (ProductionPhase) -> Unit,
    onTaskUpdate: (PhaseTask) -> Unit
) {
    Text(
        "Phases tab content will be implemented here",
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun ProjectTeamTab(
    project: CreativeProject,
    onNavigateToTeam: () -> Unit
) {
    Text(
        "Team tab content will be implemented here",
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun ProjectBudgetTab(
    project: CreativeProject,
    onBudgetUpdate: (ProjectBudget) -> Unit
) {
    Text(
        "Budget tab content will be implemented here",
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun ProjectDeliverablesTab(project: CreativeProject) {
    Text(
        "Deliverables tab content will be implemented here",
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// Tab definitions
enum class ProjectDetailTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    PHASES("Phases", Icons.Default.Timeline),
    TEAM("Team", Icons.Default.Group),
    BUDGET("Budget", Icons.Default.AttachMoney),
    DELIVERABLES("Deliverables", Icons.Default.Folder)
}

// UI State for Project Detail
sealed class ProjectDetailUiState {
    object Loading : ProjectDetailUiState()
    data class Success(val project: CreativeProject) : ProjectDetailUiState()
    data class Error(val message: String) : ProjectDetailUiState()
}


@Composable
fun PriorityBadge(priority: ProjectPriority) {
    val (color, text) = when (priority) {
        ProjectPriority.LOW -> Color(0xFF4CAF50) to "Low"
        ProjectPriority.MEDIUM -> Color(0xFFFF9800) to "Medium"
        ProjectPriority.HIGH -> Color(0xFFFF5722) to "High"
        ProjectPriority.CRITICAL -> Color(0xFFF44336) to "Critical"
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}

@Composable
fun ProjectProgressIndicator(project: CreativeProject) {
    val progress = calculateProjectProgress(project)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

fun getTeamSize(team: ProjectTeam?): Int {
    if (team == null) return 0
    return 1 + // project manager is required
            (if (team.creativeDirector != null) 1 else 0) +
            team.writers.size +
            team.designers.size +
            (if (team.technicalLead != null) 1 else 0) +
            team.qaTeam.size +
            team.clientContacts.size +
            team.aiSpecialists.size
}

private fun calculateProjectProgress(project: CreativeProject): Float {
    val phaseWeights = mapOf(
        ProductionPhase.DEVELOPMENT to 0.15f,
        ProductionPhase.PRE_PRODUCTION to 0.20f,
        ProductionPhase.PRODUCTION to 0.35f,
        ProductionPhase.POST_PRODUCTION to 0.25f,
        ProductionPhase.DISTRIBUTION to 0.05f
    )

    var totalProgress = 0f

    // Calculate progress for each phase
    ProductionPhase.values().forEach { phase ->
        val phaseTimeline = project.timeline?.phases?.get(phase)
        val phaseWeight = phaseWeights[phase] ?: 0f

        val phaseProgress = when {
            phase < project.currentPhase -> 1f // Completed phases
            phase == project.currentPhase -> phaseTimeline?.progress ?: 0f // Current phase
            else -> 0f // Future phases
        }

        totalProgress += phaseProgress * phaseWeight
    }

    return totalProgress.coerceIn(0f, 1f)
}

// Comparison operator for ProductionPhase
private operator fun ProductionPhase.compareTo(other: ProductionPhase): Int {
    val order = listOf(
        ProductionPhase.DEVELOPMENT,
        ProductionPhase.PRE_PRODUCTION,
        ProductionPhase.PRODUCTION,
        ProductionPhase.POST_PRODUCTION,
        ProductionPhase.DISTRIBUTION
    )
    return order.indexOf(this).compareTo(order.indexOf(other))
}