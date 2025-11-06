// File: desktopApp/src/jvmMain/kotlin/Main.kt
package com.beekeeper.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.beekeeper.app.di.DatabaseModule
import com.beekeeper.app.data.migration.RunMigration

fun main() = application {
    // Initialize ObjectBox for desktop using existing ObjectBoxDatabase
    val initSuccess = DatabaseModule.initialize()

    if (!initSuccess) {
        println("Failed to initialize ObjectBox database")
        exitApplication()
        return@application
    }

    val repository = DatabaseModule.provideObjectBoxRepository()

    Window(
        onCloseRequest = {
            exitApplication()
        },
        title = "CineFiller"
    ) {
        CineFillerApp(repository)
    }
}