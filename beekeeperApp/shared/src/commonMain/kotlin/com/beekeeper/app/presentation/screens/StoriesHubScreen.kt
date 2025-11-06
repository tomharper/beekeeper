// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/StoriesHubScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.rememberAppTheme
import com.beekeeper.app.presentation.viewmodels.StoryHubViewModel
import com.beekeeper.app.presentation.viewmodels.StoryHubUiState
import com.beekeeper.app.presentation.viewmodels.rememberStoryHubViewModel
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import com.beekeeper.app.domain.model.*
import kotlinx.datetime.*

/**
 * KMP-friendly date formatting functions
 */
fun formatRelativeTime(timestamp: Long): String {
    val now = Clock.System.now()
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val duration = now - instant

    return when {
        duration < 1.minutes -> "Just now"
        duration < 1.hours -> "${duration.inWholeMinutes} minutes ago"
        duration < 1.days -> "${duration.inWholeHours} hours ago"
        duration < 2.days -> "Yesterday"
        duration < 7.days -> "${duration.inWholeDays} days ago"
        duration < 30.days -> "${duration.inWholeDays / 7} weeks ago"
        duration < 365.days -> "${duration.inWholeDays / 30} months ago"
        else -> "${duration.inWholeDays / 365} years ago"
    }
}

fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    val month = when (dateTime.month) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Apr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Aug"
        Month.SEPTEMBER -> "Sep"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dec"
        else -> ""
    }

    val hour = if (dateTime.hour == 0) 12 else if (dateTime.hour > 12) dateTime.hour - 12 else dateTime.hour
    val amPm = if (dateTime.hour < 12) "AM" else "PM"
    val minute = dateTime.minute.toString().padStart(2, '0')

    return "$month ${dateTime.dayOfMonth}, ${dateTime.year} at $hour:$minute $amPm"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesHubScreen(
    projectId: String,
    viewModel: StoryHubViewModel = rememberStoryHubViewModel(projectId),
    onNavigateBack: () -> Unit,
    onStoryboardClick: (String) -> Unit,
    onCharacterBoardClick: (String) -> Unit
) {
    val theme = rememberAppTheme()
    val uiState by viewModel.uiState.collectAsState()
    val stories by remember { derivedStateOf { uiState.stories } }
    val storyboards by viewModel.storyboards.collectAsState()
    val selectedStory by viewModel.selectedStory.collectAsState()
    val project by viewModel.project.collectAsState()

    var selectedFilter by remember { mutableStateOf<ContentStatus?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showStoryDetails by remember { mutableStateOf(false) }


    viewModel.filterStoriesByStatus(null)
    // Debug info
    println("StoriesHubScreen - projectId: $projectId")
    println("StoriesHubScreen - stories count: ${stories.size}")
    println("StoriesHubScreen - isLoading: ${uiState.isLoading}")

    // Error and success message handling
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Show error snackbar or toast
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.showSuccessMessage) {
        uiState.showSuccessMessage?.let {
            // Show success snackbar or toast
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        containerColor = theme.colors.background,
        topBar = {
            SecondaryTopBar(
                title = project?.title ?: "Stories Hub",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = theme.colors.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Story",
                    tint = theme.colors.onPrimary
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = theme.colors.primary)
                }
            } else {
                // Stories Overview
                StoriesOverviewSection(
                    storyCount = stories.size,
                    storyboardCount = uiState.storyboardCount,
                    totalScenes = uiState.totalScenes,
                    characterBoardCount = uiState.characterBoardCount,
                    theme = theme
                )

                // Stories Grid
                if (stories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = theme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No stories yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = theme.colors.textSecondary
                            )
                            Text(
                                "Create your first story to get started",
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.colors.textSecondary
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(300.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = stories,
                            key = { it.id }
                        ) { story ->
                            StoryCard(
                                story = story,
                                onClick = {
                                    // navigate to storyboard list by storyId
                                    val storyboard = storyboards.find { it.storyId == story.id }
                                    if (storyboard != null) {
                                        onStoryboardClick(story.id)
                                    }
                                },
                                onGenerateScript = {
                                    viewModel.generateScriptFromStory(story.id)
                                },
                                onExtractCharacters = {
                                    viewModel.extractCharactersFromStory(story.id)
                                },
                                theme = theme
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Story Dialog
    if (showCreateDialog) {
        CreateStoryDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, synopsis, genre ->
                viewModel.createStory(title, synopsis, genre)
                showCreateDialog = false
            },
            theme = theme
        )
    }

    // Story Details Dialog
    selectedStory?.let { story ->
        if (showStoryDetails) {
            StoryDetailsDialog(
                story = story,
                onDismiss = { showStoryDetails = false },
                onEdit = { /* TODO: Implement edit */ },
                onDelete = { /* TODO: Implement delete */ },
                theme = theme
            )
        }
    }
}

@Composable
fun StoriesOverviewSection(
    storyCount: Int,
    storyboardCount: Int,
    totalScenes: Int,
    characterBoardCount: Int,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OverviewStat(
                icon = Icons.Default.Description,
                value = storyCount.toString(),
                label = "Stories",
                theme = theme
            )
            OverviewStat(
                icon = Icons.Default.ViewCarousel,
                value = storyboardCount.toString(),
                label = "Storyboards",
                theme = theme
            )
            OverviewStat(
                icon = Icons.Default.Movie,
                value = totalScenes.toString(),
                label = "Total Scenes",
                theme = theme
            )
            OverviewStat(
                icon = Icons.Default.People,
                value = characterBoardCount.toString(),
                label = "Characters",
                theme = theme
            )
        }
    }
}

@Composable
fun OverviewStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = theme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = theme.colors.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = theme.colors.textSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryCard(
    story: Story,
    onClick: () -> Unit,
    onGenerateScript: () -> Unit,
    onExtractCharacters: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        story.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.onSurface,
                        maxLines = 1
                    )
                    Text(
                        story.genre,
                        style = MaterialTheme.typography.labelMedium,
                        color = theme.colors.primary
                    )
                }

                // Status Chip
                val statusColor = when (story.status) {
                    ContentStatus.DRAFT, ContentStatus.FINAL_DRAFT -> Color(0xFF9E9E9E)
                    ContentStatus.IN_PROGRESS -> Color(0xFFFF9800)
                    ContentStatus.APPROVED -> Color(0xFF4CAF50)
                    ContentStatus.IN_REVIEW -> Color(0xFF2196F3)
                    ContentStatus.IN_PRODUCTION -> Color(0xFFE91E63)
                    ContentStatus.ARCHIVED -> Color(0xFF607D8B)
                    ContentStatus.COMPLETED -> Color(0xFF607D8B)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        story.status.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Synopsis
            Text(
                story.synopsis,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.colors.textSecondary,
                maxLines = 3,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Last modified
            val lastModified = formatRelativeTime(story.updatedAt)

            Text(
                "Last modified: $lastModified",
                style = MaterialTheme.typography.labelSmall,
                color = theme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGenerateScript,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = theme.colors.primary
                    ),
                    border = BorderStroke(1.dp, theme.colors.primary)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Script", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onExtractCharacters,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = theme.colors.primary
                    ),
                    border = BorderStroke(1.dp, theme.colors.primary)
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Characters", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, synopsis: String, genre: String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    var storyTitle by remember { mutableStateOf("") }
    var storySynopsis by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("Action") }

    val genres = listOf("Action", "Comedy", "Drama", "Horror", "Sci-Fi", "Romance", "Thriller", "Fantasy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create New Story",
                style = MaterialTheme.typography.headlineSmall,
                color = theme.colors.onSurface
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = storyTitle,
                    onValueChange = { storyTitle = it },
                    label = { Text("Story Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary,
                        unfocusedBorderColor = theme.colors.textSecondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = storySynopsis,
                    onValueChange = { storySynopsis = it },
                    label = { Text("Synopsis") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary,
                        unfocusedBorderColor = theme.colors.textSecondary
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Genre",
                    style = MaterialTheme.typography.labelLarge,
                    color = theme.colors.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Genre Selection
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genres) { genre ->
                        FilterChip(
                            selected = selectedGenre == genre,
                            onClick = { selectedGenre = genre },
                            label = { Text(genre) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.colors.primary,
                                selectedLabelColor = theme.colors.onPrimary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(storyTitle, storySynopsis, selectedGenre) },
                enabled = storyTitle.isNotBlank() && storySynopsis.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary,
                    contentColor = theme.colors.onPrimary
                )
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = theme.colors.primary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailsDialog(
    story: Story,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    story.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = theme.colors.onSurface
                )

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = theme.colors.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE57373)
                        )
                    }
                }
            }
        },
        text = {
            Column {
                // Metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            "Genre",
                            style = MaterialTheme.typography.labelMedium,
                            color = theme.colors.textSecondary
                        )
                        Text(
                            story.genre,
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.colors.onSurface
                        )
                    }

                    Column {
                        Text(
                            "Status",
                            style = MaterialTheme.typography.labelMedium,
                            color = theme.colors.textSecondary
                        )
                        Text(
                            story.status.name.replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.colors.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Synopsis
                Text(
                    "Synopsis",
                    style = MaterialTheme.typography.labelLarge,
                    color = theme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    story.synopsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Timestamps
                val createdDate = formatDate(story.createdAt)
                val updatedDate = formatDate(story.updatedAt)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Created",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.colors.textSecondary
                        )
                        Text(
                            createdDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.colors.textSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Last Modified",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.colors.textSecondary
                        )
                        Text(
                            updatedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.colors.textSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary,
                    contentColor = theme.colors.onPrimary
                )
            ) {
                Text("Close")
            }
        }
    )
}

