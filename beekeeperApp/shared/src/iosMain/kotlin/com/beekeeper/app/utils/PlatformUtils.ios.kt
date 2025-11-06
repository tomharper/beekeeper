// ===== iOS MODULE =====
// File: shared/src/iosMain/kotlin/com/cinefiller/fillerapp/utils/PlatformUtils.ios.kt
package com.beekeeper.app.utils

import platform.Foundation.*

actual fun getCurrentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun getFormattedTimestamp(timestamp: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd HH:mm:ss"
        locale = NSLocale.localeWithLocaleIdentifier("en_US")
    }
    return formatter.stringFromDate(date)
}

actual fun getTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun getFormattedDate(timestamp: Long, pattern: String): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = pattern
        locale = NSLocale.localeWithLocaleIdentifier("en_US")
    }
    return formatter.stringFromDate(date)
}

actual fun getFormattedTime(timestamp: Long, pattern: String): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = pattern
        locale = NSLocale.localeWithLocaleIdentifier("en_US")
    }
    return formatter.stringFromDate(date)
}

actual fun getRelativeTimeString(timestamp: Long): String {
    val now = NSDate()
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val formatter = NSDateComponentsFormatter().apply {
        unitsStyle = NSDateComponentsFormatterUnitsStyleFull
        maximumUnitCount = 1
        allowedUnits = (NSCalendarUnitSecond or NSCalendarUnitMinute or
                       NSCalendarUnitHour or NSCalendarUnitDay or
                       NSCalendarUnitWeekOfMonth or NSCalendarUnitMonth or
                       NSCalendarUnitYear).toULong()
    }

    val interval = now.timeIntervalSinceDate(date)
    return formatter.stringFromTimeInterval(interval) ?: "just now"
}

actual fun getCurrentDateTime(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        locale = NSLocale.localeWithLocaleIdentifier("en_US")
    }
    return formatter.stringFromDate(NSDate())
}
