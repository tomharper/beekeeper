package com.beekeeper.app.ui.screens.hive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beekeeper.app.data.MockDataRepository
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveDetailsScreen(
    hiveId: String,
    onBackClick: () -> Unit
) {
    val hive = MockDataRepository.getHiveById(hiveId)
    val recommendations = MockDataRepository.getRecommendationsForHive(hiveId)
    var selectedTab by remember { mutableStateOf(0) }

    if (hive == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Hive not found", color = TextPrimary)
        }
        return
    }

    val apiary = MockDataRepository.getApiaryById(hive.apiaryId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            hive.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            apiary?.name ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeekeeperGreenDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = BeekeeperGold,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BeekeeperGreenDark,
                contentColor = BeekeeperGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BeekeeperGold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview") },
                    selectedContentColor = BeekeeperGold,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Logbook") },
                    selectedContentColor = BeekeeperGold,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Photos") },
                    selectedContentColor = BeekeeperGold,
                    unselectedContentColor = TextSecondary
                )
            }

            // Content
            when (selectedTab) {
                0 -> OverviewTab(hive = hive, recommendations = recommendations)
                1 -> LogbookTab()
                2 -> PhotosTab()
            }
        }
    }
}

@Composable
fun OverviewTab(
    hive: Hive,
    recommendations: List<Recommendation>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current Status Section
        Text(
            "Current Status",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CheckCircle,
                label = "Colony Strength",
                value = hive.colonyStrength.name.lowercase().replaceFirstChar { it.uppercase() },
                color = when (hive.colonyStrength) {
                    ColonyStrength.STRONG -> HealthyGreen
                    ColonyStrength.MODERATE -> WarningOrange
                    ColonyStrength.WEAK -> AlertRed
                }
            )
            StatusCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Favorite,
                label = "Queen Status",
                value = hive.queenStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                color = when (hive.queenStatus) {
                    QueenStatus.LAYING -> HealthyGreen
                    QueenStatus.NOT_LAYING -> WarningOrange
                    QueenStatus.MISSING -> AlertRed
                    QueenStatus.UNKNOWN -> TextSecondary
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Face,
                label = "Temperament",
                value = hive.temperament.name.lowercase().replaceFirstChar { it.uppercase() },
                color = when (hive.temperament) {
                    Temperament.CALM -> HealthyGreen
                    Temperament.MODERATE -> WarningOrange
                    Temperament.DEFENSIVE -> AlertRed
                }
            )
            StatusCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Star,
                label = "Honey Stores",
                value = hive.honeyStores.name.lowercase().replaceFirstChar { it.uppercase() },
                color = when (hive.honeyStores) {
                    HoneyStores.FULL -> HealthyGreen
                    HoneyStores.ADEQUATE -> HealthyGreen
                    HoneyStores.LOW -> WarningOrange
                    HoneyStores.EMPTY -> AlertRed
                }
            )
        }

        // AI Recommendations Section
        if (recommendations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "AI Recommendations",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            recommendations.forEach { recommendation ->
                RecommendationCard(recommendation = recommendation)
            }
        }
    }
}

@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TextSecondary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun RecommendationCard(recommendation: Recommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (recommendation.type) {
                RecommendationType.POSITIVE -> HealthyGreen.copy(alpha = 0.15f)
                RecommendationType.WARNING -> WarningOrange.copy(alpha = 0.15f)
                RecommendationType.ACTION_REQUIRED -> AlertRed.copy(alpha = 0.15f)
                RecommendationType.INFO -> BeekeeperGold.copy(alpha = 0.15f)
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(
                when (recommendation.type) {
                    RecommendationType.POSITIVE -> HealthyGreen.copy(alpha = 0.5f)
                    RecommendationType.WARNING -> WarningOrange.copy(alpha = 0.5f)
                    RecommendationType.ACTION_REQUIRED -> AlertRed.copy(alpha = 0.5f)
                    RecommendationType.INFO -> BeekeeperGold.copy(alpha = 0.5f)
                }
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (recommendation.type) {
                    RecommendationType.POSITIVE -> Icons.Default.CheckCircle
                    RecommendationType.WARNING -> Icons.Default.Warning
                    RecommendationType.ACTION_REQUIRED -> Icons.Default.Warning
                    RecommendationType.INFO -> Icons.Default.Info
                },
                contentDescription = null,
                tint = when (recommendation.type) {
                    RecommendationType.POSITIVE -> HealthyGreen
                    RecommendationType.WARNING -> WarningOrange
                    RecommendationType.ACTION_REQUIRED -> AlertRed
                    RecommendationType.INFO -> BeekeeperGold
                },
                modifier = Modifier.size(24.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun LogbookTab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = TextSecondary
            )
            Text(
                "No logbook entries yet",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun PhotosTab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = TextSecondary
            )
            Text(
                "No photos yet",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
    }
}
