// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/AssetModels.kt
package com.beekeeper.app.domain.model

import com.beekeeper.app.domain.model.Permission
import com.beekeeper.app.presentation.viewmodels.GenerationPlatform

/**
 * Missing models for Asset management
 */

// Request models
data class AvatarGenerationRequest(
    val prompt: String,
    val style: AvatarStyle,
    val gender: Gender? = null,
    val age: AgeRange? = null,
    val ethnicity: String? = null,
    val platform: GenerationPlatform,
    val parameters: Map<String, Any> = emptyMap()
)

data class AvatarFilters(
    val styles: List<String>? = null,
    val gender: Gender? = null,
    val ageRange: AgeRange? = null,
    val tags: List<String>? = null,
    val onlyFavorites: Boolean = false
)

data class ImageGenerationRequest(
    val prompt: String,
    val style: String? = null,
    val width: Int,
    val height: Int,
    val platform: GenerationPlatform
)

data class TextToSpeechRequest(
    val text: String,
    val voice: String,
    val language: String,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f
)

data class LibraryFilters(
    val types: List<AssetType>? = null,
    val collections: List<String>? = null,
    val tags: List<String>? = null,
    val dateRange: DateRange? = null,
    val onlyFavorites: Boolean = false
)

data class ShareSettings(
    val type: ShareType,
    val expiresAt: Long? = null,
    val password: String? = null,
    val permissions: List<Permission> = emptyList()
)

// Asset interface
interface Asset {
    val id: String
    val type: AssetType
    val url: String
    val thumbnailUrl: String?
    val createdAt: Long
}

// Concrete Asset implementation for general assets
data class GenericAsset(
    override val id: String,
    override val type: AssetType,
    override val url: String,
    override val thumbnailUrl: String? = null,
    override val createdAt: Long,
    val projectId: String? = null,
    val name: String? = null,
    val size: Long? = null,
    val metadata: Map<String, Any> = emptyMap()
) : Asset
