package com.paperloong.lux.data

import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.model.DetectRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvExporterTest {

    @Test
    fun `starts with bom and header`() {
        val csv = CsvExporter.export(emptyList())
        assertEquals('﻿', csv[0])
        assertEquals("time,value,unit,location,remark\n", csv.substring(1))
    }

    @Test
    fun `exports rows in order`() {
        val csv = CsvExporter.export(
            listOf(
                DetectRecord(
                    value = 8420f,
                    unit = IlluminanceUnit.LUX,
                    location = "窗台",
                    remark = "ok",
                    createTime = 0L
                ),
                DetectRecord(
                    value = 782.24f,
                    unit = IlluminanceUnit.FC,
                    remark = "",
                    createTime = 86_400_000L
                )
            )
        )
        val lines = csv.substring(1).trim().lines()
        assertEquals(3, lines.size)
        val row1 = lines[1].split(',')
        assertEquals("8420.0", row1[1])
        assertEquals("LUX", row1[2])
        assertEquals("窗台", row1[3])
        assertEquals("ok", row1[4])
        val row2 = lines[2].split(',')
        assertEquals("FC 值应换算回 lux", 8420.0, row2[1].toDouble(), 0.5)
    }

    @Test
    fun `escapes comma quote and newline`() {
        val csv = CsvExporter.export(
            listOf(
                DetectRecord(value = 1f, unit = IlluminanceUnit.LUX, remark = "a,b\"c\nd")
            )
        )
        val lines = csv.substring(1).trim().lines()
        assertEquals("\"a,b\"\"c", lines[1].split(',', limit = 5)[4])
        assertEquals("d\"", lines[2])
    }
}
