// ===== COMMON MODULE =====
// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/utils/PlatformUtils.kt
package com.beekeeper.app.utils

/**
 * Platform-specific utility functions for time and date operations
 */

// Expect declarations for platform-specific implementations
expect fun getCurrentTimeMillis(): Long

expect fun getFormattedTimestamp(timestamp: Long): String

expect fun getTimeMillis(): Long

expect fun getFormattedDate(timestamp: Long, pattern: String = "yyyy-MM-dd"): String

expect fun getFormattedTime(timestamp: Long, pattern: String = "HH:mm:ss"): String

expect fun getRelativeTimeString(timestamp: Long): String

expect fun getCurrentDateTime(): String

// Additional utility functions that can be implemented in common
fun getElapsedTimeSeconds(startTime: Long): Long {
    return (getCurrentTimeMillis() - startTime) / 1000
}

fun getElapsedTimeMinutes(startTime: Long): Long {
    return getElapsedTimeSeconds(startTime) / 60
}

fun isToday(timestamp: Long): Boolean {
    val now = getCurrentTimeMillis()
    val dayInMillis = 24 * 60 * 60 * 1000
    return (now - timestamp) < dayInMillis
}

fun isYesterday(timestamp: Long): Boolean {
    val now = getCurrentTimeMillis()
    val dayInMillis = 24 * 60 * 60 * 1000
    return (now - timestamp) in dayInMillis until (2 * dayInMillis)
}

// KMP-compatible time formatting functions

// Option 1: Using string templates and padStart
fun formatTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        else -> "${minutes}:${seconds.toString().padStart(2, '0')}"
    }
}

// Option 2: Using buildString for more complex formatting
fun formatDuration(totalSeconds: Int): String = buildString {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    if (hours > 0) {
        append(hours)
        append(":")
        append(minutes.toString().padStart(2, '0'))
        append(":")
        append(seconds.toString().padStart(2, '0'))
    } else {
        append(minutes)
        append(":")
        append(seconds.toString().padStart(2, '0'))
    }
}

// Option 3: Extension function for cleaner usage
fun Int.toTimeString(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    return when {
        hours > 0 -> "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        else -> "${minutes}:${seconds.toString().padStart(2, '0')}"
    }
}

// Option 4: More comprehensive formatting with days
fun formatElapsedTime(totalSeconds: Long): String {
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        days > 0 -> "${days}d ${hours}h ${minutes}m"
        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

// Option 5: For milliseconds to time format
fun Long.millisToTimeString(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val millis = (this % 1000) / 10 // Get two decimal places

    return buildString {
        if (hours > 0) {
            append(hours)
            append(":")
        }
        append(minutes.toString().padStart(if (hours > 0) 2 else 1, '0'))
        append(":")
        append(seconds.toString().padStart(2, '0'))
        append(".")
        append(millis.toString().padStart(2, '0'))
    }
}

// Number formatting utilities (multiplatform-compatible)

/**
 * Format a number with thousand separators
 * Example: 1234567 -> "1,234,567"
 */
fun formatNumberWithCommas(number: Double): String {
    val intPart = number.toInt()
    val parts = mutableListOf<String>()
    var remaining = intPart

    while (remaining >= 1000) {
        val group = remaining % 1000
        parts.add(0, group.toString().padStart(3, '0'))
        remaining /= 1000
    }

    if (remaining > 0 || parts.isEmpty()) {
        parts.add(0, remaining.toString())
    }

    return parts.joinToString(",")
}

/**
 * Format a percentage with one decimal place
 * Example: 45.678 -> "45.7"
 */
fun formatPercentage(value: Double, decimals: Int = 1): String {
    val multiplier = when (decimals) {
        0 -> 1.0
        1 -> 10.0
        2 -> 100.0
        else -> 10.0
    }
    val rounded = (value * multiplier).toInt() / multiplier
    return if (decimals == 0) {
        rounded.toInt().toString()
    } else {
        val intPart = rounded.toInt()
        val decimalPart = ((rounded - intPart) * multiplier).toInt()
        "$intPart.${decimalPart.toString().padStart(decimals, '0')}"
    }
}

/**
 * Format currency without commas (for smaller amounts)
 * Example: 1234.567 -> "1234"
 */
fun formatCurrency(value: Double): String {
    return value.toInt().toString()
}

/**
 * Format currency with thousand separators
 * Example: 1234567.89 -> "1,234,568"
 */
fun formatCurrencyWithCommas(value: Double): String {
    return formatNumberWithCommas(value)
}

/**
 * Format view count (integer)
 * Example: 1234567 -> "1,234,567"
 */
fun formatViewCount(count: Long): String {
    return formatNumberWithCommas(count.toDouble())
}

/**
 * Generate a simple unique ID using timestamp and random value
 * This is a multiplatform-compatible alternative to java.util.UUID
 */
fun generateUniqueId(): String {
    val timestamp = getCurrentTimeMillis()
    val random = kotlin.random.Random.nextInt(0, 999999)
    return "${timestamp}-${random.toString().padStart(6, '0')}"
}

// Usage examples:
// val time1 = formatTime(3661) // "1:01:01"
// val time2 = formatTime(125)  // "2:05"
// val time3 = 3661.toTimeString() // "1:01:01"
// val time4 = formatElapsedTime(90061) // "1d 1h 1m"
// val time5 = 125500L.millisToTimeString() // "2:05.50"
// val num1 = formatNumberWithCommas(1234567.0) // "1,234,567"
// val pct1 = formatPercentage(45.678) // "45.7"
// val cur1 = formatCurrencyWithCommas(1234567.89) // "1,234,568"
// val id1 = generateUniqueId() // "1705234567890-123456"
