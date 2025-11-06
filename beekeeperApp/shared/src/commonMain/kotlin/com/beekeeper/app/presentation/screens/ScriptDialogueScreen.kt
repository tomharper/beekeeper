// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/screens/ScriptDialogueScreen.kt
package com.beekeeper.app.presentation.screens

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
fun ScriptDialogueScreen(
    projectId: String,
    scriptId: String,
    sceneNumber: String,
    characterName: String,
    onNavigateBack: () -> Unit,
    onSave: (DialogueLine) -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    val contentRepository = remember { com.beekeeper.app.domain.repository.ContentRepositoryImpl() }
    val characterRepository = remember { com.beekeeper.app.domain.repository.CharacterRepositoryImpl() }
    var dialogueLine by remember { mutableStateOf<DialogueLine?>(null) }
    var characters by remember { mutableStateOf<List<CharacterProfile>>(emptyList()) }

    var dialogue by remember { mutableStateOf("") }
    var selectedCharacterId by remember { mutableStateOf("") }
    var parenthetical by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }

    // Load dialogue line and characters
    LaunchedEffect(projectId, scriptId, sceneNumber, characterName) {
        val scripts = contentRepository.getScripts(projectId)
        val script = scripts.find { it.id == scriptId }
        val scene = script?.sceneScripts?.find { it.sceneNumber == sceneNumber }
        val foundDialogue = scene?.dialogue?.find { it.characterName == characterName }
        dialogueLine = foundDialogue
        characters = characterRepository.getCharacters(projectId)

        // Set state values when dialogue is loaded
        foundDialogue?.let {
            dialogue = it.dialogue
            selectedCharacterId = it.characterId
            parenthetical = it.parenthetical ?: ""
        }
    }

    if (dialogueLine == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = theme.colors.primary)
        }
        return
    }

    Scaffold(
        topBar = {
            SecondaryTopBar(
                title = "Script: Dialogue",
                onNavigateBack = onNavigateBack
            )
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
            // Character Selection
            Text(
                text = "Character",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = characters.find { it.id == selectedCharacterId }?.name ?: "Select Character",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.colors.primary,
                        unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.3f)
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    characters.forEach { character ->
                        DropdownMenuItem(
                            text = { Text(character.name) },
                            onClick = {
                                selectedCharacterId = character.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Dialogue Text
            Text(
                text = "Dialogue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            OutlinedTextField(
                value = dialogue,
                onValueChange = { dialogue = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.colors.primary,
                    unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.3f),
                    focusedTextColor = theme.colors.textPrimary,
                    unfocusedTextColor = theme.colors.textPrimary
                )
            )

            // Voice Input Button
            Button(
                onClick = { isRecording = !isRecording },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) theme.colors.error else theme.colors.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRecording) "Stop Recording" else "Voice Input")
            }

            // Parenthetical (Stage Direction)
            Text(
                text = "Parenthetical / Stage Direction",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            OutlinedTextField(
                value = parenthetical,
                onValueChange = { parenthetical = it },
                placeholder = { Text("(optional direction)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.colors.primary,
                    unfocusedBorderColor = theme.colors.textSecondary.copy(alpha = 0.3f),
                    focusedTextColor = theme.colors.textPrimary,
                    unfocusedTextColor = theme.colors.textPrimary
                )
            )

            // AI Suggestions Section
            Text(
                text = "AI Suggestions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.colors.textPrimary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Suggested variations:",
                        fontSize = 14.sp,
                        color = theme.colors.textSecondary
                    )

                    // TODO: Add AI-generated suggestions
                    Text(
                        text = "• AI suggestions will appear here",
                        fontSize = 14.sp,
                        color = theme.colors.textSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    dialogueLine?.let { currentDialogue ->
                        val updatedDialogue = currentDialogue.copy(
                            dialogue = dialogue,
                            characterId = selectedCharacterId,
                            characterName = characters.find { it.id == selectedCharacterId }?.name ?: currentDialogue.characterName,
                            parenthetical = parenthetical.ifBlank { null }
                        )
                        onSave(updatedDialogue)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.colors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Save Dialogue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
