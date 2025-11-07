package com.beekeeper.app.data.database

import com.beekeeper.app.database.Hive as DbHive
import com.beekeeper.app.domain.model.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Extension function to convert domain Hive to database Hive
 */
fun Hive.toDbHive(): DbHive {
    return DbHive(
        id = id,
        name = name,
        apiaryId = apiaryId,
        status = status.name,
        lastInspected = lastInspected.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
        imageUrl = imageUrl,
        colonyStrength = colonyStrength.name,
        queenStatus = queenStatus.name,
        temperament = temperament.name,
        honeyStores = honeyStores.name
    )
}

/**
 * Extension function to convert database Hive to domain Hive
 */
fun DbHive.toDomainHive(): Hive {
    return Hive(
        id = id,
        name = name,
        apiaryId = apiaryId,
        status = HiveStatus.valueOf(status),
        lastInspected = Instant.fromEpochMilliseconds(lastInspected).toLocalDateTime(TimeZone.currentSystemDefault()),
        imageUrl = imageUrl,
        colonyStrength = ColonyStrength.valueOf(colonyStrength),
        queenStatus = QueenStatus.valueOf(queenStatus),
        temperament = Temperament.valueOf(temperament),
        honeyStores = HoneyStores.valueOf(honeyStores)
    )
}
