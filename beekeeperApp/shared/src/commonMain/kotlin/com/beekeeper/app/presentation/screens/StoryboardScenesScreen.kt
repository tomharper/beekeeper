// File: fillerApp/shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/StoryboardScenesScreen.kt

package com.beekeeper.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.rememberAppTheme
import com.beekeeper.app.presentation.viewmodels.StoryboardScenesViewModel
import com.beekeeper.app.utils.formatDuration
import kotlinx.coroutines.delay

// View Type for switching between layouts
enum class SceneViewType {
    GRID, LIST, TIMELINE, STORYBOARD, KANBAN
}

// Filter and Sort options
data class SceneFilters(
    val showOnlyKeyScenes: Boolean = false,
    val showOnlyApproved: Boolean = false,
    val characters: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val minimumDuration: Int? = null,
    val maximumDuration: Int? = null,
    val searchQuery: String = ""
)

enum class SceneSortOption {
    SCENE_NUMBER, DURATION, TITLE, APPROVAL_STATUS, COMPLETION, DATE_CREATED, DATE_MODIFIED
}

enum class ApprovalStatus {
    PENDING, IN_REVIEW, APPROVED, REJECTED, REVISION_REQUESTED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StoryboardScenesScreen(
    storyboardId: String,
    projectId: String,
    onNavigateBack: () -> Unit,
    onSceneClick: (String) -> Unit,
    onFrameClick: (String, String) -> Unit, // sceneId, frameId
    onCreateScene: () -> Unit = {},
    onEditScene: (String) -> Unit = {},
    onExportStoryboard: () -> Unit = {},
    viewModel: StoryboardScenesViewModel = remember { StoryboardScenesViewModel(storyboardId, projectId) }
) {
    val theme = rememberAppTheme()
    val uiState by viewModel.uiState.collectAsState()

    var viewType by remember { mutableStateOf(SceneViewType.GRID) }
    var showFilters by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(SceneFilters()) }
    var sortOption by remember { mutableStateOf(SceneSortOption.SCENE_NUMBER) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedScene by remember { mutableStateOf<Scene?>(null) }
    var showSceneDetails by remember { mutableStateOf(false) }
    var showBatchActions by remember { mutableStateOf(false) }
    var selectedScenes by remember { mutableStateOf(setOf<String>()) }
    var isSelectionMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = uiState.storyboard?.title ?: "Storyboard Scenes",
                subtitle = "${uiState.scenes.size} scenes • ${formatDuration(uiState.totalDuration)}",
                onNavigateBack = onNavigateBack,
                actions = {
                    // View Type Toggle
                    IconButton(onClick = {
                        viewType = when(viewType) {
                            SceneViewType.GRID -> SceneViewType.LIST
                            SceneViewType.LIST -> SceneViewType.TIMELINE
                            else -> SceneViewType.GRID
                        }
                    }) {
                        Icon(
                            when(viewType) {
                                SceneViewType.GRID -> Icons.Default.GridView
                                SceneViewType.LIST -> Icons.Default.ViewList
                                else -> Icons.Default.ViewCarousel
                            },
                            contentDescription = "Change View",
                            tint = theme.colors.onSurface
                        )
                    }

                    // Filter Toggle
                    BadgedBox(
                        badge = {
                            if (filters != SceneFilters()) {
                                Badge(
                                    containerColor = theme.colors.secondary,
                                    contentColor = Color.White
                                ) {
                                    Text("!")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (filters != SceneFilters()) theme.colors.secondary else theme.colors.onSurface
                            )
                        }
                    }

                    // More Options
                    IconButton(onClick = { /* Show menu */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = theme.colors.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = theme.colors.primary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, "Add Scene") },
                    text = { Text("Add Scene") }
                )
            }
        },
        containerColor = theme.colors.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Storyboard Stats Bar
            StoryboardStatsBar(
                totalScenes = uiState.scenes.size,
                totalFrames = uiState.scenes.sumOf { it.frames.size },
                totalDuration = uiState.totalDuration,
                completionPercentage = uiState.completionPercentage,
                approvedScenes = uiState.approvedScenes
            )

            // Filter Panel (Animated)
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                SceneFilterPanel(
                    filters = filters,
                    onFiltersChange = { filters = it },
                    availableCharacters = uiState.availableCharacters,
                    availableLocations = uiState.availableLocations
                )
            }

            // Main Content Area
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = theme.colors.primary)
                    }
                }

                uiState.scenes.isEmpty() -> {
                    EmptySceneState(
                        onCreateScene = { showCreateDialog = true }
                    )
                }

                else -> {
                    val filteredScenes = filterScenes(uiState.scenes, filters)
                    val sortedScenes = sortScenes(filteredScenes, sortOption)

                    when (viewType) {
                        SceneViewType.GRID -> SceneGridView(
                            scenes = sortedScenes,
                            onSceneClick = onSceneClick,
                            onSceneLongPress = { scene ->
                                selectedScene = scene
                                showSceneDetails = true
                            },
                            isSelectionMode = isSelectionMode,
                            selectedScenes = selectedScenes,
                            onSelectionChange = { sceneId ->
                                selectedScenes = if (selectedScenes.contains(sceneId)) {
                                    selectedScenes - sceneId
                                } else {
                                    selectedScenes + sceneId
                                }
                            },
                            viewModel = viewModel
                        )

                        SceneViewType.LIST -> SceneListView(
                            scenes = sortedScenes,
                            selectedSceneId = selectedScene?.id,
                            onSceneSelect = { scene ->
                                onSceneClick(scene.id)
                            },
                            onSceneEdit = { scene ->
                                onEditScene(scene.id)
                            }
                        )

                        else -> {
                            // Timeline view placeholder
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${viewType.name} View - Coming Soon")
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreateDialog) {
        CreateSceneDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, description, duration ->
                viewModel.createScene(title, description, duration)
                showCreateDialog = false
            }
        )
    }

    if (showSceneDetails && selectedScene != null) {
        SceneDetailsDialog(
            scene = selectedScene!!,
            onDismiss = {
                showSceneDetails = false
                selectedScene = null
            },
            onEdit = {
                onEditScene(selectedScene!!.id)
                showSceneDetails = false
            },
            onDelete = {
                viewModel.deleteScene(selectedScene!!.id)
                showSceneDetails = false
                selectedScene = null
            }
        )
    }
}

@Composable
fun StoryboardStatsBar(
    totalScenes: Int,
    totalFrames: Int,
    totalDuration: Int,
    completionPercentage: Int,
    approvedScenes: Int
) {
    val theme = rememberAppTheme()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = theme.colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                icon = Icons.Default.Movie,
                value = totalScenes.toString(),
                label = "Scenes",
                color = theme.colors.primary
            )

            StatDivider()

            StatItem(
                icon = Icons.Default.PhotoLibrary,
                value = totalFrames.toString(),
                label = "Frames",
                color = theme.colors.secondary
            )

            StatDivider()

            StatItem(
                icon = Icons.Default.Schedule,
                value = formatSceneDuration(totalDuration),
                label = "Duration",
                color = theme.colors.tertiary
            )

            StatDivider()

            StatItem(
                icon = Icons.Default.CheckCircle,
                value = "$approvedScenes/$totalScenes",
                label = "Approved",
                color = Color(0xFF4CAF50)
            )

            StatDivider()

            StatItem(
                icon = Icons.Default.DonutLarge,
                value = "$completionPercentage%",
                label = "Complete",
                color = theme.colors.primary
            )
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun StatDivider() {
    Divider(
        modifier = Modifier
            .height(32.dp)
            .width(1.dp),
        color = Color.Gray.copy(alpha = 0.3f)
    )
}

@Composable
fun SceneGridView(
    scenes: List<Scene>,
    onSceneClick: (String) -> Unit,
    onSceneLongPress: (Scene) -> Unit,
    isSelectionMode: Boolean,
    selectedScenes: Set<String>,
    onSelectionChange: (String) -> Unit,
    viewModel: StoryboardScenesViewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(scenes, key = { it.id }) { scene ->
            SceneCard(
                scene = scene,
                onClick = {
                    if (isSelectionMode) {
                        onSelectionChange(scene.id)
                    } else {
                        onSceneClick(scene.id)
                    }
                },
                onLongClick = { onSceneLongPress(scene) },
                isSelected = selectedScenes.contains(scene.id),
                showSelection = isSelectionMode,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SceneCard(
    scene: Scene,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean,
    showSelection: Boolean,
    viewModel: StoryboardScenesViewModel, // Add viewModel for generation
) {
    val theme = rememberAppTheme()
    //val frameStates = viewModel.uiState.collectAsState().value
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                theme.colors.primary.copy(alpha = 0.1f)
            else
                theme.colors.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, theme.colors.primary) else null
    ) {
        Column {
            // Thumbnail Area with Frame Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2A2A2A),
                                Color(0xFF1A1A1A)
                            )
                        )
                    )
            ) {
                // Frame Thumbnails Row
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(scene.frames.take(5)) { frame ->
                        FrameThumbnail(frame = frame)
                    }

                    if (scene.frames.size > 5) {
                        item {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "+${scene.frames.size - 5}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Scene Number Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = CircleShape,
                    color = theme.colors.primary
                ) {
                    Text(
                        text = scene.sceneNumber.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                /*
                // Generation indicator
                val generatingFrames = scene.frames.count { frame ->
                    frameStates[frame.id] == ContentState.GENERATING
                }
                if (generatingFrames > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF4A90E2).copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color.White,
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Generating $generatingFrames",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                 */

                // Selection Checkbox
                if (showSelection) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = theme.colors.primary,
                            uncheckedColor = Color.White
                        )
                    )
                }

                // Key Scene Indicator
                if (scene.isKeyScene == true) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Key Scene",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .size(20.dp),
                        tint = Color(0xFFFFD700)
                    )
                }

                // Duration Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = formatSceneDuration(scene.duration),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            // Scene Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = scene.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = scene.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scene Metadata Tags
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scene.location?.let {
                        item {
                            MetadataChip(
                                icon = Icons.Default.LocationOn,
                                text = it,
                                backgroundColor = Color(0xFF2196F3)
                            )
                        }
                    }

                    scene.timeOfDay?.let {
                        item {
                            MetadataChip(
                                icon = Icons.Default.WbSunny,
                                text = it,
                                backgroundColor = Color(0xFFFF9800)
                            )
                        }
                    }

                    if (scene.frames.isNotEmpty()) {
                        item {
                            MetadataChip(
                                icon = Icons.Default.PhotoLibrary,
                                text = "${scene.frames.size} frames",
                                backgroundColor = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun SceneListViewItem(
    scene: Scene,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit
) {
    val theme = rememberAppTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                theme.colors.primary.copy(alpha = 0.1f)
            else
                theme.colors.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scene Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scene ${scene.sceneNumber}: ${scene.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scene.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${scene.frames.size} frames",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.colors.onSurfaceVariant
                    )
                    Text(
                        text = "• ${formatSceneDuration(scene.duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.colors.onSurfaceVariant
                    )
                }
            }

            // Actions
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = theme.colors.primary
                )
            }
        }
    }
}

@Composable
fun FrameThumbnail(frame: Frame) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF3A3A3A))
            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Frame ${frame.frameNumber}",
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MetadataChip(
    icon: ImageVector,
    text: String,
    backgroundColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = backgroundColor
            )
            Text(
                text = text,
                fontSize = 10.sp,
                color = backgroundColor
            )
        }
    }
}

@Composable
fun SceneFilterPanel(
    filters: SceneFilters,
    onFiltersChange: (SceneFilters) -> Unit,
    availableCharacters: List<String>,
    availableLocations: List<String>
) {
    val theme = rememberAppTheme()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = theme.colors.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Field
            OutlinedTextField(
                value = filters.searchQuery,
                onValueChange = {
                    onFiltersChange(filters.copy(searchQuery = it))
                },
                label = { Text("Search scenes") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = filters.showOnlyKeyScenes,
                    onClick = {
                        onFiltersChange(filters.copy(showOnlyKeyScenes = !filters.showOnlyKeyScenes))
                    },
                    label = { Text("Key Scenes Only") }
                )

                FilterChip(
                    selected = filters.showOnlyApproved,
                    onClick = {
                        onFiltersChange(filters.copy(showOnlyApproved = !filters.showOnlyApproved))
                    },
                    label = { Text("Approved Only") }
                )
            }
        }
    }
}

@Composable
fun EmptySceneState(
    onCreateScene: () -> Unit
) {
    val theme = rememberAppTheme()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Movie,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = theme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No scenes yet",
                style = MaterialTheme.typography.headlineSmall,
                color = theme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Create your first scene to start building the storyboard",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateScene,
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Scene")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSceneDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, duration: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Scene") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Scene Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = duration,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            duration = it
                        }
                    },
                    label = { Text("Duration (seconds)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, description, duration.toIntOrNull() ?: 60)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneDetailsDialog(
    scene: Scene,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Scene ${scene.sceneNumber}: ${scene.title}")
        },
        text = {
            Column {
                Text(
                    scene.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Scene Details
                SceneDetailRow("Duration", formatSceneDuration(scene.duration))
                SceneDetailRow("Frames", scene.frames.size.toString())
                scene.location?.let { SceneDetailRow("Location", it) }
                scene.timeOfDay?.let { SceneDetailRow("Time of Day", it) }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SceneDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Helper functions with unique names to avoid conflicts
fun formatSceneDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "${minutes}m ${remainingSeconds}s"
    } else {
        "${seconds}s"
    }
}


fun filterScenes(scenes: List<Scene>, filters: SceneFilters): List<Scene> {
    return scenes.filter { scene ->
        (!filters.showOnlyKeyScenes || scene.isKeyScene == true) &&
                (filters.searchQuery.isEmpty() ||
                        scene.title.contains(filters.searchQuery, ignoreCase = true) ||
                        scene.description.contains(filters.searchQuery, ignoreCase = true))
    }
}

fun sortScenes(scenes: List<Scene>, sortOption: SceneSortOption): List<Scene> {
    return when (sortOption) {
        SceneSortOption.SCENE_NUMBER -> scenes.sortedBy { it.sceneNumber }
        SceneSortOption.DURATION -> scenes.sortedByDescending { it.duration }
        SceneSortOption.TITLE -> scenes.sortedBy { it.title }
        else -> scenes
    }
}

@Composable
fun SceneListView(
    scenes: List<Scene>,
    selectedSceneId: String?,
    onSceneSelect: (Scene) -> Unit,
    onSceneEdit: (Scene) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(scenes) { scene ->
            SceneListItem(
                scene = scene,
                isSelected = scene.id == selectedSceneId,
                onClick = { onSceneSelect(scene) },
                onEdit = { onSceneEdit(scene) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneListItem(
    scene: Scene,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF3A3A3A) else Color(0xFF2A2A2A)
        ),
        border = if (isSelected) BorderStroke(1.dp, Color(0xFF4A90E2)) else null,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Scene Number Badge
            Surface(
                color = Color(0xFF4A90E2).copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = scene.sceneNumber.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A90E2)
                    )
                }
            }

            // Scene Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = scene.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = formatDuration(scene.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = scene.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (scene.location != null) {
                        InfoChip(
                            icon = Icons.Default.LocationOn,
                            text = scene.location
                        )
                    }
                    if (scene.characterIds.isNotEmpty()) {
                        InfoChip(
                            icon = Icons.Default.People,
                            text = "${scene.characterIds.size} characters"
                        )
                    }
                    InfoChip(
                        icon = Icons.Default.Movie,
                        text = scene.transitionType.name
                    )
                }
            }

            // Edit Button
            IconButton(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF4A90E2),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
