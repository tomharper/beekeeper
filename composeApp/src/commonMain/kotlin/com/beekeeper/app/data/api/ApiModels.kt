package com.beekeeper.app.data.api

import com.beekeeper.app.domain.model.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val taskType: TaskType,
    val dueDate: LocalDateTime,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val hiveId: String? = null,
    val apiaryId: String? = null,
    val recurrenceFrequency: RecurrenceFrequency? = null,
    val recurrenceInterval: Int? = null,
    val recurrenceEndDate: LocalDateTime? = null
)

@Serializable
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val taskType: TaskType? = null,
    val dueDate: LocalDateTime? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val hiveId: String? = null,
    val apiaryId: String? = null,
    val recurrenceFrequency: RecurrenceFrequency? = null,
    val recurrenceInterval: Int? = null,
    val recurrenceEndDate: LocalDateTime? = null
)

@Serializable
data class CreateInspectionRequest(
    val hiveId: String,
    val inspectionDate: LocalDateTime,
    val durationMinutes: Int? = null,
    val queenSeen: Boolean = false,
    val queenMarked: Boolean = false,
    val queenColor: String? = null,
    val queenCells: QueenCellStatus = QueenCellStatus.NONE,
    val eggsSeen: Boolean = false,
    val larvaeSeen: Boolean = false,
    val cappedBroodSeen: Boolean = false,
    val broodPattern: BroodPattern = BroodPattern.GOOD,
    val broodFrames: Int? = null,
    val population: ColonyPopulation = ColonyPopulation.MEDIUM,
    val temperament: ColonyTemperament = ColonyTemperament.CALM,
    val healthStatus: InspectionHealthStatus = InspectionHealthStatus.HEALTHY,
    val honeyStores: ResourceLevel = ResourceLevel.MEDIUM,
    val honeyFrames: Int? = null,
    val pollenStores: ResourceLevel = ResourceLevel.MEDIUM,
    val pollenFrames: Int? = null,
    val varroaMitesDetected: Boolean = false,
    val varroaLevel: String? = null,
    val otherPestsDetected: Boolean = false,
    val pestDescription: String? = null,
    val diseaseDetected: Boolean = false,
    val diseaseDescription: String? = null,
    val feedingDone: Boolean = false,
    val feedingType: String? = null,
    val feedingAmount: String? = null,
    val treatmentApplied: Boolean = false,
    val treatmentType: String? = null,
    val equipmentChanges: String? = null,
    val weatherTemp: Double? = null,
    val weatherConditions: String? = null,
    val notes: String? = null,
    val photos: List<String>? = null,
    val nextInspectionDate: LocalDateTime? = null
)

@Serializable
data class UpdateInspectionRequest(
    val inspectionDate: LocalDateTime? = null,
    val durationMinutes: Int? = null,
    val queenSeen: Boolean? = null,
    val queenMarked: Boolean? = null,
    val queenColor: String? = null,
    val queenCells: QueenCellStatus? = null,
    val eggsSeen: Boolean? = null,
    val larvaeSeen: Boolean? = null,
    val cappedBroodSeen: Boolean? = null,
    val broodPattern: BroodPattern? = null,
    val broodFrames: Int? = null,
    val population: ColonyPopulation? = null,
    val temperament: ColonyTemperament? = null,
    val healthStatus: InspectionHealthStatus? = null,
    val honeyStores: ResourceLevel? = null,
    val honeyFrames: Int? = null,
    val pollenStores: ResourceLevel? = null,
    val pollenFrames: Int? = null,
    val varroaMitesDetected: Boolean? = null,
    val varroaLevel: String? = null,
    val otherPestsDetected: Boolean? = null,
    val pestDescription: String? = null,
    val diseaseDetected: Boolean? = null,
    val diseaseDescription: String? = null,
    val feedingDone: Boolean? = null,
    val feedingType: String? = null,
    val feedingAmount: String? = null,
    val treatmentApplied: Boolean? = null,
    val treatmentType: String? = null,
    val equipmentChanges: String? = null,
    val weatherTemp: Double? = null,
    val weatherConditions: String? = null,
    val notes: String? = null,
    val photos: List<String>? = null,
    val nextInspectionDate: LocalDateTime? = null
)
