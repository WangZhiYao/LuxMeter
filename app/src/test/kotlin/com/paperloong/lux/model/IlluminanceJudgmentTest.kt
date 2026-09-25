package com.paperloong.lux.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IlluminanceJudgmentTest {

    private val range = TargetIlluminanceRange(2000f, 5000f)

    @Test
    fun `no target returns null`() {
        assertNull(judgeIlluminance(100f, null))
    }

    @Test
    fun `below min is too low with gap`() {
        assertEquals(IlluminanceJudgment.TooLow(796f), judgeIlluminance(1204f, range))
    }

    @Test
    fun `equal min is in range`() {
        assertEquals(IlluminanceJudgment.InRange, judgeIlluminance(2000f, range))
    }

    @Test
    fun `equal max is in range`() {
        assertEquals(IlluminanceJudgment.InRange, judgeIlluminance(5000f, range))
    }

    @Test
    fun `above max is too high with gap`() {
        assertEquals(IlluminanceJudgment.TooHigh(208f), judgeIlluminance(5208f, range))
    }

    @Test
    fun `range validity`() {
        assertTrue(range.isValid)
        assertFalse(TargetIlluminanceRange(0f, 100f).isValid)
        assertFalse(TargetIlluminanceRange(500f, 500f).isValid)
        assertFalse(TargetIlluminanceRange(5000f, 2000f).isValid)
        assertNull(TargetIlluminanceRange.of(5000f, 2000f))
        assertEquals(range, TargetIlluminanceRange.of(2000f, 5000f))
    }
}
