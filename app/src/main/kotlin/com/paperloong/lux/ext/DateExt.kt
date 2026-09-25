package com.paperloong.lux.ext

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 *
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */

/** 距今天几天：0=今天，1=昨天，以此类推。 */
fun Long.dayOffsetFromToday(): Int {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val day = Calendar.getInstance().apply {
        timeInMillis = this@dayOffsetFromToday
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diff = today.timeInMillis - day.timeInMillis
    return (diff / MILLIS_PER_DAY).toInt()
}

fun Long.formatToShortTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(this)

fun Long.formatToShortDate(): String =
    SimpleDateFormat("M/d", Locale.getDefault()).format(this)

private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
