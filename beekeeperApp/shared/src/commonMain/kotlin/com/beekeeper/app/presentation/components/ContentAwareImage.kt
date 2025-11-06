// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/components/ContentAwareImage.kt
package com.beekeeper.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeper.app.domain.model.CharacterData
import com.beekeeper.app.domain.model.ContentType
import com.beekeeper.app.domain.model.Frame
import com.beekeeper.app.domain.model.Scene

/**
 * Content state for images in creation pipeline
 */
enum class ContentState {
    NOT_CREATED,      // Content doesn't exist yet
    GENERATING,       // AI is generating the content
    PROCESSING,       // Post-processing or uploading
    READY,           // Image URL exists and is ready
    ERROR,           // Generation failed
    PLACEHOLDER      // Using placeholder/mock content
}

/**
 * Smart image component that handles various states of content creation
 */
@Composable
fun ContentAwareImage(
    imageUrl: String?,
    contentState: ContentState,
    contentType: ContentType,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onGenerateClick: (() -> Unit)? = null,
    onRetryClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A2A))
    ) {
        when {
            // Image exists and is ready
            !imageUrl.isNullOrEmpty() && contentState == ContentState.READY -> {
                CineFillerImage(
                    url = imageUrl,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Content is being generated
            contentState == ContentState.GENERATING -> {
                GeneratingContent(contentType)
            }
            
            // Content is processing
            contentState == ContentState.PROCESSING -> {
                ProcessingContent(contentType)
            }
            
            // Error occurred
            contentState == ContentState.ERROR -> {
                ErrorContent(
                    contentType = contentType,
                    onRetryClick = onRetryClick
                )
            }
            
            // Content not created yet
            contentState == ContentState.NOT_CREATED -> {
                NotCreatedContent(
                    contentType = contentType,
                    onGenerateClick = onGenerateClick
                )
            }
            
            // Placeholder/mock content
            else -> {
                PlaceholderContent(contentType)
            }
        }
    }
}



@Composable
private fun NotCreatedContent(
    contentType: ContentType,
    onGenerateClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = getIconForContentType(contentType),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = getPlaceholderText(contentType),
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        
        if (onGenerateClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onGenerateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2)
                )
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate")
            }
        }
    }
}

@Composable
private fun GeneratingContent(contentType: ContentType) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color(0xFF4A90E2)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Generating ${contentType.name.lowercase().replace('_', ' ')}...",
                color = Color.White,
                fontSize = 12.sp
            )
            
            // Animated generating effect
            AnimatedDots()
        }
    }
}

@Composable
private fun ProcessingContent(contentType: ContentType) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF4A90E2)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Processing...",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ErrorContent(
    contentType: ContentType,
    onRetryClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Color(0xFFFF5252)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Generation failed",
            color = Color(0xFFFF5252),
            fontSize = 14.sp
        )
        
        if (onRetryClick != null) {
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = onRetryClick) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun PlaceholderContent(contentType: ContentType) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = getPlaceholderGradient(contentType)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getIconForContentType(contentType),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.White.copy(alpha = 0.7f)
            )
            
            Text(
                text = "Placeholder",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun AnimatedDots() {
    var dotCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            dotCount = (dotCount + 1) % 4
        }
    }
    
    Text(
        text = ".".repeat(dotCount),
        color = Color.White,
        fontSize = 16.sp,
        modifier = Modifier.width(20.dp)
    )
}

// Helper functions
private fun getIconForContentType(contentType: ContentType): ImageVector {
    return when (contentType) {
        ContentType.STORYBOARD_FRAME -> Icons.Default.ViewCarousel
        ContentType.SCENE_PREVIEW -> Icons.Default.Movie
        ContentType.CHARACTER_AVATAR -> Icons.Default.Person
        ContentType.BACKGROUND -> Icons.Default.Landscape
        ContentType.PROP -> Icons.Default.Category
        ContentType.VFX_PREVIEW -> Icons.Default.AutoAwesome
        ContentType.THUMBNAIL -> Icons.Default.Image
        else -> Icons.Default.Image
    }
}

private fun getPlaceholderText(contentType: ContentType): String {
    return when (contentType) {
        ContentType.STORYBOARD_FRAME -> "Frame not generated yet"
        ContentType.SCENE_PREVIEW -> "Scene preview pending"
        ContentType.CHARACTER_AVATAR -> "Avatar not created"
        ContentType.BACKGROUND -> "Background needed"
        ContentType.PROP -> "Prop not designed"
        ContentType.VFX_PREVIEW -> "VFX preview pending"
        ContentType.THUMBNAIL -> "Thumbnail pending"
        else -> "pending"
    }
}

private fun getPlaceholderGradient(contentType: ContentType): List<Color> {
    return when (contentType) {
        ContentType.STORYBOARD_FRAME -> listOf(Color(0xFF3A3A3A), Color(0xFF2A2A2A))
        ContentType.SCENE_PREVIEW -> listOf(Color(0xFF4A4A6A), Color(0xFF2A2A4A))
        ContentType.CHARACTER_AVATAR -> listOf(Color(0xFF5A4A5A), Color(0xFF3A2A3A))
        ContentType.BACKGROUND -> listOf(Color(0xFF4A5A6A), Color(0xFF2A3A4A))
        ContentType.PROP -> listOf(Color(0xFF5A5A4A), Color(0xFF3A3A2A))
        ContentType.VFX_PREVIEW -> listOf(Color(0xFF6A4A5A), Color(0xFF4A2A3A))
        ContentType.THUMBNAIL -> listOf(Color(0xFF4A4A4A), Color(0xFF2A2A2A))
        else -> listOf(Color(0xFF4A4A4A), Color(0xFF2A2A2A))
    }
}

/**
 * Frame-specific image component with generation states
 */
@Composable
fun FrameImage(
    frame: Frame,
    isGenerating: Boolean = false,
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentState = when {
        isGenerating -> ContentState.GENERATING
        !frame.imageUrl.isNullOrEmpty() -> ContentState.READY
        else -> ContentState.NOT_CREATED
    }
    
    ContentAwareImage(
        imageUrl = frame.imageUrl,
        contentState = contentState,
        contentType = ContentType.STORYBOARD_FRAME,
        modifier = modifier,
        contentDescription = "Frame ${frame.frameNumber}: ${frame.description}",
        onGenerateClick = if (contentState == ContentState.NOT_CREATED) onGenerateClick else null
    )
}

/**
 * Scene preview with generation support
 */
@Composable
fun ScenePreviewImage(
    scene: Scene,
    modifier: Modifier = Modifier,
    onGeneratePreview: (() -> Unit)? = null
) {
    val hasImage = !scene.imageUrl.isNullOrEmpty()
    val firstFrameImage = scene.frames.firstOrNull()?.imageUrl
    
    ContentAwareImage(
        imageUrl = scene.imageUrl ?: firstFrameImage,
        contentState = when {
            hasImage -> ContentState.READY
            firstFrameImage != null -> ContentState.PLACEHOLDER
            else -> ContentState.NOT_CREATED
        },
        contentType = ContentType.SCENE_PREVIEW,
        modifier = modifier,
        contentDescription = "Scene ${scene.sceneNumber}: ${scene.title}",
        onGenerateClick = onGeneratePreview
    )
}

/**
 * Avatar with generation states
 */
@Composable
fun GeneratableAvatar(
    character: CharacterData,
    modifier: Modifier = Modifier,
    onGenerateAvatar: (() -> Unit)? = null
) {
    ContentAwareImage(
        imageUrl = character.avatarUrl,
        contentState = if (character.avatarUrl != null) ContentState.READY else ContentState.NOT_CREATED,
        contentType = ContentType.CHARACTER_AVATAR,
        modifier = modifier
            .size(80.dp)
            .clip(androidx.compose.foundation.shape.CircleShape),
        contentDescription = "${character.name} avatar",
        onGenerateClick = onGenerateAvatar
    )
}
