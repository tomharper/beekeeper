// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/ProjectBibleScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectBibleScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEpisode: ((String) -> Unit)? = null,
    onNavigateToStoryboard: ((String) -> Unit)? = null,
    viewModel: com.beekeeper.app.presentation.viewmodel.ProjectDevelopmentViewModel = remember {
        com.beekeeper.app.presentation.viewmodel.ProjectDevelopmentViewModel(
            projectId = projectId,
            projectRepository = com.beekeeper.app.domain.repository.RepositoryManager.projectRepository,
            characterRepository = com.beekeeper.app.domain.repository.RepositoryManager.characterRepository,
            contentRepository = com.beekeeper.app.domain.repository.RepositoryManager.contentRepository
        )
    }
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(ProjectDevelopmentTab.BLUEPRINTS) }
    var showWorldLogicEditor by remember { mutableStateOf(false) }
    var showPlotEditor by remember { mutableStateOf(false) }
    var selectedBlueprint by remember { mutableStateOf<Pair<EpisodeBlueprintV3, Int>?>(null) }
    var showBibleEditor by remember { mutableStateOf(false) }
    var editingBibleField by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Stories",
                subtitle = "Bible & Episode Blueprints",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                ProjectDevelopmentTab.WORLD -> {
                    FloatingActionButton(
                        onClick = { showBibleEditor = true },
                        containerColor = theme.colors.primary,
                        contentColor = theme.colors.onPrimary
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Bible"
                        )
                    }
                }
                else -> {}
            }
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Selection
            ProjectDevelopmentTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                theme = theme
            )

            // Content based on selected tab
            when (selectedTab) {
                ProjectDevelopmentTab.BLUEPRINTS -> {
                    EpisodeBlueprintsContent(
                        uiState = uiState,
                        projectId = projectId,
                        onNavigateToEpisode = onNavigateToEpisode,
                        onBlueprintClick = { blueprint, episodeNum ->
                            // Convert V2 blueprint to V3 (temporary until API integration)
                            selectedBlueprint = Pair(blueprint.toV3(), episodeNum)
                        },
                        theme = theme
                    )
                }
                ProjectDevelopmentTab.WORLD -> {
                    WorldBuildingContent(
                        uiState = uiState,
                        projectId = projectId,
                        theme = theme
                    )
                }
            }
        }
    }

    // Show blueprint detail sheet when selected
    selectedBlueprint?.let { (blueprint, episodeNum) ->
        ModalBottomSheet(
            onDismissRequest = { selectedBlueprint = null },
            containerColor = theme.colors.background
        ) {
            EpisodeBlueprintDetailScreen(
                blueprint = blueprint,
                episodeNumber = episodeNum,
                onNavigateBack = { selectedBlueprint = null },
                onEdit = {
                    // TODO: Navigate to edit screen
                    selectedBlueprint = null
                }
            )
        }
    }

    // Show Bible Editor
    if (showBibleEditor) {
        ModalBottomSheet(
            onDismissRequest = { showBibleEditor = false },
            containerColor = theme.colors.background
        ) {
            ProjectBibleEditScreen(
                projectBible = uiState.project?.projectBible,
                onNavigateBack = { showBibleEditor = false },
                onFieldEdit = { fieldName, currentValue ->
                    editingBibleField = Pair(fieldName, currentValue)
                },
                onSave = { updatedBible ->
                    // TODO: Save updated bible
                    showBibleEditor = false
                }
            )
        }
    }

    // Show Field Editor Dialog (at top level to avoid z-index issues)
    editingBibleField?.let { (fieldName, currentValue) ->
        var editValue by remember { mutableStateOf(currentValue) }
        var isRecording by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { editingBibleField = null },
            title = {
                Text(
                    "Edit: $fieldName",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = { editValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.colors.primary,
                            unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.3f),
                            focusedTextColor = theme.colors.textPrimary,
                            unfocusedTextColor = theme.colors.textPrimary,
                            cursorColor = theme.colors.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        minLines = 4,
                        maxLines = 8
                    )

                    // Audio input button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isRecording = !isRecording },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) theme.colors.error else theme.colors.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isRecording) "Stop Recording" else "Voice Input")
                        }
                    }

                    if (isRecording) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = theme.colors.error,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Recording...",
                                fontSize = 12.sp,
                                color = theme.colors.error
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Save the edited value
                        editingBibleField = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.colors.primary
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBibleField = null }) {
                    Text("Cancel", color = theme.colors.textSecondary)
                }
            },
            containerColor = theme.colors.background
        )
    }
}

enum class ProjectDevelopmentTab(val title: String) {
    BLUEPRINTS("Episode Blueprints"),
    WORLD("World Building")
}

@Composable
private fun ProjectDevelopmentTabBar(
    selectedTab: ProjectDevelopmentTab,
    onTabSelected: (ProjectDevelopmentTab) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    TabRow(
        selectedTabIndex = ProjectDevelopmentTab.values().indexOf(selectedTab),
        containerColor = theme.colors.surface,
        contentColor = theme.colors.textPrimary
    ) {
        ProjectDevelopmentTab.values().forEach { tab ->
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
private fun EpisodeBlueprintsContent(
    uiState: com.beekeeper.app.presentation.viewmodel.ProjectDevelopmentUiState,
    projectId: String,
    onNavigateToEpisode: ((String) -> Unit)?,
    onBlueprintClick: (EpisodeBlueprint, Int) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    // TODO: Fetch V3 blueprints from API instead of using ProjectBible.episodeBlueprints
    val episodeBlueprints = uiState.projectBible?.episodeBlueprints ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Loading state
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = theme.colors.primary)
                }
            }
            return@LazyColumn
        }

        // Episode Blueprints Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Episode Blueprints",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )

                IconButton(onClick = { /* TODO: Add new blueprint */ }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Blueprint",
                        tint = theme.colors.primary
                    )
                }
            }
        }

        // Episode Blueprints from real data
        if (episodeBlueprints.isNotEmpty()) {
            items(episodeBlueprints.size) { index ->
                val blueprint = episodeBlueprints[index]
                val episodeNum = index + 1
                EpisodeBlueprintCard(
                    blueprint = blueprint,
                    episodeNumber = episodeNum,
                    onClick = { onBlueprintClick(blueprint, episodeNum) },
                    theme = theme
                )
            }
        } else {
            // Show placeholder if no blueprints
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: Add first blueprint */ },
                    colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = theme.colors.textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "No episode blueprints yet",
                            fontSize = 16.sp,
                            color = theme.colors.textSecondary
                        )
                        Text(
                            "Tap to create your first blueprint",
                            fontSize = 14.sp,
                            color = theme.colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorldBuildingContent(
    uiState: com.beekeeper.app.presentation.viewmodel.ProjectDevelopmentUiState,
    projectId: String,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    val projectBible = uiState.projectBible
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // World Foundation
        item {
            WorldBuildingSection(
                title = "World Logic",
                description = projectBible?.worldLogic?.fundamentalPrinciple
                    ?: "The fundamental rules that govern this universe",
                icon = Icons.Default.Public,
                onEdit = { /* TODO: Open world logic editor */ },
                theme = theme,
                content = projectBible?.worldLogic?.let {
                    buildString {
                        append("Divergence: ${it.divergenceFromReality.whatDiverges}\n")
                        append("Rules: ${it.universalRules.size} defined\n")
                        append("Mechanics: ${it.mechanicsSystem.systemName}")
                    }
                }
            )
        }

        // Plot Framework
        item {
            WorldBuildingSection(
                title = "Plot Logic",
                description = projectBible?.plotLogic?.centralEngine?.mainConflict
                    ?: "The cause-and-effect chains that drive the narrative",
                icon = Icons.Default.Timeline,
                onEdit = { /* TODO: Open plot logic editor */ },
                theme = theme,
                content = projectBible?.plotLogic?.let {
                    buildString {
                        append("Main conflict: ${it.centralEngine.mainConflict}\n")
                        append("Point of no return: ${it.centralEngine.pointOfNoReturn}\n")
                        append("Causal chains: ${it.causalChains.size} defined")
                    }
                }
            )
        }

        // Thematic Foundation
        item {
            WorldBuildingSection(
                title = "Thematic Structure",
                description = projectBible?.thematicStructure?.centralThesis
                    ?: "How themes are systematically explored throughout the project",
                icon = Icons.Default.Psychology,
                onEdit = { /* TODO: Open thematic editor */ },
                theme = theme,
                content = projectBible?.thematicStructure?.let {
                    buildString {
                        append("Central thesis: ${it.centralThesis}\n")
                        append("Antithesis: ${it.antithesis}\n")
                        append("Thematic threads: ${it.thematicThreads.size}")
                    }
                }
            )
        }

        // Character Foundations
        item {
            WorldBuildingSection(
                title = "Character Archetypes",
                description = "Core character types and their roles in this world",
                icon = Icons.Default.People,
                onEdit = { /* TODO: Navigate to characters */ },
                theme = theme,
                content = "${uiState.characters.size} characters defined"
            )
        }

        // Production Guidelines
        item {
            WorldBuildingSection(
                title = "Production Guidelines",
                description = "Visual and audio style guidelines for the project",
                icon = Icons.Default.Palette,
                onEdit = { /* TODO: Open production guidelines editor */ },
                theme = theme,
                content = projectBible?.productionGuidelines?.let {
                    buildString {
                        append("Visual Style: ${it.visualStyle}\n")
                        append("Audio Style: ${it.audioStyle}\n")
                        append("Color Palette: ${it.colorPalette.joinToString(", ")}")
                    }
                }
            )
        }

        // Historical Context
        item {
            WorldBuildingSection(
                title = "Backstory & History",
                description = "The events and context that shaped this world",
                icon = Icons.Default.History,
                onEdit = { /* TODO: Open backstory editor */ },
                theme = theme,
                content = projectBible?.worldLogic?.divergenceFromReality?.whenItDiverges
            )
        }
    }
}

@Composable
private fun ProjectStatusCard(
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Project Status",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem("Episodes", "8", theme)
                StatusItem("Scenes", "96", theme)
                StatusItem("Frames", "1,152", theme)
                StatusItem("Completion", "75%", theme)
            }
        }
    }
}

@Composable
private fun StatusItem(
    label: String,
    value: String,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = theme.colors.textSecondary
        )
    }
}

@Composable
private fun EpisodeBlueprintsSection(
    projectId: String,
    onNavigateToEpisode: ((String) -> Unit)?,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Episode Blueprints",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )

                TextButton(onClick = { /* TODO: Add new episode */ }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Episode")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TODO: Replace with actual episode data
            repeat(3) { index ->
                EpisodeRow(
                    episodeNumber = index + 1,
                    title = "Episode ${index + 1}",
                    status = if (index == 0) "Complete" else if (index == 1) "In Progress" else "Draft",
                    onNavigateToEpisode = onNavigateToEpisode,
                    theme = theme
                )
                if (index < 2) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episodeNumber: Int,
    title: String,
    status: String,
    onNavigateToEpisode: ((String) -> Unit)?,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToEpisode?.invoke("episode_$episodeNumber") }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = theme.colors.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "$episodeNumber",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                color = theme.colors.textPrimary
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = when (status) {
                "Complete" -> Color.Green.copy(alpha = 0.2f)
                "In Progress" -> Color.Blue.copy(alpha = 0.2f)
                else -> Color.Gray.copy(alpha = 0.2f)
            }
        ) {
            Text(
                text = status,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = when (status) {
                    "Complete" -> Color.Green.copy(alpha = 0.8f)
                    "In Progress" -> Color.Blue.copy(alpha = 0.8f)
                    else -> Color.Gray.copy(alpha = 0.8f)
                }
            )
        }
    }
}

@Composable
private fun CoreDevelopmentToolsSection(
    onEditWorldLogic: () -> Unit,
    onEditPlotLogic: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Core Development Tools",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEditWorldLogic,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Public, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("World Logic")
                }

                Button(
                    onClick = onEditPlotLogic,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Timeline, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Plot Logic")
                }
            }
        }
    }
}

@Composable
private fun NarrativeStructureCard(
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Narrative Structure Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "• Total Episodes: 8-12 (Standard series length)\n" +
                      "• Scenes per Episode: 12+ (Minimum requirement)\n" +
                      "• Dialogue Lines: 144+ per episode (12 scenes × 12 lines)\n" +
                      "• Estimated Runtime: 8-12 hours total",
                fontSize = 14.sp,
                color = theme.colors.textSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun EpisodesStatsCard(
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Episodes Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem("Total Episodes", "8", theme)
                StatusItem("Completed", "3", theme)
                StatusItem("In Progress", "3", theme)
                StatusItem("Draft", "2", theme)
            }
        }
    }
}

@Composable
private fun EpisodeManagementCard(
    episodeNumber: Int,
    episodeTitle: String,
    status: String,
    sceneCount: Int,
    dialogueLines: Int,
    onNavigateToEpisode: ((String) -> Unit)?,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToEpisode?.invoke("episode_${episodeNumber}") },
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.colors.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$episodeNumber",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = episodeTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$sceneCount scenes • $dialogueLines dialogue lines",
                        fontSize = 14.sp,
                        color = theme.colors.textSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (status) {
                        "Complete" -> Color.Green.copy(alpha = 0.2f)
                        "In Progress" -> Color.Blue.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = when (status) {
                            "Complete" -> Color.Green.copy(alpha = 0.8f)
                            "In Progress" -> Color.Blue.copy(alpha = 0.8f)
                            else -> Color.Gray.copy(alpha = 0.8f)
                        }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "View Episode",
                    tint = theme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun WorldBuildingSection(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onEdit: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme,
    content: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = theme.colors.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = theme.colors.textSecondary
                )
                if (content != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = content,
                        fontSize = 12.sp,
                        color = theme.colors.textSecondary.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                }
            }

            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                tint = theme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun NarrativeArcCard(
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Narrative Arc Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Arc visualization (simplified)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ArcPoint("Setup", "Episode 1-2", theme)
                ArcPoint("Rising Action", "Episode 3-5", theme)
                ArcPoint("Climax", "Episode 6-7", theme)
                ArcPoint("Resolution", "Episode 8", theme)
            }
        }
    }
}

@Composable
private fun ArcPoint(
    label: String,
    episodes: String,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = theme.colors.primary.copy(alpha = 0.2f)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = theme.colors.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = episodes,
            fontSize = 10.sp,
            color = theme.colors.textSecondary
        )
    }
}

@Composable
private fun EpisodeBlueprintCard(
    blueprint: EpisodeBlueprint,
    episodeNumber: Int,
    onClick: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Episode title and number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = blueprint.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.colors.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Ep $episodeNumber",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = theme.colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Logline
            Text(
                text = blueprint.logline,
                fontSize = 14.sp,
                color = theme.colors.textSecondary,
                maxLines = 2
            )

            // Themes
            if (blueprint.themes.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    blueprint.themes.take(3).forEach { theme_text ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.colors.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = theme_text,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                color = theme.colors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
