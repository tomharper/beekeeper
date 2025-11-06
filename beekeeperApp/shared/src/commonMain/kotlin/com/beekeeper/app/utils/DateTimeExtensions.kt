// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/utils/DateTimeExtensions.kt
package com.beekeeper.app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Extension functions for date/time operations in the CineFiller app
 */

// Format timestamp for display in UI
fun Long.toDisplayDate(): String {
    return when {
        isToday(this) -> "Today, ${getFormattedTime(this, "HH:mm")}"
        isYesterday(this) -> "Yesterday, ${getFormattedTime(this, "HH:mm")}"
        else -> getFormattedDate(this, "MMM dd, yyyy")
    }
}

// Format duration in milliseconds to readable format
fun Long.toReadableDuration(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "$days day${if (days > 1) "s" else ""}"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}"
        else -> "$seconds second${if (seconds > 1) "s" else ""}"
    }
}

// Format timestamp for file names
fun Long.toFileNameTimestamp(): String {
    return getFormattedDate(this, "yyyyMMdd_HHmmss")
}

// Get time ago string
fun Long.timeAgo(): String {
    return getRelativeTimeString(this)
}

// Check if timestamp is within last N days
fun Long.isWithinDays(days: Int): Boolean {
    val dayInMillis = 24L * 60 * 60 * 1000
    val now = getCurrentTimeMillis()
    return (now - this) < (days * dayInMillis)
}

// Format for API calls
fun Long.toISOString(): String {
    return getFormattedTimestamp(this).replace(" ", "T") + "Z"
}

// Project-specific formatting
fun formatProjectDate(timestamp: Long): String {
    return getFormattedDate(timestamp, "MMM dd, yyyy")
}

fun formatSceneTimestamp(timestamp: Long): String {
    return getFormattedTime(timestamp, "mm:ss")
}

fun formatCharacterLastModified(timestamp: Long): String {
    return when {
        isToday(timestamp) -> "Modified today at ${getFormattedTime(timestamp, "HH:mm")}"
        isYesterday(timestamp) -> "Modified yesterday"
        else -> "Modified on ${getFormattedDate(timestamp, "MMM dd")}"
    }
}

// Reuse formatTimestamp from FeedScreen
fun formatTimestamp(timestamp: Instant): String {
    val now = Clock.System.now()
    val duration = now - timestamp

    return when {
        duration < 1.minutes -> "now"
        duration < 1.hours -> "${duration.inWholeMinutes}m"
        duration < 1.days -> "${duration.inWholeHours}h"
        duration < 7.days -> "${duration.inWholeDays}d"
        else -> {
            val date = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
            "${date.monthNumber}/${date.dayOfMonth}"
        }
    }
}

/**
 * Utility: Format timestamp from seconds
 */
fun formatTimestampFromSeconds(totalSeconds: Float): String {
    val minutes = (totalSeconds / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()
    val milliseconds = ((totalSeconds % 1) * 100).toInt()
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}:${milliseconds.toString().padStart(2, '0')}"
}

fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 1_000_000 -> "${count / 1000}K"
        else -> "${count / 1_000_000}M"
    }
}

