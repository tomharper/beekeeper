package com.beekeeper.app.ui.screens.inspections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.ui.viewmodel.InspectionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionsScreen(
    viewModel: InspectionsViewModel,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inspections") },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Navigate to create inspection screen */ },
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Inspection")
            }
        },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFD700))
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "An error occurred",
                            color = Color(0xFFFF6B6B)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadInspections() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            uiState.inspections.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No inspections found",
                        color = Color(0xFF888888),
                        fontSize = 16.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.inspections, key = { it.id }) { inspection ->
                        InspectionCard(
                            inspection = inspection,
                            onDeleteClick = { viewModel.deleteInspection(inspection.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InspectionCard(
    inspection: Inspection,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatInspectionDate(inspection.inspectionDate),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // More options menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color(0xFF888888)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Health status badge
            Surface(
                color = getHealthStatusBackgroundColor(inspection.healthStatus),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = getHealthStatusIcon(inspection.healthStatus),
                        contentDescription = null,
                        tint = getHealthStatusColor(inspection.healthStatus),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = inspection.healthStatus.name.replace("_", " "),
                        color = getHealthStatusColor(inspection.healthStatus),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Observation grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ObservationItem(
                        icon = Icons.Default.Star,
                        label = "Queen",
                        value = if (inspection.queenSeen) "Seen" else "Not Seen",
                        color = if (inspection.queenSeen) Color(0xFF4CAF50) else Color(0xFF888888),
                        modifier = Modifier.weight(1f)
                    )

                    ObservationItem(
                        icon = Icons.Default.Group,
                        label = "Population",
                        value = inspection.population.name.replace("_", " "),
                        color = getPopulationColor(inspection.population),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ObservationItem(
                        icon = Icons.Default.FavoriteBorder,
                        label = "Brood",
                        value = inspection.broodPattern.name.replace("_", " "),
                        color = getBroodPatternColor(inspection.broodPattern),
                        modifier = Modifier.weight(1f)
                    )

                    ObservationItem(
                        icon = Icons.Default.MoodBad,
                        label = "Temperament",
                        value = inspection.temperament.name.replace("_", " "),
                        color = getTemperamentColor(inspection.temperament),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ObservationItem(
                        icon = Icons.Default.LocalDining,
                        label = "Honey",
                        value = inspection.honeyStores.name,
                        color = getResourceLevelColor(inspection.honeyStores),
                        modifier = Modifier.weight(1f)
                    )

                    ObservationItem(
                        icon = Icons.Default.LocalFlorist,
                        label = "Pollen",
                        value = inspection.pollenStores.name,
                        color = getResourceLevelColor(inspection.pollenStores),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pest/Disease warnings
            if (inspection.varroaMitesDetected || inspection.otherPestsDetected || inspection.diseaseDetected) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFF3D1F1F),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            if (inspection.varroaMitesDetected) {
                                Text(
                                    text = "Varroa mites detected",
                                    color = Color(0xFFFF6B6B),
                                    fontSize = 12.sp
                                )
                            }
                            if (inspection.otherPestsDetected) {
                                Text(
                                    text = "Other pests detected",
                                    color = Color(0xFFFF6B6B),
                                    fontSize = 12.sp
                                )
                            }
                            if (inspection.diseaseDetected) {
                                Text(
                                    text = "Disease detected",
                                    color = Color(0xFFFF6B6B),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Notes
            inspection.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Notes:",
                        color = Color(0xFF888888),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notes,
                        color = Color(0xFFCCCCCC),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ObservationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF2A2A2A),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    color = Color(0xFF888888),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun getHealthStatusColor(status: InspectionHealthStatus): Color {
    return when (status) {
        InspectionHealthStatus.EXCELLENT -> Color(0xFF4CAF50)
        InspectionHealthStatus.HEALTHY -> Color(0xFF8BC34A)
        InspectionHealthStatus.FAIR -> Color(0xFFFFD700)
        InspectionHealthStatus.CONCERNING -> Color(0xFFFF9F40)
        InspectionHealthStatus.CRITICAL -> Color(0xFFFF6B6B)
    }
}

fun getHealthStatusBackgroundColor(status: InspectionHealthStatus): Color {
    return when (status) {
        InspectionHealthStatus.EXCELLENT -> Color(0xFF1B3D1F)
        InspectionHealthStatus.HEALTHY -> Color(0xFF253D1F)
        InspectionHealthStatus.FAIR -> Color(0xFF3D3A1F)
        InspectionHealthStatus.CONCERNING -> Color(0xFF3D2F1F)
        InspectionHealthStatus.CRITICAL -> Color(0xFF3D1F1F)
    }
}

fun getHealthStatusIcon(status: InspectionHealthStatus): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        InspectionHealthStatus.EXCELLENT, InspectionHealthStatus.HEALTHY -> Icons.Default.CheckCircle
        InspectionHealthStatus.FAIR -> Icons.Default.Info
        InspectionHealthStatus.CONCERNING, InspectionHealthStatus.CRITICAL -> Icons.Default.Warning
    }
}

fun getPopulationColor(population: ColonyPopulation): Color {
    return when (population) {
        ColonyPopulation.VERY_STRONG, ColonyPopulation.STRONG -> Color(0xFF4CAF50)
        ColonyPopulation.MEDIUM -> Color(0xFFFFD700)
        ColonyPopulation.WEAK, ColonyPopulation.VERY_WEAK -> Color(0xFFFF6B6B)
    }
}

fun getBroodPatternColor(pattern: BroodPattern): Color {
    return when (pattern) {
        BroodPattern.EXCELLENT, BroodPattern.GOOD -> Color(0xFF4CAF50)
        BroodPattern.FAIR -> Color(0xFFFFD700)
        BroodPattern.POOR, BroodPattern.SPOTTY -> Color(0xFFFF6B6B)
    }
}

fun getTemperamentColor(temperament: ColonyTemperament): Color {
    return when (temperament) {
        ColonyTemperament.VERY_CALM, ColonyTemperament.CALM -> Color(0xFF4CAF50)
        ColonyTemperament.MODERATE -> Color(0xFFFFD700)
        ColonyTemperament.DEFENSIVE, ColonyTemperament.VERY_DEFENSIVE, ColonyTemperament.AGGRESSIVE -> Color(0xFFFF6B6B)
    }
}

fun getResourceLevelColor(level: ResourceLevel): Color {
    return when (level) {
        ResourceLevel.FULL, ResourceLevel.HIGH -> Color(0xFF4CAF50)
        ResourceLevel.MEDIUM -> Color(0xFFFFD700)
        ResourceLevel.LOW, ResourceLevel.EMPTY -> Color(0xFFFF6B6B)
    }
}

fun formatInspectionDate(date: kotlinx.datetime.LocalDateTime): String {
    return "${date.monthNumber}/${date.dayOfMonth}/${date.year}"
}
