package com.beekeeper.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    isPlaying: Boolean,
    showControls: Boolean,
    autoPlay: Boolean,
    onStateChange: (VideoPlayerState) -> Unit
) {
    // Desktop video player stub
    // TODO: Implement with VLC or JavaFX MediaPlayer
}
