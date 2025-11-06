// shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/CharacterAnalysisScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import com.beekeeper.app.domain.model.CharacterProfile
import com.beekeeper.app.domain.model.CharacterRelationship
import com.beekeeper.app.presentation.viewmodels.CharacterAnalysisViewModel

/**
 * Character Analysis Screen - Shows character grid with AI analysis
 * This is the default screen when entering Character Hub
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterAnalysisScreen(
    projectId: String,
    viewModel: CharacterAnalysisViewModel,
    onCharacterSelect: (CharacterProfile) -> Unit,
    onAddCharacter: () -> Unit,
    onExtractFromScript: () -> Unit,
    onViewDetails: (CharacterProfile) -> Unit,
    onAssignAvatar: (CharacterProfile) -> Unit,
    onNavigateBack: () -> Unit
) {
    val characters by viewModel.characters.collectAsState()
    val selectedCharacter by viewModel.selectedCharacter.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisResults by viewModel.analysisResults.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        // Left Panel - Character Grid
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Character Grid
            if (characters.isEmpty()) {
                EmptyCharacterState(
                    onAddCharacter = onAddCharacter,
                    onExtractFromScript = onExtractFromScript
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(characters) { character ->
                        CharacterAnalysisCard(
                            character = character,
                            isSelected = selectedCharacter?.id == character.id,
                            onClick = {
                                onCharacterSelect(character)
                                viewModel.selectCharacter(character)
                            }
                        )
                    }
                }
            }
        }

        // Right Panel - Analysis Details
        AnimatedVisibility(
            visible = selectedCharacter != null,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .width(400.dp)
                    .fillMaxHeight()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                selectedCharacter?.let { character ->
                    CharacterAnalysisPanel(
                        character = character,
                        analysisResults = analysisResults,
                        isAnalyzing = isAnalyzing,
                        onAnalyze = { viewModel.analyzeCharacter(character) },
                        onViewDetails = { onViewDetails(character) },
                        onAssignAvatar = { onAssignAvatar(character) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterAnalysisCard(
    character: CharacterProfile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFF7C3AED).copy(alpha = 0.2f)
            else Color(0xFF2A2A3E)
        ),
        border = if (isSelected)
            BorderStroke(2.dp, Color(0xFF7C3AED))
        else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF7C3AED).copy(alpha = 0.5f),
                                Color(0xFF5B2AAF).copy(alpha = 0.5f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (character.assignedAvatarId != null) {
                    // TODO: Show actual avatar image
                    Icon(
                        Icons.Filled.Face,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Text(
                        text = character.name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Name and Role
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = character.archetype,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    value = "${character.dialogueCount}"
                )
                StatChip(
                    icon = Icons.Outlined.Movie,
                    value = "${character.relationships.size}"  // Using connections for now
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF7C3AED),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun CharacterAnalysisPanel(
    character: CharacterProfile,
    analysisResults: Map<String, Any>?,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit,
    onViewDetails: () -> Unit,
    onAssignAvatar: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = character.archetype,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onAssignAvatar,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF7C3AED).copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Face,
                            contentDescription = "Assign Avatar",
                            tint = Color(0xFF7C3AED)
                        )
                    }
                    IconButton(
                        onClick = onViewDetails,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF7C3AED).copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "View Details",
                            tint = Color(0xFF7C3AED)
                        )
                    }
                }
            }
        }

        // AI Analysis Section
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "AI Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF7C3AED),
                                strokeWidth = 2.dp
                            )
                        } else {
                            TextButton(
                                onClick = onAnalyze,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFF7C3AED)
                                )
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Generate")
                            }
                        }
                    }

                    if (analysisResults != null) {
                        Text(
                            text = analysisResults["insights"] as? String ?:
                            "AI analysis will provide deep insights into character motivation, arc, and personality.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    } else {
                        Text(
                            text = "Click 'Generate' to analyze this character's personality, motivations, and story arc.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Personality Traits
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Personality Traits",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    val characterTraits = character.personality?.traits ?: emptyList()
                    if (characterTraits.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            characterTraits.forEach { trait ->
                                TraitChip(trait.name)
                            }
                        }
                    } else {
                        Text(
                            "No traits defined yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Screen Presence
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Screen Presence",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        PresenceMetric(
                            label = "Screen Time",
                            value = "${(character.screenTime * 100).toInt()}%",
                            icon = Icons.Outlined.Timer
                        )
                        PresenceMetric(
                            label = "Dialogue",
                            value = character.dialogueCount.toString(),
                            icon = Icons.Outlined.ChatBubbleOutline
                        )
                        PresenceMetric(
                            label = "Connections",
                            value = character.relationships.size.toString(),
                            icon = Icons.Outlined.People
                        )
                    }
                }
            }
        }

        // Relationships
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Key Relationships",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    if (character.relationships.isNotEmpty()) {
                        character.relationships.take(3).forEach { relationship ->
                            RelationshipItem(relationship)
                        }
                    } else {
                        Text(
                            "No relationships defined yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TraitChip(trait: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF7C3AED).copy(alpha = 0.2f)
    ) {
        Text(
            text = trait,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7C3AED)
        )
    }
}

@Composable
private fun PresenceMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF7C3AED),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun RelationshipItem(relationship: CharacterRelationship) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7C3AED).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = relationship.targetCharacterName.take(1),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Column {
                Text(
                    text = relationship.targetCharacterName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = relationship.relationshipType.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun EmptyCharacterState(
    onAddCharacter: () -> Unit,
    onExtractFromScript: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.People,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "No Characters Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Text(
            "Add characters manually or extract them from your script",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onExtractFromScript,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF7C3AED)
                ),
                border = BorderStroke(1.dp, Color(0xFF7C3AED))
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Extract from Script")
            }

            Button(
                onClick = onAddCharacter,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED)
                )
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Manually")
            }
        }
    }
}

// Helper Composable for FlowRow layout
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val rows = mutableListOf<MutableList<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentRowWidth = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)
            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                if (currentRow.isNotEmpty()) {
                    rows.add(currentRow)
                    currentRow = mutableListOf()
                    currentRowWidth = 0
                }
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height = rows.sumOf { row -> row.maxOf { it.height } }

        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width
                }
                y += row.maxOf { it.height }
            }
        }
    }
}