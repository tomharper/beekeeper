package com.beekeeper.app.location

import androidx.compose.runtime.Composable

/**
 * Returns a function that, when invoked, requests foreground location permission and
 * reports the outcome via [onResult]. Platform-specific (Android uses an activity-result
 * launcher; iOS asks CoreLocation). Call the returned lambda from a click handler.
 */
@Composable
expect fun rememberLocationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit
