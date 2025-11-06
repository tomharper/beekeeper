// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/sqldelight/DatabaseDriverFactory.kt
package com.beekeeper.app.data.sqldelight

import app.cash.sqldelight.db.SqlDriver

/**
 * Expect/Actual pattern for platform-specific SQLDelight drivers
 * Each platform (Android, iOS, Desktop) provides its own driver implementation
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
