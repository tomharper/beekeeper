package com.beekeeper.app.location

import com.beekeeper.app.domain.model.Coordinates

/**
 * Reads the device's current location ("set the apiary while you're at the spot").
 * Constructed per-platform via [com.beekeeper.app.di.platformModule] (Android needs a
 * Context; iOS needs none), mirroring DatabaseDriverFactory.
 */
expect class LocationProvider {
    /** True if foreground location permission is currently granted. */
    fun hasPermission(): Boolean

    /** Best-effort last-known device location, or null if unavailable / no permission. */
    suspend fun current(): Coordinates?
}
