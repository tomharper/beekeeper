// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/ai/AIServiceConfiguration.kt
package com.beekeeper.app.domain.ai

import kotlinx.serialization.Serializable

/**
 * Secure configuration for all AI services
 * API keys are loaded from environment or secure storage, never hardcoded
 */
@Serializable
data class AIServiceConfiguration(
    // Core Services
    val heygen: HeyGenConfig? = null,
    val elevenlabs: ElevenLabsConfig? = null,
    val claude: ClaudeConfig? = null,
    val openai: OpenAIConfig? = null,
    
    // Video Generation
    val runway: RunwayConfig? = null,
    val pika: PikaConfig? = null,
    val synthesia: SynthesiaConfig? = null,
    val did: DIDConfig? = null,
    
    // Music & Audio
    val aiva: AIVAConfig? = null,
    val mubert: MubertConfig? = null,
    val soundraw: SoundrawConfig? = null,
    
    // Image & Scene Generation
    val krea: KreaConfig? = null,
    val leonardo: LeonardoConfig? = null,
    val midjourney: MidjourneyConfig? = null,
    val stability: StabilityConfig? = null,
    
    // Video Processing
    val shotstack: ShotstackConfig? = null,
    val bannerbear: BannerbearConfig? = null,
    
    // Translation & Subtitles
    val assemblyai: AssemblyAIConfig? = null,
    val deepl: DeepLConfig? = null,
    
    // Configuration metadata
    val environment: String = "production",
    val enabledServices: List<String> = emptyList(),
    val fallbackChains: Map<String, List<String>> = emptyMap()
)

// Individual service configurations
@Serializable
data class HeyGenConfig(
    val apiKey: String = "", // Will be loaded from environment
    val baseUrl: String = "https://api.heygen.com",
    val timeoutMs: Long = 30000,
    val rateLimitPerMinute: Int = 60
)

@Serializable
data class ElevenLabsConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.elevenlabs.io",
    val timeoutMs: Long = 45000,
    val rateLimitPerMinute: Int = 120
)

@Serializable
data class ClaudeConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.anthropic.com",
    val model: String = "claude-3-5-sonnet-20241022",
    val maxTokens: Int = 4096
)

@Serializable
data class OpenAIConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.openai.com",
    val model: String = "gpt-4-turbo",
    val organization: String? = null
)

@Serializable
data class RunwayConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.runwayml.com"
)

@Serializable
data class PikaConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.pika.art"
)

@Serializable
data class SynthesiaConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.synthesia.io"
)

@Serializable
data class DIDConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.d-id.com"
)

@Serializable
data class AIVAConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.aiva.ai"
)

@Serializable
data class MubertConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api-b2b.mubert.com"
)

@Serializable
data class SoundrawConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.soundraw.io"
)

@Serializable
data class KreaConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.krea.ai"
)

@Serializable
data class LeonardoConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://cloud.leonardo.ai/api/rest"
)

@Serializable
data class MidjourneyConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.midjourney.com"
)

@Serializable
data class StabilityConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.stability.ai"
)

@Serializable
data class ShotstackConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.shotstack.io"
)

@Serializable
data class BannerbearConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.bannerbear.com"
)

@Serializable
data class AssemblyAIConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.assemblyai.com"
)

@Serializable
data class DeepLConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api-free.deepl.com"
)
