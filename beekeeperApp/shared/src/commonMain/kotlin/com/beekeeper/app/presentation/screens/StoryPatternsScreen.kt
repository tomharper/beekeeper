// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/StoryPatternsScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.rememberAppTheme
import com.beekeeper.app.presentation.viewmodels.StoryPatternsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryPatternsScreen(
    projectId: String,
    onPatternSelect: (String) -> Unit = {}
) {
    val theme = rememberAppTheme()

    val viewModel: StoryPatternsViewModel = viewModel { StoryPatternsViewModel(projectId) }
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf<PatternCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showPatternDetails by remember { mutableStateOf(false) }
    var selectedPattern by remember { mutableStateOf<StoryPattern?>(null) }

    val filteredPatterns = remember(searchQuery, selectedCategory, uiState.patterns) {
        uiState.patterns.filter { pattern ->
            (selectedCategory == null || pattern.category == selectedCategory) &&
                    (searchQuery.isEmpty() ||
                            pattern.name.contains(searchQuery, ignoreCase = true) ||
                            pattern.description.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold( )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Patterns List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (filteredPatterns.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A2A2A)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Gray
                                    )
                                    Text(
                                        "No patterns found",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredPatterns) { pattern ->
                        PatternCard(
                            pattern = pattern,
                            onClick = {
                                selectedPattern = pattern
                                showPatternDetails = true
                            },
                            onApply = {
                                viewModel.applyPattern(pattern.id)
                                onPatternSelect(pattern.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Pattern Details Dialog
    if (showPatternDetails && selectedPattern != null) {
        PatternDetailsDialog(
            pattern = selectedPattern!!,
            onDismiss = {
                showPatternDetails = false
                selectedPattern = null
            },
            onApply = {
                viewModel.applyPattern(selectedPattern!!.id)
                onPatternSelect(selectedPattern!!.id)
                showPatternDetails = false
            }
        )
    }
}

@Composable
private fun PatternCard(
    pattern: StoryPattern,
    onClick: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pattern.icon?.let { icon ->
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF4FC3F7)
                            )
                        }
                        Text(
                            pattern.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        pattern.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                AssistChip(
                    onClick = { },
                    label = { Text(pattern.category.name.replace('_', ' ')) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF3A3A3A)
                    )
                )
            }

            // Pattern Structure Preview
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    pattern.structure.beats.take(4).forEach { beat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color(0xFF4FC3F7))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                beat.name,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (pattern.structure.beats.size > 4) {
                        Text(
                            "+${pattern.structure.beats.size - 4}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Stats and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (pattern.frequency > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Text(
                                "${pattern.frequency} uses",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    if (pattern.confidence > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Text(
                                "${(pattern.confidence * 100).toInt()}% match",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4FC3F7)
                    )
                ) {
                    Text("Apply", color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun PatternDetailsDialog(
    pattern: StoryPattern,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pattern.icon?.let { icon ->
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Color(0xFF4FC3F7)
                    )
                }
                Text(pattern.name)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(pattern.description)

                Divider()

                Text(
                    "Story Beats",
                    fontWeight = FontWeight.Bold
                )

                pattern.structure.beats.forEach { beat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "${(beat.position * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color(0xFF4FC3F7),
                            modifier = Modifier.width(40.dp)
                        )
                        Column {
                            Text(
                                beat.name,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                beat.description,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                if (pattern.examples.isNotEmpty()) {
                    Divider()
                    Text(
                        "Examples",
                        fontWeight = FontWeight.Bold
                    )
                    pattern.examples.forEach { example ->
                        Text(
                            "• $example",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4FC3F7)
                )
            ) {
                Text("Apply Pattern", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}