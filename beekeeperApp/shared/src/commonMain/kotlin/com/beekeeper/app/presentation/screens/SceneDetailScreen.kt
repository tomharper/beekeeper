// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/SceneDetailScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.repository.ContentRepositoryImpl
import com.beekeeper.app.domain.repository.FrameRepositoryImpl
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.rememberAppTheme
import com.beekeeper.app.presentation.viewmodels.SceneDetailViewModel
import com.beekeeper.app.presentation.viewmodels.FrameSortOption
import com.beekeeper.app.presentation.viewmodels.FrameViewType

@Composable
fun rememberSceneDetailViewModel(
    projectId: String,
    storyboardId: String,
    sceneId: String
): SceneDetailViewModel {
    return remember(projectId, storyboardId, sceneId) {
        SceneDetailViewModel(
            projectId = projectId,
            storyboardId = storyboardId,
            sceneId = sceneId,
            contentRepository = ContentRepositoryImpl(),
            frameRepository = FrameRepositoryImpl()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneDetailScreen(
    projectId: String,
    storyboardId: String,
    sceneId: String,
    onNavigateBack: () -> Unit,
    onEditScene: () -> Unit,
    onEditFrame: (String) -> Unit,
    viewModel: SceneDetailViewModel = rememberSceneDetailViewModel(
        projectId = projectId,
        storyboardId = storyboardId,
        sceneId = sceneId
    )
) {
    val theme = rememberAppTheme()
    val uiState by viewModel.uiState.collectAsState()

    var showFramePreview by remember { mutableStateOf(false) }
    var showAddFrameDialog by remember { mutableStateOf(false) }

    // Handle loading and error states
    val currentScene = uiState.scene
    val errorMessage = uiState.error

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = currentScene?.title ?: "Loading...",
                subtitle = currentScene?.let { "Scene ${it.sceneNumber} • ${it.duration}s" } ?: "",
                onNavigateBack = onNavigateBack,
                actions = {
                    if (currentScene != null) {
                        IconButton(onClick = onEditScene) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Scene")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentScene != null) {
                FloatingActionButton(
                    onClick = { showAddFrameDialog = true },
                    containerColor = Color(0xFF4FC3F7) // Use explicit color instead of theme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Frame")
                }
            }
        }
    ) { paddingValues ->
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
                        Button(
                            onClick = { viewModel.retry() } // Use public retry method
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            currentScene != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Scene Info Card
                    SceneInfoCard(scene = currentScene)

                    // View Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Frames (${uiState.frames.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Sort dropdown
                            var sortExpanded by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { sortExpanded = true }) {
                                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                                }
                                DropdownMenu(
                                    expanded = sortExpanded,
                                    onDismissRequest = { sortExpanded = false }
                                ) {
                                    FrameSortOption.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.name.replace('_', ' ')) },
                                            onClick = {
                                                viewModel.setSortOption(option)
                                                sortExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // View type toggle
                            IconButton(
                                onClick = {
                                    val newType = when (uiState.viewType) {
                                        FrameViewType.GRID -> FrameViewType.LIST
                                        FrameViewType.LIST -> FrameViewType.TIMELINE
                                        FrameViewType.TIMELINE -> FrameViewType.FILMSTRIP
                                        FrameViewType.FILMSTRIP -> FrameViewType.GRID
                                    }
                                    viewModel.setViewType(newType)
                                }
                            ) {
                                Icon(
                                    when (uiState.viewType) {
                                        FrameViewType.GRID -> Icons.Default.GridView
                                        FrameViewType.LIST -> Icons.Default.ViewList
                                        FrameViewType.TIMELINE -> Icons.Default.Timeline
                                        FrameViewType.FILMSTRIP -> Icons.Default.ViewCarousel
                                    },
                                    contentDescription = "View Type"
                                )
                            }
                        }
                    }

                    // Frames Grid
                    if (uiState.frames.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A2A2A)
                            )
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
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Gray
                                    )
                                    Text(
                                        "No frames yet",
                                        color = Color.Gray
                                    )
                                    TextButton(
                                        onClick = { showAddFrameDialog = true }
                                    ) {
                                        Text("Add First Frame")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 200.dp),
                            modifier = Modifier.height(400.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.frames) { frame ->
                                FrameCard(
                                    frame = frame,
                                    isSelected = frame.id == uiState.selectedFrameId,
                                    onClick = {
                                        viewModel.selectFrame(frame.id)
                                        showFramePreview = true
                                    },
                                    onEditClick = { onEditFrame(frame.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Frame Preview Dialog
    if (showFramePreview && uiState.selectedFrameId != null) {
        val selectedFrame = uiState.frames.find { it.id == uiState.selectedFrameId }
        selectedFrame?.let { frame ->
            FramePreviewDialog(
                frame = frame,
                onDismiss = {
                    showFramePreview = false
                    viewModel.selectFrame(null)
                },
                onEdit = {
                    onEditFrame(frame.id)
                    showFramePreview = false
                },
                onDelete = {
                    viewModel.deleteFrame(frame.id)
                    showFramePreview = false
                }
            )
        }
    }

    // Add Frame Dialog
    if (showAddFrameDialog) {
        AddFrameDialog(
            onDismiss = { showAddFrameDialog = false },
            onConfirm = { description, shotType, cameraAngle, cameraMovement ->
                viewModel.addFrame(description, shotType, cameraAngle, cameraMovement)
                showAddFrameDialog = false
            }
        )
    }
}

@Composable
private fun SceneInfoCard(
    scene: Scene,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Scene description
            Text(
                scene.description,
                fontSize = 14.sp,
                color = Color.White,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Scene metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SceneMetadataItem(
                    icon = Icons.Default.Timer,
                    label = "Duration",
                    value = "${scene.duration}s"
                )
                SceneMetadataItem(
                    icon = Icons.Default.LocationOn,
                    label = "Location",
                    value = scene.location ?: "Not specified"
                )
                SceneMetadataItem(
                    icon = Icons.Default.WbSunny,
                    label = "Time",
                    value = scene.timeOfDay ?: "Not specified"
                )
            }
        }
    }
}

@Composable
private fun SceneMetadataItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Column {
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                value,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun FrameCard(
    frame: Frame,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF3A3A3A) else Color(0xFF2A2A2A)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Frame ${frame.frameNumber}",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                frame.description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    frame.shotType.name,
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FramePreviewDialog(
    frame: Frame,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Frame ${frame.frameNumber}") },
        text = {
            Column {
                Text(frame.description)
                Text("Shot: ${frame.shotType}")
                Text("Angle: ${frame.cameraAngle}")
                frame.cameraMovement?.let { movement ->
                    Text("Movement: $movement")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFrameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, ShotType, CameraAngle, CameraMovement) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var shotType by remember { mutableStateOf(ShotType.MEDIUM_SHOT) }
    var cameraAngle by remember { mutableStateOf(CameraAngle.EYE_LEVEL) }
    var cameraMovement by remember { mutableStateOf(CameraMovement.STATIC) }

    var shotTypeExpanded by remember { mutableStateOf(false) }
    var cameraAngleExpanded by remember { mutableStateOf(false) }
    var cameraMovementExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Frame") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Shot Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = shotTypeExpanded,
                    onExpandedChange = { shotTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = shotType.name.replace('_', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Shot Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shotTypeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = shotTypeExpanded,
                        onDismissRequest = { shotTypeExpanded = false }
                    ) {
                        ShotType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.replace('_', ' ')) },
                                onClick = {
                                    shotType = type
                                    shotTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Camera Angle Dropdown
                ExposedDropdownMenuBox(
                    expanded = cameraAngleExpanded,
                    onExpandedChange = { cameraAngleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = cameraAngle.name.replace('_', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Camera Angle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cameraAngleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = cameraAngleExpanded,
                        onDismissRequest = { cameraAngleExpanded = false }
                    ) {
                        CameraAngle.values().forEach { angle ->
                            DropdownMenuItem(
                                text = { Text(angle.name.replace('_', ' ')) },
                                onClick = {
                                    cameraAngle = angle
                                    cameraAngleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Camera Movement Dropdown
                ExposedDropdownMenuBox(
                    expanded = cameraMovementExpanded,
                    onExpandedChange = { cameraMovementExpanded = it }
                ) {
                    OutlinedTextField(
                        value = cameraMovement.name.replace('_', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Camera Movement") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cameraMovementExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = cameraMovementExpanded,
                        onDismissRequest = { cameraMovementExpanded = false }
                    ) {
                        CameraMovement.values().forEach { movement ->
                            DropdownMenuItem(
                                text = { Text(movement.name.replace('_', ' ')) },
                                onClick = {
                                    cameraMovement = movement
                                    cameraMovementExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (description.isNotBlank()) {
                        onConfirm(description, shotType, cameraAngle, cameraMovement)
                    }
                },
                enabled = description.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}