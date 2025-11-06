// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/ContentQualityReviewScreen.kt
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.beekeeper.app.domain.model.ComplianceRequirement
import com.beekeeper.app.domain.model.ConsistencyCheck
import com.beekeeper.app.domain.model.ConsistencyChecks
import com.beekeeper.app.domain.model.ConsistencyStatus
import com.beekeeper.app.domain.model.IssueSeverity
import com.beekeeper.app.domain.model.PlatformCompliance
import com.beekeeper.app.domain.model.Priority
import com.beekeeper.app.domain.model.QualityIssue
import com.beekeeper.app.domain.model.QualityMetric
import com.beekeeper.app.domain.model.QualityStatus
import com.beekeeper.app.domain.model.Recommendation
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.viewmodels.ContentQualityReviewViewModel
import com.beekeeper.app.presentation.viewmodels.rememberContentQualityReviewViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
enum class QualityTab {
    CONSISTENCY,
    COMPLIANCE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentQualityReviewScreen(
    projectId: String,
    navController: NavHostController,
    viewModel: ContentQualityReviewViewModel = rememberContentQualityReviewViewModel(projectId)
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    var selectedTab by remember { mutableStateOf(QualityTab.CONSISTENCY) }
    val scope = rememberCoroutineScope()

    val project by viewModel.project.collectAsState()
    val contentItems by viewModel.contentItems.collectAsState()
    var isAnalyzing by remember { mutableStateOf(false) }

    // Mock quality data
    val qualityMetrics = remember { generateQualityMetrics() }
    val overallScore = remember {
        qualityMetrics.map { it.score }.average().toFloat()
    }

    Scaffold(
        containerColor = theme.colors.background,
        topBar = {
            SecondaryTopBar(
                title = "Analytics",
                subtitle = project?.title,
                onNavigateBack = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        if (isAnalyzing) "Analyzing..." else "Run Full Analysis",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                icon = {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = theme.colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Run Analysis",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                onClick = {
                    scope.launch {
                        isAnalyzing = true
                        isAnalyzing = false
                    }
                },
                expanded = !isAnalyzing,
                containerColor = theme.colors.primary,
                contentColor = theme.colors.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Overall Score Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        overallScore >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        overallScore >= 60 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        else -> Color(0xFFF44336).copy(alpha = 0.1f)
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Overall Quality Score",
                                style = MaterialTheme.typography.titleMedium,
                                color = theme.colors.onSurface
                            )
                            Text(
                                getQualityDescription(overallScore),
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.colors.onSurfaceVariant
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(80.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { overallScore / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = when {
                                    overallScore >= 80 -> Color(0xFF4CAF50)
                                    overallScore >= 60 -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                },
                                strokeWidth = 8.dp
                            )
                            Text(
                                "${overallScore.toInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = theme.colors.onSurface
                            )
                        }
                    }

                    HorizontalDivider(
                        color = theme.colors.onSurfaceVariant.copy(alpha = 0.2f)
                    )

                    // Coherence Metrics Bar Chart
                    Text(
                        "Coherence Metrics",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.colors.onSurface
                    )

                    CoherenceBarChart(
                        characterScore = 85f,
                        narrativeScore = 88f,
                        soundScore = 78f,
                        visualScore = 92f
                    )
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = theme.colors.background,
                contentColor = theme.colors.primary
            ) {
                QualityTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                tab.name.replace('_', ' '),
                                color = if (selectedTab == tab)
                                    theme.colors.primary
                                else
                                    theme.colors.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                QualityTab.CONSISTENCY -> {
                    ConsistencyTabContent(projectId, qualityMetrics)
                }
                QualityTab.COMPLIANCE -> {
                    ComplianceTabContent()
                }
            }
        }
    }
}

@Composable
private fun ConsistencyTabContent(
    projectId: String,
    qualityMetrics: List<QualityMetric>
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    val consistencyChecks = remember { generateConsistencyChecks() }
    val recommendations = remember { generateRecommendations(qualityMetrics) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                "Character Consistency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.colors.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(consistencyChecks.characterConsistency) { check ->
            ConsistencyCheckCard(check)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Narrative Consistency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.colors.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(consistencyChecks.narrativeConsistency) { check ->
            ConsistencyCheckCard(check)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Sound Consistency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.colors.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(consistencyChecks.soundConsistency) { check ->
            ConsistencyCheckCard(check)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Visual Consistency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.colors.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(consistencyChecks.visualConsistency) { check ->
            ConsistencyCheckCard(check)
        }

        // Recommendations section
        if (recommendations.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Recommendations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(recommendations) { recommendation ->
                RecommendationCard(recommendation)
            }
        }
    }
}

@Composable
private fun ConsistencyCheckCard(
    check: ConsistencyCheck
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = check.details.isNotEmpty()) { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (check.status) {
                            ConsistencyStatus.CONSISTENT -> Icons.Default.CheckCircle
                            ConsistencyStatus.MINOR_ISSUES -> Icons.Default.Warning
                            ConsistencyStatus.MAJOR_ISSUES -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = when (check.status) {
                            ConsistencyStatus.CONSISTENT -> Color(0xFF4CAF50)
                            ConsistencyStatus.MINOR_ISSUES -> Color(0xFFFF9800)
                            ConsistencyStatus.MAJOR_ISSUES -> Color(0xFFF44336)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            check.element,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = theme.colors.onSurface
                        )
                        Text(
                            check.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.colors.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (check.affectedCount > 0) {
                        Badge(
                            containerColor = when (check.status) {
                                ConsistencyStatus.CONSISTENT -> Color(0xFF4CAF50)
                                ConsistencyStatus.MINOR_ISSUES -> Color(0xFFFF9800)
                                ConsistencyStatus.MAJOR_ISSUES -> Color(0xFFF44336)
                            }
                        ) {
                            Text(
                                "${check.affectedCount}",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    if (check.details.isNotEmpty()) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = theme.colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable details section
            if (isExpanded && check.details.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = theme.colors.onSurfaceVariant.copy(alpha = 0.2f)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    check.details.forEach { detail ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = theme.colors.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.colors.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplianceTabContent() {
    val platforms = remember { generatePlatformCompliance() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(platforms) { platform ->
            PlatformComplianceCard(platform)
        }
    }
}

@Composable
private fun PlatformComplianceCard(
    platform: PlatformCompliance
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = platform.icon,
                        contentDescription = null,
                        tint = theme.colors.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            platform.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.colors.onSurface
                        )
                        Text(
                            "${platform.requirements.count { it.met }} of ${platform.requirements.size} requirements met",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.colors.onSurfaceVariant
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (platform.isCompliant)
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else
                                Color(0xFFF44336).copy(alpha = 0.1f)
                        )
                ) {
                    Icon(
                        imageVector = if (platform.isCompliant)
                            Icons.Default.Check
                        else
                            Icons.Default.Close,
                        contentDescription = null,
                        tint = if (platform.isCompliant)
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            platform.requirements.forEach { requirement ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (requirement.met)
                            Icons.Default.CheckBox
                        else
                            Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = null,
                        tint = if (requirement.met)
                            Color(0xFF4CAF50)
                        else
                            theme.colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        requirement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: Recommendation
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (recommendation.priority) {
                Priority.CRITICAL -> Color(0xFFF44336).copy(alpha = 0.05f)
                Priority.HIGH -> Color(0xFFF44336).copy(alpha = 0.05f)
                Priority.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.05f)
                Priority.LOW -> Color(0xFF2196F3).copy(alpha = 0.05f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            when (recommendation.priority) {
                Priority.CRITICAL -> Color(0xFFF44336).copy(alpha = 0.3f)
                Priority.HIGH -> Color(0xFFF44336).copy(alpha = 0.3f)
                Priority.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.3f)
                Priority.LOW -> Color(0xFF2196F3).copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recommendation.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        recommendation.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.colors.onSurfaceVariant
                    )
                }

                Badge(
                    containerColor = when (recommendation.priority) {
                        Priority.CRITICAL -> Color(0xFFF44336)
                        Priority.HIGH -> Color(0xFFF44336)
                        Priority.MEDIUM -> Color(0xFFFF9800)
                        Priority.LOW -> Color(0xFF2196F3)
                    }
                ) {
                    Text(
                        recommendation.priority.name,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }

            if (recommendation.estimatedImpact != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Expected improvement: +${recommendation.estimatedImpact}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
private fun CoherenceBarChart(
    characterScore: Float,
    narrativeScore: Float,
    soundScore: Float,
    visualScore: Float
) {
    val theme = com.beekeeper.app.presentation.theme.rememberAppTheme()
    val metrics = listOf(
        "Character" to characterScore,
        "Narrative" to narrativeScore,
        "Sound" to soundScore,
        "Visual" to visualScore
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        metrics.forEach { (label, score) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    modifier = Modifier.width(80.dp),
                    fontSize = 14.sp,
                    color = theme.colors.onSurface
                )

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    // Background bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.colors.surfaceVariant.copy(alpha = 0.3f))
                    )

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(score / 100f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    score >= 80 -> Color(0xFF4CAF50)
                                    score >= 60 -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                }
                            )
                    )

                    // Score text
                    Text(
                        "${score.toInt()}%",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (score > 50) Color.White else theme.colors.onSurface
                    )
                }
            }
        }
    }
}

// Helper functions
private fun getQualityDescription(score: Float): String {
    return when {
        score >= 90 -> "Excellent - Ready for production"
        score >= 80 -> "Good - Minor improvements recommended"
        score >= 70 -> "Fair - Some issues need attention"
        score >= 60 -> "Needs Work - Multiple areas require improvement"
        else -> "Critical - Major issues detected"
    }
}

private fun generateQualityMetrics(): List<QualityMetric> {
    return listOf(
        QualityMetric(
            category = "Character Consistency",
            score = 85f,
            status = QualityStatus.GOOD,
            issues = listOf(
                QualityIssue(
                    severity = IssueSeverity.MEDIUM,
                    description = "Character voice varies in scenes 3-5",
                    affectedScenes = listOf("Scene 3", "Scene 4", "Scene 5"),
                    recommendation = "Review dialogue patterns and maintain consistent tone"
                )
            )
        ),
        QualityMetric(
            category = "Visual Quality",
            score = 92f,
            status = QualityStatus.EXCELLENT,
            issues = emptyList()
        ),
        QualityMetric(
            category = "Audio Sync",
            score = 78f,
            status = QualityStatus.GOOD,
            issues = listOf(
                QualityIssue(
                    severity = IssueSeverity.LOW,
                    description = "Minor lip-sync issues detected",
                    affectedScenes = listOf("Scene 2"),
                    recommendation = "Adjust audio timing by 100ms"
                )
            )
        ),
        QualityMetric(
            category = "Narrative Flow",
            score = 88f,
            status = QualityStatus.GOOD,
            issues = emptyList()
        ),
        QualityMetric(
            category = "Platform Compliance",
            score = 95f,
            status = QualityStatus.EXCELLENT,
            issues = emptyList()
        )
    )
}

private fun generateConsistencyChecks(): ConsistencyChecks {
    return ConsistencyChecks(
        characterConsistency = listOf(
            ConsistencyCheck(
                element = "Isabella Martinez",
                description = "Appearance consistent across all scenes",
                status = ConsistencyStatus.CONSISTENT,
                affectedCount = 0,
                details = listOf(
                    "Hair color matches in all 12 scenes",
                    "Outfit continuity maintained",
                    "Facial features rendered consistently"
                )
            ),
            ConsistencyCheck(
                element = "Chris Thompson",
                description = "Voice tone variation detected",
                status = ConsistencyStatus.MINOR_ISSUES,
                affectedCount = 3,
                details = listOf(
                    "Scene 3: Voice pitch higher than baseline",
                    "Scene 7: Audio quality differs from previous scenes",
                    "Scene 11: Inconsistent emotional tone in delivery"
                )
            ),
            ConsistencyCheck(
                element = "Background Characters",
                description = "Clothing inconsistency in crowd scenes",
                status = ConsistencyStatus.MINOR_ISSUES,
                affectedCount = 2,
                details = listOf(
                    "Scene 5: Background character changes outfit mid-scene",
                    "Scene 9: Crowd member appears with different hairstyle"
                )
            )
        ),
        narrativeConsistency = listOf(
            ConsistencyCheck(
                element = "Plot Timeline",
                description = "All events properly sequenced",
                status = ConsistencyStatus.CONSISTENT,
                affectedCount = 0,
                details = listOf(
                    "All flashbacks clearly marked",
                    "Event sequence follows logical progression",
                    "Time references are accurate"
                )
            ),
            ConsistencyCheck(
                element = "Character Motivations",
                description = "Clear and consistent throughout",
                status = ConsistencyStatus.CONSISTENT,
                affectedCount = 0,
                details = listOf(
                    "Main character goals remain focused",
                    "Decisions align with established personality",
                    "Character arcs progress naturally"
                )
            )
        ),
        soundConsistency = listOf(
            ConsistencyCheck(
                element = "Audio Levels",
                description = "Consistent volume across scenes",
                status = ConsistencyStatus.CONSISTENT,
                affectedCount = 0,
                details = listOf(
                    "Dialogue levels normalized across all scenes",
                    "Background ambience balanced properly",
                    "No sudden volume spikes detected"
                )
            ),
            ConsistencyCheck(
                element = "Lip Sync",
                description = "Minor sync issues detected",
                status = ConsistencyStatus.MINOR_ISSUES,
                affectedCount = 2,
                details = listOf(
                    "Scene 4: 150ms delay in dialogue sync",
                    "Scene 8: Mouth movements slightly ahead of audio"
                )
            ),
            ConsistencyCheck(
                element = "Background Music",
                description = "Volume levels need balancing",
                status = ConsistencyStatus.MINOR_ISSUES,
                affectedCount = 1,
                details = listOf(
                    "Scene 6: Music overpowers dialogue by 3dB"
                )
            )
        ),
        visualConsistency = listOf(
            ConsistencyCheck(
                element = "Color Grading",
                description = "Consistent across all scenes",
                status = ConsistencyStatus.CONSISTENT,
                affectedCount = 0,
                details = listOf(
                    "Color temperature matches throughout",
                    "Saturation levels uniform",
                    "Contrast ratios within target range"
                )
            ),
            ConsistencyCheck(
                element = "Lighting",
                description = "Day/night transitions need adjustment",
                status = ConsistencyStatus.MINOR_ISSUES,
                affectedCount = 1,
                details = listOf(
                    "Scene 10: Sunset lighting changes too abruptly to night"
                )
            )
        )
    )
}

private fun generatePlatformCompliance(): List<PlatformCompliance> {
    return listOf(
        PlatformCompliance(
            name = "YouTube",
            icon = Icons.Default.PlayCircle,
            isCompliant = true,
            requirements = listOf(
                ComplianceRequirement("Resolution: 1080p or higher", true),
                ComplianceRequirement("Format: MP4", true),
                ComplianceRequirement("Subtitles available", true),
                ComplianceRequirement("Content rating appropriate", true)
            )
        ),
        PlatformCompliance(
            name = "Netflix",
            icon = Icons.Default.Tv,
            isCompliant = false,
            requirements = listOf(
                ComplianceRequirement("Resolution: 4K required", false),
                ComplianceRequirement("HDR support", false),
                ComplianceRequirement("IMF package format", true),
                ComplianceRequirement("QC report passed", true)
            )
        ),
        PlatformCompliance(
            name = "Instagram",
            icon = Icons.Default.PhotoCamera,
            isCompliant = true,
            requirements = listOf(
                ComplianceRequirement("Aspect ratio: 9:16 for Reels", true),
                ComplianceRequirement("Duration: Under 90 seconds", true),
                ComplianceRequirement("File size: Under 4GB", true)
            )
        )
    )
}

private fun generateRecommendations(metrics: List<QualityMetric>): List<Recommendation> {
    val recommendations = mutableListOf<Recommendation>()

    metrics.forEach { metric ->
        when {
            metric.score < 70 -> {
                recommendations.add(
                    Recommendation(
                        title = "Improve ${metric.category}",
                        description = "Critical issues detected in ${metric.category}. Immediate attention required.",
                        priority = Priority.HIGH,
                        estimatedImpact = ((100 - metric.score) / 2).toInt()
                    )
                )
            }
            metric.score < 85 -> {
                recommendations.add(
                    Recommendation(
                        title = "Enhance ${metric.category}",
                        description = "Some improvements needed in ${metric.category} to reach optimal quality.",
                        priority = Priority.MEDIUM,
                        estimatedImpact = ((100 - metric.score) / 3).toInt()
                    )
                )
            }
        }
    }

    // Add general recommendations
    recommendations.add(
        Recommendation(
            title = "Run AI Consistency Check",
            description = "Use advanced AI analysis to detect subtle inconsistencies",
            priority = Priority.LOW
        )
    )

    return recommendations.sortedBy { it.priority.ordinal }
}