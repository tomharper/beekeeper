// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/ui/components/MediaUploadComponents.kt
package com.beekeeper.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.presentation.media.MediaUploadViewModel
import com.beekeeper.app.presentation.media.MediaUploadState
import com.beekeeper.app.utils.FilePicker
import com.beekeeper.app.utils.FilePickerConfig
import com.beekeeper.app.utils.PickedFile
import kotlinx.coroutines.launch

/**
 * Main media upload button with dropdown options
 * @param filePicker Platform-specific file picker instance
 */
@Composable
fun MediaUploadButton(
    projectId: String,
    filePicker: FilePicker,
    modifier: Modifier = Modifier,
    onUploadComplete: (Asset) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val uploadViewModel: MediaUploadViewModel = viewModel()

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(
                Icons.Filled.CloudUpload,
                contentDescription = "Upload",
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Upload Media")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Upload Images") },
                leadingIcon = { Icon(Icons.Filled.Image, null) },
                onClick = {
                    expanded = false
                    scope.launch {
                        filePicker.pickImages(
                            FilePickerConfig(
                                allowMultiple = true,
                                allowedMimeTypes = listOf("image/jpeg", "image/png", "image/webp")
                            )
                        ).forEach { file ->
                            uploadViewModel.uploadMedia(file, projectId)
                        }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Upload Video") },
                leadingIcon = { Icon(Icons.Filled.VideoLibrary, null) },
                onClick = {
                    expanded = false
                    scope.launch {
                        filePicker.pickVideo(
                            FilePickerConfig(
                                maxFileSize = 500 * 1024 * 1024 // 500MB
                            )
                        )?.let { file ->
                            uploadViewModel.uploadMedia(file, projectId)
                        }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Upload Audio") },
                leadingIcon = { Icon(Icons.Filled.AudioFile, null) },
                onClick = {
                    expanded = false
                    scope.launch {
                        filePicker.pickAudio()?.let { file ->
                            uploadViewModel.uploadMedia(file, projectId)
                        }
                    }
                }
            )
        }
    }
}

/**
 * Simple upload button without file picker for UI preview
 */
@Composable
fun MediaUploadButtonPreview(
    projectId: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(
                Icons.Filled.CloudUpload,
                contentDescription = "Upload",
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Upload Media")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Upload Images") },
                leadingIcon = { Icon(Icons.Filled.Image, null) },
                onClick = { expanded = false }
            )

            DropdownMenuItem(
                text = { Text("Upload Video") },
                leadingIcon = { Icon(Icons.Filled.VideoLibrary, null) },
                onClick = { expanded = false }
            )

            DropdownMenuItem(
                text = { Text("Upload Audio") },
                leadingIcon = { Icon(Icons.Filled.AudioFile, null) },
                onClick = { expanded = false }
            )
        }
    }
}

/**
 * Drag and drop upload area
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaDropZone(
    projectId: String,
    filePicker: FilePicker? = null,
    modifier: Modifier = Modifier
) {
    val uploadViewModel: MediaUploadViewModel = viewModel()
    var isDragging by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(
                width = 2.dp,
                color = if (isDragging) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Drag and drop media files here",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                "or click to browse",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            if (filePicker != null) {
                MediaUploadButton(
                    projectId = projectId,
                    filePicker = filePicker,
                    onUploadComplete = { /* Handle completion */ }
                )
            } else {
                MediaUploadButtonPreview(projectId = projectId)
            }
        }
    }
}

/**
 * Upload progress indicator
 */
@Composable
fun UploadProgressItem(
    progress: UploadProgress,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uploadViewModel: MediaUploadViewModel = viewModel()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on status
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                when (progress.status) {
                    UploadStatus.IN_PROGRESS -> {
                        CircularProgressIndicator(
                            progress = progress.progress,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                    UploadStatus.COMPLETED -> {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    UploadStatus.FAILED -> {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    else -> {
                        Icon(
                            Icons.Filled.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            // Upload details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    uploadViewModel.getProgressText(progress),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                uploadViewModel.getTimeRemainingText(progress)?.let { timeText ->
                    Text(
                        timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (progress.status == UploadStatus.IN_PROGRESS) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = progress.progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Action buttons
            when (progress.status) {
                UploadStatus.IN_PROGRESS -> {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Cancel, contentDescription = "Cancel")
                    }
                }
                UploadStatus.FAILED -> {
                    IconButton(onClick = onRetry) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Retry")
                    }
                }
                else -> {}
            }
        }
    }
}

/**
 * Upload queue display
 */
@Composable
fun UploadQueue(
    modifier: Modifier = Modifier
) {
    val uploadViewModel: MediaUploadViewModel = viewModel()
    val state by uploadViewModel.state.collectAsState()

    if (state.uploads.isNotEmpty()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Uploads",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            state.uploads.forEach { (assetId, progress) ->
                UploadProgressItem(
                    progress = progress,
                    onCancel = { uploadViewModel.cancelUpload(assetId) },
                    onRetry = { uploadViewModel.retryUpload(assetId) }
                )
            }
        }
    }
}

/**
 * Recent uploads display
 */
@Composable
fun RecentUploads(
    modifier: Modifier = Modifier,
    onAssetClick: (Asset) -> Unit = {}
) {
    val uploadViewModel: MediaUploadViewModel = viewModel()
    val state by uploadViewModel.state.collectAsState()

    if (state.recentUploads.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(
                "Recent Uploads",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.recentUploads.size) { index ->
                    RecentUploadCard(
                        asset = state.recentUploads[index],
                        onClick = { onAssetClick(state.recentUploads[index]) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentUploadCard(
    asset: Asset,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Thumbnail placeholder
            Icon(
                when (asset.type) {
                    AssetType.IMAGE -> Icons.Filled.Image
                    AssetType.VIDEO -> Icons.Filled.VideoLibrary
                    AssetType.AUDIO -> Icons.Filled.AudioFile
                    else -> Icons.Filled.Description
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Type badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                    .padding(4.dp)
            ) {
                Icon(
                    getAssetTypeIcon(asset.type),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

private fun getAssetTypeIcon(type: AssetType): ImageVector {
    return when (type) {
        AssetType.IMAGE -> Icons.Filled.Image
        AssetType.VIDEO -> Icons.Filled.VideoLibrary
        AssetType.AUDIO -> Icons.Filled.AudioFile
        AssetType.AVATAR -> Icons.Filled.Person
        AssetType.STYLE -> Icons.Filled.Palette
        AssetType.FONT -> Icons.Filled.FontDownload
        AssetType.TEMPLATE -> Icons.Filled.Dashboard
        AssetType.DOCUMENT -> Icons.Filled.Description
        AssetType.MODEL_3D -> Icons.Filled.ModelTraining
    }
}

/**
 * Error display
 */
@Composable
fun UploadErrorMessage(
    error: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(Modifier.width(12.dp))

            Text(
                error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}