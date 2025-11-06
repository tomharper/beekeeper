// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/ProjectBibleEditScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager

enum class BibleSection {
    WORLD_LOGIC,
    PLOT_LOGIC,
    THEMATIC_STRUCTURE,
    PRODUCTION_GUIDELINES,
    EPISODE_BLUEPRINTS,
    PROPS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectBibleEditScreen(
    projectBible: ProjectBible?,
    onNavigateBack: () -> Unit,
    onFieldEdit: (String, String) -> Unit,
    onSave: (ProjectBible) -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    var selectedSection by remember { mutableStateOf(BibleSection.WORLD_LOGIC) }

    if (projectBible == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No project bible available", color = theme.colors.textSecondary)
        }
        return
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Edit Story Bible",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Section tabs
            ScrollableTabRow(
                selectedTabIndex = selectedSection.ordinal,
                containerColor = theme.colors.surface,
                contentColor = theme.colors.textPrimary
            ) {
                Tab(
                    selected = selectedSection == BibleSection.WORLD_LOGIC,
                    onClick = { selectedSection = BibleSection.WORLD_LOGIC },
                    text = { Text("World Logic") }
                )
                Tab(
                    selected = selectedSection == BibleSection.PLOT_LOGIC,
                    onClick = { selectedSection = BibleSection.PLOT_LOGIC },
                    text = { Text("Plot Logic") }
                )
                Tab(
                    selected = selectedSection == BibleSection.THEMATIC_STRUCTURE,
                    onClick = { selectedSection = BibleSection.THEMATIC_STRUCTURE },
                    text = { Text("Themes") }
                )
                Tab(
                    selected = selectedSection == BibleSection.PRODUCTION_GUIDELINES,
                    onClick = { selectedSection = BibleSection.PRODUCTION_GUIDELINES },
                    text = { Text("Production") }
                )
                Tab(
                    selected = selectedSection == BibleSection.EPISODE_BLUEPRINTS,
                    onClick = { selectedSection = BibleSection.EPISODE_BLUEPRINTS },
                    text = { Text("Episodes") }
                )
                Tab(
                    selected = selectedSection == BibleSection.PROPS,
                    onClick = { selectedSection = BibleSection.PROPS },
                    text = { Text("Props") }
                )
            }

            // Content based on selected section
            when (selectedSection) {
                BibleSection.WORLD_LOGIC -> {
                    projectBible.worldLogic?.let { worldLogic ->
                        WorldLogicEditor(
                            worldLogic = worldLogic,
                            onFieldClick = onFieldEdit,
                            theme = theme
                        )
                    } ?: Text("World Logic not yet defined", color = theme.colors.textSecondary)
                }
                BibleSection.PLOT_LOGIC -> {
                    projectBible.plotLogic?.let { plotLogic ->
                        PlotLogicEditor(
                            plotLogic = plotLogic,
                            onFieldClick = onFieldEdit,
                            theme = theme
                        )
                    } ?: Text("Plot Logic not yet defined", color = theme.colors.textSecondary)
                }
                BibleSection.THEMATIC_STRUCTURE -> {
                    projectBible.thematicStructure?.let { thematicStructure ->
                        ThematicStructureEditor(
                            thematicStructure = thematicStructure,
                            onFieldClick = onFieldEdit,
                            theme = theme
                        )
                    } ?: Text("Thematic Structure not yet defined", color = theme.colors.textSecondary)
                }
                BibleSection.PRODUCTION_GUIDELINES -> {
                    ProductionGuidelinesEditor(
                        productionGuidelines = projectBible.productionGuidelines,
                        onFieldClick = onFieldEdit,
                        theme = theme
                    )
                }
                BibleSection.EPISODE_BLUEPRINTS -> {
                    EpisodeBlueprintsEditor(
                        episodes = projectBible.episodeBlueprints,
                        onFieldClick = onFieldEdit,
                        theme = theme
                    )
                }
                BibleSection.PROPS -> {
                    PropsEditor(
                        props = projectBible.props,
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
private fun WorldLogicEditor(
    worldLogic: WorldLogic,
    onFieldClick: (String, String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            EditableField(
                label = "Fundamental Principle",
                value = worldLogic.fundamentalPrinciple,
                onClick = { onFieldClick("fundamentalPrinciple", worldLogic.fundamentalPrinciple) },
                theme = theme
            )
        }

        item {
            Text(
                "Divergence from Reality",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            EditableField(
                label = "What Diverges",
                value = worldLogic.divergenceFromReality.whatDiverges,
                onClick = { onFieldClick("divergence.whatDiverges", worldLogic.divergenceFromReality.whatDiverges) },
                theme = theme
            )
        }

        item {
            EditableField(
                label = "When It Diverges",
                value = worldLogic.divergenceFromReality.whenItDiverges,
                onClick = { onFieldClick("divergence.whenItDiverges", worldLogic.divergenceFromReality.whenItDiverges) },
                theme = theme
            )
        }

        item {
            EditableField(
                label = "Why",
                value = worldLogic.divergenceFromReality.why,
                onClick = { onFieldClick("divergence.why", worldLogic.divergenceFromReality.why) },
                theme = theme
            )
        }

        item {
            Text(
                "Universal Rules",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(worldLogic.universalRules) { rule ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                ),
                onClick = { onFieldClick("rule.${rule.ruleName}", rule.description) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        rule.ruleName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        rule.description,
                        fontSize = 14.sp,
                        color = theme.colors.textSecondary
                    )
                }
            }
        }

        item {
            Text(
                "Mechanics System",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            EditableField(
                label = "System Name",
                value = worldLogic.mechanicsSystem.systemName,
                onClick = { onFieldClick("mechanics.systemName", worldLogic.mechanicsSystem.systemName) },
                theme = theme
            )
        }

        item {
            EditableField(
                label = "How It Works",
                value = worldLogic.mechanicsSystem.howItWorks,
                onClick = { onFieldClick("mechanics.howItWorks", worldLogic.mechanicsSystem.howItWorks) },
                theme = theme
            )
        }
    }
}

@Composable
private fun PlotLogicEditor(
    plotLogic: PlotLogic,
    onFieldClick: (String, String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Central Engine",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )
        }

        item {
            EditableField(
                label = "Main Conflict",
                value = plotLogic.centralEngine.mainConflict,
                onClick = { onFieldClick("central.mainConflict", plotLogic.centralEngine.mainConflict) },
                theme = theme
            )
        }

        item {
            EditableField(
                label = "Why Unresolvable",
                value = plotLogic.centralEngine.whyUnresolvable,
                onClick = { onFieldClick("central.whyUnresolvable", plotLogic.centralEngine.whyUnresolvable) },
                theme = theme
            )
        }

        item {
            EditableField(
                label = "Point of No Return",
                value = plotLogic.centralEngine.pointOfNoReturn,
                onClick = { onFieldClick("central.pointOfNoReturn", plotLogic.centralEngine.pointOfNoReturn) },
                theme = theme
            )
        }

        item {
            EditableField(
                label = "Only Possible Resolution",
                value = plotLogic.centralEngine.onlyPossibleResolution,
                onClick = { onFieldClick("central.resolution", plotLogic.centralEngine.onlyPossibleResolution) },
                theme = theme
            )
        }
    }
}

@Composable
private fun ThematicStructureEditor(
    thematicStructure: ThematicStructure,
    onFieldClick: (String, String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            EditableField(
                label = "Central Thesis",
                value = thematicStructure.centralThesis,
                onClick = { onFieldClick("theme.thesis", thematicStructure.centralThesis) },
                theme = theme
            )
        }

        item {
            EditableField(
                label = "Antithesis",
                value = thematicStructure.antithesis,
                onClick = { onFieldClick("theme.antithesis", thematicStructure.antithesis) },
                theme = theme
            )
        }

        item {
            EditableField(
                label = "Synthesis",
                value = thematicStructure.synthesis ?: "",
                onClick = { onFieldClick("theme.synthesis", thematicStructure.synthesis ?: "") },
                theme = theme
            )
        }

        item {
            Text(
                "Philosophical Questions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(thematicStructure.philosophicalQuestions) { question ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                ),
                onClick = { onFieldClick("question.${question.question}", question.seriesAnswer ?: "") }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        question.question,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Answer: ${question.seriesAnswer ?: "Not specified"}",
                        fontSize = 14.sp,
                        color = theme.colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductionGuidelinesEditor(
    productionGuidelines: ProductionGuidelines?,
    onFieldClick: (String, String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (productionGuidelines == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
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
                            "No production guidelines yet",
                            fontSize = 16.sp,
                            color = theme.colors.textSecondary
                        )
                        Text(
                            "Add visual and audio style guidelines for AI generation",
                            fontSize = 14.sp,
                            color = theme.colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            return@LazyColumn
        }

        item {
            Text(
                "Visual Guidelines",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            EditableField(
                label = "Visual Style",
                value = productionGuidelines.visualStyle,
                onClick = { onFieldClick("production.visualStyle", productionGuidelines.visualStyle) },
                theme = theme
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                ),
                onClick = { onFieldClick("production.colorPalette", productionGuidelines.colorPalette.joinToString(", ")) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        "Color Palette",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        productionGuidelines.colorPalette.forEach { color ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.colors.primary.copy(alpha = 0.1f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    color,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 13.sp,
                                    color = theme.colors.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                ),
                onClick = { onFieldClick("production.keyVisualMotifs", productionGuidelines.keyVisualMotifs.joinToString(", ")) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        "Key Visual Motifs",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        productionGuidelines.keyVisualMotifs.forEach { motif ->
                            Text(
                                "• $motif",
                                fontSize = 13.sp,
                                color = theme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Audio Guidelines",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            EditableField(
                label = "Audio Style",
                value = productionGuidelines.audioStyle,
                onClick = { onFieldClick("production.audioStyle", productionGuidelines.audioStyle) },
                theme = theme
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                ),
                onClick = { onFieldClick("production.keyAudioMotifs", productionGuidelines.keyAudioMotifs.joinToString(", ")) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        "Key Audio Motifs",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        productionGuidelines.keyAudioMotifs.forEach { motif ->
                            Text(
                                "• $motif",
                                fontSize = 13.sp,
                                color = theme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        productionGuidelines.productionNotes?.let { notes ->
            item {
                Text(
                    "Additional Notes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                EditableField(
                    label = "Production Notes",
                    value = notes,
                    onClick = { onFieldClick("production.notes", notes) },
                    theme = theme
                )
            }
        }
    }
}

@Composable
private fun EpisodeBlueprintsEditor(
    episodes: List<EpisodeBlueprint>,
    onFieldClick: (String, String) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(episodes) { episode ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Episode ${episode.episodeNumber}: ${episode.title}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.primary
                    )

                    EditableField(
                        label = "Logline",
                        value = episode.logline,
                        onClick = { onFieldClick("ep${episode.episodeNumber}.logline", episode.logline) },
                        theme = theme,
                        compact = true
                    )

                    EditableField(
                        label = "Synopsis",
                        value = episode.synopsis,
                        onClick = { onFieldClick("ep${episode.episodeNumber}.synopsis", episode.synopsis) },
                        theme = theme,
                        compact = true
                    )

                    EditableField(
                        label = "Narrative Function",
                        value = episode.narrativeFunction,
                        onClick = { onFieldClick("ep${episode.episodeNumber}.function", episode.narrativeFunction) },
                        theme = theme,
                        compact = true
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onClick: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 8.dp else 12.dp)
        ) {
            Text(
                label,
                fontSize = if (compact) 12.sp else 14.sp,
                fontWeight = FontWeight.Medium,
                color = theme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value.take(if (compact) 100 else 200) + if (value.length > (if (compact) 100 else 200)) "..." else "",
                fontSize = if (compact) 13.sp else 14.sp,
                color = theme.colors.textPrimary
            )
        }
    }
}

@Composable
private fun PropsEditor(
    props: List<Prop>,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    var selectedProp by remember { mutableStateOf<Prop?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (props.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = theme.colors.textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "No props defined yet",
                            fontSize = 16.sp,
                            color = theme.colors.textSecondary
                        )
                        Text(
                            "Props will appear here once added to the project bible",
                            fontSize = 14.sp,
                            color = theme.colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(props) { prop ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colors.surface
                    ),
                    onClick = {
                        selectedProp = prop
                        showDetailDialog = true
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    prop.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    // Category chip
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = theme.colors.primary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            prop.category.toString(),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            color = theme.colors.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Importance chip
                                    prop.importance?.let { importance ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = theme.colors.secondary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                importance,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                color = theme.colors.secondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    // Size chip
                                    prop.size?.let { size ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = theme.colors.textSecondary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                size,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                color = theme.colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "View details",
                                tint = theme.colors.textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Description (truncated)
                        prop.description?.let { description ->
                            Text(
                                description.take(120) + if (description.length > 120) "..." else "",
                                fontSize = 13.sp,
                                color = theme.colors.textSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Prop Detail Dialog
    if (showDetailDialog && selectedProp != null) {
        PropDetailDialog(
            prop = selectedProp!!,
            theme = theme,
            onDismiss = {
                showDetailDialog = false
                selectedProp = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropDetailDialog(
    prop: Prop,
    theme: com.beekeeper.app.presentation.theme.AppTheme,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = theme.colors.background,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        prop.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Basic Info
                    Text(
                        "Basic Information",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.colors.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                prop.category.toString(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = theme.colors.primary
                            )
                        }
                        prop.importance?.let {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = theme.colors.secondary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "Importance: $it",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    color = theme.colors.secondary
                                )
                            }
                        }
                        prop.size?.let {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = theme.colors.textSecondary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "Size: $it",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    color = theme.colors.textSecondary
                                )
                            }
                        }
                        prop.interactionType?.let {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = theme.colors.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    it,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    color = theme.colors.primary
                                )
                            }
                        }
                    }
                    prop.description?.let { description ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            description,
                            fontSize = 13.sp,
                            color = theme.colors.textSecondary
                        )
                    }

                    // Visual Properties
                    if (prop.texture != null || prop.color != null || prop.reflectivity != null || prop.transparency != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = theme.colors.primary.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    "Visual Properties",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                prop.texture?.let {
                                    Text("Texture: $it", fontSize = 12.sp, color = theme.colors.textSecondary)
                                }
                                prop.color?.let {
                                    Text("Color: $it", fontSize = 12.sp, color = theme.colors.textSecondary)
                                }
                                prop.reflectivity?.let {
                                    Text("Reflectivity: ${(it * 100).toInt()}%", fontSize = 12.sp, color = theme.colors.textSecondary)
                                }
                                prop.transparency?.let {
                                    Text("Transparency: ${(it * 100).toInt()}%", fontSize = 12.sp, color = theme.colors.textSecondary)
                                }
                            }
                        }
                    }

                    // Continuity Notes
                    prop.continuityNotes?.let { notes ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = theme.colors.secondary.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    "Continuity Notes",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    notes,
                                    fontSize = 12.sp,
                                    color = theme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // Scene References
                    if (prop.sceneIds.isNotEmpty()) {
                        Text(
                            "Appears in Scenes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            prop.sceneIds.forEach { sceneId ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = theme.colors.surface
                                ) {
                                    Text(
                                        sceneId,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        color = theme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Character Association
                    prop.characterId?.let { charId ->
                        Text(
                            "Associated Character",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.colors.secondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                charId,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = theme.colors.secondary
                            )
                        }
                    }

                    // ID
                    Text(
                        "ID: ${prop.id}",
                        fontSize = 11.sp,
                        color = theme.colors.textSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

