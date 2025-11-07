package com.beekeeper.app.data.database

import com.beekeeper.app.database.Task as DbTask
import com.beekeeper.app.domain.model.Task
import com.beekeeper.app.domain.model.TaskType
import com.beekeeper.app.domain.model.TaskStatus
import com.beekeeper.app.domain.model.TaskPriority
import com.beekeeper.app.domain.model.RecurrenceFrequency
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Task.toDbTask(): DbTask {
    return DbTask(
        id = id,
        title = title,
        description = description,
        taskType = taskType.name,
        dueDate = dueDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
        status = status.name,
        priority = priority.name,
        hiveId = hiveId,
        apiaryId = apiaryId,
        userId = userId,
        recurrenceFrequency = recurrenceFrequency?.name,
        recurrenceInterval = recurrenceInterval?.toLong(),
        recurrenceEndDate = recurrenceEndDate?.toInstant(TimeZone.currentSystemDefault())?.toEpochMilliseconds(),
        completedDate = completedDate?.toInstant(TimeZone.currentSystemDefault())?.toEpochMilliseconds(),
        createdAt = createdAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
        updatedAt = updatedAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    )
}

fun DbTask.toDomainTask(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        taskType = TaskType.valueOf(taskType),
        dueDate = Instant.fromEpochMilliseconds(dueDate).toLocalDateTime(TimeZone.currentSystemDefault()),
        status = TaskStatus.valueOf(status),
        priority = TaskPriority.valueOf(priority),
        hiveId = hiveId,
        apiaryId = apiaryId,
        userId = userId,
        recurrenceFrequency = recurrenceFrequency?.let { RecurrenceFrequency.valueOf(it) },
        recurrenceInterval = recurrenceInterval?.toInt(),
        recurrenceEndDate = recurrenceEndDate?.let {
            Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault())
        },
        completedDate = completedDate?.let {
            Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault())
        },
        createdAt = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt).toLocalDateTime(TimeZone.currentSystemDefault())
    )
}
