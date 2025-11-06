package com.beekeeper.app.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Represents a beekeeping task or reminder
 */
@Serializable
data class BeekeepingTask(
    val id: String,
    val title: String,
    val description: String = "",
    val taskType: TaskType,

    // Scheduling
    val dueDate: LocalDateTime,
    val reminderDate: LocalDateTime? = null,
    val recurrence: TaskRecurrence? = null,

    // Association
    val hiveId: String? = null,  // null for general tasks
    val relatedInspectionId: String? = null,

    // Status
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val completedDate: LocalDateTime? = null,

    // Additional info
    val estimatedDurationMinutes: Int? = null,
    val weatherDependent: Boolean = false,
    val minimumTemperature: Double? = null,  // Minimum temp for task
    val notes: String = "",
    val checklist: List<TaskChecklistItem> = emptyList(),

    val createdAt: String,
    val updatedAt: String
)

/**
 * Checklist item within a task
 */
@Serializable
data class TaskChecklistItem(
    val id: String,
    val description: String,
    val completed: Boolean = false
)

/**
 * Recurrence pattern for repeating tasks
 */
@Serializable
data class TaskRecurrence(
    val frequency: RecurrenceFrequency,
    val interval: Int = 1,  // Every X frequency units
    val endDate: LocalDateTime? = null,
    val count: Int? = null  // Number of occurrences
)

/**
 * Type of beekeeping task
 */
@Serializable
enum class TaskType {
    // Regular maintenance
    INSPECTION,
    FEEDING,
    WATER_CHECK,

    // Seasonal
    SPRING_INSPECTION,
    SUMMER_INSPECTION,
    FALL_PREPARATION,
    WINTER_CHECK,

    // Health
    PEST_TREATMENT,
    DISEASE_TREATMENT,
    MEDICATION,

    // Production
    HARVEST_HONEY,
    EXTRACT_HONEY,
    BOTTLE_HONEY,
    HARVEST_WAX,
    HARVEST_PROPOLIS,

    // Colony management
    SPLIT_HIVE,
    COMBINE_HIVES,
    REQUEEN,
    SWARM_PREVENTION,
    SWARM_COLLECTION,

    // Equipment
    ADD_BOXES,
    REMOVE_BOXES,
    CLEAN_EQUIPMENT,
    REPAIR_EQUIPMENT,
    BUILD_FRAMES,

    // Other
    EDUCATION,
    RECORD_KEEPING,
    ORDER_SUPPLIES,
    GENERAL,
    OTHER
}

/**
 * Status of a task
 */
@Serializable
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    OVERDUE
}

/**
 * Priority level of a task
 */
@Serializable
enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

/**
 * Frequency for recurring tasks
 */
@Serializable
enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY
}

/**
 * Template for common beekeeping tasks
 */
@Serializable
data class TaskTemplate(
    val id: String,
    val name: String,
    val taskType: TaskType,
    val description: String,
    val estimatedDurationMinutes: Int,
    val defaultChecklist: List<String>,
    val seasonalRecommendation: List<Season>,
    val weatherDependent: Boolean,
    val minimumTemperature: Double?
)

/**
 * Season for seasonal recommendations
 */
@Serializable
enum class Season {
    SPRING,
    SUMMER,
    FALL,
    WINTER
}
