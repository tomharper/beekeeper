// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/components/PromptGenerationCard.kt
package com.beekeeper.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.generators.prompts.CompletePromptGenerator
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.utils.FilePicker
import com.beekeeper.app.utils.FilePickerConfig
import com.beekeeper.app.utils.PickedFile
import kotlinx.coroutines.launch

// Type alias for convenience
typealias VideoPromptGenerator = CompletePromptGenerator
typealias VideoPromptResponse = CompletePromptGenerator.VideoPromptResponse

/**
 * Card component for displaying prompts and handling image uploads
 * for storyboards and scenes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptGenerationCard(
    title: String,
    subtitle: String? = null,
    promptType: PromptType,
    onGeneratePrompt: () -> VideoPromptResponse,
    onImageSelected: (ByteArray) -> Unit,
    existingImageUrl: String? = null,
    filePicker: FilePicker? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var generatedPrompt by remember { mutableStateOf<VideoPromptResponse?>(null) }
    var copiedPromptType by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        ),
        border = BorderStroke(1.dp, Color(0xFF2A2A3E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Generate Prompt Button
                    IconButton(
                        onClick = {
                            generatedPrompt = onGeneratePrompt()
                            expanded = true
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Generate Prompt",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }

                    // Upload Image Button
                    filePicker?.let { picker ->
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isUploading = true
                                    val config = FilePickerConfig(
                                        allowMultiple = false,
                                        maxFileSize = 10 * 1024 * 1024, // 10MB in bytes
                                        allowedMimeTypes = listOf("image/jpeg", "image/png", "image/webp")
                                    )
                                    val result = picker.pickImage(config)
                                    result?.let { file ->
                                        onImageSelected(file.bytes)
                                    }
                                    isUploading = false
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color(0xFF10B981)
                            ),
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Upload,
                                    contentDescription = "Upload Image",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Expand/Collapse Button
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            // Existing Image Preview
            existingImageUrl?.let { url ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A2A3E))
                ) {
                    // In real implementation, use AsyncImage or similar
                    // AsyncImage(url, contentDescription = "Existing image")
                    Text(
                        "Image: $url",
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Generated Prompts Section
            AnimatedVisibility(
                visible = expanded && generatedPrompt != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                generatedPrompt?.let { prompt ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Primary Prompt
                        PromptField(
                            label = "Primary Prompt",
                            content = prompt.primaryPrompt,
                            onCopy = {
                                copiedPromptType = "primary"
                                // Copy to clipboard logic here
                            },
                            isCopied = copiedPromptType == "primary"
                        )

                        // Negative Prompt
                        PromptField(
                            label = "Negative Prompt",
                            content = prompt.negativePrompt,
                            onCopy = {
                                copiedPromptType = "negative"
                                // Copy to clipboard logic here
                            },
                            isCopied = copiedPromptType == "negative"
                        )

                        // Style Prompt
                        PromptField(
                            label = "Style",
                            content = prompt.stylePrompt,
                            onCopy = {
                                copiedPromptType = "style"
                                // Copy to clipboard logic here
                            },
                            isCopied = copiedPromptType == "style"
                        )

                        // Character Details
                        if (prompt.characterPrompt.isNotEmpty()) {
                            PromptField(
                                label = "Characters",
                                content = prompt.characterPrompt,
                                onCopy = {
                                    copiedPromptType = "character"
                                    // Copy to clipboard logic here
                                },
                                isCopied = copiedPromptType == "character"
                            )
                        }

                        // Environment
                        PromptField(
                            label = "Environment",
                            content = prompt.environmentPrompt,
                            onCopy = {
                                copiedPromptType = "environment"
                                // Copy to clipboard logic here
                            },
                            isCopied = copiedPromptType == "environment"
                        )

                        // Platform Optimizations
                        if (prompt.platformOptimizations.isNotEmpty()) {
                            Column {
                                Text(
                                    "Platform Settings",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    prompt.platformOptimizations.forEach { (platform, setting) ->
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("$platform: $setting") },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = Color(0xFF2A2A3E),
                                                labelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Copy feedback
            LaunchedEffect(copiedPromptType) {
                if (copiedPromptType != null) {
                    kotlinx.coroutines.delay(2000)
                    copiedPromptType = null
                }
            }
        }
    }
}

@Composable
private fun PromptField(
    label: String,
    content: String,
    onCopy: () -> Unit,
    isCopied: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF9CA3AF)
            )

            AnimatedContent(targetState = isCopied) { copied ->
                if (copied) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Copied",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF059669)
                        )
                        Text(
                            "Copied!",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }

        Text(
            content,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .background(Color(0xFF2A2A3E), RoundedCornerShape(4.dp))
                .padding(12.dp)
        )
    }
}

enum class PromptType {
    STORYBOARD,
    SCENE,
    CHARACTER
}

// Extension function to integrate with existing screens
@Composable
fun StoryboardPromptSection(
    storyboard: Storyboard,
    script: Script,
    characters: List<CharacterProfile>,
    filePicker: FilePicker?,
    onImageUploaded: (String, ByteArray) -> Unit
) {
    val promptGenerator = remember { VideoPromptGenerator() }

    PromptGenerationCard(
        title = "Storyboard: ${storyboard.title}",
        subtitle = "${storyboard.scenes.size} scenes, ${storyboard.scenes.sumOf { it.frames.size }} frames",
        promptType = PromptType.STORYBOARD,
        onGeneratePrompt = {
            promptGenerator.generateStoryboardPrompt(
                storyboard = storyboard,
                script = script,
                characters = characters
            )
        },
        onImageSelected = { imageData ->
            onImageUploaded(storyboard.id, imageData)
        },
        existingImageUrl = storyboard.thumbnailUrl,
        filePicker = filePicker
    )
}

@Composable
fun ScenePromptSection(
    scene: Scene,
    sceneScript: SceneScript,
    characters: List<CharacterProfile>,
    filePicker: FilePicker?,
    onImageUploaded: (String, ByteArray) -> Unit
) {
    val promptGenerator = remember { VideoPromptGenerator() }

    PromptGenerationCard(
        title = sceneScript.heading ?: "Scene ${scene.sceneNumber}",
        subtitle = "${scene.frames.size} frames, ${scene.duration}s total",
        promptType = PromptType.SCENE,
        onGeneratePrompt = {
            promptGenerator.generateSceneSummaryPrompt(
                scene = scene,
                sceneScript = sceneScript,
                characters = characters
            )
        },
        onImageSelected = { imageData ->
            onImageUploaded(scene.id, imageData)
        },
        existingImageUrl = scene.imageUrl,
        filePicker = filePicker
    )
}

@Composable
fun CharacterAvatarPromptSection(
    character: CharacterProfile,
    frame: Frame,
    scene: Scene,
    sceneScript: SceneScript,
    filePicker: FilePicker?,
    onImageUploaded: (String, ByteArray) -> Unit
) {
    val promptGenerator = remember { VideoPromptGenerator() }

    PromptGenerationCard(
        title = "Avatar: ${character.name}",
        subtitle = character.role.name.replace("_", " "),
        promptType = PromptType.CHARACTER,
        onGeneratePrompt = {
            promptGenerator.generateCharacterPrompt(
                character = character,
                frame = frame,
                scene = scene,
                sceneScript = sceneScript,
                additionalContext = mapOf(
                    "costume" to "character default outfit",
                    "mood" to "neutral portrait"
                )
            )
        },
        onImageSelected = { imageData ->
            onImageUploaded(character.id, imageData)
        },
        existingImageUrl = character.imageUrl,
        filePicker = filePicker
    )
}

/**
 * Frame-specific prompt generation card
 */
@Composable
fun FramePromptSection(
    frame: Frame,
    scene: Scene,
    sceneScript: SceneScript,
    characters: List<CharacterProfile>,
    platform: SocialPlatform = SocialPlatform.YOUTUBE,
    filePicker: FilePicker?,
    onImageUploaded: (String, ByteArray) -> Unit
) {
    val promptGenerator = remember { VideoPromptGenerator() }

    PromptGenerationCard(
        title = "Frame ${frame.frameNumber}",
        subtitle = "${frame.shotType.name.replace("_", " ")} - ${frame.duration}s",
        promptType = PromptType.SCENE,
        onGeneratePrompt = {
            promptGenerator.generateFramePrompts(
                frame = frame,
                scene = scene,
                sceneScript = sceneScript,
                characters = characters,
                platform = platform
            )
        },
        onImageSelected = { imageData ->
            onImageUploaded(frame.id, imageData)
        },
        existingImageUrl = frame.imageUrl,
        filePicker = filePicker
    )
}