// File: shared/src/wasmJsMain/kotlin/com/cinefiller/fillerapp/network/WasmNetworkClient.kt
package com.beekeeper.app.network

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import com.beekeeper.app.presentation.viewmodels.*

class WasmNetworkClient : AIServiceManager {
    
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        
        install(WebSockets) {
            pingInterval = 20_000
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
        
        // CORS handling for browser
        install(DefaultRequest) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.ContentType, "application/json")
                
                // Add origin header for CORS
                window.location.origin?.let {
                    append(HttpHeaders.Origin, it)
                }
            }
        }
    }
    
    private var currentProvider = "openai"
    private val apiEndpoints = mutableMapOf(
        "openai" to "https://api.openai.com/v1",
        "anthropic" to "https://api.anthropic.com/v1",
        "local" to window.location.origin + "/api"
    )
    
    override suspend fun generateContent(
        request: ContentGenerationRequest
    ): Result<ContentGenerationResult> {
        return try {
            val endpoint = apiEndpoints[currentProvider] ?: apiEndpoints["local"]!!
            val response: HttpResponse = client.post("$endpoint/generate") {
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val result = response.body<ContentGenerationResult>()
                Result.success(result)
            } else {
                Result.failure(Exception("API Error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun generateScript(prompt: String, type: String): Result<String> {
        return generateContent(
            ContentGenerationRequest(
                type = "script",
                prompt = prompt,
                parameters = mapOf("scriptType" to type)
            )
        ).map { it.content }
    }
    
    override suspend fun generateAvatar(characterDescription: String): Result<String> {
        return generateContent(
            ContentGenerationRequest(
                type = "avatar",
                prompt = characterDescription
            )
        ).map { it.metadata["avatarUrl"] ?: "" }
    }
    
    override suspend fun generateVideo(script: String, avatarId: String): Result<String> {
        return generateContent(
            ContentGenerationRequest(
                type = "video",
                prompt = script,
                parameters = mapOf("avatarId" to avatarId)
            )
        ).map { it.metadata["videoUrl"] ?: "" }
    }
    
    override fun getAvailableProviders(): List<String> {
        return apiEndpoints.keys.toList()
    }
    
    override fun switchProvider(provider: String): Boolean {
        if (provider in apiEndpoints) {
            currentProvider = provider
            return true
        }
        return false
    }
    
    override fun getCurrentProvider(): String = currentProvider
    
    // WebSocket support for real-time features
    suspend fun connectWebSocket(
        path: String,
        onMessage: (String) -> Unit
    ): Flow<String> = flow {
        val wsEndpoint = apiEndpoints[currentProvider]
            ?.replace("https://", "wss://")
            ?.replace("http://", "ws://")
            ?: "ws://localhost:8080"
            
        client.webSocket("$wsEndpoint/$path") {
            for (frame in incoming) {
                frame as? Frame.Text?.let {
                    val text = it.readText()
                    onMessage(text)
                    emit(text)
                }
            }
        }
    }
    
    // Streaming support for large content generation
    suspend fun streamContent(
        request: ContentGenerationRequest,
        onChunk: (String) -> Unit
    ): Result<String> {
        return try {
            val endpoint = apiEndpoints[currentProvider] ?: apiEndpoints["local"]!!
            val response = client.post("$endpoint/stream") {
                setBody(request)
            }
            
            val content = StringBuilder()
            
            // Use browser's ReadableStream API for streaming
            val body = response.bodyAsChannel()
            // Process streaming response
            
            Result.success(content.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // File upload support
    suspend fun uploadFile(
        file: ByteArray,
        filename: String,
        mimeType: String
    ): Result<String> {
        return try {
            val endpoint = apiEndpoints[currentProvider] ?: apiEndpoints["local"]!!
            
            val response = client.post("$endpoint/upload") {
                headers {
                    append(HttpHeaders.ContentType, "multipart/form-data")
                }
                setBody(MultiPartFormDataContent(
                    formData {
                        append("file", file, Headers.build {
                            append(HttpHeaders.ContentType, mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                        })
                    }
                ))
            }
            
            if (response.status.isSuccess()) {
                val result = response.bodyAsText()
                Result.success(result)
            } else {
                Result.failure(Exception("Upload failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Progressive Web App support
    fun registerServiceWorker(scriptUrl: String = "/service-worker.js") {
        if (js("'serviceWorker' in navigator") as Boolean) {
            window.navigator.serviceWorker.register(scriptUrl).then(
                onFulfilled = { registration ->
                    console.log("Service Worker registered: $registration")
                },
                onRejected = { error ->
                    console.error("Service Worker registration failed: $error")
                }
            )
        }
    }
    
    // Cache management for offline support
    suspend fun cacheResponse(url: String, response: String) {
        if (js("'caches' in window") as Boolean) {
            val cache = window.caches.open("cinefiller-v1").await()
            val request = Request(url)
            val responseObj = Response(response, ResponseInit(
                status = 200,
                statusText = "OK",
                headers = Headers().apply {
                    append("Content-Type", "application/json")
                }
            ))
            cache.put(request, responseObj).await()
        }
    }
}

// External declarations for browser APIs
external interface Navigator {
    val serviceWorker: ServiceWorkerContainer
}

external interface ServiceWorkerContainer {
    fun register(scriptURL: String): Promise<ServiceWorkerRegistration>
}

external interface ServiceWorkerRegistration

external interface Window {
    val caches: CacheStorage
}

external interface CacheStorage {
    fun open(cacheName: String): Promise<Cache>
}

external interface Cache {
    fun put(request: Request, response: Response): Promise<Unit>
    fun match(request: Request): Promise<Response?>
}

external class Request(url: String)
external class Response(body: String, init: ResponseInit)
external interface ResponseInit {
    var status: Int
    var statusText: String
    var headers: Headers
}
external class Headers {
    fun append(name: String, value: String)
}
