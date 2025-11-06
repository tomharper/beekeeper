// ImageDownloadService.kt
package com.beekeeper.app.data.media

import com.beekeeper.app.domain.repository.FrameRepository
import com.beekeeper.app.domain.repository.ContentRepository
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Service for systematic downloading of images from the content hierarchy
 * Supports downloading at various levels: project, storyboard, scene, or frame
 */
class ImageDownloadService(
    private val frameRepository: FrameRepository,
    private val contentRepository: ContentRepository,
    private val backendFileService: BackendFileService // For getting signed URLs
) {

    /**
     * Download progress tracking
     */
    data class DownloadProgress(
        val totalImages: Int,
        val downloadedImages: Int,
        val currentImage: String? = null,
        val progress: Float = 0f
    )

    /**
     * Result of a download operation
     */
    data class DownloadResult(
        val frameId: String,
        val imagePath: String,
        val localPath: String? = null,
        val success: Boolean,
        val error: String? = null
    )

    /**
     * Download all images for a specific frame
     */
    suspend fun downloadFrameImage(
        projectId: String,
        frameId: String,
        customerAlias: String
    ): DownloadResult {
        return try {
            val frame = frameRepository.getFrameById(frameId)
                ?: return DownloadResult(frameId, "", null, false, "Frame not found")

            if (frame.imageUrl.isNullOrEmpty()) {
                return DownloadResult(frameId, "", null, false, "No image URL")
            }

            val signedUrl = backendFileService.getSignedDownloadUrl(
                customerAlias = customerAlias,
                projectId = projectId,
                assetType = "images",
                version = "v1",
                filename = frame.imageUrl!!.substringAfterLast("/")
            )

            // Platform-specific download implementation would go here
            // For now, return the signed URL
            DownloadResult(
                frameId = frameId,
                imagePath = frame.imageUrl!!,
                localPath = signedUrl,
                success = true
            )
        } catch (e: Exception) {
            DownloadResult(frameId, "", null, false, e.message)
        }
    }

    /**
     * Download all images for a scene with progress tracking
     */
    fun downloadSceneImages(
        projectId: String,
        sceneId: String,
        customerAlias: String
    ): Flow<DownloadProgress> = flow {
        val frames = frameRepository.getFramesBySceneId(sceneId)
        val framesWithImages = frames.filter { !it.imageUrl.isNullOrEmpty() }

        if (framesWithImages.isEmpty()) {
            emit(DownloadProgress(0, 0, null, 1f))
            return@flow
        }

        var downloaded = 0
        val total = framesWithImages.size

        framesWithImages.forEach { frame ->
            emit(DownloadProgress(
                totalImages = total,
                downloadedImages = downloaded,
                currentImage = frame.imageUrl,
                progress = downloaded.toFloat() / total
            ))

            downloadFrameImage(projectId, frame.id, customerAlias)

            downloaded++

            emit(DownloadProgress(
                totalImages = total,
                downloadedImages = downloaded,
                currentImage = frame.imageUrl,
                progress = downloaded.toFloat() / total
            ))
        }
    }

    /**
     * Download all images for a storyboard
     */
    fun downloadStoryboardImages(
        projectId: String,
        storyboardId: String,
        customerAlias: String
    ): Flow<DownloadProgress> = flow {
        val storyboard = contentRepository.getStoryboard(storyboardId)
            ?: run {
                emit(DownloadProgress(0, 0, null, 1f))
                return@flow
            }

        val allFrames = storyboard.scenes.flatMap { scene ->
            scene.frames.filter { !it.imageUrl.isNullOrEmpty() }
        }

        if (allFrames.isEmpty()) {
            emit(DownloadProgress(0, 0, null, 1f))
            return@flow
        }

        var downloaded = 0
        val total = allFrames.size

        allFrames.forEach { frame ->
            emit(DownloadProgress(
                totalImages = total,
                downloadedImages = downloaded,
                currentImage = frame.imageUrl,
                progress = downloaded.toFloat() / total
            ))

            downloadFrameImage(projectId, frame.id, customerAlias)

            downloaded++

            emit(DownloadProgress(
                totalImages = total,
                downloadedImages = downloaded,
                currentImage = frame.imageUrl,
                progress = downloaded.toFloat() / total
            ))
        }
    }

    /**
     * Batch download with concurrent downloads
     */
    suspend fun batchDownloadImages(
        projectId: String,
        frameIds: List<String>,
        customerAlias: String,
        concurrency: Int = 3
    ): List<DownloadResult> = coroutineScope {
        frameIds.chunked(concurrency).flatMap { chunk ->
            chunk.map { frameId ->
                async {
                    downloadFrameImage(projectId, frameId, customerAlias)
                }
            }.awaitAll()
        }
    }

    /**
     * Get download statistics for a hierarchy level
     */
    suspend fun getDownloadStatistics(
        projectId: String,
        level: ContentLevel,
        id: String
    ): DownloadStatistics {
        val frames = when (level) {
            ContentLevel.PROJECT -> {
                val storyboards = contentRepository.getStoryboards(projectId)
                storyboards.flatMap { it.scenes }.flatMap { it.frames }
            }
            ContentLevel.STORYBOARD -> {
                val storyboard = contentRepository.getStoryboard(id)
                storyboard?.scenes?.flatMap { it.frames } ?: emptyList()
            }
            ContentLevel.SCENE -> {
                frameRepository.getFramesBySceneId(id)
            }
            ContentLevel.FRAME -> {
                listOfNotNull(frameRepository.getFrameById(id))
            }
        }

        val framesWithImages = frames.filter { !it.imageUrl.isNullOrEmpty() }
        val totalSize = framesWithImages.size // In production, calculate actual file sizes

        return DownloadStatistics(
            totalFrames = frames.size,
            framesWithImages = framesWithImages.size,
            estimatedSizeMB = totalSize * 2, // Rough estimate: 2MB per image
            imageFormats = framesWithImages.mapNotNull { frame ->
                frame.imageUrl?.substringAfterLast(".")?.lowercase()
            }.distinct()
        )
    }

    /**
     * Create a download manifest for offline use
     */
    suspend fun createDownloadManifest(
        projectId: String,
        storyboardId: String,
        customerAlias: String
    ): DownloadManifest {
        val storyboard = contentRepository.getStoryboard(storyboardId)
            ?: throw IllegalArgumentException("Storyboard not found")

        val imageEntries = mutableListOf<ImageEntry>()

        storyboard.scenes.forEach { scene ->
            scene.frames.forEach { frame ->
                if (!frame.imageUrl.isNullOrEmpty()) {
                    val signedUrl = backendFileService.getSignedDownloadUrl(
                        customerAlias = customerAlias,
                        projectId = projectId,
                        assetType = "images",
                        version = "v1",
                        filename = frame.imageUrl!!.substringAfterLast("/")
                    )

                    imageEntries.add(
                        ImageEntry(
                            frameId = frame.id,
                            sceneId = scene.id,
                            originalUrl = frame.imageUrl!!,
                            downloadUrl = signedUrl,
                            filename = "${scene.id}_${frame.frameNumber}_${frame.id}.jpg",
                            metadata = mapOf(
                                "shotType" to frame.shotType.name,
                                "duration" to frame.duration.toString(),
                                "frameNumber" to frame.frameNumber.toString()
                            )
                        )
                    )
                }
            }
        }

        return DownloadManifest(
            projectId = projectId,
            storyboardId = storyboardId,
            storyboardTitle = storyboard.title,
            totalImages = imageEntries.size,
            createdAt =getCurrentTimeMillis(),
            expiresAt = getCurrentTimeMillis() + (4 * 60 * 60 * 1000), // 4 hours
            images = imageEntries
        )
    }
}

// Supporting data classes
enum class ContentLevel {
    PROJECT,
    STORYBOARD,
    SCENE,
    FRAME
}

data class DownloadStatistics(
    val totalFrames: Int,
    val framesWithImages: Int,
    val estimatedSizeMB: Int,
    val imageFormats: List<String>
)

data class DownloadManifest(
    val projectId: String,
    val storyboardId: String,
    val storyboardTitle: String,
    val totalImages: Int,
    val createdAt: Long,
    val expiresAt: Long,
    val images: List<ImageEntry>
)

data class ImageEntry(
    val frameId: String,
    val sceneId: String,
    val originalUrl: String,
    val downloadUrl: String,
    val filename: String,
    val metadata: Map<String, String>
)

// Backend File Service interface
interface BackendFileService {
    suspend fun getSignedDownloadUrl(
        customerAlias: String,
        projectId: String,
        assetType: String,
        version: String,
        filename: String,
        expirationHours: Int = 4
    ): String
}