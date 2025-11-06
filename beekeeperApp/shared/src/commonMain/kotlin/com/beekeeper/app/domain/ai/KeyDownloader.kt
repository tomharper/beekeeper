// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/ai/KeyDownloader.kt
package com.beekeeper.app.domain.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Utility for securely downloading API keys from a remote vault
 * This ensures keys are never stored in code or config files
 */
class KeyDownloader {

    private val client = HttpClient()

    suspend fun downloadKeysFromVault(
        vaultUrl: String,
        authToken: String,
        environment: String = "production"
    ): AIServiceConfiguration? {
        return withContext(Dispatchers.Default) {
            try {
                val response = client.get("$vaultUrl/api/v1/keys/$environment") {
                    header("Authorization", "Bearer $authToken")
                    header("Content-Type", "application/json")
                }

                if (response.status == HttpStatusCode.OK) {
                    val configJson = response.body<String>()
                    Json.decodeFromString<AIServiceConfiguration>(configJson)
                } else {
                    println("Failed to download keys: ${response.status}")
                    null
                }
            } catch (e: Exception) {
                println("Error downloading keys: ${e.message}")
                null
            }
        }
    }

    suspend fun downloadAndCacheKeys(
        vaultUrl: String,
        authToken: String,
        environment: String = "production"
    ): Boolean {
        val config = downloadKeysFromVault(vaultUrl, authToken, environment)
        return if (config != null) {
            // Cache keys securely for this session
            cacheKeysSecurely(config)
            true
        } else {
            false
        }
    }

    private suspend fun cacheKeysSecurely(config: AIServiceConfiguration) {
        // Platform-specific secure storage implementation
        // This would store keys in keychain/keystore for the session
    }
}
