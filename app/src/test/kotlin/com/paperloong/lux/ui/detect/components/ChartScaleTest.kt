package com.paperloong.lux.ui.detect.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ChartScaleTest {

    @Test
    fun `spike 640 rounds up to 800 with three ticks`() {
        val scale = niceChartScale(640f)
        assertEquals(800f, scale.top, 0.01f)
        assertEquals(listOf(200f, 400f, 600f), scale.ticks)
    }

    @Test
    fun `exact multiple keeps tight top`() {
        val scale = niceChartScale(100f)
        assertEquals(100f, scale.top, 0.01f)
        assertEquals(listOf(20f, 40f, 60f, 80f), scale.ticks)
    }

    @Test
    fun `non power of ten steps`() {
        // 6.3 → 步长 2、量程 8
        val scale = niceChartScale(6.3f)
        assertEquals(8f, scale.top, 0.01f)
        assertEquals(listOf(2f, 4f, 6f), scale.ticks)
    }

    @Test
    fun `quarter step mantissa`() {
        // 11 → 步长 2.5、量程 12.5（7 会先命中步长 2，故取 >10 使 2 被拒）
        val scale = niceChartScale(11f)
        assertEquals(12.5f, scale.top, 0.001f)
        assertEquals(listOf(2.5f, 5f, 7.5f, 10f), scale.ticks)
    }

    @Test
    fun `all zero input is protected`() {
        val scale = niceChartScale(0f)
        assertEquals(1f, scale.top, 0.0001f)
        assertEquals(4, scale.ticks.size)
    }

    @Test
    fun `nan input falls back to protected scale`() {
        val scale = niceChartScale(Float.NaN)
        assertEquals(1f, scale.top, 0.0001f)
        assertEquals(4, scale.ticks.size)
    }

    @Test
    fun `tick count stays between 2 and 4`() {
        // 覆盖多个量级的原始值
        listOf(1f, 5f, 17f, 99f, 250f, 999f, 6000f, 123456f).forEach { raw ->
            val scale = niceChartScale(raw)
            assert(scale.ticks.size in 2..4) { "raw=$raw ticks=${scale.ticks}" }
            assert(scale.ticks.all { it < scale.top }) { "raw=$raw top=${scale.top}" }
            assert(scale.top >= raw) { "raw=$raw top=${scale.top}" }
        }
    }
}
