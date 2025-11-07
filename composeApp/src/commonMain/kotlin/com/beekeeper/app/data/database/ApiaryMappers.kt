package com.beekeeper.app.data.database

import com.beekeeper.app.database.Apiary as DbApiary
import com.beekeeper.app.domain.model.Apiary
import com.beekeeper.app.domain.model.ApiaryStatus

/**
 * Extension function to convert domain Apiary to database Apiary
 */
fun Apiary.toDbApiary(): DbApiary {
    return DbApiary(
        id = id,
        name = name,
        location = location,
        latitude = latitude,
        longitude = longitude,
        hiveCount = hiveCount.toLong(),
        status = status.name
    )
}

/**
 * Extension function to convert database Apiary to domain Apiary
 */
fun DbApiary.toDomainApiary(): Apiary {
    return Apiary(
        id = id,
        name = name,
        location = location,
        latitude = latitude,
        longitude = longitude,
        hiveCount = hiveCount.toInt(),
        status = ApiaryStatus.valueOf(status)
    )
}
