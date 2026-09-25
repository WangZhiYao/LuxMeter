package com.paperloong.lux.ext

import org.junit.Assert.assertEquals
import org.junit.Test

class FloatExtTest {

    @Test
    fun `lux to fc and back round trips`() {
        val lux = 8420f
        assertEquals(lux, lux.luxToFc().fcToLux(), 0.01f)
    }

    @Test
    fun `lux to fc known value`() {
        assertEquals(782.24f, 8420f.luxToFc(), 0.01f)
    }
}
