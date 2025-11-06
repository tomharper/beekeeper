// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/ScriptSceneScreen.kt
package com.beekeeper.app.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
fun ScriptSceneScreen(
    projectId: String,
    scriptId: String,
    sceneNumber: String,
    onNavigateBack: () -> Unit,
    onDialogueClick: (DialogueLine) -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    val contentRepository = remember { com.beekeeper.app.domain.repository.ContentRepositoryImpl() }
    val characterRepository = remember { com.beekeeper.app.domain.repository.CharacterRepositoryImpl() }
    var scene by remember { mutableStateOf<SceneScript?>(null) }
    var characters by remember { mutableStateOf<List<CharacterProfile>>(emptyList()) }

    // Load scene and characters
    LaunchedEffect(projectId, scriptId, sceneNumber) {
        val scripts = contentRepository.getScripts(projectId)
        val script = scripts.find { it.id == scriptId }
        scene = script?.sceneScripts?.find { it.sceneNumber == sceneNumber }
        characters = characterRepository.getCharacters(projectId)
    }

    if (scene == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = theme.colors.primary)
        }
        return
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Script: Scene",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add new dialogue */ },
                containerColor = theme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Dialogue")
            }
        },
        containerColor = theme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scene Title
            OutlinedTextField(
                value = scene!!.heading ?: "Scene ${scene!!.sceneNumber}",
                onValueChange = { /* TODO: Update scene heading */ },
                label = { Text("Scene Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.colors.primary,
                    unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.3f),
                    focusedTextColor = theme.colors.textPrimary,
                    unfocusedTextColor = theme.colors.textPrimary
                )
            )

            // Scene Description
            OutlinedTextField(
                value = scene!!.action ?: "",
                onValueChange = { /* TODO: Update scene action */ },
                label = { Text("Scene Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.colors.primary,
                    unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.3f),
                    focusedTextColor = theme.colors.textPrimary,
                    unfocusedTextColor = theme.colors.textPrimary
                )
            )

            // Characters Section
            Text(
                text = "Characters",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scene!!.characterIds.size) { index ->
                    val characterId = scene!!.characterIds[index]
                    val character = characters.find { it.id == characterId }
                    character?.let {
                        CharacterChip(
                            name = it.name,
                            theme = theme
                        )
                    }
                }
            }

            // Dialogue Section
            Text(
                text = "Dialogue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scene!!.dialogue.size) { index ->
                    val dialogueLine = scene!!.dialogue[index]
                    DialogueLineCard(
                        characterName = dialogueLine.characterName,
                        dialogue = dialogueLine.dialogue,
                        onClick = { onDialogueClick(dialogueLine) },
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterChip(
    name: String,
    theme: com.beekeeper.app.presentation.theme.AppTheme
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = theme.colors.primary.copy(alpha = 0.2f)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp,
            color = theme.colors.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DialogueLineCard(
    characterName: String,
    dialogue: String,
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
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = null,
                tint = theme.colors.primary,
                modifier = Modifier.size(20.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = characterName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.primary
                )
                Text(
                    text = dialogue,
                    fontSize = 14.sp,
                    color = theme.colors.textPrimary,
                    maxLines = 2
                )
            }
        }
    }
}
