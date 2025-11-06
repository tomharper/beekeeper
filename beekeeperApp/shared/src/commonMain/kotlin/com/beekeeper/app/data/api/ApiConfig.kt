// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/api/ApiConfig.kt
package com.beekeeper.app.data.api

/**
 * API configuration for backend communication
 */
object ApiConfig {
    /**
     * Base URL for the backend API
     * Production: cinefiller-production.up.railway.app (Railway hosted)
     * Local development:
     *   - Android emulator: http://10.0.2.2:4050
     *   - iOS simulator: http://localhost:4050
     *   - Physical device: http://<YOUR_COMPUTER_IP>:4050
     */
    private const val USE_PRODUCTION = true
    private const val USE_LOCAL_DEVICE = false // Set to true when testing on physical device
    private const val USE_IOS = true // Set to true for iOS, false for Android

    private const val PRODUCTION_URL = "https://cinefiller-production.up.railway.app"
    private const val LOCAL_ANDROID_URL = "http://10.0.2.2:4050"
    private const val LOCAL_IOS_URL = "http://localhost:4050"
    private const val LOCAL_DEVICE_URL = "http://192.168.1.X:4050" // Replace X with your computer's local IP

    val BASE_URL = if (USE_PRODUCTION) {
        PRODUCTION_URL
    } else if (USE_LOCAL_DEVICE) {
        LOCAL_DEVICE_URL
    } else {
        if (USE_IOS) LOCAL_IOS_URL else LOCAL_ANDROID_URL
    }

    /**
     * API version path
     */
    const val API_VERSION = "/api/v1"

    /**
     * Full API base URL
     */
    val API_BASE_URL = "$BASE_URL$API_VERSION"

    /**
     * Timeout configurations (in milliseconds)
     */
    const val CONNECT_TIMEOUT = 30_000L
    const val REQUEST_TIMEOUT = 60_000L
    const val SOCKET_TIMEOUT = 60_000L

    /**
     * Enable/disable offline mode (fallback to local factories)
     * Set to true to use only local factory data
     * Set to false to fetch from backend API
     *
     * NOTE: Should be FALSE when using production backend
     */
    var offlineMode = false
}
