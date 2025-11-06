// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/media/MediaUploadService.kt
package com.beekeeper.app.data.media

import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.Flow

// Common interface that all platforms must implement
expect class MediaUploadService {
    suspend fun uploadImage(
        projectId: String,
        file: ByteArray,
        filename: String,
        mimeType: String = "image/jpeg",
        tags: List<String> = emptyList(),
        metadata: Map<String, Any>? = null
    ): Flow<UploadProgress>

    suspend fun uploadVideo(
        projectId: String,
        file: ByteArray,
        filename: String,
        mimeType: String = "video/mp4",
        tags: List<String> = emptyList(),
        autoTranscode: Boolean = true
    ): Flow<UploadProgress>

    suspend fun uploadAudio(
        projectId: String,
        file: ByteArray,
        filename: String,
        mimeType: String = "audio/mpeg",
        tags: List<String> = emptyList()
    ): Flow<UploadProgress>

    suspend fun cancelUpload(assetId: String)

    suspend fun uploadBatch(
        projectId: String,
        files: List<Pair<ByteArray, String>>,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<Result<Asset>>
}
