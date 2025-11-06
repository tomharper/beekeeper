// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/HomeScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.PrimaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager
import com.beekeeper.app.presentation.theme.rememberAppTheme
import com.beekeeper.app.presentation.viewmodels.*
import com.beekeeper.app.utils.formatNumberWithCommas
import com.beekeeper.app.utils.formatPercentage
import com.beekeeper.app.utils.formatCurrencyWithCommas
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

// Updated Quick Action Types aligned with content creation workflow
enum class HomeQuickAction(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    NEW_PROJECT("New Project", "Start a new movie/series project", Icons.Default.MovieCreation),
    CREATE_STORY("Create Story", "Write and plan your story", Icons.Default.AutoStories),
    GENERATE_SCRIPT("Generate Script", "AI-powered script generation", Icons.Default.Description),
    CREATE_FRAMES("Create Frames", "Design scenes and frames", Icons.Default.ViewInAr),
    AI_CHARACTERS("AI Characters", "Generate character avatars", Icons.Default.GroupAdd),
    EXPORT_CONTENT("Export Content", "Export to social media", Icons.Default.Share)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = rememberHomeViewModel(),
    onNavigateToProjects: () -> Unit,
    onNavigateToProject: (String) -> Unit,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onQuickAction: (HomeQuickAction) -> Unit,
    // Additional navigation handlers for compatibility
    onNavigateToCreate: () -> Unit = {},
    onNavigateToContent: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onNavigateToAvatarStudio: () -> Unit = {},
    onNavigateToPublishing: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            PrimaryTopBar(
                title = "CineFiller",
                subtitle = "AI-Powered Content Creation",
                onNotificationClick = onNavigateToNotifications
            )
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Welcome Section
            item {
                WelcomeSection(
                    stats = uiState.quickStats,
                    theme = theme
                )
            }

            // Getting Started Section
            item {
                GettingStartedSection(
                    onTutorialClick = { /* TODO: Navigate to tutorial */ },
                    theme = theme
                )
            }

            // Content Performance Section
            item {
                ContentPerformanceSection(
                    projects = uiState.recentProjects,
                    theme = theme
                )
            }

            // Recent Content Generated
            item {
                RecentContentSection(
                    projects = uiState.recentProjects,
                    theme = theme
                )
            }

            // View All Projects Link
            item {
                ViewAllProjectsCard(
                    onViewAll = onNavigateToProjects,
                    theme = theme
                )
            }

            // Error Message
            uiState.error?.let { error ->
                item {
                    ErrorCard(
                        message = error,
                        onRetry = { viewModel.refresh() },
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    theme: com.beekeeper.app.presentation.theme.AppTheme,
    onNavigateToProfile: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = theme.colors.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Movie,
                    contentDescription = "CineFiller",
                    tint = theme.colors.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "CineFiller",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
            }

            IconButton(onClick = onNavigateToProfile) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = theme.colors.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun WelcomeSection(
    stats: QuickStats?,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Welcome to CineFiller!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        Text(
            "Create amazing AI-powered content for all platforms",
            fontSize = 16.sp,
            color = theme.colors.textSecondary
        )

        stats?.let {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StatChip(
                    label = "Projects",
                    value = it.activeProjects.toString(),
                    icon = Icons.Default.Folder,
                    theme = theme
                )
                StatChip(
                    label = "Team",
                    value = it.teamMembers.toString(),
                    icon = Icons.Default.Group,
                    theme = theme
                )
                StatChip(
                    label = "Progress",
                    value = "${it.averageProgress.toInt()}%",
                    icon = Icons.Default.TrendingUp,
                    theme = theme
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    icon: ImageVector,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = theme.colors.surface.copy(alpha = 0.8f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, theme.colors.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = theme.colors.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "$value $label",
                fontSize = 14.sp,
                color = theme.colors.textPrimary
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onQuickAction: (HomeQuickAction) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(HomeQuickAction.values().toList()) { action ->
                QuickActionCard(
                    action = action,
                    onClick = { onQuickAction(action) },
                    theme = theme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionCard(
    action: HomeQuickAction,
    onClick: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        border = BorderStroke(1.dp, theme.colors.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                theme.colors.primary.copy(alpha = 0.2f),
                                theme.colors.primary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    action.icon,
                    contentDescription = null,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                action.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = theme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                action.description,
                fontSize = 12.sp,
                color = theme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ContentPipelineSection(
    stats: QuickStats,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, theme.colors.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Content Creation Pipeline",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            // Pipeline stages based on available stats
            val stages = listOf(
                PipelineStage("Projects", stats.totalProjects, Icons.Default.Folder),
                PipelineStage("Active", stats.activeProjects, Icons.Default.PlayArrow),
                PipelineStage("This Month", stats.completedThisMonth, Icons.Default.CheckCircle),
                PipelineStage("Team", stats.teamMembers, Icons.Default.Group)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stages.forEach { stage ->
                    PipelineStageCard(stage = stage, theme = theme)
                }
            }

            // Budget Progress
            if (stats.totalBudget > 0) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Budget Usage",
                            fontSize = 14.sp,
                            color = theme.colors.textSecondary
                        )
                        Text(
                            "${((stats.budgetSpent / stats.totalBudget) * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.colors.textPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = stats.budgetSpent / stats.totalBudget,
                        modifier = Modifier.fillMaxWidth(),
                        color = theme.colors.primary,
                        trackColor = theme.colors.surface
                    )
                }
            }
        }
    }
}

data class PipelineStage(
    val name: String,
    val count: Int,
    val icon: ImageVector
)

@Composable
private fun PipelineStageCard(
    stage: PipelineStage,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            stage.icon,
            contentDescription = null,
            tint = theme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            stage.count.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )
        Text(
            stage.name,
            fontSize = 12.sp,
            color = theme.colors.textSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveProjectsSection(
    projects: List<CreativeProject>,
    onProjectClick: (String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Active Projects",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        // Show up to 3 active projects
        projects.take(3).forEach { project ->
            Card(
                onClick = { onProjectClick(project.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, theme.colors.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            project.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.colors.textPrimary
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Project type chip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = theme.colors.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    project.type.name.replace("_", " "),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = theme.colors.primary
                                )
                            }

                            // Phase indicator
                            Text(
                                "Phase: ${project.currentPhase.name.replace("_", " ")}",
                                fontSize = 12.sp,
                                color = theme.colors.textSecondary
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Open project",
                        tint = theme.colors.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentContentSection(
    projects: List<CreativeProject>,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Recent Content",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        if (projects.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, theme.colors.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.MovieCreation,
                        contentDescription = null,
                        tint = theme.colors.textSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "No content yet",
                        fontSize = 16.sp,
                        color = theme.colors.textSecondary
                    )
                    Text(
                        "Start creating your first project!",
                        fontSize = 14.sp,
                        color = theme.colors.textSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Show content preview grid
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { index ->
                    ContentPreviewCard(
                        type = if (index % 2 == 0) "Frame" else "Scene",
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentPreviewCard(
    type: String,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.size(120.dp),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, theme.colors.outline.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    if (type == "Frame") Icons.Default.Image else Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = theme.colors.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    type,
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun GettingStartedSection(
    onTutorialClick: (String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Getting Started",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(3) { index ->
                val tutorials = listOf(
                    Triple("Welcome to CineFiller", "Get started with the basics", Icons.Default.PlayCircle),
                    Triple("Creating Your First Project", "Learn project creation workflow", Icons.Default.MovieCreation),
                    Triple("AI Avatar Generation", "Generate AI avatars for characters", Icons.Default.AutoAwesome)
                )

                GettingStartedCard(
                    title = tutorials[index].first,
                    description = tutorials[index].second,
                    icon = tutorials[index].third,
                    onClick = { onTutorialClick("tutorial_$index") },
                    theme = theme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GettingStartedCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(260.dp),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, theme.colors.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.colors.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Data model for content performance with details
data class ContentPerformanceItem(
    val id: String,
    val title: String,
    val revenue: Float,
    val views: Long,
    val engagement: Float,
    val platforms: List<String>,
    val duration: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentPerformanceSection(
    projects: List<CreativeProject>,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    var selectedContent by remember { mutableStateOf<ContentPerformanceItem?>(null) }
    var showDetailsSheet by remember { mutableStateOf(false) }

    // Generate performance data from projects using real analytics
    val performanceItems = remember(projects) {
        projects.take(5).map { project ->
            val analytics = project.analytics
            ContentPerformanceItem(
                id = project.id,
                title = project.title,
                revenue = analytics?.performanceMetrics?.revenueGenerated ?: 0f,
                views = analytics?.performanceMetrics?.totalViews ?: 0L,
                engagement = ((analytics?.performanceMetrics?.engagementRate ?: 0f) * 100),
                platforms = analytics?.platformPerformance?.keys?.toList() ?: listOf("YouTube"),
                duration = "N/A" // Can be calculated from deliverables if needed
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, theme.colors.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Content Performance by Title",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
            }

            if (performanceItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            tint = theme.colors.textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "No performance data yet",
                            fontSize = 16.sp,
                            color = theme.colors.textSecondary
                        )
                    }
                }
            } else {
                // Table Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Title",
                        modifier = Modifier.weight(2f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.colors.textSecondary
                    )
                    Text(
                        "Revenue",
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                    Text(
                        "Views",
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = theme.colors.textSecondary.copy(alpha = 0.2f)
                )

                // Table Rows
                performanceItems.forEach { item ->
                    ContentPerformanceRow(
                        item = item,
                        onClick = {
                            selectedContent = item
                            showDetailsSheet = true
                        },
                        theme = theme
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = theme.colors.textSecondary.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }

    // Details Bottom Sheet
    if (showDetailsSheet && selectedContent != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailsSheet = false },
            containerColor = theme.colors.surface
        ) {
            ContentPerformanceDetailsSheet(
                item = selectedContent!!,
                onDismiss = { showDetailsSheet = false },
                theme = theme
            )
        }
    }
}

@Composable
private fun ContentPerformanceRow(
    item: ContentPerformanceItem,
    onClick: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = theme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.duration,
                fontSize = 12.sp,
                color = theme.colors.textSecondary
            )
        }
        Text(
            "$${formatNumberWithCommas(item.revenue.toDouble())}",
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = theme.colors.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
        Text(
            "${formatNumberWithCommas((item.views.toDouble() / 1000))}K",
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = theme.colors.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun ContentPerformanceDetailsSheet(
    item: ContentPerformanceItem,
    onDismiss: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            item.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        // Metrics Overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$${formatNumberWithCommas(item.revenue.toDouble())}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
                Text(
                    "Total Revenue",
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${formatNumberWithCommas((item.views.toDouble() / 1000))}K",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
                Text(
                    "Total Views",
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${formatPercentage(item.engagement.toDouble(), 1)}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
                Text(
                    "Engagement",
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary
                )
            }
        }

        HorizontalDivider(color = theme.colors.textSecondary.copy(alpha = 0.2f))

        // Viewership Retention Chart
        Text(
            "Viewership Retention",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        SimpleLineChart(
            data = generateMockRetentionData(),
            theme = theme
        )

        HorizontalDivider(color = theme.colors.textSecondary.copy(alpha = 0.2f))

        // Platform Performance
        Text(
            "Platform Performance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        item.platforms.forEach { platform ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = theme.colors.background),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, theme.colors.outline.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when (platform) {
                                        "YouTube" -> Color(0xFFFF0000)
                                        "TikTok" -> Color(0xFF000000)
                                        "Instagram" -> Color(0xFFE4405F)
                                        else -> theme.colors.primary
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                platform.take(2),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            platform,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.colors.textPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$${formatNumberWithCommas((item.revenue / item.platforms.size).toDouble())}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.colors.textPrimary
                        )
                        Text(
                            "${formatNumberWithCommas((item.views.toDouble() / item.platforms.size / 1000))}K views",
                            fontSize = 12.sp,
                            color = theme.colors.textSecondary
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = theme.colors.textSecondary.copy(alpha = 0.2f))

        // User Reviews Section
        // TODO: Pass project through to get real reviews
        val reviews: List<UserReview> = remember(item) {
            generateMockUserReviews()
        }

        val averageRating = remember(reviews) {
            if (reviews.isEmpty()) 0f else reviews.map { it.rating }.average().toFloat()
        }

        val reviewCount = reviews.count()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "User Reviews",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            // Overall rating display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    formatPercentage(averageRating.toDouble(), 1),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
                Text(
                    "($reviewCount)",
                    fontSize = 14.sp,
                    color = theme.colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        reviews.forEach { review ->
            UserReviewCard(review = review, theme = theme)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ContentPerformanceCard(
    project: CreativeProject,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, theme.colors.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    project.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.colors.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        project.currentPhase.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = theme.colors.primary
                    )
                }
            }

            // Performance Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PerformanceMetric(
                    label = "Views",
                    value = "0", // TODO: Add real metrics
                    icon = Icons.Default.Visibility,
                    theme = theme
                )
                PerformanceMetric(
                    label = "Engagement",
                    value = "0%",
                    icon = Icons.Default.Favorite,
                    theme = theme
                )
                PerformanceMetric(
                    label = "Shares",
                    value = "0",
                    icon = Icons.Default.Share,
                    theme = theme
                )
            }
        }
    }
}

@Composable
private fun PerformanceMetric(
    label: String,
    value: String,
    icon: ImageVector,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = theme.colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )
        Text(
            label,
            fontSize = 12.sp,
            color = theme.colors.textSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewAllProjectsCard(
    onViewAll: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        onClick = onViewAll,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.primary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = theme.colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "View All Projects",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = theme.colors.primary
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Error",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// Line chart data model
data class LineChartPoint(
    val x: Float,
    val y: Float
)

@Composable
private fun SimpleLineChart(
    data: List<LineChartPoint>,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 8.dp)
    ) {
        if (data.isEmpty() || data.size < 2) return@Canvas

        val maxY = data.maxOfOrNull { it.y } ?: 100f
        val minY = data.minOfOrNull { it.y } ?: 0f
        val xStep = size.width / (data.size - 1)

        val path = Path()
        data.forEachIndexed { index, point ->
            val x = index * xStep
            val y = size.height - ((point.y - minY) / (maxY - minY) * size.height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw point
            drawCircle(
                color = Color(0xFF8B5CF6),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = Color(0xFF8B5CF6),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun generateMockRetentionData(): List<LineChartPoint> {
    return listOf(
        LineChartPoint(0f, 55f),
        LineChartPoint(1f, 60f),
        LineChartPoint(2f, 65f),
        LineChartPoint(3f, 68f),
        LineChartPoint(4f, 72f),
        LineChartPoint(5f, 78f),
        LineChartPoint(6f, 85f),
        LineChartPoint(7f, 88f),
        LineChartPoint(8f, 95f),
        LineChartPoint(9f, 100f)
    )
}

private fun generateMockUserReviews(): List<UserReview> {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    return listOf(
        UserReview(
            id = "review_1",
            userName = "Sarah Mitchell",
            rating = 5f,
            comment = "Absolutely incredible! The storytelling and visuals are top-notch. Can't wait for the next episode!",
            timestamp = currentTime - 2.days.inWholeMilliseconds,
            platform = "YouTube",
            isVerifiedPurchase = true
        ),
        UserReview(
            id = "review_2",
            userName = "Alex Chen",
            rating = 4.5f,
            comment = "Great production quality. The character development is impressive and keeps me engaged throughout.",
            timestamp = currentTime - 5.days.inWholeMilliseconds,
            platform = "Amazon Prime",
            isVerifiedPurchase = true
        ),
        UserReview(
            id = "review_3",
            userName = "Jordan Banks",
            rating = 4f,
            comment = "Really enjoying this series. The plot twists are well executed and the pacing is perfect.",
            timestamp = currentTime - 7.days.inWholeMilliseconds,
            platform = "YouTube",
            isVerifiedPurchase = false
        )
    )
}

@Composable
private fun UserReviewCard(
    review: UserReview,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, theme.colors.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with name and rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(theme.colors.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            review.userName.first().toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.colors.primary
                        )
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                review.userName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.colors.textPrimary
                            )
                            if (review.isVerifiedPurchase) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        review.platform?.let { platform ->
                            Text(
                                "via $platform",
                                fontSize = 11.sp,
                                color = theme.colors.textSecondary
                            )
                        }
                    }
                }

                // Star rating
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(5) { index ->
                        Icon(
                            if (index < review.rating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Review comment
            Text(
                review.comment,
                fontSize = 13.sp,
                color = theme.colors.textPrimary,
                lineHeight = 18.sp
            )

            // Timestamp
            Text(
                formatTimestamp(review.timestamp),
                fontSize = 11.sp,
                color = theme.colors.textSecondary
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = now - timestamp
    val days = diff / (24 * 60 * 60 * 1000)

    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        days < 30 -> "${days / 7} weeks ago"
        else -> "${days / 30} months ago"
    }
}