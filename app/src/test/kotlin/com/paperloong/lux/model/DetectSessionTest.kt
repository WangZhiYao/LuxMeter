package com.paperloong.lux.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectSessionTest {

    @Test
    fun `empty session has no stats`() {
        val session = DetectSession()
        assertEquals(0, session.count)
        assertNull(session.min)
        assertNull(session.avg)
        assertNull(session.max)
        assertTrue(session.trend.isEmpty())
    }

    @Test
    fun `single reading`() {
        val session = DetectSession()
        session.add(100f)
        assertEquals(1, session.count)
        assertEquals(100f, session.min!!)
        assertEquals(100f, session.avg!!, 0.01f)
        assertEquals(100f, session.max!!)
        assertEquals(listOf(100f), session.trend)
    }

    @Test
    fun `min avg max over all readings`() {
        val session = DetectSession()
        listOf(100f, 300f, 200f).forEach { session.add(it) }
        assertEquals(3, session.count)
        assertEquals(100f, session.min!!)
        assertEquals(200f, session.avg!!, 0.01f)
        assertEquals(300f, session.max!!)
    }

    @Test
    fun `trend is capped but stats are not`() {
        val session = DetectSession(chartCapacity = 3)
        listOf(1f, 2f, 3f, 4f, 5f).forEach { session.add(it) }
        assertEquals(5, session.count)
        assertEquals(1f, session.min!!)
        assertEquals(3f, session.avg!!, 0.01f)
        assertEquals(5f, session.max!!)
        assertEquals(listOf(3f, 4f, 5f), session.trend)
    }

    @Test
    fun `reset clears everything`() {
        val session = DetectSession()
        session.add(100f)
        session.reset()
        assertEquals(0, session.count)
        assertNull(session.min)
        assertTrue(session.trend.isEmpty())
    }
}
