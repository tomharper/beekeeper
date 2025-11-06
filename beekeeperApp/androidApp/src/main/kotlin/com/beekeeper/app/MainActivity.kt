// File: androidApp/src/main/kotlin/com/cinefiller/fillerapp/android/MainActivity.kt
package com.beekeeper.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.beekeeper.app.App
import com.beekeeper.app.presentation.theme.CineFillerTheme
import com.beekeeper.app.data.ObjectBoxDatabase
import com.beekeeper.app.domain.repository.ObjectBoxRepository
import com.beekeeper.app.data.MigrationManager
import com.beekeeper.app.data.ObjectBoxMigrationService
import com.beekeeper.app.domain.repository.DataInitializer
import com.beekeeper.app.config.FeatureFlags
import com.beekeeper.app.data.sqldelight.CineFillerDatabase
import com.beekeeper.app.data.sqldelight.DatabaseDriverFactory
import com.beekeeper.app.domain.repository.SQLDelightProjectFactoryRepository
import com.beekeeper.app.domain.auth.AndroidTokenStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Print feature flags on startup
        FeatureFlags.printStatus()

        // Initialize TokenStorage for authentication (MUST be before RepositoryManager.authManager is accessed)
        com.beekeeper.app.domain.repository.RepositoryManager.initializeTokenStorage(
            AndroidTokenStorage(applicationContext)
        )

        // Initialize SQLDelight (primary)
        val sqlDelightDriver = DatabaseDriverFactory(this).createDriver()
        val sqlDelightDatabase = CineFillerDatabase(sqlDelightDriver)
        val sqlDelightRepository = SQLDelightProjectFactoryRepository(sqlDelightDatabase)

        // Register SQLDelight repository and database in RepositoryManager
        com.beekeeper.app.domain.repository.RepositoryManager.initializeSQLDelight(sqlDelightRepository, sqlDelightDatabase)

        // Initialize ObjectBox (legacy - for fallback)
        val objectBoxDatabase = ObjectBoxDatabase()
        val objectBoxInitSuccess = objectBoxDatabase.initialize(this)

        val dataInitializer = if (objectBoxInitSuccess) {
            // Create repository and migration services for ObjectBox
            val objectBoxRepository = ObjectBoxRepository(objectBoxDatabase.boxStore)
            val migrationService = ObjectBoxMigrationService(objectBoxDatabase.boxStore, objectBoxRepository)
            val migrationManager = MigrationManager(objectBoxRepository, migrationService)

            // Initialize with both SQLDelight and ObjectBox
            DataInitializer(
                objectBoxRepository = objectBoxRepository,
                migrationService = migrationService,
                migrationManager = migrationManager,
                sqlDelightRepository = sqlDelightRepository
            )
        } else {
            // Initialize with only SQLDelight
            DataInitializer(
                sqlDelightRepository = sqlDelightRepository
            )
        }

        // Initialize data based on feature flags
        dataInitializer.initializeAsync()


        setContent {
            CineFillerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}