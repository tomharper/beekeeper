package com.beekeeper.app.data.api

actual object ApiConfig {
    actual fun getBaseUrl(): String {
        // iOS simulator can use localhost
        return "http://localhost:2020/api"
    }
}
