package com.beekeeper.app

import androidx.compose.ui.window.ComposeUIViewController
import com.beekeeper.app.di.appModule
import com.beekeeper.app.di.platformModule
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }

fun initialize() {
    startKoin {
        modules(appModule, platformModule)
    }
}
