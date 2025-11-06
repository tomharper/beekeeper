// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/TutorialsScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.presentation.components.PrimaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager
import com.beekeeper.app.utils.formatNumberWithCommas
import com.beekeeper.app.utils.formatPercentage
import com.beekeeper.app.utils.formatCurrencyWithCommas

// Platform Analytics Data
data class PlatformAnalytics(
    val platform: String,
    val revenue: Float,
    val views: Long,
    val engagement: Float,
    val color: Color
)

// Content Performance Data
data class ContentPerformanceData(
    val title: String,
    val platform: String,
    val views: Long,
    val revenue: Float,
    val engagement: Float,
    val duration: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialsScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToBookmarks: () -> Unit = {},
    onTutorialSelect: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    var selectedPlatform by remember { mutableStateOf<String?>(null) }

    // Sample analytics data
    val platformAnalytics = remember {
        listOf(
            PlatformAnalytics("YouTube", 45000f, 1500000L, 8.5f, Color(0xFFFF0000)),
            PlatformAnalytics("TikTok", 35000f, 1200000L, 12.3f, Color(0xFF000000)),
            PlatformAnalytics("Instagram", 25000f, 500000L, 6.8f, Color(0xFFE4405F)),
            PlatformAnalytics("Facebook", 20000f, 300000L, 5.2f, Color(0xFF1877F2))
        )
    }

    val contentPerformance = remember {
        listOf(
            ContentPerformanceData("Episode 1: The Beginning", "YouTube", 250000L, 5000f, 9.2f, "10:30"),
            ContentPerformanceData("Short: Behind the Scenes", "TikTok", 800000L, 8000f, 15.5f, "0:30"),
            ContentPerformanceData("Character Intro: Hero", "Instagram", 150000L, 3000f, 7.1f, "1:00"),
            ContentPerformanceData("Full Episode 2", "YouTube", 220000L, 4500f, 8.8f, "12:15")
        )
    }

    val totalRevenue = platformAnalytics.sumOf { it.revenue.toDouble() }.toFloat()
    val totalViews = platformAnalytics.sumOf { it.views }
    val avgEngagement = platformAnalytics.map { it.engagement }.average().toFloat()

    Scaffold(
        topBar = {
            PrimaryTopBar(
                title = "Analytics",
                subtitle = "Track your content performance across platforms",
                onNotificationClick = onNavigateToNotifications,
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Export",
                            tint = theme.colors.textPrimary
                        )
                    }
                    IconButton(onClick = onNavigateToBookmarks) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = theme.colors.textPrimary
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
            // Overview Stats
            item {
                AnalyticsOverviewSection(
                    totalRevenue = totalRevenue,
                    totalViews = totalViews,
                    avgEngagement = avgEngagement,
                    theme = theme
                )
            }

            // Revenue Distribution Pie Chart
            item {
                RevenueDistributionSection(
                    platformAnalytics = platformAnalytics,
                    theme = theme
                )
            }

            // Platform Performance
            item {
                PlatformPerformanceSection(
                    platformAnalytics = platformAnalytics,
                    selectedPlatform = selectedPlatform,
                    onPlatformSelect = { selectedPlatform = it },
                    theme = theme
                )
            }

            // Platform Bar Chart
            item {
                PlatformBarChartSection(
                    platformAnalytics = platformAnalytics,
                    theme = theme
                )
            }

            // Content Performance Table
            item {
                ContentPerformanceSection(
                    contentPerformance = contentPerformance,
                    theme = theme
                )
            }

            // Optimization Opportunities
            item {
                OptimizationOpportunitiesSection(
                    theme = theme
                )
            }
        }
    }
}

@Composable
private fun RevenueDistributionSection(
    platformAnalytics: List<PlatformAnalytics>,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    val totalRevenue = platformAnalytics.sumOf { it.revenue.toDouble() }.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Revenue Distribution",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            // Pie chart visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                ) {
                    var startAngle = -90f
                    platformAnalytics.forEach { platform ->
                        val sweepAngle = (platform.revenue / totalRevenue) * 360f
                        drawArc(
                            color = platform.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            // Legend
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                platformAnalytics.forEach { platform ->
                    val percentage = (platform.revenue / totalRevenue * 100).toDouble()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(platform.color)
                            )
                            Text(
                                platform.platform,
                                fontSize = 14.sp,
                                color = theme.colors.textPrimary
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "${formatPercentage(percentage, 1)}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.colors.textPrimary
                            )
                            Text(
                                "$${formatNumberWithCommas(platform.revenue.toDouble())}",
                                fontSize = 14.sp,
                                color = theme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsOverviewSection(
    totalRevenue: Float,
    totalViews: Long,
    avgEngagement: Float,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnalyticsStat(
                    label = "Total Revenue",
                    value = "$${formatNumberWithCommas(totalRevenue.toDouble())}",
                    icon = Icons.Default.AttachMoney,
                    theme = theme
                )
                AnalyticsStat(
                    label = "Total Views",
                    value = formatNumberWithCommas(totalViews.toDouble()),
                    icon = Icons.Default.Visibility,
                    theme = theme
                )
                AnalyticsStat(
                    label = "Avg Engagement",
                    value = "${formatPercentage(avgEngagement.toDouble(), 1)}%",
                    icon = Icons.Default.TrendingUp,
                    theme = theme
                )
            }
        }
    }
}

@Composable
private fun AnalyticsStat(
    label: String,
    value: String,
    icon: ImageVector,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = theme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )
        Text(
            label,
            fontSize = 12.sp,
            color = theme.colors.textSecondary
        )
    }
}

@Composable
private fun PlatformPerformanceSection(
    platformAnalytics: List<PlatformAnalytics>,
    selectedPlatform: String?,
    onPlatformSelect: (String?) -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Platform Performance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        // Platform filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedPlatform == null,
                    onClick = { onPlatformSelect(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.colors.primary,
                        selectedLabelColor = theme.colors.onPrimary
                    )
                )
            }
            items(platformAnalytics) { platform ->
                FilterChip(
                    selected = selectedPlatform == platform.platform,
                    onClick = { onPlatformSelect(platform.platform) },
                    label = { Text(platform.platform) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.colors.primary,
                        selectedLabelColor = theme.colors.onPrimary
                    )
                )
            }
        }

        // Platform cards
        platformAnalytics.forEach { platform ->
            if (selectedPlatform == null || selectedPlatform == platform.platform) {
                PlatformAnalyticsCard(platform = platform, theme = theme)
            }
        }
    }
}

@Composable
private fun PlatformAnalyticsCard(
    platform: PlatformAnalytics,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(platform.color)
                )
                Text(
                    platform.platform,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.colors.textPrimary
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "$${formatNumberWithCommas(platform.revenue.toDouble())}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "${formatNumberWithCommas(platform.views.toDouble())} views",
                        fontSize = 12.sp,
                        color = theme.colors.textSecondary
                    )
                    Text(
                        "${formatPercentage(platform.engagement.toDouble(), 1)}% eng",
                        fontSize = 12.sp,
                        color = theme.colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentPerformanceSection(
    contentPerformance: List<ContentPerformanceData>,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Top Content",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.colors.textPrimary
        )

        contentPerformance.forEach { content ->
            ContentPerformanceCard(content = content, theme = theme)
        }
    }
}

@Composable
private fun ContentPerformanceCard(
    content: ContentPerformanceData,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        content.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.colors.textPrimary,
                        maxLines = 2
                    )
                    Text(
                        content.platform,
                        fontSize = 12.sp,
                        color = theme.colors.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.colors.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        content.duration,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = theme.colors.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${formatNumberWithCommas(content.views.toDouble())} views",
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary
                )
                Text(
                    "$${formatNumberWithCommas(content.revenue.toDouble())}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.colors.textPrimary
                )
                Text(
                    "${formatPercentage(content.engagement.toDouble(), 1)}% eng",
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary
                )
            }
        }
    }
}