// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/model/Avatar.kt
package com.beekeeper.app.domain.model

import com.beekeeper.app.presentation.viewmodels.GenerationPlatform
import kotlinx.datetime.Instant

/**
 * Domain entity representing an AI-generated Avatar
 */
data class Avatar(
    val id: String,
    val name: String,
    val description: String? = null,
    val thumbnailUrl: String,
    val fullImageUrl: String,
    val videoPreviewUrl: String? = null,
    val style: AvatarStyle,
    val generationPlatform: GenerationPlatform,
    val generationParams: Map<String, Any> = emptyMap(),
    val tags: List<String> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
    val createdBy: String,
    val isPublic: Boolean = false
)

enum class AvatarStyle {
    REALISTIC,
    CARTOON,
    ANIME,
    PIXAR,
    WATERCOLOR,
    SKETCH,
    CORPORATE,
    FANTASY,
    CYBERPUNK,
    VINTAGE
}

