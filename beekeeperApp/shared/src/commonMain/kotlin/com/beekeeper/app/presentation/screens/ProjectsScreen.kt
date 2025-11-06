// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/ProjectsScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.PrimaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager
import com.beekeeper.app.presentation.viewmodels.ProjectsViewModel
import com.beekeeper.app.presentation.viewmodels.rememberProjectsViewModel
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

// Project Tab enum
enum class ProjectTab(val title: String) {
    ALL("All"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel = rememberProjectsViewModel(),
    onNavigateToProject: (String) -> Unit,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToEditProject: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var selectedProjectForAction by remember { mutableStateOf<CreativeProject?>(null) }

    Scaffold(
        topBar = {
            PrimaryTopBar(
                title = "Projects",
                onNotificationClick = onNavigateToNotifications,
                actions = {
                    IconButton(onClick = onNavigateToCreateProject) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Project",
                            tint = theme.colors.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateProject,
                containerColor = theme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Project")
            }
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(theme.colors.background)
        ) {
            // Tab Bar
            ProjectTabBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) },
                theme = theme
            )

            // Search Bar
            ProjectSearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                theme = theme
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = theme.colors.primary)
                    }
                }

                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error ?: "An error occurred",
                        onRetry = { viewModel.refresh() },
                        theme = theme
                    )
                }

                uiState.filteredProjects.isEmpty() -> {
                    EmptyProjectsMessage(
                        tab = uiState.selectedTab,
                        hasSearch = uiState.searchQuery.isNotBlank(),
                        theme = theme
                    )
                }

                else -> {
                    // Projects List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.filteredProjects) { project ->
                            ProjectCard(
                                project = project,
                                onClick = { onNavigateToProject(project.id) },
                                onLongPress = {
                                    selectedProjectForAction = project
                                },
                                onDeleteSelected = {
                                    selectedProjectForAction = project
                                    showDeleteDialog = true
                                },
                                onArchiveSelected = {
                                    selectedProjectForAction = project
                                    showArchiveDialog = true
                                },
                                onEditSelected = {
                                    onNavigateToEditProject(project.id)
                                },
                                theme = theme
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showDeleteDialog && selectedProjectForAction != null) {
        ProjectDeleteConfirmationDialog(
            project = selectedProjectForAction!!,
            onConfirm = {
                viewModel.deleteProject(selectedProjectForAction!!.id)
                showDeleteDialog = false
                selectedProjectForAction = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedProjectForAction = null
            },
            theme = theme
        )
    }

    if (showArchiveDialog && selectedProjectForAction != null) {
        ProjectArchiveConfirmationDialog(
            project = selectedProjectForAction!!,
            onConfirm = {
                viewModel.archiveProject(selectedProjectForAction!!.id)
                showArchiveDialog = false
                selectedProjectForAction = null
            },
            onDismiss = {
                showArchiveDialog = false
                selectedProjectForAction = null
            },
            theme = theme
        )
    }
}

@Composable
private fun ProjectSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search projects...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = theme.colors.primary,
            unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.5f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectCard(
    project: CreativeProject,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDeleteSelected: () -> Unit,
    onArchiveSelected: () -> Unit,
    onEditSelected: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    var showActionMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongPress()
                    showActionMenu = true
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProjectTypeChip(project.type, theme)
                        ProjectStatusChip(project.status, theme)
                        if (project.priority == ProjectPriority.HIGH || project.priority == ProjectPriority.CRITICAL) {
                            ProjectPriorityChip(project.priority, theme)
                        }
                    }
                }

                // Right side controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Edit Button
                    IconButton(
                        onClick = {
                            onEditSelected()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.04f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp),
                            tint = theme.colors.textPrimary
                        )
                    }

                    // Progress Indicator
                    Box(
                        modifier = Modifier.size(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val progress = calculateProjectProgress(project)
                        CircularProgressIndicator(
                            progress = progress / 100f,
                            modifier = Modifier.fillMaxSize(),
                            color = theme.colors.primary,
                            trackColor = theme.colors.primary.copy(alpha = 0.2f),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "${progress.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = project.description,
                fontSize = 14.sp,
                color = theme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current Phase
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timeline,
                        contentDescription = "Phase",
                        modifier = Modifier.size(16.dp),
                        tint = theme.colors.textSecondary
                    )
                    Text(
                        text = project.currentPhase.name.replace("_", " "),
                        fontSize = 12.sp,
                        color = theme.colors.textSecondary
                    )
                }

                // Team Size
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Team",
                        modifier = Modifier.size(16.dp),
                        tint = theme.colors.textSecondary
                    )
                    val teamSize = (project.team?.writers?.size ?: 0) +
                                   (project.team?.designers?.size ?: 0) +
                                   (project.team?.qaTeam?.size ?: 0) +
                                   (project.team?.aiSpecialists?.size ?: 0) +
                                   (if (project.team?.projectManager != null) 1 else 0) +
                                   (if (project.team?.creativeDirector != null) 1 else 0) +
                                   (if (project.team?.technicalLead != null) 1 else 0)
                    Text(
                        text = "$teamSize members",
                        fontSize = 12.sp,
                        color = theme.colors.textSecondary
                    )
                }

                // Last Updated
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Update,
                        contentDescription = "Updated",
                        modifier = Modifier.size(16.dp),
                        tint = theme.colors.textSecondary
                    )
                    Text(
                        text = getRelativeTimeString(project.updatedAt),
                        fontSize = 12.sp,
                        color = theme.colors.textSecondary
                    )
                }
            }
        }
    }

    // Action Menu (DropdownMenu style - shown on long press)
    if (showActionMenu) {
        AlertDialog(
            onDismissRequest = { showActionMenu = false },
            title = { Text("Project Actions", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit Option
                    Surface(
                        onClick = {
                            onEditSelected()
                            showActionMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                            Text("Edit", fontSize = 16.sp)
                        }
                    }

                    // Archive Option
                    Surface(
                        onClick = {
                            onArchiveSelected()
                            showActionMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive")
                            Text("Archive", fontSize = 16.sp)
                        }
                    }

                    // Delete Option
                    Surface(
                        onClick = {
                            onDeleteSelected()
                            showActionMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Red.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red
                            )
                            Text("Delete", fontSize = 16.sp, color = Color.Red)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActionMenu = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProjectTypeChip(
    type: ProjectType,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (type) {
            ProjectType.FEATURE_FILM -> Color(0xFF6C63FF).copy(alpha = 0.2f)
            ProjectType.SHORT_FILM -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            ProjectType.DOCUMENTARY -> Color(0xFF2196F3).copy(alpha = 0.2f)
            ProjectType.COMMERCIAL -> Color(0xFFE91E63).copy(alpha = 0.2f)
            ProjectType.MUSIC_VIDEO -> Color(0xFF9C27B0).copy(alpha = 0.2f)
            ProjectType.ANIMATION -> Color(0xFF00BCD4).copy(alpha = 0.2f)
            ProjectType.EXPERIMENTAL -> Color(0xFF795548).copy(alpha = 0.2f)
            ProjectType.SOCIAL_MEDIA_CONTENT -> Color(0xFF00E676).copy(alpha = 0.2f)
            ProjectType.MARKETING_CAMPAIGN -> Color(0xFFFF6D00).copy(alpha = 0.2f)
            ProjectType.MINI_SERIES -> Color(0xFF651FFF).copy(alpha = 0.2f)
            ProjectType.WEB_SERIES -> Color(0xFF00B8D4).copy(alpha = 0.2f)
            ProjectType.TV_EPISODE -> Color(0xFF304FFE).copy(alpha = 0.2f)
            ProjectType.TV_SERIES -> Color(0xFF6200EA).copy(alpha = 0.2f)
            ProjectType.FEATURE_LENGTH -> Color(0xFFAA00FF).copy(alpha = 0.2f)
            else -> Color.Gray.copy(alpha = 0.2f)
        }
    ) {
        Text(
            text = type.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = theme.colors.textPrimary
        )
    }
}

@Composable
private fun ProjectStatusChip(
    status: ProjectStatus,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (status) {
            ProjectStatus.DRAFT -> Color.Gray.copy(alpha = 0.2f)
            ProjectStatus.IN_DEVELOPMENT -> Color.Blue.copy(alpha = 0.2f)
            ProjectStatus.PRE_PRODUCTION -> Color.Cyan.copy(alpha = 0.2f)
            ProjectStatus.IN_PRODUCTION -> Color.Green.copy(alpha = 0.2f)
            ProjectStatus.POST_PRODUCTION -> Color.Yellow.copy(alpha = 0.2f)
            ProjectStatus.READY_FOR_DISTRIBUTION -> Color.Magenta.copy(alpha = 0.2f)
            ProjectStatus.COMPLETED -> Color.Green.copy(alpha = 0.3f)
            ProjectStatus.ON_HOLD -> Color.Red.copy(alpha = 0.2f)
            ProjectStatus.CANCELLED -> Color.Red.copy(alpha = 0.3f)
            ProjectStatus.PUBLISHED -> Color(0xFF9C27B0).copy(alpha = 0.2f)
            ProjectStatus.ARCHIVED -> Color(0xFF757575).copy(alpha = 0.2f)
            ProjectStatus.PLANNING -> Color(0xFF00BCD4).copy(alpha = 0.2f)
            ProjectStatus.IN_REVIEW -> Color(0xFFFFC107).copy(alpha = 0.2f)
            ProjectStatus.DELIVERED -> Color(0xFF8BC34A).copy(alpha = 0.2f)
            else -> Color.Gray.copy(alpha = 0.2f)
        }
    ) {
        Text(
            text = status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = theme.colors.textPrimary
        )
    }
}

@Composable
private fun ProjectPriorityChip(
    priority: ProjectPriority,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (priority) {
            ProjectPriority.CRITICAL -> Color.Red.copy(alpha = 0.3f)
            ProjectPriority.HIGH -> Color(0xFFFF9800).copy(alpha = 0.3f)
            else -> Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PriorityHigh,
                contentDescription = "Priority",
                modifier = Modifier.size(12.dp),
                tint = if (priority == ProjectPriority.CRITICAL) Color.Red else Color(0xFFFF9800)
            )
            Text(
                text = priority.name,
                fontSize = 11.sp,
                color = theme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Helper functions
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

private fun getRelativeTimeString(instant: kotlinx.datetime.Instant?): String {
    if (instant == null) return "Unknown"
    val now = Clock.System.now()
    val duration = now - instant

    return when {
        duration < 1.days -> "Today"
        duration < 2.days -> "Yesterday"
        duration < 7.days -> "${duration.inWholeDays} days ago"
        duration < 30.days -> "${duration.inWholeDays / 7} weeks ago"
        else -> "${duration.inWholeDays / 30} months ago"
    }
}

@Composable
private fun ProjectTabBar(
    selectedTab: ProjectTab,
    onTabSelected: (ProjectTab) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    TabRow(
        selectedTabIndex = ProjectTab.values().indexOf(selectedTab),
        containerColor = theme.colors.surface,
        contentColor = theme.colors.textPrimary,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[ProjectTab.values().indexOf(selectedTab)]),
                color = theme.colors.primary
            )
        }
    ) {
        ProjectTab.values().forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        tab.title,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun EmptyProjectsMessage(
    tab: ProjectTab,
    hasSearch: Boolean,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = theme.colors.textSecondary.copy(alpha = 0.5f)
            )
            Text(
                text = when {
                    hasSearch -> "No projects match your search"
                    tab == ProjectTab.COMPLETED -> "No completed projects yet"
                    tab == ProjectTab.IN_PROGRESS -> "No projects in progress"
                    else -> "No projects yet"
                },
                color = theme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Red.copy(alpha = 0.7f)
            )
            Text(
                text = message,
                color = theme.colors.textSecondary
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun ProjectDeleteConfirmationDialog(
    project: CreativeProject,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Delete Project",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Are you sure you want to permanently delete this project?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "\"${project.title}\"",
                    fontWeight = FontWeight.Medium,
                    color = theme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This action cannot be undone. All project data, including stories, characters, and generated content will be permanently lost.",
                    color = Color.Red.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ProjectArchiveConfirmationDialog(
    project: CreativeProject,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Archive Project",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Archive this project to remove it from active projects?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "\"${project.title}\"",
                    fontWeight = FontWeight.Medium,
                    color = theme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Archived projects can be restored later from the archived projects section.",
                    color = theme.colors.textSecondary,
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA726)
                )
            ) {
                Icon(
                    Icons.Default.Archive,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Archive")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}