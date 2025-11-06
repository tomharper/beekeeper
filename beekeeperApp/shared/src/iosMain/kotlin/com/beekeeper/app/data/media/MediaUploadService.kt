// ===================================================================
// File: shared/src/iosMain/kotlin/com/cinefiller/fillerapp/data/media/MediaUploadService.kt
// iOS ACTUAL IMPLEMENTATION - Uses Ktor with Darwin engine
// ===================================================================
package com.beekeeper.app.data.media

import com.beekeeper.app.domain.model.*
import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

actual class MediaUploadService {
    private val httpClient = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
    
    private val baseUrl = "https://api.cinefiller.com"
    private val activeUploads = mutableMapOf<String, Boolean>()

    actual suspend fun uploadImage(
        projectId: String,
        file: ByteArray,
        filename: String,
        mimeType: String,
        tags: List<String>,
        metadata: Map<String, Any>?
    ): Flow<UploadProgress> = flow {
        // iOS implementation similar to Android but using Darwin engine
        val assetId = generateAssetId()
        
        emit(UploadProgress(
            assetId = assetId,
            progress = 0f,
            status = UploadStatus.IN_PROGRESS,
            error = null
        ))

        try {
            val response = httpClient.submitFormWithBinaryData(
                url = "$baseUrl/api/upload/image",
                formData = formData {
                    append("projectId", projectId)
                    append("assetId", assetId)
                    append("tags", tags.joinToString(","))
                    append("file", file, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                    })
                }
            )

            if (response.status == HttpStatusCode.OK) {
                emit(UploadProgress(
                    assetId = assetId,
                    progress = 1f,
                    status = UploadStatus.COMPLETED,
                    error = null
                ))
            } else {
                throw Exception("Upload failed: ${response.status}")
            }
        } catch (e: Exception) {
            emit(UploadProgress(
                assetId = assetId,
                progress = 0f,
                status = UploadStatus.FAILED,
                error = e.message
            ))
            throw e
        }
    }

    actual suspend fun uploadVideo(
        projectId: String,
        file: ByteArray,
        filename: String,
        mimeType: String,
        tags: List<String>,
        autoTranscode: Boolean
    ): Flow<UploadProgress> = flow {
        // Reuse image upload for simplicity
        uploadImage(projectId, file, filename, mimeType, tags, null)
            .collect { emit(it) }
    }

    actual suspend fun uploadAudio(
        projectId: String,
        file: ByteArray,
        filename: String,
        mimeType: String,
        tags: List<String>
    ): Flow<UploadProgress> = flow {
        // Reuse image upload for simplicity
        uploadImage(projectId, file, filename, mimeType, tags, null)
            .collect { emit(it) }
    }

    actual suspend fun cancelUpload(assetId: String) {
        activeUploads[assetId] = false
    }

    actual suspend fun uploadBatch(
        projectId: String,
        files: List<Pair<ByteArray, String>>,
        onProgress: (Int, Int) -> Unit
    ): List<Result<Asset>> {
        val results = mutableListOf<Result<Asset>>()
        files.forEachIndexed { index, (fileData, filename) ->
            onProgress(index, files.size)
            try {
                var success = false
                uploadImage(projectId, fileData, filename, "image/jpeg", emptyList(), null)
                    .collect { progress ->
                        if (progress.status == UploadStatus.COMPLETED) {
                            success = true
                        }
                    }
                
                if (success) {
                    results.add(Result.success(createAssetFromUpload(projectId, fileData, filename)))
                } else {
                    results.add(Result.failure(Exception("Upload failed")))
                }
            } catch (e: Exception) {
                results.add(Result.failure(e))
            }
        }
        return results
    }

    private fun generateAssetId(): String {
        return "asset_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }

    private fun createAssetFromUpload(
        projectId: String,
        file: ByteArray,
        filename: String
    ): Asset {
        return GenericAsset(
            id = generateAssetId(),
            projectId = projectId,
            type = AssetType.IMAGE,
            url = "$baseUrl/assets/$filename",
            thumbnailUrl = null,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            name = filename,
            size = file.size.toLong(),
            metadata = mapOf("filename" to filename)
        )
    }
}
