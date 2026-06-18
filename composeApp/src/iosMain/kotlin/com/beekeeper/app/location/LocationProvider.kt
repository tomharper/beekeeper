package com.beekeeper.app.location

import com.beekeeper.app.domain.model.Coordinates
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

@OptIn(ExperimentalForeignApi::class)
actual class LocationProvider {

    private val manager = CLLocationManager()

    actual fun hasPermission(): Boolean {
        val status = CLLocationManager.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
    }

    actual suspend fun current(): Coordinates? {
        if (!hasPermission()) return null
        val location = manager.location ?: return null
        return location.coordinate.useContents { Coordinates(latitude, longitude) }
    }
}
