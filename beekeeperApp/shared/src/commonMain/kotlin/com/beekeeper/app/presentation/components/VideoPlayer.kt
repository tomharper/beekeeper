// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/components/VideoPlayer.kt
package com.beekeeper.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Video player state
 */
data class VideoPlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val isFullscreen: Boolean = false,
    val volume: Float = 1f,
    val error: String? = null
)

/**
 * Multiplatform video player interface
 */
@Composable
expect fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    showControls: Boolean = true,
    autoPlay: Boolean = false,
    onStateChange: (VideoPlayerState) -> Unit = {}
)

/**
 * Video player with controls wrapper
 */
@Composable
fun VideoPlayerWithControls(
    url: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    thumbnailUrl: String? = null
) {
    var playerState by remember { mutableStateOf(VideoPlayerState()) }
    var showControls by remember { mutableStateOf(true) }
    
    Box(modifier = modifier) {
        VideoPlayer(
            url = url,
            modifier = Modifier.fillMaxSize(),
            isPlaying = playerState.isPlaying,
            showControls = false, // We'll handle controls ourselves
            autoPlay = autoPlay,
            onStateChange = { state ->
                playerState = state
            }
        )
        
        // Overlay controls
        if (showControls && !playerState.isLoading) {
            VideoControls(
                state = playerState,
                onPlayPauseClick = {
                    playerState = playerState.copy(isPlaying = !playerState.isPlaying)
                },
                onSeek = { position ->
                    // Seeking will be handled by platform implementation
                },
                onVolumeChange = { volume ->
                    playerState = playerState.copy(volume = volume)
                },
                onFullscreenClick = {
                    playerState = playerState.copy(isFullscreen = !playerState.isFullscreen)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Loading indicator
        if (playerState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White
                )
            }
        }
        
        // Error display
        playerState.error?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        error,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Video player controls overlay
 */
@Composable
private fun VideoControls(
    state: VideoPlayerState,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        // Center play/pause button
        IconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
        ) {
            Icon(
                if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (state.isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        
        // Bottom controls bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = Color.White
                )
            }
            
            // Time display
            Text(
                "${formatTime(state.currentPosition)} / ${formatTime(state.duration)}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            
            // Progress slider
            Slider(
                value = if (state.duration > 0) {
                    state.currentPosition.toFloat() / state.duration.toFloat()
                } else 0f,
                onValueChange = { value ->
                    onSeek((value * state.duration).toLong())
                },
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
            
            // Volume
            IconButton(onClick = { /* Toggle mute */ }) {
                Icon(
                    if (state.volume > 0) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = "Volume",
                    tint = Color.White
                )
            }
            
            // Fullscreen
            IconButton(onClick = onFullscreenClick) {
                Icon(
                    if (state.isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Video thumbnail with play button
 */
@Composable
fun VideoThumbnail(
    thumbnailUrl: String,
    duration: Long? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            CineFillerImage(
                url = thumbnailUrl,
                contentDescription = "Video thumbnail",
                modifier = Modifier.fillMaxSize()
            )
            
            // Play button overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play video",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        tint = Color.Black
                    )
                }
            }
            
            // Duration badge
            duration?.let {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        formatTime(duration),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Format time in mm:ss or hh:mm:ss
 */
private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return if (hours > 0) {
        "$hours:${(minutes % 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"
    } else {
        "$minutes:${(seconds % 60).toString().padStart(2, '0')}"
    }
}
