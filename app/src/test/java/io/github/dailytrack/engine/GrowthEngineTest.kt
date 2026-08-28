package io.github.dailytrack.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GrowthEngineTest {
    private lateinit var engine: GrowthEngine

    @Before
    fun setup() {
        engine = GrowthEngine()
    }

    @Test
    fun `zero inputs gives zero score`() {
        val result = engine.calculateGrowthScore(
            learningMinutes = 0.0,
            productiveMinutes = 0.0,
            exerciseMinutes = 0.0,
            sleepHours = 0.0,
            socialMinutes = 0.0,
            noveltyScore = 0.0,
            consecutiveDaysActive = 0
        )
        assertEquals(0.0, result.totalScore, 0.01)
    }

    @Test
    fun `maximum inputs gives high score`() {
        val result = engine.calculateGrowthScore(
            learningMinutes = 120.0,
            productiveMinutes = 480.0,
            exerciseMinutes = 60.0,
            sleepHours = 8.0,
            socialMinutes = 60.0,
            noveltyScore = 1.0,
            consecutiveDaysActive = 7
        )
        assertTrue(result.totalScore > 90.0)
    }

    @Test
    fun `learning component respects cap`() {
        val result1 = engine.calculateGrowthScore(
            learningMinutes = 120.0,
            productiveMinutes = 0.0,
            exerciseMinutes = 0.0,
            sleepHours = 0.0,
            socialMinutes = 0.0,
            noveltyScore = 0.0,
            consecutiveDaysActive = 0
        )
        val result2 = engine.calculateGrowthScore(
            learningMinutes = 300.0,
            productiveMinutes = 0.0,
            exerciseMinutes = 0.0,
            sleepHours = 0.0,
            socialMinutes = 0.0,
            noveltyScore = 0.0,
            consecutiveDaysActive = 0
        )
        assertEquals(result1.learningComponent, result2.learningComponent, 0.01)
    }

    @Test
    fun `score is clamped to 0-100`() {
        val result = engine.calculateGrowthScore(
            learningMinutes = 1000.0,
            productiveMinutes = 1000.0,
            exerciseMinutes = 1000.0,
            sleepHours = 20.0,
            socialMinutes = 1000.0,
            noveltyScore = 5.0,
            consecutiveDaysActive = 30
        )
        assertTrue(result.totalScore <= 100.0)
        assertTrue(result.totalScore >= 0.0)
    }
}
