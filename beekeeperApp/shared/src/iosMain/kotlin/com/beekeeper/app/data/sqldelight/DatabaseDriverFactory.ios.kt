// File: shared/src/iosMain/kotlin/com/cinefiller/fillerapp/data/sqldelight/DatabaseDriverFactory.ios.kt
package com.beekeeper.app.data.sqldelight

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS implementation of DatabaseDriverFactory
 * Uses NativeSqliteDriver
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = CineFillerDatabase.Schema,
            name = "cinefiller.db"
        )
    }
}
