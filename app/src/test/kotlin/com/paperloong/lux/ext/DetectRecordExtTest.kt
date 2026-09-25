package com.paperloong.lux.ext

import com.paperloong.lux.constant.IlluminanceUnit
import com.paperloong.lux.model.DetectRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class DetectRecordExtTest {

    @Test
    fun `same unit returns raw value`() {
        val record = DetectRecord(value = 8420f, unit = IlluminanceUnit.LUX)
        assertEquals(8420f, record.displayValue(IlluminanceUnit.LUX), 0.01f)
    }

    @Test
    fun `stored lux displayed as fc`() {
        val record = DetectRecord(value = 8420f, unit = IlluminanceUnit.LUX)
        assertEquals(8420f.luxToFc(), record.displayValue(IlluminanceUnit.FC), 0.01f)
    }

    @Test
    fun `stored fc displayed as lux`() {
        val record = DetectRecord(value = 782.24f, unit = IlluminanceUnit.FC)
        assertEquals(8420f, record.displayValue(IlluminanceUnit.LUX), 0.5f)
    }
}
