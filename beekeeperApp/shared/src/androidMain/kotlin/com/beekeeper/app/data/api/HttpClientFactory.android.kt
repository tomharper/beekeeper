// File: shared/src/androidMain/kotlin/com/cinefiller/fillerapp/data/api/HttpClientFactory.android.kt
package com.beekeeper.app.data.api

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*

/**
 * Android implementation using OkHttp engine
 */
actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(OkHttp)
}
