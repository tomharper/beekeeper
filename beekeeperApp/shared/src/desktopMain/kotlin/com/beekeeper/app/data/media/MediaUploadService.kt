// ===================================================================
// File: shared/src/desktopMain/kotlin/com/cinefiller/fillerapp/data/media/MediaUploadService.kt
// DESKTOP ACTUAL IMPLEMENTATION - Uses Ktor with CIO engine
// ===================================================================
package com.beekeeper.app.data.media

import com.beekeeper.app.domain.model.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
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
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 30000
        }
    }
    
    private val baseUrl = "https://api.cinefiller.com"

    actual suspend fun uploadImage(
        projectId: String,
        file: ByteArray,
        filename: String,
        mimeType: String,
        tags: List<String>,
        metadata: Map<String, Any>?
    ): Flow<UploadProgress> = flow {
        // Desktop implementation similar to Android
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
            }
        } catch (e: Exception) {
            emit(UploadProgress(
                assetId = assetId,
                progress = 0f,
                status = UploadStatus.FAILED,
                error = e.message
            ))
        }
    }

    actual suspend fun uploadVideo(
        projectId: String,
        file: ByteArray,
        filename: String,
        mimeType: String,
        tags: List<String>,
        autoTranscode: Boolean
    ): Flow<UploadProgress> = uploadImage(projectId, file, filename, mimeType, tags, null)

    actual suspend fun uploadAudio(
        projectId: String,
        file: ByteArray,
        filename: String,
        mimeType: String,
        tags: List<String>
    ): Flow<UploadProgress> = uploadImage(projectId, file, filename, mimeType, tags, null)

    actual suspend fun cancelUpload(assetId: String) {
        // Desktop cancellation logic
    }

    actual suspend fun uploadBatch(
        projectId: String,
        files: List<Pair<ByteArray, String>>,
        onProgress: (Int, Int) -> Unit
    ): List<Result<Asset>> {
        return files.mapIndexed { index, (fileData, filename) ->
            onProgress(index, files.size)
            try {
                Result.success(GenericAsset(
                    id = generateAssetId(),
                    projectId = projectId,
                    type = AssetType.IMAGE,
                    url = "$baseUrl/assets/$filename",
                    thumbnailUrl = null,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                    size = fileData.size.toLong(),
                    metadata = mapOf("filename" to filename)
                ))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun generateAssetId(): String {
        return "asset_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
