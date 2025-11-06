// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/ui/components/ImageDisplay.kt
package com.beekeeper.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

/**
 * Main image display component using Kamel
 */
@Composable
fun CineFillerImage(
    url: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showLoading: Boolean = true,
    fallbackImage: @Composable () -> Unit = { DefaultImageFallback() }
) {
    KamelImage(
        resource = asyncPainterResource(url),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        onLoading = { progress ->
            if (showLoading) {
                ImageLoadingIndicator(progress)
            }
        },
        onFailure = {
            fallbackImage()
        }
    )
}

/**
 * Image with thumbnail support - loads thumbnail first, then full image
 */
@Composable
fun ThumbnailImage(
    thumbnailUrl: String,
    fullImageUrl: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var loadFullImage by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Load thumbnail first
        if (!loadFullImage) {
            KamelImage(
                resource = asyncPainterResource(thumbnailUrl),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }

        // Load full image on top
        if (loadFullImage) {
            CineFillerImage(
                url = fullImageUrl,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                showLoading = false // Don't show loading since thumbnail is visible
            )
        }
    }
}

/**
 * Avatar image with circular shape and fallback
 */
@Composable
fun AvatarImage(
    url: String,
    name: String = "",
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    KamelImage(
        resource = asyncPainterResource(url),
        contentDescription = "Avatar for $name",
        modifier = modifier
            .size(size.dp)
            .clip(androidx.compose.foundation.shape.CircleShape),
        contentScale = ContentScale.Crop,
        onLoading = {
            AvatarPlaceholder(name = name, size = size)
        },
        onFailure = {
            AvatarPlaceholder(name = name, size = size)
        }
    )
}

/**
 * Media grid item with aspect ratio preservation
 */
@Composable
fun MediaGridImage(
    url: String,
    thumbnailUrl: String? = null,
    contentDescription: String? = null,
    aspectRatio: Float = 1f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(8.dp)),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (thumbnailUrl != null) {
            ThumbnailImage(
                thumbnailUrl = thumbnailUrl,
                fullImageUrl = url,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CineFillerImage(
                url = url,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Loading indicator for images
 */
@Composable
private fun ImageLoadingIndicator(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (progress > 0f && progress < 1f) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 60.dp)
            )
        } else {
            // Indeterminate progress
            val infiniteTransition = rememberInfiniteTransition()
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing)
                )
            )

            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
        }
    }
}

/**
 * Default fallback for failed image loads
 */
@Composable
private fun DefaultImageFallback() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.BrokenImage,
                contentDescription = "Failed to load image",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Failed to load",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Avatar placeholder with initials
 */
@Composable
private fun AvatarPlaceholder(
    name: String,
    size: Int
) {
    val initials = name
        .split(" ")
        .take(2)
        .map { it.firstOrNull()?.uppercase() ?: "" }
        .joinToString("")
        .ifEmpty { "?" }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Aspect ratio modifier
 */
fun Modifier.aspectRatio(ratio: Float): Modifier = this.then(
    Modifier.fillMaxWidth().aspectRatio(ratio)
)

/**
 * Image carousel component
 */
@Composable
fun ImageCarousel(
    images: List<String>,
    modifier: Modifier = Modifier,
    autoScroll: Boolean = false,
    autoScrollDelay: Long = 1000
) {
    var currentIndex by remember { mutableStateOf(0) }

    if (autoScroll && images.isNotEmpty()) {
        LaunchedEffect(currentIndex) {
            currentIndex = (currentIndex + 1) % images.size
        }
    }

    Box(modifier = modifier) {
        androidx.compose.foundation.pager.HorizontalPager(
            state = androidx.compose.foundation.pager.rememberPagerState(
                initialPage = currentIndex,
                pageCount = { images.size }
            ),
            modifier = Modifier.fillMaxSize()
        ) { page ->
            CineFillerImage(
                url = images[page],
                contentDescription = "Image ${page + 1} of ${images.size}",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Page indicators
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(
                                if (index == currentIndex) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

/**
 * Zoomable image viewer
 */
@Composable
fun ZoomableImage(
    url: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        CineFillerImage(
            url = url,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }
}