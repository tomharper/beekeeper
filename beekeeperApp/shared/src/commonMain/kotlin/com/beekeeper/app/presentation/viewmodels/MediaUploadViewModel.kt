// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/media/MediaUploadViewModel.kt
package com.beekeeper.app.presentation.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.data.media.MediaUploadService
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.utils.PickedFile
import com.beekeeper.app.utils.ValidationResult
import com.beekeeper.app.utils.getCurrentTimeMillis
import com.beekeeper.app.utils.isImage
import com.beekeeper.app.utils.isVideo
import com.beekeeper.app.utils.isAudio
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MediaUploadState(
    val uploads: Map<String, UploadProgress> = emptyMap(),
    val recentUploads: List<Asset> = emptyList(),
    val isProcessing: Boolean = false,
    val error: String? = null
)

class MediaUploadViewModel(
    private val uploadService: MediaUploadService
) : ViewModel() {
    
    private val _state = MutableStateFlow(MediaUploadState())
    val state: StateFlow<MediaUploadState> = _state.asStateFlow()
    
    // Track individual upload progress
    private val uploadFlows = mutableMapOf<String, Flow<UploadProgress>>()
    
    /**
     * Upload a single media file
     */
    fun uploadMedia(
        file: PickedFile,
        projectId: String,
        tags: List<String> = emptyList(),
        metadata: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            when {
                file.isImage() -> uploadImage(file, projectId, tags, metadata)
                file.isVideo() -> uploadVideo(file, projectId, tags)
                file.isAudio() -> uploadAudio(file, projectId, tags)
                else -> {
                    _state.update { it.copy(error = "Unsupported file type: ${file.mimeType}") }
                }
            }
        }
    }
    
    /**
     * Upload multiple media files
     */
    fun uploadMultipleMedia(
        files: List<PickedFile>,
        projectId: String,
        tags: List<String> = emptyList()
    ) {
        files.forEach { file ->
            uploadMedia(file, projectId, tags)
        }
    }
    
    /**
     * Cancel an ongoing upload
     */
    fun cancelUpload(assetId: String) {
        viewModelScope.launch {
            try {
                uploadService.cancelUpload(assetId)
                _state.update { state ->
                    state.copy(
                        uploads = state.uploads - assetId
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to cancel upload: ${e.message}") }
            }
        }
    }
    
    /**
     * Retry a failed upload
     */
    fun retryUpload(assetId: String) {
        val failedUpload = _state.value.uploads[assetId]
        if (failedUpload?.status == UploadStatus.FAILED) {
            // In a real implementation, you'd store the original file data
            // For now, just remove the failed upload
            _state.update { state ->
                state.copy(uploads = state.uploads - assetId)
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    // Private upload functions
    
    private suspend fun uploadImage(
        file: PickedFile,
        projectId: String,
        tags: List<String>,
        metadata: Map<String, Any>?
    ) {
        try {
            _state.update { it.copy(isProcessing = true) }
            
            uploadService.uploadImage(
                projectId = projectId,
                file = file.bytes,
                filename = file.name,
                mimeType = file.mimeType,
                tags = tags,
                metadata = metadata
            ).collect { progress ->
                updateUploadProgress(progress)
                
                if (progress.status == UploadStatus.COMPLETED) {
                    // Add to recent uploads
                    handleUploadComplete(progress.assetId, AssetType.IMAGE)
                }
            }
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    error = "Failed to upload image: ${e.message}",
                    isProcessing = false
                )
            }
        }
    }
    
    private suspend fun uploadVideo(
        file: PickedFile,
        projectId: String,
        tags: List<String>
    ) {
        try {
            _state.update { it.copy(isProcessing = true) }
            
            uploadService.uploadVideo(
                projectId = projectId,
                file = file.bytes,
                filename = file.name,
                mimeType = file.mimeType,
                tags = tags,
                autoTranscode = true
            ).collect { progress ->
                updateUploadProgress(progress)
                
                if (progress.status == UploadStatus.COMPLETED) {
                    handleUploadComplete(progress.assetId, AssetType.VIDEO)
                }
            }
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    error = "Failed to upload video: ${e.message}",
                    isProcessing = false
                )
            }
        }
    }
    
    private suspend fun uploadAudio(
        file: PickedFile,
        projectId: String,
        tags: List<String>
    ) {
        try {
            _state.update { it.copy(isProcessing = true) }
            
            uploadService.uploadAudio(
                projectId = projectId,
                file = file.bytes,
                filename = file.name,
                mimeType = file.mimeType,
                tags = tags
            ).collect { progress ->
                updateUploadProgress(progress)
                
                if (progress.status == UploadStatus.COMPLETED) {
                    handleUploadComplete(progress.assetId, AssetType.AUDIO)
                }
            }
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    error = "Failed to upload audio: ${e.message}",
                    isProcessing = false
                )
            }
        }
    }
    
    private fun updateUploadProgress(progress: UploadProgress) {
        _state.update { state ->
            state.copy(
                uploads = state.uploads + (progress.assetId to progress),
                isProcessing = state.uploads.values.any { 
                    it.status == UploadStatus.IN_PROGRESS 
                }
            )
        }
    }
    
    private fun handleUploadComplete(assetId: String, assetType: AssetType) {
        // In a real app, you'd fetch the complete asset details
        val mockAsset = GenericAsset(
            id = assetId,
            type = assetType,
            name = "Uploaded ${assetType.name.lowercase()}",
            url = "https://example.com/assets/$assetId",
            thumbnailUrl = "https://example.com/assets/$assetId/thumb",
            size = 0,
            createdAt = getCurrentTimeMillis()
        )
        
        _state.update { state ->
            state.copy(
                recentUploads = (listOf(mockAsset) + state.recentUploads).take(10),
                uploads = state.uploads - assetId,
                isProcessing = state.uploads.values.any { 
                    it.assetId != assetId && it.status == UploadStatus.IN_PROGRESS 
                }
            )
        }
    }
    
    /**
     * Get formatted upload progress text
     */
    fun getProgressText(progress: UploadProgress): String {
        return when (progress.status) {
            UploadStatus.PENDING -> "Preparing upload..."
            UploadStatus.IN_PROGRESS -> {
                val percent = (progress.progress * 100).toInt()
                val uploaded = formatBytes(progress.bytesUploaded ?: 0)
                val total = formatBytes(progress.totalBytes ?: 0)
                "$percent% ($uploaded / $total)"
            }
            UploadStatus.COMPLETED -> "Upload complete"
            UploadStatus.FAILED -> "Upload failed: ${progress.error}"
            UploadStatus.CANCELLED -> "Upload cancelled"
            UploadStatus.PAUSED -> "Upload paused"
        }
    }
    
    /**
     * Get estimated time remaining text
     */
    fun getTimeRemainingText(progress: UploadProgress): String? {
        val seconds = progress.estimatedTimeRemaining ?: return null
        return when {
            seconds < 60 -> "${seconds}s remaining"
            seconds < 3600 -> "${seconds / 60}m remaining"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m remaining"
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> {
                val gb = bytes / (1024.0 * 1024.0 * 1024.0)
                val rounded = (gb * 100).toInt() / 100.0
                "$rounded GB"
            }
        }
    }
}
