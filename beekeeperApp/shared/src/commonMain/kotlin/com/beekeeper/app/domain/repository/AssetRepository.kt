// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/AssetRepository.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing all asset-related data
 * ALL MODELS ARE IMPORTED FROM domain.model PACKAGE
 */
interface AssetRepository {

    // Avatar Management
    suspend fun getAvatars(): List<Avatar>
    suspend fun getAvatar(avatarId: String): Avatar?
    suspend fun createAvatar(avatar: Avatar): Avatar
    suspend fun updateAvatar(avatar: Avatar): Avatar
    suspend fun deleteAvatar(avatarId: String): Boolean
    suspend fun getFavoriteAvatars(): List<Avatar>
    suspend fun toggleFavoriteAvatar(avatarId: String): Boolean
    suspend fun searchAvatars(query: String, filters: AvatarFilters): List<Avatar>
    suspend fun generateAvatar(prompt: AvatarGenerationRequest): Avatar
    suspend fun getAvatarStyles(): List<AvatarStyle>

    // Image Assets
    suspend fun getImages(projectId: String): List<ImageAsset>
    suspend fun uploadImage(image: ImageUploadRequest): ImageAsset
    suspend fun deleteImage(imageId: String): Boolean
    suspend fun getImagesByTag(tag: String): List<ImageAsset>
    suspend fun generateImage(prompt: ImageGenerationRequest): ImageAsset

    // Video Assets
    suspend fun getVideos(projectId: String): List<VideoAsset>
    suspend fun uploadVideo(video: VideoUploadRequest): VideoAsset
    suspend fun deleteVideo(videoId: String): Boolean
    suspend fun getVideoMetadata(videoId: String): VideoMetadata
    suspend fun generateVideoThumbnail(videoId: String): String

    // Audio Assets
    suspend fun getAudioFiles(projectId: String): List<AudioAsset>
    suspend fun uploadAudio(audio: AudioUploadRequest): AudioAsset
    suspend fun deleteAudio(audioId: String): Boolean
    suspend fun generateAudioFromText(request: TextToSpeechRequest): AudioAsset

    // Style Management
    suspend fun getStyles(projectId: String): List<VisualStyle>
    suspend fun getStyle(styleId: String): VisualStyle?
    suspend fun createStyle(style: VisualStyle): VisualStyle
    suspend fun updateStyle(style: VisualStyle): VisualStyle
    suspend fun deleteStyle(styleId: String): Boolean
    suspend fun applyStyleToAsset(assetId: String, styleId: String): Asset?

    // Library Management
    suspend fun getLibraryItems(filters: LibraryFilters): List<LibraryItem>
    suspend fun addToLibrary(assetId: String, assetType: AssetType): Boolean
    suspend fun removeFromLibrary(itemId: String): Boolean
    suspend fun getRecentAssets(limit: Int = 20): List<Asset>
    suspend fun getAssetCollections(): List<AssetCollection>
    suspend fun createCollection(collection: AssetCollection): AssetCollection

    // Asset Search and Discovery
    suspend fun searchAssets(query: String, types: List<AssetType>): List<Asset>
    suspend fun getRelatedAssets(assetId: String): List<Asset>
    suspend fun getTrendingAssets(): List<Asset>

    // Real-time updates
    fun observeAvatars(): Flow<List<Avatar>>
    fun observeLibrary(): Flow<List<LibraryItem>>
    fun observeAssetUploads(): Flow<UploadProgress>
}
