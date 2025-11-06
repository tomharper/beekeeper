package com.beekeeper.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beekeeper.app.domain.model.Scene
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.rememberAppTheme
import com.beekeeper.app.presentation.viewmodels.StoryboardDetailViewModel
import com.beekeeper.app.utils.formatTime
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryboardDetailScreen(
    projectId: String,
    storyboardId: String,
    onNavigateBack: () -> Unit,
    onSceneClick: (String) -> Unit = {},
    onEditScene: (String) -> Unit = {},
    onAddScene: () -> Unit = {},
    onExport: () -> Unit = {}
) {
    val viewModel: StoryboardDetailViewModel = viewModel<StoryboardDetailViewModel> {
        StoryboardDetailViewModel(projectId, storyboardId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var currentSceneIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var showSceneDetails by remember { mutableStateOf(true) }

    val storyboard = uiState.storyboard
    val scenes = storyboard?.scenes ?: emptyList()
    val currentScene = scenes.getOrNull(currentSceneIndex)

    // Calculate total duration
    val totalDuration = scenes.sumOf { it.duration }
    val formattedDuration = formatTime(totalDuration)

    // Auto-play functionality
    LaunchedEffect(isPlaying, scenes) {
        if (isPlaying && scenes.isNotEmpty()) {
            while (isPlaying && currentSceneIndex < scenes.size - 1) {
                val currentSceneDuration = scenes[currentSceneIndex].duration // Convert seconds to milliseconds
                currentSceneIndex++
            }
            if (currentSceneIndex >= scenes.size - 1) {
                isPlaying = false
                currentSceneIndex = 0 // Reset to beginning
            }
        }
    }

    Scaffold(
        topBar = {
            StoryboardDetailTopBar(
                title = storyboard?.title ?: "Loading...",
                subtitle = "Duration: $formattedDuration • ${scenes.size} scenes",
                onNavigateBack = onNavigateBack,
                onExport = onExport
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddScene,
                containerColor = Color(0xFF4FC3F7)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Scene")
            }
        }
    ) { paddingValues ->
        val errorMessage = uiState.error
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            storyboard != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.Black)
                ) {
                    // Main Viewer Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Scene Display
                        currentScene?.let { scene ->
                            SceneViewer(
                                scene = scene,
                                sceneNumber = currentSceneIndex + 1,
                                totalScenes = scenes.size,
                                modifier = Modifier.fillMaxSize(),
                                onClick = { onSceneClick(scene.id) }
                            )
                        } ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.MovieFilter,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.Gray
                                    )
                                    Text(
                                        "No scenes available",
                                        color = Color.Gray
                                    )
                                    TextButton(onClick = onAddScene) {
                                        Text("Add First Scene")
                                    }
                                }
                            }
                        }

                        // Overlay Controls
                        if (showSceneDetails && currentScene != null) {
                            SceneDetailsOverlay(
                                scene = currentScene,
                                sceneNumber = currentSceneIndex + 1,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            )
                        }

                        // Progress Indicator
                        if (scenes.isNotEmpty()) {
                            LinearProgressIndicator(
                                progress = (currentSceneIndex + 1).toFloat() / scenes.size,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter),
                                color = Color(0xFF4FC3F7),
                                trackColor = Color(0xFF1A1A1A)
                            )
                        }
                    }

                    // Timeline and Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1A1A))
                    ) {
                        // Scene Timeline
                        SceneTimeline(
                            scenes = scenes,
                            currentIndex = currentSceneIndex,
                            onSceneSelect = { index ->
                                currentSceneIndex = index
                                isPlaying = false
                            }
                        )

                        // Playback Controls
                        PlaybackControls(
                            isPlaying = isPlaying,
                            onPlayPause = { isPlaying = !isPlaying },
                            onPrevious = {
                                if (currentSceneIndex > 0) {
                                    currentSceneIndex--
                                }
                            },
                            onNext = {
                                if (currentSceneIndex < scenes.size - 1) {
                                    currentSceneIndex++
                                }
                            },
                            onToggleDetails = { showSceneDetails = !showSceneDetails },
                            showDetails = showSceneDetails,
                            currentScene = currentScene,
                            onEditScene = currentScene?.let { { onEditScene(it.id) } },
                            onViewFrames = currentScene?.let { { onSceneClick(it.id) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryboardDetailTopBar(
    title: String,
    subtitle: String,
    onNavigateBack: () -> Unit,
    onExport: () -> Unit
) {
    SecondaryTopBar(
        title = title,
        subtitle = subtitle,
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = onExport) {
                Icon(Icons.Default.Download, contentDescription = "Export")
            }
        }
    )
}

@Composable
private fun SceneViewer(
    scene: Scene,
    sceneNumber: Int,
    totalScenes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scene Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scene Header
                Text(
                    "Scene $sceneNumber of $totalScenes",
                    fontSize = 14.sp,
                    color = Color(0xFF4FC3F7),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    scene.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Scene Description
                Text(
                    scene.description,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                )

                // Scene Metadata
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    SceneMetadata(
                        icon = Icons.Default.Timer,
                        label = "Duration",
                        value = "${scene.duration}s"
                    )
                    scene.location?.let { location ->
                        SceneMetadata(
                            icon = Icons.Default.LocationOn,
                            label = "Location",
                            value = location
                        )
                    }
                    scene.timeOfDay?.let { time ->
                        SceneMetadata(
                            icon = Icons.Default.WbSunny,
                            label = "Time",
                            value = time
                        )
                    }
                }

                // Click hint
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Click to view frames",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

@Composable
private fun SceneMetadata(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF4FC3F7)
        )
        Text(
            label,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            value,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SceneDetailsOverlay(
    scene: Scene,
    sceneNumber: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A).copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Scene $sceneNumber: ${scene.title}",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                scene.description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailChip(Icons.Default.Timer, "${scene.duration}s")
                scene.location?.let {
                    DetailChip(Icons.Default.LocationOn, it)
                }
                scene.dialogueSnippet?.let {
                    DetailChip(Icons.Default.Mood, it)
                }
            }
        }
    }
}

@Composable
private fun DetailChip(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = Color(0xFF4FC3F7)
        )
        Text(
            text,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun SceneTimeline(
    scenes: List<Scene>,
    currentIndex: Int,
    onSceneSelect: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(scenes) { index, scene ->
            SceneTimelineItem(
                scene = scene,
                sceneNumber = index + 1,
                isSelected = index == currentIndex,
                onClick = { onSceneSelect(index) }
            )
        }
    }
}

@Composable
private fun SceneTimelineItem(
    scene: Scene,
    sceneNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .fillMaxHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(4.dp),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF4FC3F7)) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF3A3A3A) else Color(0xFF2A2A2A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Scene $sceneNumber",
                fontSize = 10.sp,
                color = if (isSelected) Color(0xFF4FC3F7) else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                scene.title,
                fontSize = 11.sp,
                color = if (isSelected) Color.White else Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                "${scene.duration}s",
                fontSize = 9.sp,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleDetails: () -> Unit,
    showDetails: Boolean,
    currentScene: Scene?,
    onEditScene: (() -> Unit)?,
    onViewFrames: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Scene")
        }

        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(56.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFF4FC3F7))
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black
            )
        }

        IconButton(onClick = onNext) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next Scene")
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onToggleDetails) {
            Icon(
                if (showDetails) Icons.Outlined.Info else Icons.Default.Info,
                contentDescription = "Toggle Details"
            )
        }

        if (currentScene != null) {
            onEditScene?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Scene")
                }
            }

            onViewFrames?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Default.ViewCarousel, contentDescription = "View Frames")
                }
            }
        }
    }
}