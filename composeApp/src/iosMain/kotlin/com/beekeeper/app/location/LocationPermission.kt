package com.beekeeper.app.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager

@Composable
actual fun rememberLocationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit {
    val manager = remember { CLLocationManager() }
    return remember {
        {
            // iOS shows its own prompt asynchronously; report optimistically and let the
            // subsequent location read reflect the actual grant.
            manager.requestWhenInUseAuthorization()
            onResult(true)
        }
    }
}
