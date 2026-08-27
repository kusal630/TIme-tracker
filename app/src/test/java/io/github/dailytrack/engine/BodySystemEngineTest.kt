package io.github.dailytrack.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BodySystemEngineTest {
    private lateinit var engine: BodySystemEngine

    @Before
    fun setup() {
        engine = BodySystemEngine()
    }

    @Test
    fun `generates all 8 body system cards`() {
        val cards = engine.generateCards(
            sleepDebtHours = 0.0,
            lowRecovery = false,
            lowCalorieIntake = false,
            highSugarIntake = false,
            lowMovement = false,
            highRestingHR = false,
            goodExercise = false,
            goodSleep = true,
            painAfterExercise = false,
            noMovementDays = 0,
            lowFiber = false,
            lowWater = false,
            highSodium = false,
            hasConstipation = false,
            lowIron = false,
            lowCalcium = false,
            lowVitD = false,
            lowB12 = false,
            lowFolate = false,
            persistentLowMood = false,
            highStress = false,
            lowSleepHighWasted = false,
            goodLearning = false
        )
        assertEquals(8, cards.size)
    }

    @Test
    fun `sleep debt shows attention status`() {
        val cards = engine.generateCards(
            sleepDebtHours = 12.0,
            lowRecovery = false,
            lowCalorieIntake = false,
            highSugarIntake = false,
            lowMovement = false,
            highRestingHR = false,
            goodExercise = false,
            goodSleep = false,
            painAfterExercise = false,
            noMovementDays = 0,
            lowFiber = false,
            lowWater = false,
            highSodium = false,
            hasConstipation = false,
            lowIron = false,
            lowCalcium = false,
            lowVitD = false,
            lowB12 = false,
            lowFolate = false,
            persistentLowMood = false,
            highStress = false,
            lowSleepHighWasted = false,
            goodLearning = false
        )
        val sleepCard = cards.find { it.name == "Sleep & Recovery" }
        assertNotNull(sleepCard)
        assertEquals("Attention", sleepCard!!.status)
    }

    @Test
    fun `no disclaimer tampering`() {
        val cards = engine.generateCards(
            sleepDebtHours = 0.0,
            lowRecovery = false,
            lowCalorieIntake = false,
            highSugarIntake = false,
            lowMovement = false,
            highRestingHR = false,
            goodExercise = false,
            goodSleep = true,
            painAfterExercise = false,
            noMovementDays = 0,
            lowFiber = false,
            lowWater = false,
            highSodium = false,
            hasConstipation = false,
            lowIron = false,
            lowCalcium = false,
            lowVitD = false,
            lowB12 = false,
            lowFolate = false,
            persistentLowMood = false,
            highStress = false,
            lowSleepHighWasted = false,
            goodLearning = false
        )
        cards.forEach { card ->
            assertTrue("All cards should have disclaimer", card.disclaimer.contains("not medical advice"))
        }
    }
}
