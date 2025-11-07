package com.beekeeper.app.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.beekeeper.app.database.BeekeeperDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = BeekeeperDatabase.Schema,
            context = context,
            name = "beekeeper.db"
        )
    }
}
