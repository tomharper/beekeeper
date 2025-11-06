// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/EpisodeBlueprintDetailScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.EpisodeBlueprintV3
import com.beekeeper.app.domain.model.ActStructureV3
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeBlueprintDetailScreen(
    blueprint: EpisodeBlueprintV3,
    episodeNumber: Int,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val theme by ThemeManager.currentTheme.collectAsState()

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Episode ${episodeNumber}: ${blueprint.episodeMetadata.title}",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = theme.colors.primary
                        )
                    }
                }
            )
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Production Metadata (V3)
            item {
                DetailSection(
                    title = "Production Info",
                    icon = Icons.Default.Info,
                    theme = theme
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Archetype: ${blueprint.archetype}",
                            fontSize = 14.sp,
                            color = theme.colors.textPrimary
                        )
                        Text(
                            text = "Structure: ${blueprint.structurePattern}",
                            fontSize = 14.sp,
                            color = theme.colors.textPrimary
                        )
                        Text(
                            text = "Duration: ${blueprint.duration}",
                            fontSize = 14.sp,
                            color = theme.colors.textPrimary
                        )
                        Text(
                            text = "Pacing: ${blueprint.pacingStyle}",
                            fontSize = 14.sp,
                            color = theme.colors.textPrimary
                        )
                    }
                }
            }

            // Genres & Tones (V3)
            item {
                DetailSection(
                    title = "Genres & Tones",
                    icon = Icons.Default.Category,
                    theme = theme
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            blueprint.genres.forEach { genre ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = theme.colors.primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = genre,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 14.sp,
                                        color = theme.colors.primary
                                    )
                                }
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            blueprint.tones.forEach { tone ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = theme.colors.secondary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = tone,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 14.sp,
                                        color = theme.colors.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Logline
            item {
                DetailSection(
                    title = "Logline",
                    icon = Icons.Default.Star,
                    theme = theme
                ) {
                    Text(
                        text = blueprint.episodeMetadata.logline,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = theme.colors.textPrimary
                    )
                }
            }

            // Synopsis
            blueprint.episodeMetadata.synopsis?.let { synopsis ->
                item {
                    DetailSection(
                        title = "Synopsis",
                        icon = Icons.Default.Description,
                        theme = theme
                    ) {
                        Text(
                            text = synopsis,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = theme.colors.textPrimary
                        )
                    }
                }
            }

            // Narrative Function (V3)
            blueprint.episodeMetadata.narrativeFunction?.let { narrativeFunction ->
                item {
                    DetailSection(
                        title = "Narrative Function",
                        icon = Icons.Default.Psychology,
                        theme = theme
                    ) {
                        Text(
                            text = narrativeFunction,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = theme.colors.textPrimary
                        )
                    }
                }
            }

            // Themes (V3)
            if (blueprint.episodeMetadata.themes.isNotEmpty()) {
                item {
                    DetailSection(
                        title = "Themes",
                        icon = Icons.Default.Lightbulb,
                        theme = theme
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            blueprint.episodeMetadata.themes.forEach { themeText ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = theme.colors.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = themeText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 14.sp,
                                        color = theme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // World Building (V3)
            blueprint.episodeMetadata.worldBuilding?.let { worldBuilding ->
                item {
                    DetailSection(
                        title = "World Building",
                        icon = Icons.Default.Public,
                        theme = theme
                    ) {
                        Text(
                            text = worldBuilding,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = theme.colors.textPrimary
                        )
                    }
                }
            }

            // Characters Present (V3)
            if (blueprint.charactersPresent.isNotEmpty()) {
                item {
                    DetailSection(
                        title = "Characters Present",
                        icon = Icons.Default.Person,
                        theme = theme
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            blueprint.charactersPresent.forEach { charId ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = theme.colors.surface
                                ) {
                                    Text(
                                        text = charId,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 14.sp,
                                        color = theme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Act Structure (V3)
            blueprint.actStructure.forEachIndexed { index, act ->
                item {
                    ActSection(act = act, theme = theme)
                }
            }

            // Essential Events
            if (blueprint.essentialEvents.isNotEmpty()) {
                item {
                    DetailSection(
                        title = "Essential Events",
                        icon = Icons.Default.Event,
                        theme = theme
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            blueprint.essentialEvents.forEach { event ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = theme.colors.background.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = event.event,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = theme.colors.textPrimary
                                        )
                                        Text(
                                            text = "Why: ${event.whyEssential}",
                                            fontSize = 12.sp,
                                            color = theme.colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActSection(
    act: ActStructureV3,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Act Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = act.actName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
            }

            // Act Description
            Text(
                text = act.description,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = theme.colors.textPrimary
            )

            // Key Moments
            if (act.keyMoments.isNotEmpty()) {
                Text(
                    text = "Key Moments:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textSecondary
                )
                act.keyMoments.forEach { moment ->
                    Text(
                        text = "• $moment",
                        fontSize = 13.sp,
                        color = theme.colors.textPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Character Beats (V3)
            if (act.characterBeats.isNotEmpty()) {
                Text(
                    text = "Character Beats:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textSecondary
                )
                act.characterBeats.forEach { beat ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = theme.colors.background.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = beat.moment,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.colors.textPrimary
                            )
                            beat.arcReference?.let {
                                Text(
                                    text = "Arc: $it",
                                    fontSize = 11.sp,
                                    color = theme.colors.textSecondary
                                )
                            }
                            Text(
                                text = "Purpose: ${beat.purpose}",
                                fontSize = 11.sp,
                                color = theme.colors.textSecondary
                            )
                            beat.emotionalTone?.let {
                                Text(
                                    text = "Tone: $it",
                                    fontSize = 11.sp,
                                    color = theme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Revelations
            if (act.revelations.isNotEmpty()) {
                Text(
                    text = "Revelations:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textSecondary
                )
                act.revelations.forEach { revelation ->
                    Text(
                        text = "• ${revelation.information} (${revelation.impact})",
                        fontSize = 13.sp,
                        color = theme.colors.textPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Planted Seeds
            if (act.plantedSeeds.isNotEmpty()) {
                Text(
                    text = "Planted Seeds:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textSecondary
                )
                act.plantedSeeds.forEach { seed ->
                    Text(
                        text = "• ${seed.seed} (${seed.type})",
                        fontSize = 13.sp,
                        color = theme.colors.textPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: com.beekeeper.app.presentation.theme.AppTheme,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
            }
            content()
        }
    }
}
