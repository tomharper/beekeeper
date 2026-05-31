package com.beekeeper.app.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * A single activity item from a followed user.
 *
 * Mirrors the backend FeedItemResponse: the record itself is the content
 * (no separate "post" abstraction). [type] discriminates which record kind
 * is carried, and the full record is kept in [inspection] / [task] when the
 * item came straight from the API. [summary] is a short human-readable line
 * used for the cached/list view.
 */
@Serializable
data class FeedItem(
    val id: String,
    val type: FeedItemType,
    val author: FollowUser,
    val occurredAt: LocalDateTime,
    val hiveId: String? = null,
    val summary: String,
    val inspection: Inspection? = null,
    val task: Task? = null
)

enum class FeedItemType {
    INSPECTION,
    TASK
}

/** Minimal public view of another beekeeper — id + name only, no PII. */
@Serializable
data class FollowUser(
    val id: String,
    val fullName: String
)
