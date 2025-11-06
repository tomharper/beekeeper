// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/ai/AIServiceFactory.kt
package com.beekeeper.app.domain.ai

import com.beekeeper.app.domain.ai.interfaces.AvatarServiceProvider
import com.beekeeper.app.domain.ai.interfaces.ScriptServiceProvider
import com.beekeeper.app.domain.ai.interfaces.VideoServiceProvider

/**
 * Factory for creating AI Service Manager with user preferences

object AIServiceFactory {
    fun createFromPreferences(preferences: UserPreferences): AIServiceManager {
        val config = AIServiceConfiguration(
            claudeApiKey = preferences.claudeApiKey,
            bedrockConfig = preferences.bedrockConfig?.let {
                BedrockConfig(
                    region = it.region,
                    accessKeyId = it.accessKeyId,
                    secretAccessKey = it.secretAccessKey
                )
            },
            heygenApiKey = preferences.heygenApiKey,
            didApiKey = preferences.didApiKey,
            synthesiaApiKey = preferences.synthesiaApiKey
        )
        
        return AIServiceManager(config)
    }
}
 */

