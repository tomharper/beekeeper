package com.beekeeper.app.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.beekeeper.app.database.BeekeeperDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = BeekeeperDatabase.Schema,
            name = "beekeeper.db"
        )
    }
}
