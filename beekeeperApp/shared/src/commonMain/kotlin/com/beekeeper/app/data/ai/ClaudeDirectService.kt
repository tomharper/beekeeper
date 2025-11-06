// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/ai/claude/ClaudeDirectService.kt
package com.beekeeper.app.data.ai
/*
import com.beekeeper.app.domain.ai.interfaces.ScriptGenerationService
import com.beekeeper.app.domain.ai.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ClaudeDirectService(
    private val apiKey: String,
    private val model: String = "claude-3-5-sonnet-20241022"
) : ScriptGenerationService {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    override suspend fun generateScript(request: ScriptGenerationRequest): Result<ScriptGenerationResult> {
        return try {
            val prompt = buildScriptPrompt(request)
            
            val response = client.post("https://api.anthropic.com/v1/messages") {
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                contentType(ContentType.Application.Json)
                setBody(ClaudeRequest(
                    model = model,
                    messages = listOf(
                        ClaudeMessage(
                            role = "user",
                            content = prompt
                        )
                    ),
                    max_tokens = 4096
                ))
            }
            
            val claudeResponse = response.body<ClaudeResponse>()
            val script = claudeResponse.content.firstOrNull()?.text ?: ""
            
            Result.success(ScriptGenerationResult(
                script = script,
                estimatedDuration = request.duration,
                segments = parseScriptSegments(script)
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun improveScript(script: String, improvements: List<String>): Result<String> {
        val prompt = """
            Please improve the following script with these suggestions: ${improvements.joinToString(", ")}
            
            Original script:
            $script
            
            Provide only the improved script without any explanation.
        """.trimIndent()
        
        return try {
            val response = client.post("https://api.anthropic.com/v1/messages") {
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                contentType(ContentType.Application.Json)
                setBody(ClaudeRequest(
                    model = model,
                    messages = listOf(
                        ClaudeMessage(role = "user", content = prompt)
                    ),
                    max_tokens = 4096
                ))
            }
            
            val claudeResponse = response.body<ClaudeResponse>()
            Result.success(claudeResponse.content.firstOrNull()?.text ?: script)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun translateScript(script: String, targetLanguage: String): Result<String> {
        val prompt = """
            Translate the following script to $targetLanguage. 
            Maintain the tone, style, and timing markers if present.
            
            Script:
            $script
        """.trimIndent()
        
        return try {
            val response = client.post("https://api.anthropic.com/v1/messages") {
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                contentType(ContentType.Application.Json)
                setBody(ClaudeRequest(
                    model = model,
                    messages = listOf(
                        ClaudeMessage(role = "user", content = prompt)
                    ),
                    max_tokens = 4096
                ))
            }
            
            val claudeResponse = response.body<ClaudeResponse>()
            Result.success(claudeResponse.content.firstOrNull()?.text ?: script)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getServiceName(): String = "Claude Direct API"
    
    override fun isAvailable(): Boolean = apiKey.isNotEmpty()
    
    private fun buildScriptPrompt(request: ScriptGenerationRequest): String {
        return """
            Create a ${request.duration}-second video script about "${request.topic}".
            
            Requirements:
            - Tone: ${request.tone}
            - Style: ${request.style ?: "engaging and informative"}
            - Language: ${request.language}
            - Include natural pauses and timing markers
            - Make it suitable for avatar narration
            
            Format the script with clear segments and timing markers like:
            [0:00-0:05] Introduction
            [0:05-0:15] Main point 1
            etc.
        """.trimIndent()
    }
    
    private fun parseScriptSegments(script: String): List<ScriptSegment> {
        // Parse timing markers from the script
        val regex = """\[(\d+:\d+)-(\d+:\d+)\]\s*(.+)""".toRegex()
        return regex.findAll(script).map { match ->
            val startTime = parseTime(match.groupValues[1])
            val endTime = parseTime(match.groupValues[2])
            val text = match.groupValues[3]
            ScriptSegment(text, startTime, endTime)
        }.toList()
    }
    
    private fun parseTime(timeStr: String): Float {
        val parts = timeStr.split(":")
        val minutes = parts[0].toFloatOrNull() ?: 0f
        val seconds = parts[1].toFloatOrNull() ?: 0f
        return minutes * 60 + seconds
    }
}

@Serializable
data class ClaudeRequest(
    val model: String,
    val messages: List<ClaudeMessage>,
    val max_tokens: Int
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeResponse(
    val content: List<ClaudeContent>
)

@Serializable
data class ClaudeContent(
    val text: String
)
*/
