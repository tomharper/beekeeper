package com.beekeeper.app.data.database

import com.beekeeper.app.database.FeedItem as DbFeedItem
import com.beekeeper.app.data.api.FeedItemDto
import com.beekeeper.app.domain.model.FeedItem
import com.beekeeper.app.domain.model.FeedItemType
import com.beekeeper.app.domain.model.FollowUser
import com.beekeeper.app.domain.model.Inspection
import com.beekeeper.app.domain.model.Task
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Stable cache id for a feed item. The backend FeedItemResponse has no
 * top-level id, but each carried record has one and appears once, so we
 * key the cache by the record id (prefixed by type to avoid collisions).
 */
private fun FeedItemDto.cacheId(): String {
    val recordId = inspection?.id ?: task?.id ?: "${author.id}-${occurredAt}"
    return "$type:$recordId"
}

private fun FeedItemDto.summaryLine(): String {
    return when (type.lowercase()) {
        "inspection" -> inspection.inspectionSummary()
        "task" -> task?.title ?: "Task"
        else -> type
    }
}

private fun Inspection?.inspectionSummary(): String {
    if (this == null) return "Inspection"
    val tags = buildList {
        if (queenSeen) add("queen seen")
        if (feedingDone) add("fed")
        if (treatmentApplied) add("treated")
    }
    return if (tags.isEmpty()) "Inspection" else "Inspection: ${tags.joinToString(", ")}"
}

/** DTO (from API) -> domain. Carries the full record plus a short summary line. */
fun FeedItemDto.toDomain(): FeedItem {
    val itemType = if (type.lowercase() == "task") FeedItemType.TASK else FeedItemType.INSPECTION
    return FeedItem(
        id = cacheId(),
        type = itemType,
        author = FollowUser(id = author.id, fullName = author.fullName),
        occurredAt = occurredAt,
        hiveId = inspection?.hiveId ?: task?.hiveId,
        summary = summaryLine(),
        inspection = inspection,
        task = task
    )
}

/** DTO -> database row (flat cache; full record is not persisted). */
fun FeedItemDto.toDbFeedItem(): DbFeedItem {
    val itemType = if (type.lowercase() == "task") "TASK" else "INSPECTION"
    return DbFeedItem(
        id = cacheId(),
        type = itemType,
        authorId = author.id,
        authorName = author.fullName,
        occurredAt = occurredAt.toInstant(TimeZone.UTC).toEpochMilliseconds(),
        hiveId = inspection?.hiveId ?: task?.hiveId,
        summary = summaryLine()
    )
}

/** Database row -> domain. Cache only stores the summary, so the full record is null. */
fun DbFeedItem.toDomainFeedItem(): FeedItem {
    return FeedItem(
        id = id,
        type = if (type == "TASK") FeedItemType.TASK else FeedItemType.INSPECTION,
        author = FollowUser(id = authorId, fullName = authorName),
        occurredAt = Instant.fromEpochMilliseconds(occurredAt)
            .toLocalDateTime(TimeZone.UTC),
        hiveId = hiveId,
        summary = summary,
        inspection = null,
        task = null
    )
}
