package com.beekeeper.app.data.api

actual object ApiConfig {
    actual fun getBaseUrl(): String {
        // Use 10.0.2.2 for Android emulator to reach host machine
        // For physical devices, you'd need to use your machine's IP address
        return "http://10.0.2.2:2020/api"
    }
}
