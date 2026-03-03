package com.paperloong.lux.ext

import java.text.SimpleDateFormat
import java.util.Locale

/**
 *
 *
 * @author WangZhiYao
 * @since 2024/4/28
 */
fun Long.formatToDateString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(this)
}