package com.beekeeper.app.data.database

import com.beekeeper.app.database.Inspection as DbInspection
import com.beekeeper.app.domain.model.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Extension function to convert domain Inspection to database Inspection
 */
fun Inspection.toDbInspection(): DbInspection {
    return DbInspection(
        id = id,
        hiveId = hiveId,
        userId = userId,
        inspectionDate = inspectionDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
        durationMinutes = durationMinutes?.toLong(),
        queenSeen = if (queenSeen) 1L else 0L,
        queenMarked = if (queenMarked) 1L else 0L,
        queenColor = queenColor,
        queenCells = queenCells.name,
        eggsSeen = if (eggsSeen) 1L else 0L,
        larvaeSeen = if (larvaeSeen) 1L else 0L,
        cappedBroodSeen = if (cappedBroodSeen) 1L else 0L,
        broodPattern = broodPattern.name,
        broodFrames = broodFrames?.toLong(),
        population = population.name,
        temperament = temperament.name,
        healthStatus = healthStatus.name,
        honeyStores = honeyStores.name,
        honeyFrames = honeyFrames?.toLong(),
        pollenStores = pollenStores.name,
        pollenFrames = pollenFrames?.toLong(),
        varroaMitesDetected = if (varroaMitesDetected) 1L else 0L,
        varroaLevel = varroaLevel,
        otherPestsDetected = if (otherPestsDetected) 1L else 0L,
        pestDescription = pestDescription,
        diseaseDetected = if (diseaseDetected) 1L else 0L,
        diseaseDescription = diseaseDescription,
        feedingDone = if (feedingDone) 1L else 0L,
        feedingType = feedingType,
        feedingAmount = feedingAmount,
        treatmentApplied = if (treatmentApplied) 1L else 0L,
        treatmentType = treatmentType,
        equipmentChanges = equipmentChanges,
        weatherTemp = weatherTemp,
        weatherConditions = weatherConditions,
        notes = notes,
        photos = photos?.let { Json.encodeToString(it) },
        nextInspectionDate = nextInspectionDate?.toInstant(TimeZone.currentSystemDefault())?.toEpochMilliseconds(),
        createdAt = createdAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
        updatedAt = updatedAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    )
}

/**
 * Extension function to convert database Inspection to domain Inspection
 */
fun DbInspection.toDomainInspection(): Inspection {
    return Inspection(
        id = id,
        hiveId = hiveId,
        userId = userId,
        inspectionDate = Instant.fromEpochMilliseconds(inspectionDate).toLocalDateTime(TimeZone.currentSystemDefault()),
        durationMinutes = durationMinutes?.toInt(),
        queenSeen = queenSeen == 1L,
        queenMarked = queenMarked == 1L,
        queenColor = queenColor,
        queenCells = QueenCellStatus.valueOf(queenCells),
        eggsSeen = eggsSeen == 1L,
        larvaeSeen = larvaeSeen == 1L,
        cappedBroodSeen = cappedBroodSeen == 1L,
        broodPattern = BroodPattern.valueOf(broodPattern),
        broodFrames = broodFrames?.toInt(),
        population = ColonyPopulation.valueOf(population),
        temperament = ColonyTemperament.valueOf(temperament),
        healthStatus = InspectionHealthStatus.valueOf(healthStatus),
        honeyStores = ResourceLevel.valueOf(honeyStores),
        honeyFrames = honeyFrames?.toInt(),
        pollenStores = ResourceLevel.valueOf(pollenStores),
        pollenFrames = pollenFrames?.toInt(),
        varroaMitesDetected = varroaMitesDetected == 1L,
        varroaLevel = varroaLevel,
        otherPestsDetected = otherPestsDetected == 1L,
        pestDescription = pestDescription,
        diseaseDetected = diseaseDetected == 1L,
        diseaseDescription = diseaseDescription,
        feedingDone = feedingDone == 1L,
        feedingType = feedingType,
        feedingAmount = feedingAmount,
        treatmentApplied = treatmentApplied == 1L,
        treatmentType = treatmentType,
        equipmentChanges = equipmentChanges,
        weatherTemp = weatherTemp,
        weatherConditions = weatherConditions,
        notes = notes,
        photos = photos?.let { Json.decodeFromString<List<String>>(it) },
        nextInspectionDate = nextInspectionDate?.let {
            Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault())
        },
        createdAt = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt).toLocalDateTime(TimeZone.currentSystemDefault())
    )
}
