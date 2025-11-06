// File: shared/src/iosMain/kotlin/com/cinefiller/fillerapp/domain/auth/IosTokenStorage.kt
package com.beekeeper.app.domain.auth

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*
import platform.CoreFoundation.*
import platform.darwin.memcpy
import platform.posix.*

/**
 * iOS implementation of TokenStorage using Keychain
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
class IosTokenStorage : TokenStorage {

    override suspend fun saveToken(token: String) {
        // Delete any existing token first
        clearToken()

        val tokenData = token.encodeToByteArray().usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = token.length.toULong())
        }

        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to KEY_AUTH_TOKEN,
            kSecValueData to tokenData
        )

        val status = SecItemAdd(query as CFDictionaryRef, null)
        if (status != errSecSuccess) {
            println("Failed to save token to keychain: $status")
        }
    }

    override suspend fun getToken(): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to KEY_AUTH_TOKEN,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)

            if (status == errSecSuccess) {
                val data = result.value as? NSData
                if (data != null) {
                    val bytes = ByteArray(data.length.toInt())
                    bytes.usePinned { pinned ->
                        memcpy(pinned.addressOf(0), data.bytes, data.length)
                    }
                    return bytes.decodeToString()
                }
            }
        }
        return null
    }

    override suspend fun clearToken() {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to KEY_AUTH_TOKEN
        )

        SecItemDelete(query as CFDictionaryRef)
    }

    companion object {
        private const val SERVICE_NAME = "com.beekeeper.app"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
}
