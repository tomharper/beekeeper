// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/auth/AuthManager.kt
package com.beekeeper.app.domain.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages authentication state and token across the application
 * Equivalent to React's AuthContext
 */
class AuthManager(
    private val tokenStorage: TokenStorage
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    /**
     * Initialize auth state by checking for stored token
     */
    suspend fun initialize() {
        _authState.value = AuthState.Loading

        val storedToken = tokenStorage.getToken()
        if (storedToken != null) {
            _token.value = storedToken
            _authState.value = AuthState.Authenticated(storedToken)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Set authentication token after successful login
     */
    suspend fun setToken(newToken: String) {
        _token.value = newToken
        tokenStorage.saveToken(newToken)
        _authState.value = AuthState.Authenticated(newToken)
    }

    /**
     * Clear authentication token on logout
     */
    suspend fun clearToken() {
        _token.value = null
        tokenStorage.clearToken()
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Get current token for API requests
     */
    fun getCurrentToken(): String? = _token.value

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = _authState.value is AuthState.Authenticated
}

/**
 * Authentication state representation
 */
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val token: String) : AuthState()
}

/**
 * Platform-specific token storage interface
 * iOS: Keychain
 * Android: EncryptedSharedPreferences
 */
interface TokenStorage {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
}
