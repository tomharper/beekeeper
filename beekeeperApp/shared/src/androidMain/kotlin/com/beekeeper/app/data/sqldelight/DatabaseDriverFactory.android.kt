// File: shared/src/androidMain/kotlin/com/cinefiller/fillerapp/data/sqldelight/DatabaseDriverFactory.android.kt
package com.beekeeper.app.data.sqldelight

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Android implementation of DatabaseDriverFactory
 * Uses AndroidSqliteDriver with app context
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = CineFillerDatabase.Schema,
            context = context,
            name = "cinefiller.db",
            callback = object : AndroidSqliteDriver.Callback(CineFillerDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Force create v2 table if it doesn't exist (handles existing databases)
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS project_factory_v2 (
                            id TEXT PRIMARY KEY NOT NULL,
                            project_id TEXT NOT NULL,
                            factory_type TEXT NOT NULL,
                            title TEXT,
                            description TEXT,
                            project_type TEXT,
                            project_json TEXT NOT NULL,
                            characters_json TEXT,
                            stories_json TEXT,
                            scripts_json TEXT,
                            storyboards_json TEXT,
                            bible_json TEXT,
                            bible_overview_json TEXT,
                            blueprints_json TEXT,
                            publishing_json TEXT,
                            metadata_json TEXT,
                            created_at INTEGER NOT NULL,
                            updated_at INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    // Create indexes
                    db.execSQL("CREATE INDEX IF NOT EXISTS project_factory_v2_project_id ON project_factory_v2(project_id)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS project_factory_v2_type ON project_factory_v2(factory_type)")
                }
            }
        )
    }
}
