// ===== DESKTOP MODULE (JVM) =====
// File: shared/src/desktopMain/kotlin/com/cinefiller/fillerapp/utils/PlatformUtils.desktop.kt
package com.beekeeper.app.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}

actual fun getFormattedTimestamp(timestamp: Long): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return format.format(Date(timestamp))
}

actual fun getTimeMillis(): Long {
    return System.currentTimeMillis()
}

actual fun getFormattedDate(timestamp: Long, pattern: String): String {
    val format = SimpleDateFormat(pattern, Locale.US)
    return format.format(Date(timestamp))
}

actual fun getFormattedTime(timestamp: Long, pattern: String): String {
    val format = SimpleDateFormat(pattern, Locale.US)
    return format.format(Date(timestamp))
}

actual fun getRelativeTimeString(timestamp: Long): String {
    val now = Instant.now()
    val then = Instant.ofEpochMilli(timestamp)
    
    val minutes = ChronoUnit.MINUTES.between(then, now)
    val hours = ChronoUnit.HOURS.between(then, now)
    val days = ChronoUnit.DAYS.between(then, now)
    val weeks = ChronoUnit.WEEKS.between(then, now)
    val months = ChronoUnit.MONTHS.between(then, now)
    val years = ChronoUnit.YEARS.between(then, now)
    
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        weeks < 4 -> "$weeks week${if (weeks > 1) "s" else ""} ago"
        months < 12 -> "$months month${if (months > 1) "s" else ""} ago"
        else -> "$years year${if (years > 1) "s" else ""} ago"
    }
}

actual fun getCurrentDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    return LocalDateTime.now().format(formatter)
}
