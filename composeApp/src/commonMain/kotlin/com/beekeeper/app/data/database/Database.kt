package com.beekeeper.app.data.database

import com.beekeeper.app.database.BeekeeperDatabase

class Database(driverFactory: DatabaseDriverFactory) {
    private val driver = driverFactory.createDriver()
    val database = BeekeeperDatabase(driver)

    val taskQueries = database.taskQueries
    val inspectionQueries = database.inspectionQueries
    val hiveQueries = database.hiveQueries
    val apiaryQueries = database.apiaryQueries
}
