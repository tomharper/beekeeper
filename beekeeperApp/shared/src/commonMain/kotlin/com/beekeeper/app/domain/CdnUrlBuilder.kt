// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/utils/CdnUrlBuilder.kt
package com.beekeeper.app.domain.utils

import com.beekeeper.app.domain.model.User

/**
 * Centralized CDN URL builder for all CineFiller assets
 * Follows the pattern: /{mediaType}/{customerAlias}/{projectId}/{assetType}/{filename}
 */
object CdnUrlBuilder {

    private const val CDN_BASE_URL = "https://cinefiller.b-cdn.net"

    // Avatar variants
    enum class AvatarVariant(val suffix: String) {
        FULL("full"),
        THUMB_256("thumb-256"),
        THUMB_128("thumb-128"),
        THUMB_64("thumb-64")
    }

    // Video quality variants
    enum class VideoQuality(val suffix: String) {
        ORIGINAL("original"),
        HD_1080("1080p"),
        HD_720("720p"),
        SD_480("480p"),
        PREVIEW("preview")
    }

    /**
     * Get character avatar URL
     * Example: /images/disney/frozen3/characters/elsa-001/avatar/avatar-thumb-256.webp
     */
    fun getCharacterAvatarUrl(
        user: User,
        projectId: String,
        characterId: String,
        variant: AvatarVariant = AvatarVariant.FULL
    ): String {
        return "$CDN_BASE_URL/images/${user.customerAlias}/$projectId/characters/$characterId/avatar/avatar-${variant.suffix}.webp"
    }

    /**
     * Get character gallery image URL
     * Example: /images/disney/frozen3/characters/elsa-001/gallery/concept-01.webp
     */
    fun getCharacterGalleryUrl(
        user: User,
        projectId: String,
        characterId: String,
        imageName: String
    ): String {
        return "$CDN_BASE_URL/images/${user.customerAlias}/$projectId/characters/$characterId/gallery/$imageName"
    }

    /**
     * Get character expression URL
     * Example: /images/disney/frozen3/characters/elsa-001/expressions/happy.webp
     */
    fun getCharacterExpressionUrl(
        user: User,
        projectId: String,
        characterId: String,
        emotion: String
    ): String {
        return "$CDN_BASE_URL/images/${user.customerAlias}/$projectId/characters/$characterId/expressions/$emotion.webp"
    }

    /**
     * Get storyboard frame URL
     * Example: /images/disney/frozen3/ep01-awakening/storyboards/scene-001/frame-001.webp
     */
    fun getStoryboardFrameUrl(
        user: User,
        projectId: String,
        episodeId: String,
        sceneId: String,
        frameNumber: Int
    ): String {
        val paddedFrame = frameNumber.toString().padStart(3, '0')
        return "$CDN_BASE_URL/images/${user.customerAlias}/$projectId/$episodeId/storyboards/$sceneId/frame-$paddedFrame.webp"
    }

    /**
     * Get scene video URL
     * Example: /videos/disney/frozen3/ep01-awakening/scenes/scene-001/scene-001-final.mp4
     */
    fun getSceneVideoUrl(
        user: User,
        projectId: String,
        episodeId: String,
        sceneId: String,
        version: String = "final",
        quality: VideoQuality = VideoQuality.HD_1080
    ): String {
        return "$CDN_BASE_URL/videos/${user.customerAlias}/$projectId/$episodeId/scenes/$sceneId/$sceneId-$version-${quality.suffix}.mp4"
    }

    /**
     * Get dialogue audio URL
     * Example: /audio/disney/frozen3/ep01-awakening/dialogue/scene-001/dialogue-001.mp3
     */
    fun getDialogueAudioUrl(
        user: User,
        projectId: String,
        episodeId: String,
        sceneId: String,
        lineId: String
    ): String {
        return "$CDN_BASE_URL/audio/${user.customerAlias}/$projectId/$episodeId/dialogue/$sceneId/dialogue-$lineId.mp3"
    }

    /**
     * Get script document URL
     * Example: /documents/disney/frozen3/scripts/v1.0.0/full-script.json
     */
    fun getScriptUrl(
        user: User,
        projectId: String,
        version: String = "latest",
        filename: String = "full-script.json"
    ): String {
        return "$CDN_BASE_URL/documents/${user.customerAlias}/$projectId/scripts/$version/$filename"
    }

    /**
     * Get character preview video URL
     * Example: /videos/disney/frozen3/characters/elsa-001/preview/character-intro.mp4
     */
    fun getCharacterVideoUrl(
        user: User,
        projectId: String,
        characterId: String,
        videoType: String = "preview",
        filename: String = "character-intro.mp4"
    ): String {
        return "$CDN_BASE_URL/videos/${user.customerAlias}/$projectId/characters/$characterId/$videoType/$filename"
    }

    /**
     * Build a custom asset URL for edge cases
     */
    fun buildCustomUrl(
        user: User,
        mediaType: String,
        projectId: String,
        assetPath: String,
        filename: String
    ): String {
        return "$CDN_BASE_URL/$mediaType/${user.customerAlias}/$projectId/$assetPath/$filename"
    }

    /**
     * Get the base path for a project (useful for batch operations)
     */
    fun getProjectBasePath(
        user: User,
        projectId: String,
        mediaType: String = "images"
    ): String {
        return "$CDN_BASE_URL/$mediaType/${user.customerAlias}/$projectId"
    }

    /**
     * Check if a URL is from our CDN
     */
    fun isCdnUrl(url: String): Boolean {
        return url.startsWith(CDN_BASE_URL)
    }

    /**
     * Extract path components from a CDN URL
     */
    fun parseUrl(url: String): CdnUrlComponents? {
        if (!isCdnUrl(url)) return null

        val path = url.removePrefix(CDN_BASE_URL).trimStart('/')
        val parts = path.split('/')

        if (parts.size < 4) return null

        return CdnUrlComponents(
            mediaType = parts[0],
            customerAlias = parts[1],
            projectId = parts[2],
            assetPath = parts.drop(3).dropLast(1).joinToString("/"),
            filename = parts.last()
        )
    }

    data class CdnUrlComponents(
        val mediaType: String,
        val customerAlias: String,
        val projectId: String,
        val assetPath: String,
        val filename: String
    )
}

/**
 * Extension functions for convenience
 */

// Extension on User for common operations
fun User.getAvatarUrl(
    projectId: String,
    characterId: String,
    variant: CdnUrlBuilder.AvatarVariant = CdnUrlBuilder.AvatarVariant.FULL
): String {
    return CdnUrlBuilder.getCharacterAvatarUrl(this, projectId, characterId, variant)
}

fun User.getStoryboardUrl(
    projectId: String,
    episodeId: String,
    sceneId: String,
    frameNumber: Int
): String {
    return CdnUrlBuilder.getStoryboardFrameUrl(this, projectId, episodeId, sceneId, frameNumber)
}

/**
 * Usage Examples:
 *
 * // In a Composable or ViewModel
 * val user = getCurrentUser() // Your existing user object
 *
 * // Get avatar URL
 * val avatarUrl = CdnUrlBuilder.getCharacterAvatarUrl(
 *     user = user,
 *     projectId = "frozen3",
 *     characterId = "elsa-001",
 *     variant = CdnUrlBuilder.AvatarVariant.THUMB_256
 * )
 *
 * // Or using extension function
 * val avatarUrl = user.getAvatarUrl("frozen3", "elsa-001", CdnUrlBuilder.AvatarVariant.FULL)
 *
 * // Get storyboard frame
 * val frameUrl = CdnUrlBuilder.getStoryboardFrameUrl(
 *     user = user,
 *     projectId = "frozen3",
 *     episodeId = "ep01-awakening",
 *     sceneId = "scene-001",
 *     frameNumber = 1
 * )
 *
 * // Parse an existing CDN URL
 * val components = CdnUrlBuilder.parseUrl(existingUrl)
 * components?.let {
 *     println("Media type: ${it.mediaType}")
 *     println("Customer: ${it.customerAlias}")
 * }
 */