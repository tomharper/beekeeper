package com.beekeeper.app.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val taskType: TaskType,
    val dueDate: LocalDateTime,
    val status: TaskStatus,
    val priority: TaskPriority,
    val hiveId: String? = null,
    val apiaryId: String? = null,
    val userId: String,
    val recurrenceFrequency: RecurrenceFrequency? = null,
    val recurrenceInterval: Int? = null,
    val recurrenceEndDate: LocalDateTime? = null,
    val completedDate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class TaskType {
    INSPECTION,
    FEEDING,
    MEDICATION,
    HARVESTING,
    HIVE_MAINTENANCE,
    EQUIPMENT_CHECK,
    QUEEN_CHECK,
    SWARM_PREVENTION,
    SUPERING,
    WINTER_PREP,
    SPRING_INSPECTION,
    VARROA_TREATMENT,
    NOSEMA_TREATMENT,
    SMALL_HIVE_BEETLE_CHECK,
    WAX_MOTH_PREVENTION,
    COMBINE_HIVES,
    SPLIT_HIVE,
    REQUEEN,
    EMERGENCY_CHECK,
    WEIGHT_CHECK,
    TEMPERATURE_MONITORING,
    VENTILATION_CHECK,
    ENTRANCE_REDUCER_INSTALL,
    ENTRANCE_REDUCER_REMOVE,
    MOUSE_GUARD_INSTALL,
    MOUSE_GUARD_REMOVE,
    RECORD_KEEPING,
    EDUCATION,
    RESEARCH,
    OTHER,
    GENERAL
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
