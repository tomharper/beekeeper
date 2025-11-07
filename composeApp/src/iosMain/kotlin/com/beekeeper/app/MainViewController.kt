package com.beekeeper.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }

fun initialize() {
    // Initialize Koin or any other setup needed
}
