package io.github.dailytrack.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SleepEngineTest {
    private lateinit var engine: SleepEngine

    @Before
    fun setup() {
        engine = SleepEngine()
    }

    @Test
    fun `adequate sleep has no debt`() {
        val result = engine.calculateSleepDebt(
            lastNightHours = 8.0,
            recentNightHours = listOf(8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0),
            targetHours = 8.0
        )
        assertEquals(0.0, result.debtHours, 0.01)
        assertFalse(result.isAcuteLowSleep)
        assertFalse(result.isChronicDebt)
    }

    @Test
    fun `acute low sleep detected`() {
        val result = engine.calculateSleepDebt(
            lastNightHours = 4.0,
            recentNightHours = listOf(4.0, 7.0, 7.5, 8.0, 7.0, 8.0, 7.5),
            targetHours = 8.0
        )
        assertTrue(result.isAcuteLowSleep)
    }

    @Test
    fun `chronic debt accumulates`() {
        val result = engine.calculateSleepDebt(
            lastNightHours = 6.0,
            recentNightHours = listOf(6.0, 5.0, 6.0, 5.5, 6.0, 5.0, 6.0),
            targetHours = 8.0
        )
        assertTrue(result.isChronicDebt)
        assertTrue(result.debtHours > 0)
    }

    @Test
    fun `recovery warning with high exertion and low sleep`() {
        val result = engine.assessRecovery(
            exerciseMinutesToday = 120.0,
            sleepHours = 5.0,
            fatigueLevel = 4,
            painLevel = 2,
            stressLevel = 3
        )
        assertTrue(result.needsRest)
        assertNotNull(result.warningMessage)
        assertTrue(result.warningMessage!!.contains("recovery"))
    }

    @Test
    fun `no recovery warning with adequate sleep`() {
        val result = engine.assessRecovery(
            exerciseMinutesToday = 60.0,
            sleepHours = 8.0,
            fatigueLevel = 2,
            painLevel = 0,
            stressLevel = 2
        )
        assertFalse(result.needsRest)
        assertNull(result.warningMessage)
    }

    @Test
    fun `pain with high training triggers warning`() {
        val result = engine.assessRecovery(
            exerciseMinutesToday = 120.0,
            sleepHours = 7.0,
            fatigueLevel = 3,
            painLevel = 5,
            stressLevel = 3
        )
        assertNotNull(result.warningMessage)
        assertTrue(result.warningMessage!!.contains("pain"))
    }
}
