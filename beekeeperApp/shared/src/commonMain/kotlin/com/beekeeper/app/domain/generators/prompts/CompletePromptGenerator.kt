// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/generators/prompts/CompletePromptGenerator.kt
package com.beekeeper.app.domain.generators.prompts

import com.beekeeper.app.domain.model.*
import com.beekeeper.app.utils.getCurrentTimeMillis

/**
 * Complete Prompt Generator - ACTUALLY USING REAL DATA FROM MODELS
 *
 * This version properly uses the actual fields that exist in:
 * - Frame: action, vfxNotes, audioNotes, visualNotes
 * - Scene: lightingNotes, soundNotes, vfxNotes, location, timeOfDay
 *
 * No more invented fields like environmentNotes, colorGrading, etc.
 */
class CompletePromptGenerator {

    /**
     * VideoPromptResponse - maintains backward compatibility
     */
    data class VideoPromptResponse(
        val primaryPrompt: String,
        val negativePrompt: String,
        val stylePrompt: String,
        val technicalPrompt: String,
        val compositionPrompt: String,
        val lightingPrompt: String,
        val colorPrompt: String,
        val motionPrompt: String,
        val cameraPrompt: String,
        val environmentPrompt: String,
        val characterPrompt: String,
        val vfxPrompt: String,
        val qualityPrompt: String,
        val platformOptimizations: Map<String, String>,
        val fallbackPrompts: List<String>,
        val relatedPrompts: List<String>,
        val metadata: Map<String, Any> = emptyMap()
    )

    /**
     * Generate storyboard prompt from visual Scenes
     */
    fun generateStoryboardPrompt(
        storyboard: Storyboard,
        script: Script,
        characters: List<CharacterProfile>,
        style: String = "storyboard_sketch"
    ): VideoPromptResponse {

        val keyScenes = storyboard.scenes.take(9)

        // Build scene descriptions from ACTUAL scene data
        val sceneDescriptions = keyScenes.joinToString(", ") { scene ->
            val location = scene.location ?: "unspecified location"
            val timeOfDay = scene.timeOfDay ?: ""
            "Scene ${scene.sceneNumber}: ${location} ${timeOfDay}".trim()
        }

        // Get lighting from ACTUAL Scene.lightingNotes
        val overallLighting = keyScenes
            .mapNotNull { it.lightingNotes }
            .firstOrNull() ?: "natural lighting"

        // Get VFX from ACTUAL Scene.vfxNotes
        val vfxNotes = keyScenes
            .mapNotNull { it.vfxNotes }
            .joinToString(", ").ifEmpty { "minimal effects" }

        // Get environments from ACTUAL Scene.location
        val environments = keyScenes
            .mapNotNull { it.location }
            .distinct()
            .joinToString(", ")
            .ifEmpty { "various locations" }

        return VideoPromptResponse(
            primaryPrompt = "Professional storyboard grid, $style, $sceneDescriptions",
            negativePrompt = "messy, inconsistent, unprofessional",
            stylePrompt = style,
            technicalPrompt = "storyboard format, ${keyScenes.size} panels",
            compositionPrompt = "3x3 grid layout",
            lightingPrompt = overallLighting,
            colorPrompt = "consistent color palette",
            motionPrompt = "static panels with motion indicators",
            cameraPrompt = "various shot types indicated",
            environmentPrompt = environments,
            characterPrompt = characters.joinToString(", ") { it.name },
            vfxPrompt = vfxNotes,
            qualityPrompt = "professional quality",
            platformOptimizations = mapOf("YOUTUBE" to "16:9"),
            fallbackPrompts = listOf("simplified storyboard"),
            relatedPrompts = listOf("character sheets")
        )
    }

    /**
     * Generate scene summary from ACTUAL Scene data
     */
    fun generateSceneSummaryPrompt(
        scene: Scene,
        sceneScript: SceneScript,
        characters: List<CharacterProfile>
    ): VideoPromptResponse {

        // Get characters from dialogue (SceneScript is only used for this)
        val sceneCharacters = characters.filter { char ->
            sceneScript.dialogue.any { it.characterName == char.name }
        }

        // Use ACTUAL Scene fields
        val location = scene.location ?: "unspecified location"
        val timeOfDay = scene.timeOfDay ?: "day"
        val lighting = scene.lightingNotes ?: "natural lighting"
        val vfxDesc = scene.vfxNotes ?: "natural effects"
        val soundNotes = scene.soundNotes ?: ""

        // Find a key frame for shot information
        val keyFrame = scene.frames.firstOrNull {
            it.shotType == ShotType.ESTABLISHING_SHOT ||
                    it.shotType == ShotType.WIDE_SHOT
        } ?: scene.frames.firstOrNull()

        // Build prompt from ACTUAL Scene data
        val prompt = buildString {
            append("Scene ${scene.sceneNumber}: ")
            append("${location}, ${timeOfDay}, ")

            // Add action from frame if available
            keyFrame?.action?.let { action ->
                if (action.isNotEmpty()) {
                    append("$action, ")
                }
            }

            // Add shot type from frame
            append("${keyFrame?.shotType?.name?.lowercase()?.replace("_", " ") ?: "establishing"} shot, ")

            if (sceneCharacters.isNotEmpty()) {
                append("featuring ${sceneCharacters.joinToString(", ") { it.name }}, ")
            }

            append("cinematic composition")
        }

        return VideoPromptResponse(
            primaryPrompt = prompt,
            negativePrompt = "amateur quality",
            stylePrompt = "cinematic style",
            technicalPrompt = "${scene.frames.size} frames, ${scene.duration}s total",
            compositionPrompt = keyFrame?.let { generateComposition(it.shotType) } ?: "wide establishing shot",
            lightingPrompt = lighting,
            colorPrompt = "cinematic color grading",
            motionPrompt = extractMotionFromScene(scene),
            cameraPrompt = keyFrame?.cameraAngle?.name?.lowercase()?.replace("_", " ") ?: "eye level",
            environmentPrompt = location,
            characterPrompt = sceneCharacters.joinToString(", ") { it.name },
            vfxPrompt = vfxDesc,
            qualityPrompt = "high quality, professional",
            platformOptimizations = mapOf("YOUTUBE" to "16:9"),
            fallbackPrompts = listOf("basic scene"),
            relatedPrompts = listOf("scene variations")
        )
    }

    /**
     * Generate character prompt from ACTUAL Frame data
     */
    fun generateCharacterPrompt(
        character: CharacterProfile,
        frame: Frame,
        scene: Scene,
        sceneScript: SceneScript,
        additionalContext: Map<String, String> = emptyMap()
    ): VideoPromptResponse {

        val characterDesc = buildString {
            append("${character.name}, ")
            append("${character.age} years old, ")
            character.gender?.let { append("${it.name.lowercase()}, ") }
            append(character.description)
        }

        // Use ACTUAL Frame fields
        val frameAction = frame.action ?: "in scene"
        val vfxNotes = frame.vfxNotes ?: "natural rendering"
        val audioNotes = frame.audioNotes ?: ""
        val visualNotes = frame.visualNotes ?: ""

        // Use ACTUAL Scene fields for location and lighting
        val lighting = scene.lightingNotes ?: "natural lighting"
        val location = scene.location ?: "scene location"

        return VideoPromptResponse(
            primaryPrompt = "$characterDesc, $frameAction",
            negativePrompt = "inconsistent character, off-model",
            stylePrompt = "consistent character design",
            technicalPrompt = "${frame.duration}s duration",
            compositionPrompt = generateComposition(frame.shotType),
            lightingPrompt = lighting,
            colorPrompt = "character appropriate colors",
            motionPrompt = frame.cameraMovement?.name?.lowercase()?.replace("_", " ") ?: "static",
            cameraPrompt = frame.cameraAngle.name.lowercase().replace("_", " "),
            environmentPrompt = location,
            characterPrompt = character.name,
            vfxPrompt = vfxNotes,
            qualityPrompt = "high detail character render",
            platformOptimizations = getPlatformSettings(SocialPlatform.YOUTUBE),
            fallbackPrompts = listOf("basic character"),
            relatedPrompts = listOf("character variations")
        )
    }

    /**
     * Generate frame prompts from ACTUAL Frame data
     */
    fun generateFramePrompts(
        frame: Frame,
        scene: Scene,
        sceneScript: SceneScript,
        characters: List<CharacterProfile>,
        platform: SocialPlatform = SocialPlatform.YOUTUBE
    ): VideoPromptResponse {

        // Use ACTUAL Scene fields for environment
        val environment = scene.location ?: "unspecified location"
        val timeOfDay = scene.timeOfDay ?: ""
        val sceneContext = "$environment $timeOfDay".trim()

        // Use ACTUAL Scene.lightingNotes
        val lighting = scene.lightingNotes ?: "natural lighting"

        // Only use SceneScript for dialogue lookup
        val dialogue = frame.dialogueLineId?.let { dialogueId ->
            sceneScript.dialogue.find { it.id == dialogueId }
        }

        val character = dialogue?.let { dl ->
            characters.find { it.name == dl.characterName }
        }

        // Build prompt from ACTUAL Frame data
        val videoPrompt = buildString {
            append("$sceneContext, ")

            // Use ACTUAL Frame.action field
            frame.action?.let { action ->
                if (action.isNotEmpty()) {
                    append("$action, ")
                }
            }

            character?.let { append("${it.name} in frame, ") }

            append("${frame.shotType.name.lowercase().replace("_", " ")} shot, ")
            append("${frame.cameraAngle.name.lowercase().replace("_", " ")} angle")

            // Add visual notes if present
            frame.visualNotes?.let { notes ->
                if (notes.isNotEmpty()) {
                    append(", $notes")
                }
            }
        }

        return VideoPromptResponse(
            primaryPrompt = videoPrompt,
            negativePrompt = "amateur, shaky, blurry",
            stylePrompt = "cinematic style",
            technicalPrompt = "${frame.duration}s, 24fps",
            compositionPrompt = generateComposition(frame.shotType),
            lightingPrompt = lighting,
            colorPrompt = "cinematic grading",
            motionPrompt = frame.cameraMovement?.name?.lowercase()?.replace("_", " ") ?: "static",
            cameraPrompt = frame.cameraAngle.name.lowercase().replace("_", " "),
            environmentPrompt = sceneContext,
            characterPrompt = character?.name ?: "",
            vfxPrompt = frame.vfxNotes ?: "natural rendering",
            qualityPrompt = "8k, highly detailed",
            platformOptimizations = getPlatformSettings(platform),
            fallbackPrompts = listOf("simplified version"),
            relatedPrompts = listOf("alternative angle")
        )
    }

    /**
     * Generate audio prompt from Frame and Scene audio data
     */
    fun generateAudioPrompt(
        frame: Frame,
        scene: Scene,
        sceneScript: SceneScript,
        character: CharacterProfile? = null
    ): Map<String, String> {
        val audioComponents = mutableMapOf<String, String>()

        // Get dialogue if frame references it
        frame.dialogueLineId?.let { dialogueId ->
            val dialogueLine = sceneScript.dialogue.find { it.id == dialogueId }
            dialogueLine?.let { dl ->
                audioComponents["dialogue"] = buildString {
                    append("Character: ${dl.characterName}, ")
                    append("Line: \"${dl.dialogue}\", ")
                    dl.emotion?.let { append("Emotion: $it, ") }

                    // Use voiceProfile for comprehensive voice settings
                    character?.voiceProfile?.let { profile ->
                        append("Voice: ")
                        profile.emotionalTone?.let { append("$it tone, ") }
                        profile.speakingStyle?.let { append("$it style, ") }
                        profile.accent?.let { append("$it accent, ") }
                        append("pitch: ${profile.pitch}, ")
                        append("speed: ${profile.speed}, ")
                        append("volume: ${profile.volume}")
                        profile.description?.let { append(", $it") }
                    } ?: append("Voice: natural speaking voice")
                }
            }
        }

        // Use ACTUAL Frame.audioNotes
        frame.audioNotes?.let { notes ->
            if (notes.isNotEmpty()) {
                audioComponents["frameAudio"] = notes
            }
        }

        // Use ACTUAL Scene.soundNotes
        scene.soundNotes?.let { notes ->
            if (notes.isNotEmpty()) {
                audioComponents["sceneSound"] = notes
            }
        }

        // Use ACTUAL Scene.soundEffects
        if (scene.soundEffects.isNotEmpty()) {
            audioComponents["soundEffects"] = scene.soundEffects.joinToString(", ")
        }

        // Use ACTUAL Scene.musicCues
        if (scene.musicCues.isNotEmpty()) {
            audioComponents["music"] = scene.musicCues.joinToString(", ")
        }

        return audioComponents
    }

    /**
     * Generate VFX prompt from ACTUAL VFX data
     */
    fun generateVFXPrompt(
        frame: Frame,
        scene: Scene
    ): String {
        val vfxElements = mutableListOf<String>()

        // Use ACTUAL Frame.vfxNotes
        frame.vfxNotes?.let { notes ->
            if (notes.isNotEmpty()) {
                vfxElements.add(notes)
            }
        }

        // Use ACTUAL Scene.vfxNotes
        scene.vfxNotes?.let { notes ->
            if (notes.isNotEmpty() && !vfxElements.contains(notes)) {
                vfxElements.add(notes)
            }
        }

        return vfxElements.joinToString(", ").ifEmpty { "no special effects" }
    }

    /**
     * Generate comprehensive voice prompt using VoiceProfile
     */
    fun generateVoicePrompt(
        character: CharacterProfile,
        dialogueLine: DialogueLine,
        frame: Frame,
        scene: Scene
    ): Map<String, Any> {
        val voiceSettings = mutableMapOf<String, Any>()

        // Extract voice profile settings
        character.voiceProfile?.let { profile ->
            voiceSettings["voiceId"] = profile.voiceId ?: "default"
            voiceSettings["modelType"] = profile.voiceModelType ?: "default"
            voiceSettings["pitch"] = profile.pitch
            voiceSettings["speed"] = profile.speed
            voiceSettings["volume"] = profile.volume

            profile.accent?.let { voiceSettings["accent"] = it }
            profile.emotionalTone?.let { voiceSettings["emotionalTone"] = it }
            profile.speakingStyle?.let { voiceSettings["speakingStyle"] = it }

            // Custom provider settings
            if (profile.customSettings.isNotEmpty()) {
                voiceSettings["customSettings"] = profile.customSettings
            }

            profile.description?.let { voiceSettings["voiceDescription"] = it }
        }

        // Build voice prompt
        val voicePrompt = buildString {
            append("${character.name} speaking, ")

            // Age and gender for natural voice
            append("${character.age}-year-old ")
            character.gender?.let { append("${it.name.lowercase()} ") }
            append("voice, ")

            // Voice characteristics from profile
            character.voiceProfile?.let { profile ->
                profile.emotionalTone?.let { append("$it tone, ") }
                profile.speakingStyle?.let { append("$it delivery, ") }
                profile.accent?.let { append("$it accent, ") }

                // Apply pitch modifier
                when {
                    profile.pitch > 1.2 -> append("higher pitched, ")
                    profile.pitch < 0.9 -> append("lower pitched, ")
                }

                // Apply speed modifier
                when {
                    profile.speed > 1.2 -> append("faster pace, ")
                    profile.speed < 0.9 -> append("slower pace, ")
                }
            }

            // Emotion from dialogue
            dialogueLine.emotion?.let { append("$it emotion, ") }

            // Scene context affects delivery
            scene.timeOfDay?.let { time ->
                when {
                    time.contains("NIGHT", ignoreCase = true) -> append("quieter delivery, ")
                    time.contains("MORNING", ignoreCase = true) -> append("fresh tone, ")
                }
            }

            append("clear articulation")
        }

        voiceSettings["prompt"] = voicePrompt
        voiceSettings["dialogue"] = dialogueLine.dialogue
        voiceSettings["characterName"] = character.name
        voiceSettings["duration"] = frame.duration

        return voiceSettings
    }

    // ================== HELPER METHODS ==================

    private fun extractMotionFromScene(scene: Scene): String {
        val hasMotion = scene.frames.any { it.cameraMovement != null }
        return if (hasMotion) {
            val movements = scene.frames.mapNotNull { it.cameraMovement?.name?.lowercase() }.distinct()
            movements.joinToString(", ").replace("_", " ")
        } else {
            "static shots"
        }
    }

    private fun generateComposition(shotType: ShotType): String = when(shotType) {
        ShotType.EXTREME_WIDE_SHOT -> "epic wide composition"
        ShotType.WIDE_SHOT -> "wide shot composition"
        ShotType.MEDIUM_SHOT -> "medium shot framing"
        ShotType.CLOSE_UP -> "close-up framing"
        ShotType.EXTREME_CLOSE_UP -> "extreme close detail"
        ShotType.MEDIUM_CLOSE_UP -> "medium close framing"
        ShotType.OVER_THE_SHOULDER -> "over shoulder composition"
        ShotType.POV_SHOT -> "POV perspective"
        ShotType.TWO_SHOT -> "two person framing"
        ShotType.ESTABLISHING_SHOT -> "establishing shot composition"
        ShotType.INSERT_SHOT -> "detail insert"
        ShotType.REACTION_SHOT -> "reaction framing"
        ShotType.LOW_ANGLE -> "low angle composition"
        ShotType.HIGH_ANGLE -> "high angle composition"
        ShotType.AERIAL_SHOT -> "aerial view"
        ShotType.TRACKING_SHOT -> "tracking shot"
        ShotType.MONTAGE -> "montage sequence"
        ShotType.LONG_SHOT -> "long shot framing"
        ShotType.MEDIUM_TWO_SHOT -> "medium two shot"
        ShotType.OVER_SHOULDER -> "over shoulder view"
        ShotType.TOP_DOWN -> "top down view"
        ShotType.VFX_PLATE -> "vfx plate setup"
        ShotType.HERO_SHOT -> "hero shot composition"
        ShotType.STATIC -> "static composition"
        else -> "standard framing"
    }

    private fun getPlatformSettings(platform: SocialPlatform): Map<String, String> = when(platform) {
        SocialPlatform.YOUTUBE -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.TIKTOK -> mapOf("aspectRatio" to "9:16", "resolution" to "1080x1920")
        SocialPlatform.INSTAGRAM -> mapOf("aspectRatio" to "1:1", "resolution" to "1080x1080")
        SocialPlatform.FACEBOOK -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.TWITTER -> mapOf("aspectRatio" to "16:9", "resolution" to "1280x720")
        SocialPlatform.LINKEDIN -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.SNAPCHAT -> mapOf("aspectRatio" to "9:16", "resolution" to "1080x1920")
        SocialPlatform.PINTEREST -> mapOf("aspectRatio" to "2:3", "resolution" to "1000x1500")
        SocialPlatform.REDDIT -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.DISCORD -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.TELEGRAM -> mapOf("aspectRatio" to "16:9", "resolution" to "1280x720")
        SocialPlatform.WHATSAPP -> mapOf("aspectRatio" to "16:9", "resolution" to "640x360")
        SocialPlatform.VIMEO -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.TWITCH -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.CLUBHOUSE -> mapOf("aspectRatio" to "1:1", "resolution" to "audio")
        SocialPlatform.MEDIUM -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.SUBSTACK -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.PATREON -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.ONLYFANS -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.WECHAT -> mapOf("aspectRatio" to "16:9", "resolution" to "1280x720")
        SocialPlatform.YOUTUBE_SHORTS -> mapOf("aspectRatio" to "9:16", "resolution" to "1080x1920")
        SocialPlatform.THREADS -> mapOf("aspectRatio" to "1:1", "resolution" to "1080x1080")
        SocialPlatform.MASTODON -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.BLUESKY -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
        SocialPlatform.BEREAL -> mapOf("aspectRatio" to "3:4", "resolution" to "1080x1440")
        SocialPlatform.WEIBO -> mapOf("aspectRatio" to "16:9", "resolution" to "1280x720")
        SocialPlatform.LINE -> mapOf("aspectRatio" to "1:1", "resolution" to "1080x1080")
        else -> mapOf("aspectRatio" to "16:9", "resolution" to "1920x1080")
    }

    /**
     * Comprehensive prompt generation that combines all aspects
     */
    fun generateComprehensivePrompt(
        frame: Frame,
        scene: Scene,
        sceneScript: SceneScript,
        characters: List<CharacterProfile>,
        platform: SocialPlatform = SocialPlatform.YOUTUBE
    ): Map<String, Any> {

        // Generate video prompts
        val videoPrompt = generateFramePrompts(frame, scene, sceneScript, characters, platform)

        // Find character for dialogue
        val dialogueCharacter = frame.dialogueLineId?.let { dialogueId ->
            val dialogueLine = sceneScript.dialogue.find { it.id == dialogueId }
            dialogueLine?.let { dl ->
                characters.find { it.name == dl.characterName }
            }
        }

        // Generate audio components
        val audioComponents = generateAudioPrompt(frame, scene, sceneScript, dialogueCharacter)

        // Generate voice prompt if there's dialogue
        val voicePrompt = frame.dialogueLineId?.let { dialogueId ->
            val dialogueLine = sceneScript.dialogue.find { it.id == dialogueId }
            if (dialogueLine != null && dialogueCharacter != null) {
                generateVoicePrompt(dialogueCharacter, dialogueLine, frame, scene)
            } else null
        }

        // Generate VFX prompt
        val vfxPrompt = generateVFXPrompt(frame, scene)

        return mapOf(
            "video" to videoPrompt,
            "audio" to audioComponents,
            "voice" to (voicePrompt ?: emptyMap<String, Any>()),
            "vfx" to vfxPrompt,
            "metadata" to mapOf(
                "frameId" to frame.id,
                "sceneId" to scene.id,
                "duration" to frame.duration,
                "shotType" to frame.shotType.name,
                "cameraAngle" to frame.cameraAngle.name,
                "cameraMovement" to (frame.cameraMovement?.name ?: "STATIC"),
                "hasDialogue" to (frame.dialogueLineId != null),
                "hasVoiceProfile" to (dialogueCharacter?.voiceProfile != null),
                "location" to (scene.location ?: "unspecified"),
                "timeOfDay" to (scene.timeOfDay ?: "unspecified")
            )
        )
    }
}

// Type aliases for backward compatibility
typealias VideoPromptGenerator = CompletePromptGenerator
typealias AudioPromptGenerator = CompletePromptGenerator
typealias VoicePromptGenerator = CompletePromptGenerator
typealias UnifiedPromptGenerator = CompletePromptGenerator

