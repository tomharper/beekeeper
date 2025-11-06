// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/api/HttpClientFactory.kt
package com.beekeeper.app.data.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Factory for creating configured HTTP clients
 * Uses platform-specific engines via expect/actual
 */
object HttpClientFactory {

    /**
     * Create a configured HttpClient for API communication
     */
    fun create(): HttpClient {
        return createPlatformHttpClient().config {
            // Install JSON content negotiation
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            // Install logging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            // Install timeout configuration
            install(HttpTimeout) {
                connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT
                requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT
                socketTimeoutMillis = ApiConfig.SOCKET_TIMEOUT
            }

            // Default request configuration
            defaultRequest {
                url(ApiConfig.API_BASE_URL)
            }
        }
    }
}

/**
 * Platform-specific HTTP client creation
 * Implemented in each platform's source set
 */
expect fun createPlatformHttpClient(): HttpClient
