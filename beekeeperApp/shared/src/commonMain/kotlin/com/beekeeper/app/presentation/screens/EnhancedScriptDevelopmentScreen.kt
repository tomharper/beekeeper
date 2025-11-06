package com.beekeeper.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.viewmodels.ScriptDevelopmentViewModel
import com.beekeeper.app.presentation.viewmodels.rememberScriptDevelopmentViewModel
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun EnhancedScriptDevelopmentScreen(
    projectId: String,
    navController: NavHostController,
    storyId: String? = null,
    viewModel: ScriptDevelopmentViewModel = rememberScriptDevelopmentViewModel(projectId, storyId),
    onNavigateToScript: (String) -> Unit = { navController.navigate("project/$projectId/script/$it") },
    onNavigateToStoryboard: (String) -> Unit = { navController.navigate("project/$projectId/storyboard/$it") },
    onNavigateToCharacter: (String) -> Unit = { navController.navigate("character/$it") }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddMenu by remember { mutableStateOf(false) }
    var selectedScene by remember { mutableStateOf<SceneScript?>(null) }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Story Development",
                subtitle = uiState.currentStory?.title,
                onNavigateBack = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            if (uiState.currentStory != null) {
                ExtendedFloatingActionButton(
                    onClick = { showAddMenu = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add")
                }
            }
        }
    ) { paddingValues ->
        if (uiState.currentStory == null && !uiState.isLoading) {
            // Empty state - create new story
            EmptyStoryState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onCreateStory = {
                    viewModel.setContentType(ContentType.STORY)
                    viewModel.createStoryWithPattern(
                        uiState.availablePatterns.firstOrNull() ?: StoryPattern(
                            id = "default",
                            name = "Default",
                            description = "Default story pattern",
                            category = PatternCategory.NARRATIVE_STRUCTURE,
                            structure = PatternStructure(
                                type = "THREE_ACT",
                                beats = listOf(
                                    StoryBeat("Beginning", 0f, "Setup"),
                                    StoryBeat("Middle", 0.5f, "Conflict"),
                                    StoryBeat("End", 1f, "Resolution")
                                )
                            ),
                            examples = emptyList()
                        )
                    )
                }
            )
        } else if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Story Map View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Story Overview
                item {
                    StoryOverviewCard(
                        story = uiState.currentStory,
                        beats = uiState.beats
                    )
                }

                // Narrative Arc Visualization
                if (uiState.scenes.isNotEmpty()) {
                    item {
                        NarrativeArcCard(
                            scenes = uiState.scenes,
                            beats = uiState.beats
                        )
                    }
                }

                // Scenes Section
                if (uiState.scenes.isNotEmpty()) {
                    item {
                        Text(
                            "Scenes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    itemsIndexed(uiState.scenes) { index, scene ->
                        SceneCard(
                            scene = scene,
                            index = index + 1,
                            isSelected = selectedScene?.id == scene.id,
                            onClick = { selectedScene = scene }
                        )
                    }
                }

                // Add Scene Button
                item {
                    AddSceneCard(
                        onClick = { viewModel.addScene(0f) }
                    )
                }

                // Characters Section
                if (uiState.characters.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Characters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.characters) { character ->
                                CharacterCard(
                                    character = character,
                                    onClick = { onNavigateToCharacter(character.id) }
                                )
                            }
                            item {
                                AddCharacterCard(
                                    onClick = { viewModel.addCharacter(
                                        CharacterProfile(
                                            id = "temp_${getCurrentTimeMillis()}",
                                            projectId = projectId,
                                            name = "New Character",
                                            role = CharacterRole.PROTAGONIST,
                                            archetype = "Hero",
                                            description = "A new character",
                                            personality = PersonalityProfile(
                                                archetype = "Hero",
                                                traits = emptyList(),
                                                motivations = emptyList(),
                                                fears = emptyList(),
                                                backstory = "",
                                                aiInsights = "",
                                                oceanScores = OceanPersonality(0.5f, 0.5f, 0.5f, 0.5f, 0.5f)
                                            ),
                                            relationships = emptyList(),
                                            screenTime = 0f,
                                            dialogueCount = 0,
                                            age = 25,
                                            height = "5'10\"",
                                            gender = Gender.UNSPECIFIED,
                                            build = "Average",
                                            hairColor = "Brown",
                                            eyeColor = "Brown",
                                            distinctiveFeatures = emptyList(),
                                            physicalAttributes = PhysicalAttributes(
                                                height = "5'10\"",
                                                build = "Average",
                                                hairColor = "Brown",
                                                eyeColor = "Brown",
                                                distinctiveFeatures = emptyList()
                                            ),
                                            createdAt = Clock.System.now(),
                                            updatedAt = Clock.System.now()
                                        )
                                    ) }
                                )
                            }
                        }
                    }
                }

                // Story Patterns Section
                if (uiState.availablePatterns.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Story Patterns",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.availablePatterns) { pattern ->
                                PatternCard(
                                    pattern = pattern,
                                    isApplied = pattern in uiState.appliedPatterns,
                                    onClick = {
                                        if (pattern in uiState.appliedPatterns) {
                                            viewModel.removeStoryPattern(pattern)
                                        } else {
                                            viewModel.applyStoryPattern(pattern)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Menu
    if (showAddMenu) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showAddMenu = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Add Scene") },
                    leadingContent = {
                        Icon(Icons.Default.Movie, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        viewModel.addScene(0f)
                        showAddMenu = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Add Character") },
                    leadingContent = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        viewModel.addCharacter(
                            CharacterProfile(
                                id = "temp_${getCurrentTimeMillis()}",
                                projectId = projectId,
                                name = "New Character",
                                role = CharacterRole.PROTAGONIST,
                                archetype = "Hero",
                                description = "A new character",
                                personality = PersonalityProfile(
                                    archetype = "Hero",
                                    traits = emptyList(),
                                    motivations = emptyList(),
                                    fears = emptyList(),
                                    backstory = "",
                                    aiInsights = "",
                                    oceanScores = OceanPersonality(0.5f, 0.5f, 0.5f, 0.5f, 0.5f)
                                ),
                                relationships = emptyList(),
                                screenTime = 0f,
                                dialogueCount = 0,
                                age = 25,
                                height = "5'10\"",
                                gender = Gender.UNSPECIFIED,
                                build = "Average",
                                hairColor = "Brown",
                                eyeColor = "Brown",
                                distinctiveFeatures = emptyList(),
                                physicalAttributes = PhysicalAttributes(
                                    height = "5'10\"",
                                    build = "Average",
                                    hairColor = "Brown",
                                    eyeColor = "Brown",
                                    distinctiveFeatures = emptyList()
                                ),
                                createdAt = Clock.System.now(),
                                updatedAt = Clock.System.now()
                            )
                        )
                        showAddMenu = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Apply Story Pattern") },
                    leadingContent = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        // Apply first available pattern for demo
                        uiState.availablePatterns.firstOrNull()?.let {
                            viewModel.applyStoryPattern(it)
                        }
                        showAddMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyStoryState(
    modifier: Modifier = Modifier,
    onCreateStory: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Movie,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = "Create Your Story",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Start building your narrative",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateStory,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Story")
            }
        }
    }
}

@Composable
private fun StoryOverviewCard(
    story: Story?,
    beats: List<StoryBeat>
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
            Text(
                text = "Story Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
/*
            story?.logline?.let { logline ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = logline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

 */

            if (beats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Narrative Beats",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    beats.forEach { beat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = beat.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SceneCard(
    scene: SceneScript,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scene $index",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    scene.title?.let { title ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• $title",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                scene.heading?.let { heading ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Emotional Tone Badge
                    scene.emotionalTone?.let { tone ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tone.name.replace("_", " ")) },
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    // Narrative Function Badge
                    scene.narrativeFunction?.let { function ->
                        AssistChip(
                            onClick = { },
                            label = { Text(function.name.replace("_", " ")) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${scene.dialogue.size} lines",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (scene.characterIds.isNotEmpty()) {
                    Text(
                        text = "${scene.characterIds.size} characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddSceneCard(
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add New Scene",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CharacterCard(
    character: CharacterProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = character.name.take(2).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = character.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            character.archetype?.let { archetype ->
                Text(
                    text = archetype,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun AddCharacterCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NarrativeArcCard(
    scenes: List<SceneScript>,
    beats: List<StoryBeat>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Narrative Arc",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Arc Visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val padding = 20f

                    // Draw the narrative arc curve
                    val path = Path().apply {
                        moveTo(padding, height - padding)

                        // Create a dramatic arc with peak at climax
                        cubicTo(
                            width * 0.3f, height * 0.2f,
                            width * 0.7f, height * 0.1f,
                            width - padding, height * 0.6f
                        )
                    }

                    // Draw the arc line
                    drawPath(
                        path = path,
                        color = Color(0xFF7C3AED),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.cornerPathEffect(10f)
                        )
                    )

                    // Draw beat points
                    val beatPositions = listOf(
                        0.0f to "Setup",
                        0.25f to "Rising Action",
                        0.75f to "Climax",
                        0.9f to "Resolution"
                    )

                    beatPositions.forEach { (position, label) ->
                        val x = padding + (width - 2 * padding) * position
                        val y = when (position) {
                            0.0f -> height - padding
                            0.25f -> height * 0.5f
                            0.75f -> height * 0.15f
                            0.9f -> height * 0.6f
                            else -> height * 0.5f
                        }

                        // Draw circle at beat point
                        drawCircle(
                            color = Color(0xFF7C3AED),
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )

                        // Draw beat label
                        // Android-specific text rendering - commented for desktop compatibility
                        // drawContext.canvas.nativeCanvas.apply {
                        //     drawText(
                        //         label,
                        //         x,
                        //         y + 30f,
                        //         android.graphics.Paint().apply {
                        //             color = android.graphics.Color.WHITE
                        //             textSize = 12.sp.toPx()
                        //             textAlign = android.graphics.Paint.Align.CENTER
                        //         }
                        //     )
                        // }
                    }

                    // Draw tension indicators for scenes
                    scenes.forEachIndexed { index, scene ->
                        val x = padding + (width - 2 * padding) * (index.toFloat() / scenes.size)
                        val tensionLevel = when (scene.narrativeFunction) {
                            NarrativeFunction.CLIMAX -> 0.9f
                            NarrativeFunction.RISING_ACTION -> 0.7f
                            NarrativeFunction.FALLING_ACTION -> 0.5f
                            NarrativeFunction.RESOLUTION -> 0.3f
                            else -> 0.4f
                        }
                        val y = height - (height * tensionLevel)

                        // Draw small indicator
                        drawCircle(
                            color = getEmotionalToneColor(scene.emotionalTone ?: EmotionalTone.NEUTRAL),
                            radius = 4.dp.toPx(),
                            center = Offset(x, y),
                            alpha = 0.8f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NarrativeFunction.values().take(4).forEach { function ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (function) {
                                        NarrativeFunction.OPENING -> Color(0xFF66BB6A)
                                        NarrativeFunction.RISING_ACTION -> Color(0xFFFFB74D)
                                        NarrativeFunction.CLIMAX -> Color(0xFFE91E63)
                                        NarrativeFunction.RESOLUTION -> Color(0xFF5C6BC0)
                                        else -> Color.Gray
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = function.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternCard(
    pattern: StoryPattern,
    isApplied: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isApplied)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isApplied)
            BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isApplied)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isApplied) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Applied",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pattern.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = pattern.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2,
                fontSize = 11.sp
            )
        }
    }
}