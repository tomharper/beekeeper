// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/StoryboardListScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.CineFillerImage
import com.beekeeper.app.presentation.components.ContentAwareImage
import com.beekeeper.app.presentation.components.ContentState
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.rememberAppTheme
import com.beekeeper.app.presentation.viewmodels.StoryboardListViewModel
import kotlinx.coroutines.launch

@Composable
fun StoryboardListScreen(
    projectId: String,
    storyId: String,
    onNavigateBack: () -> Unit,
    onStoryboardClick: (storyboardId: String) -> Unit,
    onCreateStoryboard: () -> Unit = {}
) {
    val theme = rememberAppTheme()
    val viewModel: StoryboardListViewModel = viewModel {
        StoryboardListViewModel(projectId, storyId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val story by viewModel.story.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedStoryboard by remember { mutableStateOf<Storyboard?>(null) }
    var showStoryboardActions by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = theme.colors.background,
        topBar = {
            SecondaryTopBar(
                title = story?.title?.let { "$it - Storyboards" } ?: "Storyboards",
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
                    contentDescription = "Create Storyboard",
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
            } else if (uiState.error != null) {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = theme.colors.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            uiState.error ?: "An error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            color = theme.colors.error
                        )
                    }
                }
            } else {
                // Storyboards Overview
                StoryboardsOverviewSection(
                    storyboardCount = uiState.storyboards.size,
                    totalScenes = uiState.totalScenes,
                    totalDuration = uiState.totalDuration,
                    completionPercentage = uiState.averageCompletion
                )

                // Storyboards Grid
                if (uiState.storyboards.isEmpty()) {
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
                                Icons.Default.ViewCarousel,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = theme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No storyboards yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = theme.colors.textSecondary
                            )
                            Text(
                                "Create your first storyboard to visualize the story",
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
                            items = uiState.storyboards,
                            key = { it.id }
                        ) { storyboard ->
                            StoryboardCard(
                                storyboard = storyboard,
                                onClick = {
                                    onStoryboardClick(storyboard.id)
                                },
                                onShowActions = {
                                    selectedStoryboard = storyboard
                                    showStoryboardActions = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Storyboard Dialog
    if (showCreateDialog) {
        CreateStoryboardDialog(
            storyTitle = story?.title ?: "Story",
            onDismiss = { showCreateDialog = false },
            onCreate = { title, description, type ->
                viewModel.createStoryboard(title, description, type, story?.scriptId!!)
                showCreateDialog = false
            }
        )
    }

    // Storyboard Actions Dialog
    if (showStoryboardActions && selectedStoryboard != null) {
        StoryboardActionsDialog(
            storyboard = selectedStoryboard!!,
            onDismiss = {
                showStoryboardActions = false
                selectedStoryboard = null
            },
            onDelete = {
                viewModel.deleteStoryboard(selectedStoryboard!!.id)
                showStoryboardActions = false
                selectedStoryboard = null
            },
            onDuplicate = {
                viewModel.duplicateStoryboard(selectedStoryboard!!.id)
                showStoryboardActions = false
                selectedStoryboard = null
            }
        )
    }
}

@Composable
fun StoryboardsOverviewSection(
    storyboardCount: Int,
    totalScenes: Int,
    totalDuration: Int,
    completionPercentage: Int
) {
    val theme = rememberAppTheme()
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
                icon = Icons.Default.ViewCarousel,
                value = storyboardCount.toString(),
                label = "Storyboards"
            )
            OverviewStat(
                icon = Icons.Default.Movie,
                value = totalScenes.toString(),
                label = "Total Scenes"
            )
            OverviewStat(
                icon = Icons.Default.Timer,
                value = "${totalDuration / 60}m",
                label = "Duration"
            )
            OverviewStat(
                icon = Icons.Default.CheckCircle,
                value = "$completionPercentage%",
                label = "Complete"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryboardCard(
    storyboard: Storyboard,
    onClick: () -> Unit,
    onShowActions: () -> Unit,
    onGenerateThumbnail: (() -> Unit)? = null // TDODO FIMXME!!!
) {
    // Determine content state for the storyboard thumbnail
    val contentState = when {
        !storyboard.thumbnailUrl.isNullOrEmpty() -> ContentState.READY
        storyboard.scenes.any { scene ->
            !scene.imageUrl.isNullOrEmpty() || scene.frames.any { !it.imageUrl.isNullOrEmpty() }
        } -> ContentState.PLACEHOLDER // Has some content but no thumbnail
        else -> ContentState.NOT_CREATED
    }


    val theme = rememberAppTheme()
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Thumbnail or placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                StoryboardThumbnail(
                    storyboard = storyboard,
                    contentState = contentState,
                    onGenerateThumbnail = onGenerateThumbnail
                )

                // Type badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = theme.colors.primary.copy(alpha = 0.9f)
                ) {
                    Text(
                        storyboard.storyboardType.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.colors.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // Lock icon if locked
                if (storyboard.isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(20.dp),
                        tint = theme.colors.onSurface
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            storyboard.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = theme.colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        storyboard.description?.let { desc ->
                            Text(
                                desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.colors.textSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onShowActions,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = theme.colors.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Scene count
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Movie,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = theme.colors.textSecondary
                            )
                            Text(
                                "${storyboard.sceneCount} scenes",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.colors.textSecondary
                            )
                        }

                        // Duration
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = theme.colors.textSecondary
                            )
                            Text(
                                "${storyboard.duration / 60}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.colors.textSecondary
                            )
                        }
                    }

                    // Completion
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = storyboard.completionPercentage / 100f,
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp),
                            color = theme.colors.primary,
                            trackColor = theme.colors.surface
                        )
                        Text(
                            "${storyboard.completionPercentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun StoryboardThumbnail(
    storyboard: Storyboard,
    contentState: ContentState,
    onGenerateThumbnail: (() -> Unit)?
) {
    when {
        // Has a thumbnail URL - use it
        !storyboard.thumbnailUrl.isNullOrEmpty() -> {
            ContentAwareImage(
                imageUrl = storyboard.thumbnailUrl,
                contentState = ContentState.READY,
                contentType = ContentType.THUMBNAIL,
                modifier = Modifier.fillMaxSize(),
                contentDescription = storyboard.title,
                onGenerateClick = null // Already has image
            )
        }

        // No thumbnail but has scene images - create a grid
        contentState == ContentState.PLACEHOLDER -> {
            StoryboardImageGrid(storyboard)
        }

        // No images at all - show generation prompt
        else -> {
            ContentAwareImage(
                imageUrl = null,
                contentState = ContentState.NOT_CREATED,
                contentType = ContentType.THUMBNAIL,
                modifier = Modifier.fillMaxSize(),
                contentDescription = storyboard.title,
                onGenerateClick = onGenerateThumbnail
            )
        }
    }
}

//TODO- IMAGE GENERATION FIXME

// Update the StoryboardListViewModel to handle thumbnail generation
fun StoryboardListViewModel.generateStoryboardThumbnail(storyboardId: String) {
    viewModelScope.launch {
        // Find the storyboard
        val storyboard = _uiState.value.storyboards.find { it.id == storyboardId } ?: return@launch

        // If it has a thumbnail already, skip
        if (!storyboard.thumbnailUrl.isNullOrEmpty()) return@launch

        // Generate thumbnail from first available image or create composite
        val thumbnailUrl = generateThumbnailForStoryboard(storyboard)

        // Update the storyboard with the thumbnail
        //thumbnailUrl?.let { url ->
        //    updateStoryboardThumbnail(storyboardId, url)
        //}
    }
}

private suspend fun generateThumbnailForStoryboard(storyboard: Storyboard): String? {
    // This would call your AI service to generate a composite thumbnail
    // For now, return first available image
    for (scene in storyboard.scenes) {
        scene.imageUrl?.let { return it }
        scene.frames.firstOrNull()?.imageUrl?.let { return it }
    }
    return null
}

@Composable
private fun StoryboardImageGrid(storyboard: Storyboard) {
    // Collect first few images from scenes/frames
    val images = remember(storyboard) {
        val imageList = mutableListOf<String>()

        for (scene in storyboard.scenes) {
            // Add scene image if available
            scene.imageUrl?.let { imageList.add(it) }

            // Add frame images
            for (frame in scene.frames) {
                frame.imageUrl?.let { imageList.add(it) }
                if (imageList.size >= 4) break
            }
            if (imageList.size >= 4) break
        }

        imageList.take(4)
    }

    when (images.size) {
        0 -> {
            // No images - show placeholder
            ContentAwareImage(
                imageUrl = null,
                contentState = ContentState.NOT_CREATED,
                contentType = ContentType.THUMBNAIL,
                modifier = Modifier.fillMaxSize()
            )
        }
        1 -> {
            // Single image - show full
            CineFillerImage(
                url = images[0],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            // Multiple images - show grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1f)) {
                        images.take(2).forEach { imageUrl ->
                            CineFillerImage(
                                url = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(0.5.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    if (images.size > 2) {
                        Column(modifier = Modifier.weight(1f)) {
                            images.drop(2).take(2).forEach { imageUrl ->
                                CineFillerImage(
                                    url = imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(0.5.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // Overlay with count if more than 4
                if (storyboard.scenes.sumOf { it.frames.size } > 4) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "+${storyboard.scenes.sumOf { it.frames.size } - 4}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private fun getTypeColor(type: StoryboardType): Color {
    return when (type) {
        StoryboardType.FULL -> Color(0xFF4CAF50)
        StoryboardType.SEQUENCE -> Color(0xFF2196F3)
        StoryboardType.CHARACTER_FOCUSED -> Color(0xFF9C27B0)
        StoryboardType.ALTERNATE -> Color(0xFFFF9800)
        StoryboardType.ROUGH -> Color(0xFF607D8B)
        StoryboardType.FINAL -> Color(0xFF4CAF50)
        StoryboardType.VFX -> Color(0xFFE91E63)
        StoryboardType.STUNT -> Color(0xFFFF5252)
        StoryboardType.MARKETING -> Color(0xFF00BCD4)
        StoryboardType.ANIMATED -> Color(0xFF8BC34A)
        StoryboardType.CONCEPT -> Color(0xFF795548)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryboardDialog(
    storyTitle: String,
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, type: StoryboardType) -> Unit
) {
    val theme = rememberAppTheme()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(StoryboardType.FULL) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Storyboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "For: $storyTitle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.colors.textSecondary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Storyboard Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        StoryboardType.values().forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(type.name.replace("_", " "))
                                        Text(
                                            getTypeDescription(type),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = theme.colors.textSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, description, selectedType)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = theme.colors.surface,
        tonalElevation = 0.dp
    )
}

@Composable
fun StoryboardActionsDialog(
    storyboard: Storyboard,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    val theme = rememberAppTheme()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Storyboard Actions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    storyboard.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.colors.onSurface
                )
                Divider()

                TextButton(
                    onClick = {
                        onDuplicate()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Duplicate Storyboard")
                }

                if (!storyboard.isLocked) {
                    TextButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = theme.colors.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Storyboard")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = theme.colors.surface,
        tonalElevation = 0.dp
    )
}

@Composable
fun OverviewStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    val theme = rememberAppTheme()
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

private fun getTypeDescription(type: StoryboardType): String {
    return when (type) {
        StoryboardType.FULL -> "Complete episode storyboard"
        StoryboardType.SEQUENCE -> "Specific sequence (action, emotional, etc.)"
        StoryboardType.CHARACTER_FOCUSED -> "Character-specific scenes"
        StoryboardType.ALTERNATE -> "Alternative version"
        StoryboardType.ROUGH -> "Initial rough boards"
        StoryboardType.FINAL -> "Locked/approved version"
        StoryboardType.VFX -> "VFX"
        StoryboardType.STUNT -> "STUNT"
        StoryboardType.MARKETING -> "MARKETING"
        StoryboardType.ANIMATED -> "ANIMATED"
        StoryboardType.CONCEPT -> "CONCEPT"
    }
}