// ===== ANDROID MODULE =====
// File: shared/src/androidMain/kotlin/com/cinefiller/fillerapp/utils/PlatformUtils.android.kt
package com.beekeeper.app.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
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
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}

actual fun getCurrentDateTime(): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    return format.format(Date())
}

