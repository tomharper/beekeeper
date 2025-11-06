// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/TutorialsScreenAdditions.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.OptimizationSuggestion
import com.beekeeper.app.domain.model.Priority
import com.beekeeper.app.domain.model.RecommendationType
import com.beekeeper.app.utils.formatNumberWithCommas

@Composable
fun PlatformBarChartSection(
    platformAnalytics: List<PlatformAnalytics>,
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
                "Platform Revenue Comparison",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            // Bar Chart
            val maxRevenue = platformAnalytics.maxOfOrNull { it.revenue } ?: 1f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                platformAnalytics.forEach { platform ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Revenue value above bar
                        Text(
                            "$${(platform.revenue / 1000).toInt()}K",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // 3D Bar
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height((platform.revenue / maxRevenue * 150).dp)
                        ) {
                            // Back shadow for 3D effect
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(x = 3.dp, y = (-3).dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(platform.color.copy(alpha = 0.3f))
                            )
                            // Main bar
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                platform.color.copy(alpha = 0.9f),
                                                platform.color
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            platform.platform,
                            fontSize = 11.sp,
                            color = theme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptimizationOpportunitiesSection(
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    val suggestions = remember {
        listOf(
            OptimizationSuggestion(
                id = "opt1",
                type = RecommendationType.CONTENT_FORMAT,
                title = "Increase YouTube Shorts Frequency",
                description = "Analytics show high engagement on short-form content. Publishing 3x per week could increase revenue.",
                priority = Priority.HIGH,
                estimatedRevenueImpact = 15000f,
                actionItems = listOf("Create 3 shorts per week", "Schedule during peak hours")
            ),
            OptimizationSuggestion(
                id = "opt2",
                type = RecommendationType.CROSS_PROMOTION,
                title = "Cross-promote on TikTok",
                description = "Your YouTube audience demographics match TikTok. Test cross-promotion strategy.",
                priority = Priority.MEDIUM,
                estimatedRevenueImpact = 8000f,
                actionItems = listOf("Set up TikTok account", "Repurpose top content")
            ),
            OptimizationSuggestion(
                id = "opt3",
                type = RecommendationType.TIMING_ADJUSTMENT,
                title = "Optimize Upload Timing",
                description = "Peak engagement times are 6-8 PM EST. Schedule posts accordingly.",
                priority = Priority.HIGH,
                estimatedRevenueImpact = 5000f,
                actionItems = listOf("Update posting schedule", "Monitor engagement metrics")
            )
        )
    }

    val totalImpact = suggestions.sumOf { it.estimatedRevenueImpact.toDouble() }.toFloat()

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
                "Optimization Opportunities",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            // Total Impact Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Potential Revenue Increase",
                        fontSize = 14.sp,
                        color = theme.colors.textPrimary
                    )
                    Text(
                        "+$${formatNumberWithCommas(totalImpact.toDouble())}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Suggestions
            suggestions.forEach { suggestion ->
                OptimizationSuggestionCard(
                    suggestion = suggestion,
                    theme = theme
                )
            }
        }
    }
}

@Composable
private fun OptimizationSuggestionCard(
    suggestion: OptimizationSuggestion,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.colors.background)
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
                        suggestion.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.colors.textPrimary
                    )
                    Text(
                        suggestion.description,
                        fontSize = 12.sp,
                        color = theme.colors.textSecondary,
                        lineHeight = 16.sp
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (suggestion.priority) {
                            Priority.HIGH -> Color(0xFFFF9800).copy(alpha = 0.2f)
                            Priority.CRITICAL -> Color(0xFFF44336).copy(alpha = 0.2f)
                            Priority.MEDIUM -> Color(0xFF2196F3).copy(alpha = 0.2f)
                            Priority.LOW -> theme.colors.textSecondary.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            suggestion.priority.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (suggestion.priority) {
                                Priority.HIGH -> Color(0xFFFF9800)
                                Priority.CRITICAL -> Color(0xFFF44336)
                                Priority.MEDIUM -> Color(0xFF2196F3)
                                Priority.LOW -> theme.colors.textSecondary
                            }
                        )
                    }
                    Text(
                        "+$${formatNumberWithCommas(suggestion.estimatedRevenueImpact.toDouble())}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}
