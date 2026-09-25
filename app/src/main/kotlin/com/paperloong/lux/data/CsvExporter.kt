package com.paperloong.lux.data

import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.ext.fcToLux
import com.paperloong.lux.model.DetectRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 记录导出为 CSV（UTF-8 带 BOM，数值统一换算为 lux）。
 *
 * @author WangZhiYao
 * @since 2026/9/19
 */
object CsvExporter {

    private val isoTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    fun export(records: List<DetectRecord>): String {
        val sb = StringBuilder()
        sb.append('﻿')
        sb.append("time,value,unit,location,remark\n")
        for (record in records) {
            val luxValue =
                if (record.unit == IlluminanceUnit.FC) record.value.fcToLux() else record.value
            sb.append(escape(isoTime.format(Date(record.createTime)))).append(',')
                .append(escape(luxValue.toString())).append(',')
                .append(escape(record.unit.name)).append(',')
                .append(escape(record.location)).append(',')
                .append(escape(record.remark)).append('\n')
        }
        return sb.toString()
    }

    internal fun escape(field: String): String =
        if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
}
