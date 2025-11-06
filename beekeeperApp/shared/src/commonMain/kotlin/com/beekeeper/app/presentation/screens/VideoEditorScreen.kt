package com.beekeeper.app.presentation.screens

import EditingTool
import Keyframe
import TextAlignment
import Timeline
import VideoEffect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.utils.formatTime
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Enhanced Data Classes with Superset of Properties
data class VideoElement(
    val id: String,
    val type: VideoElementType,
    val content: String? = null,
    val position: Position = Position(0f, 0f), // Using Position object for consistency
    val x: Float = 0f, // Backward compatibility
    val y: Float = 0f, // Backward compatibility
    val size: Float = 24f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val startTime: Float = 0f,
    val endTime: Float = 5f,
    val duration: Float = 5f,
    val opacity: Float = 1f,
    val color: String = "#FFFFFF",
    val backgroundColor: String? = null,
    val animation: TextAnimation? = null,
    val effects: List<VideoEffect> = emptyList(),
    val keyframes: List<Keyframe> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

enum class VideoElementType {
    TEXT, EMOJI, IMAGE, STICKER, SHAPE, EFFECT, AUDIO, TRANSITION
}

// Enhanced TextElement with all properties
data class TextElement(
    val id: String,
    val text: String,
    val position: Position = Position(0f, 0f),
    val x: Float = 0f, // Backward compatibility
    val y: Float = 0f, // Backward compatibility
    val fontSize: Float = 24f,
    val fontFamily: String = "System",
    val fontWeight: String = "Normal", // Added as String for flexibility
    val fontStyle: String = "Normal",
    val color: String = "#FFFFFF",
    val backgroundColor: String? = null,
    val startTime: Float = 0f,
    val endTime: Float = 5f,
    val duration: Float = 5f,
    val animation: TextAnimation? = null,
    val alignment: TextAlignment = TextAlignment.LEFT,
    val opacity: Float = 1f,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val shadow: Boolean = false,
    val outline: Boolean = false,
    val letterSpacing: Float = 0f,
    val lineHeight: Float = 1.2f,
    val metadata: Map<String, Any> = emptyMap()
)

// Data class for Position to maintain consistency
data class Position(
    val x: Float,
    val y: Float
)

// Video Edit Option
data class VideoEditOption(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val color: Color = Color(0xFF4A90E2),
    val name: String = "" // Added for backward compatibility
)

// Enhanced VideoEditState with all properties
data class EnhancedVideoEditState(
    val id: String = "",
    val frameId: String? = null,
    val sceneId: String? = null,
    val projectId: String,
    val videoUrl: String? = null,
    val duration: Float = 0f,
    val currentTime: Float = 0f,
    val isPlaying: Boolean = false,
    val volume: Float = 1f,
    val hasAudio: Boolean = false,
    // Effects and overlays
    val visualEffect: String? = null,
    val stylePreset: String? = null,
    val musicTrack: MusicTrack? = null,
    val hashtags: List<String> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val emojiOverlays: List<EmojiOverlay> = emptyList(),
    val videoElements: List<VideoElement> = emptyList(),
    val textElements: List<TextElement> = emptyList(),
    // Edit history
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val hasChanges: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    // Timeline
    val timeline: Timeline? = null,
    val selectedClips: Set<String> = emptySet(),
    // Tools
    val selectedTool: EditingTool = EditingTool.SELECTION,
    val zoomLevel: Float = 1.0f,
    val showGrid: Boolean = false,
    val showWaveforms: Boolean = true,
    val showThumbnails: Boolean = true,
    val errorMessage: String? = null
)

// Enhanced overlay classes with position support
data class EnhancedTextOverlay(
    val id: String,
    val text: String,
    val position: Position = Position(0f, 0f), // Using Position object
    val x: Float = 0f, // Backward compatibility
    val y: Float = 0f, // Backward compatibility
    val fontSize: Float = 24f,
    val fontFamily: String = "System",
    val fontWeight: String = "Normal",
    val color: String = "#FFFFFF",
    val backgroundColor: String? = null,
    val startTime: Float = 0f,
    val endTime: Float = 5f,
    val duration: Float = 5f,
    val animation: TextAnimation? = null
) {
    // Helper constructor for backward compatibility
    constructor(
        id: String,
        text: String,
        x: Float,
        y: Float,
        fontSize: Float = 24f,
        fontFamily: String = "System",
        color: String = "#FFFFFF",
        backgroundColor: String? = null,
        startTime: Float = 0f,
        endTime: Float = 5f,
        duration: Float = 5f,
        animation: TextAnimation? = null
    ) : this(
        id = id,
        text = text,
        position = Position(x, y),
        x = x,
        y = y,
        fontSize = fontSize,
        fontFamily = fontFamily,
        fontWeight = "Normal",
        color = color,
        backgroundColor = backgroundColor,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        animation = animation
    )
}

data class EnhancedEmojiOverlay(
    val id: String,
    val emoji: String,
    val position: Position = Position(0f, 0f), // Using Position object
    val x: Float = 0f, // Backward compatibility
    val y: Float = 0f, // Backward compatibility
    val size: Float = 48f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val startTime: Float = 0f,
    val endTime: Float = 5f,
    val duration: Float = 5f
) {
    // Helper constructor for backward compatibility
    constructor(
        id: String,
        emoji: String,
        x: Float,
        y: Float,
        size: Float = 48f,
        scale: Float = 1f,
        rotation: Float = 0f,
        startTime: Float = 0f,
        endTime: Float = 5f,
        duration: Float = 5f
    ) : this(
        id = id,
        emoji = emoji,
        position = Position(x, y),
        x = x,
        y = y,
        size = size,
        scale = scale,
        rotation = rotation,
        startTime = startTime,
        endTime = endTime,
        duration = duration
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    onAddMusic: () -> Unit,
    onAddHashtag: () -> Unit,
    onAddEmoji: () -> Unit,
    onAddText: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToStyle: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    // Convert VideoEditState to EnhancedVideoEditState
    val defaultState = EnhancedVideoEditState(projectId = "default")
    var currentState by remember { mutableStateOf(defaultState) }
    var showEditOptions by remember { mutableStateOf(false) }
    var captionText by remember { mutableStateOf("") }
    var showMusicDialog by remember { mutableStateOf(false) }
    var showHashtagDialog by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showTextEditor by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val editOptions = remember {
        listOf(
            VideoEditOption("music", "Music", Icons.Default.MusicNote, Color(0xFF9C27B0), "Music"),
            VideoEditOption("hashtag", "Hashtag", Icons.Default.Tag, Color(0xFF2196F3), "Hashtag"),
            VideoEditOption("emoji", "Emoji", Icons.Default.EmojiEmotions, Color(0xFFFF9800), "Emoji"),
            VideoEditOption("text", "Text", Icons.Default.TextFields, Color(0xFF4CAF50), "Text"),
            VideoEditOption("trim", "Trim", Icons.Default.ContentCut, Color(0xFFE91E63), "Trim"),
            VideoEditOption("speed", "Speed", Icons.Default.Speed, Color(0xFF00BCD4), "Speed"),
            VideoEditOption("filter", "Filter", Icons.Default.FilterAlt, Color(0xFF795548), "Filter"),
            VideoEditOption("volume", "Volume", Icons.Default.VolumeUp, Color(0xFF607D8B), "Volume")
        )
    }

    Scaffold(
        containerColor = Color(0xFF0A0A0A),
        topBar = {
            VideoEditorTopBar(
                hasChanges = currentState.hasChanges,
                canUndo = currentState.canUndo,
                canRedo = currentState.canRedo,
                onNavigateBack = onNavigateBack,
                onSave = onSave,
                onUndo = { /* Handle undo */ },
                onRedo = { /* Handle redo */ },
                onExport = { showExportDialog = true }
            )
        },
        bottomBar = {
            VideoEditorBottomBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToExplore = onNavigateToExplore,
                onNavigateToCreate = onNavigateToCreate,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Video Preview Area with Controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black)
                ) {
                    VideoPreviewArea(
                        state = currentState,
                        isPlaying = isPlaying,
                        onPlayPauseClick = { isPlaying = !isPlaying },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay indicators for applied effects
                    EffectIndicators(
                        state = currentState,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    )
                }

                // Timeline and Controls
                VideoTimeline(
                    duration = currentState.duration,
                    currentTime = currentState.currentTime,
                    onSeek = { time ->
                        currentState = currentState.copy(currentTime = time)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                )

                // Edit Options Grid
                EditOptionsGrid(
                    options = editOptions,
                    onOptionClick = { option ->
                        when (option.id) {
                            "music" -> showMusicDialog = true
                            "hashtag" -> showHashtagDialog = true
                            "emoji" -> showEmojiPicker = true
                            "text" -> showTextEditor = true
                            // Handle other options
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0F0F))
                )
            }
        }

        // Dialogs
        if (showEmojiPicker) {
            EmojiPickerDialog(
                onEmojiSelected = { emoji ->
                    val newOverlay = EnhancedEmojiOverlay(
                        id = getCurrentTimeMillis().toString(),
                        emoji = emoji,
                        x = 0.5f,
                        y = 0.5f,
                        startTime = currentState.currentTime,
                        duration = 5f,
                        endTime = currentState.currentTime + 5f
                    )
                    currentState = currentState.copy(
                        emojiOverlays = currentState.emojiOverlays +
                                EmojiOverlay(
                                    id = newOverlay.id,
                                    emoji = newOverlay.emoji,
                                    x = newOverlay.x,
                                    y = newOverlay.y,
                                    size = newOverlay.size,
                                    scale = newOverlay.scale,
                                    rotation = newOverlay.rotation,
                                    startTime = newOverlay.startTime,
                                    endTime = newOverlay.endTime,
                                    duration = newOverlay.duration
                                ),
                        hasChanges = true
                    )
                    showEmojiPicker = false
                },
                onDismiss = { showEmojiPicker = false }
            )
        }

        if (showTextEditor) {
            TextOverlayEditor(
                onTextAdded = { textOverlay ->
                    currentState = currentState.copy(
                        textOverlays = currentState.textOverlays + textOverlay,
                        hasChanges = true
                    )
                    showTextEditor = false
                },
                onDismiss = { showTextEditor = false }
            )
        }
    }
}

@Composable
private fun VideoPreviewArea(
    state: EnhancedVideoEditState,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Video placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2A2A2A),
                            Color(0xFF1A1A1A)
                        )
                    )
                )
        )

        // Play/Pause overlay
        IconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Text overlays visualization
        state.textOverlays.forEach { overlay ->
            val posX = if (overlay.x != 0f) overlay.x else overlay.position ?: 0f
            val posY = if (overlay.y != 0f) overlay.y else overlay.position ?: 0f

            Text(
                text = overlay.text,
                fontSize = overlay.fontSize.sp,
                color = Color.White,
                modifier = Modifier
                    .offset(
                        x = (posX * 100).dp,
                        y = (posY * 100).dp
                    )
            )
        }

        // Emoji overlays visualization
        state.emojiOverlays.forEach { overlay ->
            Text(
                text = overlay.emoji,
                fontSize = overlay.size.sp,
                modifier = Modifier
                    .offset(
                        x = (overlay.x * 100).dp,
                        y = (overlay.y * 100).dp
                    )
                    .scale(overlay.scale)
            )
        }
    }
}

@Composable
private fun VideoTimeline(
    duration: Float,
    currentTime: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Time indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatTime(currentTime.toInt()),
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                formatTime(duration.toInt()),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // Slider
        Slider(
            value = if (duration > 0) currentTime / duration else 0f,
            onValueChange = { onSeek(it * duration) },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4A90E2),
                activeTrackColor = Color(0xFF4A90E2),
                inactiveTrackColor = Color.Gray
            )
        )
    }
}

@Composable
private fun EditOptionsGrid(
    options: List<VideoEditOption>,
    onOptionClick: (VideoEditOption) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = options,
            key = { it.id }
        ) { option ->
            EditOptionButton(
                option = option,
                onClick = { onOptionClick(option) }
            )
        }
    }
}

@Composable
private fun EditOptionButton(
    option: VideoEditOption,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A2A))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(option.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.label,
                tint = option.color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = option.label,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoEditorTopBar(
    hasChanges: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExport: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        title = {
            Text(
                "Video Editor",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(
                onClick = onUndo,
                enabled = canUndo
            ) {
                Icon(
                    Icons.Default.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) Color.White else Color.Gray
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = canRedo
            ) {
                Icon(
                    Icons.Default.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) Color.White else Color.Gray
                )
            }

            if (hasChanges) {
                TextButton(onClick = onSave) {
                    Text("Save", color = Color(0xFF4A90E2))
                }
            }

            IconButton(onClick = onExport) {
                Icon(Icons.Default.Share, contentDescription = "Export", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0A0A0A)
        )
    )
}

@Composable
private fun VideoEditorBottomBar(
    onNavigateToHome: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0F0F0F)
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToExplore,
            icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
            label = { Text("Explore") }
        )
        NavigationBarItem(
            selected = true,
            onClick = onNavigateToCreate,
            icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
            label = { Text("Create") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToProfile,
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Composable
private fun EffectIndicators(
    state: EnhancedVideoEditState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        state.visualEffect?.let { effect ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF4A90E2).copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color(0xFF4A90E2))
            ) {
                Text(
                    text = effect,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        state.stylePreset?.let { style ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF9C27B0).copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color(0xFF9C27B0))
            ) {
                Text(
                    text = style,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EmojiPickerDialog(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val emojis = listOf(
        "😀", "😃", "😄", "😁", "😅", "😂", "🤣", "😊", "😇", "🙂",
        "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛", "😜", "🤪",
        "🤨", "🧐", "🤓", "😎", "🥸", "🤩", "🥳", "😏", "😒", "😞",
        "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺",
        "😢", "😭", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶",
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "✨", "💫", "⭐", "🌟", "💥", "💢", "💯", "🔥", "⚡", "🌈",
        "🌙", "⚡", "🎵", "🎶"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A1A1A)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Select Emoji",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(400.dp)
                ) {
                    items(
                        items = emojis,
                        key = { it }
                    ) { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 32.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEmojiSelected(emoji) }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextOverlayEditor(
    onTextAdded: (TextOverlay) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var fontSize by remember { mutableStateOf(24f) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A1A1A)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Add Text Overlay",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Enter text") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Font Size: ${fontSize.toInt()}", color = Color.White)
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 12f..72f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                onTextAdded(
                                    TextOverlay(
                                        id = getCurrentTimeMillis().toString(),
                                        text = text,
                                        x = 0.5f,
                                        y = 0.5f,
                                        fontSize = fontSize,
                                        fontFamily = "System",
                                        color = "#FFFFFF",
                                        backgroundColor = null,
                                        startTime = 0f,
                                        endTime = 5f,
                                        duration = 5f,
                                        animation = null
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A90E2)
                        )
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

// Helper extension functions for compatibility
private val TextOverlay.position: Position?
    get() = if (this.x != 0f || this.y != 0f) Position(this.x, this.y) else null

private val EmojiOverlay.position: Position
    get() = Position(this.x, this.y)