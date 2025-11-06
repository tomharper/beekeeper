// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/components/AIPromptFields.kt
package com.beekeeper.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Reusable component for AI generation prompt fields (visual, voice, ambient).
 * Can be used in Story, Storyboard, Scene, and Frame edit screens.
 */
@Composable
fun AIPromptFieldsSection(
    visualPrompt: String?,
    voicePrompt: String?,
    ambientPrompt: String?,
    isEditable: Boolean,
    onVisualPromptChange: (String) -> Unit,
    onVoicePromptChange: (String) -> Unit,
    onAmbientPromptChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "AI Generation Prompts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = visualPrompt ?: "",
                onValueChange = { if (isEditable) onVisualPromptChange(it) },
                label = { Text("Visual Prompt (Midjourney, Stable Diffusion)") },
                enabled = isEditable,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("Detailed visual description for image generation...") },
                supportingText = { Text("Describe visual elements, style, composition, lighting, colors") }
            )

            OutlinedTextField(
                value = voicePrompt ?: "",
                onValueChange = { if (isEditable) onVoicePromptChange(it) },
                label = { Text("Voice Prompt (ElevenLabs)") },
                enabled = isEditable,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text("Voice characteristics for voice generation...") },
                supportingText = { Text("Describe voice characteristics, tone, pace, emotion") }
            )

            OutlinedTextField(
                value = ambientPrompt ?: "",
                onValueChange = { if (isEditable) onAmbientPromptChange(it) },
                label = { Text("Ambient/Theme Prompt") },
                enabled = isEditable,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text("Ambient audio or music description...") },
                supportingText = { Text("Describe ambient sounds, music style, mood, atmosphere") }
            )
        }
    }
}

/**
 * Compact version for use in lists or smaller spaces
 */
@Composable
fun AIPromptFieldsCompact(
    visualPrompt: String?,
    voicePrompt: String?,
    ambientPrompt: String?,
    isEditable: Boolean,
    onVisualPromptChange: (String) -> Unit,
    onVoicePromptChange: (String) -> Unit,
    onAmbientPromptChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "AI Prompts",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = visualPrompt ?: "",
            onValueChange = { if (isEditable) onVisualPromptChange(it) },
            label = { Text("Visual") },
            enabled = isEditable,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Visual generation prompt...") }
        )

        OutlinedTextField(
            value = voicePrompt ?: "",
            onValueChange = { if (isEditable) onVoicePromptChange(it) },
            label = { Text("Voice") },
            enabled = isEditable,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Voice generation prompt...") }
        )

        OutlinedTextField(
            value = ambientPrompt ?: "",
            onValueChange = { if (isEditable) onAmbientPromptChange(it) },
            label = { Text("Ambient") },
            enabled = isEditable,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Ambient audio prompt...") }
        )
    }
}
