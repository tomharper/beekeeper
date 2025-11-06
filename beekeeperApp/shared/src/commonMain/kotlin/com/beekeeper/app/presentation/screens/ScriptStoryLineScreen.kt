// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/ScriptStoryLineScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.components.SecondaryTopBar
import com.beekeeper.app.presentation.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptStoryLineScreen(
    projectId: String,
    scriptId: String,
    onNavigateBack: () -> Unit,
    onSceneClick: (SceneScript) -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    val contentRepository = remember { com.beekeeper.app.domain.repository.ContentRepositoryImpl() }
    var script by remember { mutableStateOf<Script?>(null) }

    // Load script for this project
    LaunchedEffect(projectId, scriptId) {
        val scripts = contentRepository.getScripts(projectId)
        script = scripts.find { it.id == scriptId }
    }

    if (script == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = theme.colors.primary)
        }
        return
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Script: Story Line",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add new scene */ },
                containerColor = theme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Scene")
            }
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(script!!.sceneScripts.size) { index ->
                val scene = script!!.sceneScripts[index]
                val actionText = scene.action ?: ""
                val descriptionText = if (actionText.length > 100) {
                    actionText.take(100) + "..."
                } else {
                    actionText
                }
                SceneListCard(
                    sceneNumber = scene.sceneNumber,
                    heading = scene.heading ?: "Scene ${scene.sceneNumber}",
                    description = descriptionText,
                    dialogueCount = scene.dialogue.size,
                    onClick = { onSceneClick(scene) },
                    theme = theme
                )
            }
        }
    }
}

@Composable
private fun SceneListCard(
    sceneNumber: String,
    heading: String,
    description: String,
    dialogueCount: Int,
    onClick: () -> Unit,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Scene icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(theme.colors.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Movie,
                    contentDescription = null,
                    tint = theme.colors.primary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = heading,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.textPrimary
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = theme.colors.textSecondary,
                    maxLines = 2
                )

                Text(
                    text = "$dialogueCount dialogue lines",
                    fontSize = 12.sp,
                    color = theme.colors.textSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}
