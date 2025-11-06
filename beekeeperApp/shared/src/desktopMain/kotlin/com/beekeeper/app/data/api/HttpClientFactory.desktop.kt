// File: shared/src/desktopMain/kotlin/com/cinefiller/fillerapp/data/api/HttpClientFactory.desktop.kt
package com.beekeeper.app.data.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*

/**
 * Desktop implementation using CIO engine
 */
actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(CIO)
}
