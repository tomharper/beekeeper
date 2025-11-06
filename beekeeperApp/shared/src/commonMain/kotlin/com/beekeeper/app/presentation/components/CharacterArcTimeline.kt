// File: fillerApp/shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/components/CharacterArcTimeline.kt
package com.beekeeper.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.CharacterProfile
import kotlinx.coroutines.launch

// Character Arc Types with their stages
sealed class CharacterArcType(
    val name: String,
    val stages: List<ArcStage>,
    val color: Color
) {
    object HeroJourney : CharacterArcType(
        "Hero's Journey",
        listOf(
            ArcStage("Ordinary World", "Character's normal life", 0.0f),
            ArcStage("Call to Adventure", "The inciting incident", 0.1f),
            ArcStage("Refusal of Call", "Initial hesitation", 0.2f),
            ArcStage("Meeting Mentor", "Guidance received", 0.3f),
            ArcStage("Crossing Threshold", "Entering new world", 0.4f),
            ArcStage("Tests & Allies", "Challenges faced", 0.5f),
            ArcStage("Ordeal", "Major crisis", 0.7f),
            ArcStage("Reward", "Victory achieved", 0.85f),
            ArcStage("Return", "Coming home changed", 1.0f)
        ),
        Color(0xFF4CAF50)
    )

    object TragicFall : CharacterArcType(
        "Tragic Fall",
        listOf(
            ArcStage("Noble Beginning", "Character at their peak", 0.0f),
            ArcStage("Fatal Flaw Revealed", "Weakness shown", 0.2f),
            ArcStage("Poor Choices", "Mistakes compound", 0.4f),
            ArcStage("Point of No Return", "Cannot turn back", 0.6f),
            ArcStage("Downfall", "Everything unravels", 0.8f),
            ArcStage("Tragic End", "Final destruction", 1.0f)
        ),
        Color(0xFFE91E63)
    )

    object ComingOfAge : CharacterArcType(
        "Coming of Age",
        listOf(
            ArcStage("Innocence", "Childlike state", 0.0f),
            ArcStage("First Challenge", "Initial test", 0.25f),
            ArcStage("Loss of Innocence", "Reality hits", 0.5f),
            ArcStage("Finding Identity", "Self-discovery", 0.75f),
            ArcStage("Maturity", "Growth achieved", 1.0f)
        ),
        Color(0xFF2196F3)
    )

    object Redemption : CharacterArcType(
        "Redemption Arc",
        listOf(
            ArcStage("Fall from Grace", "Past mistakes", 0.0f),
            ArcStage("Rock Bottom", "Lowest point", 0.2f),
            ArcStage("Spark of Hope", "Chance for change", 0.4f),
            ArcStage("Active Change", "Making amends", 0.6f),
            ArcStage("Sacrifice", "Proving change", 0.8f),
            ArcStage("Redemption", "Forgiveness earned", 1.0f)
        ),
        Color(0xFF9C27B0)
    )

    object Corruption : CharacterArcType(
        "Corruption Arc",
        listOf(
            ArcStage("Pure Intent", "Good beginning", 0.0f),
            ArcStage("First Compromise", "Small concession", 0.3f),
            ArcStage("Justification", "Rationalizing actions", 0.5f),
            ArcStage("Moral Decay", "Ethics abandoned", 0.7f),
            ArcStage("Complete Corruption", "Fully corrupted", 1.0f)
        ),
        Color(0xFF795548)
    )

    object FlatArc : CharacterArcType(
        "Flat Arc",
        listOf(
            ArcStage("Established Truth", "Core belief", 0.0f),
            ArcStage("World Challenges", "Belief tested", 0.33f),
            ArcStage("Standing Firm", "Maintaining truth", 0.66f),
            ArcStage("World Changes", "Impact on others", 1.0f)
        ),
        Color(0xFF607D8B)
    )

    companion object {
        fun fromString(arcName: String): CharacterArcType = when (arcName) {
            "Hero's Journey" -> HeroJourney
            "Tragic Fall" -> TragicFall
            "Coming of Age" -> ComingOfAge
            "Redemption Arc" -> Redemption
            "Corruption Arc" -> Corruption
            "Flat Arc" -> FlatArc
            else -> HeroJourney // Default
        }

        fun all() = listOf(HeroJourney, TragicFall, ComingOfAge, Redemption, Corruption, FlatArc)
    }
}

data class ArcStage(
    val name: String,
    val description: String,
    val position: Float // 0.0 to 1.0 on timeline
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterArcTimeline(
    character: CharacterProfile,
    isEditable: Boolean,
    currentProgress: Float = 0.5f, // Character's current position in their arc
    onArcChange: (String) -> Unit,
    onStageClick: ((ArcStage) -> Unit)? = null
) {
    var selectedArcType by remember {
        mutableStateOf(
            CharacterArcType.fromString(character.metadata["characterArc"] ?: "Hero's Journey")
        )
    }

    var showArcSelector by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "arc_progress"
    )

    val density = LocalDensity.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Arc Type Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Character Arc",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        selectedArcType.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = selectedArcType.color
                    )
                }

                if (isEditable) {
                    IconButton(onClick = { showArcSelector = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Change Arc Type",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Timeline Visualization
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
            ) {
                // Timeline Background
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawTimeline(
                        stages = selectedArcType.stages,
                        progress = animatedProgress,
                        arcColor = selectedArcType.color,
                        density = density
                    )
                }

                // Stage Markers
                selectedArcType.stages.forEach { stage ->
                    StageMarker(
                        stage = stage,
                        arcColor = selectedArcType.color,
                        isActive = stage.position <= animatedProgress,
                        onClick = { onStageClick?.invoke(stage) }
                    )
                }

                // Current Position Indicator
                val currentStage = selectedArcType.stages
                    .lastOrNull { it.position <= animatedProgress }
                    ?: selectedArcType.stages.first()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 8.dp)
                ) {
                    Surface(
                        color = selectedArcType.color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Current Stage",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                currentStage.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = selectedArcType.color
                            )
                            Text(
                                currentStage.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // Arc Type Selector Dialog
    if (showArcSelector) {
        AlertDialog(
            onDismissRequest = { showArcSelector = false },
            title = { Text("Select Character Arc") },
            text = {
                LazyColumn {
                    items(CharacterArcType.all()) { arcType ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedArcType = arcType
                                    onArcChange(arcType.name)
                                    showArcSelector = false
                                },
                            color = if (selectedArcType == arcType)
                                arcType.color.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                if (selectedArcType == arcType) arcType.color
                                else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(arcType.color)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        arcType.name,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${arcType.stages.size} stages",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showArcSelector = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun BoxScope.StageMarker(
    stage: ArcStage,
    arcColor: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val x = stage.position
    val animatedScale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "stage_scale"
    )

    Column(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = (x * 300).dp) // Adjust based on your timeline width
            .scale(animatedScale)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stage dot
        Box(
            modifier = Modifier
                .size(if (isActive) 16.dp else 12.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) arcColor else arcColor.copy(alpha = 0.3f)
                )
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.surface,
                    CircleShape
                )
        )

        // Stage label (only show for active stages in edit mode)
        if (isActive) {
            Text(
                stage.name,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = arcColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun DrawScope.drawTimeline(
    stages: List<ArcStage>,
    progress: Float,
    arcColor: Color,
    density: androidx.compose.ui.unit.Density
) {
    val timelineY = size.height * 0.4f

    // Background line
    drawLine(
        color = arcColor.copy(alpha = 0.2f),
        start = Offset(0f, timelineY),
        end = Offset(size.width, timelineY),
        strokeWidth = with(density) { 4.dp.toPx() }
    )

    // Progress line
    drawLine(
        color = arcColor,
        start = Offset(0f, timelineY),
        end = Offset(size.width * progress, timelineY),
        strokeWidth = with(density) { 4.dp.toPx() }
    )

    // Draw curve showing arc progression
    val path = Path().apply {
        moveTo(0f, timelineY)

        stages.windowed(2).forEach { (from, to) ->
            val fromX = size.width * from.position
            val toX = size.width * to.position
            val midX = (fromX + toX) / 2

            // Create a curve that rises and falls
            val curveHeight = with(density) { 30.dp.toPx() } *
                    (if (to.position < 0.5f) to.position * 2 else (1 - to.position) * 2)

            quadraticBezierTo(
                midX, timelineY - curveHeight,
                toX, timelineY
            )
        }
    }

    drawPath(
        path = path,
        color = arcColor.copy(alpha = 0.3f),
        style = Stroke(width = with(density) { 2.dp.toPx() })
    )
}