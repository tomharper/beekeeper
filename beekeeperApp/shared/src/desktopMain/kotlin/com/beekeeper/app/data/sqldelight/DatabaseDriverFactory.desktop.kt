// File: shared/src/desktopMain/kotlin/com/cinefiller/fillerapp/data/sqldelight/DatabaseDriverFactory.desktop.kt
package com.beekeeper.app.data.sqldelight

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

/**
 * Desktop/JVM implementation of DatabaseDriverFactory
 * Uses JdbcSqliteDriver with file-based storage
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".cinefiller/cinefiller.db")
        databasePath.parentFile?.mkdirs()

        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        CineFillerDatabase.Schema.create(driver)
        return driver
    }
}
