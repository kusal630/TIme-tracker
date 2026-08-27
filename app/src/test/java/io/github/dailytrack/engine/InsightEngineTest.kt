package io.github.dailytrack.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InsightEngineTest {
    private lateinit var engine: InsightEngine

    @Before
    fun setup() {
        engine = InsightEngine()
    }

    @Test
    fun `no learning generates insight`() {
        val insights = engine.generateInsights(
            hasLearningToday = false,
            growthScoreTrend = listOf(50.0),
            wastedTimeRatio = 0.1,
            productiveRatio = 0.3,
            sleepDebtHours = 0.0,
            lowHydration = false,
            lowProteinDays = 0,
            lowFiberDays = 0,
            routineSimilarityDays = 0,
            noNewActivitiesDays = 0,
            noMovementToday = false,
            noReflectionToday = false,
            exerciseMinutes = 30.0,
            sleepHours = 8.0,
            fatigueLevel = 2,
            severeSymptoms = emptyList(),
            maintenanceMode = false
        )
        assertTrue(insights.any { it.title.contains("No learning") })
    }

    @Test
    fun `low growth streak generates warning`() {
        val insights = engine.generateInsights(
            hasLearningToday = true,
            growthScoreTrend = listOf(30.0, 25.0, 20.0),
            wastedTimeRatio = 0.1,
            productiveRatio = 0.3,
            sleepDebtHours = 0.0,
            lowHydration = false,
            lowProteinDays = 0,
            lowFiberDays = 0,
            routineSimilarityDays = 0,
            noNewActivitiesDays = 0,
            noMovementToday = false,
            noReflectionToday = false,
            exerciseMinutes = 30.0,
            sleepHours = 8.0,
            fatigueLevel = 2,
            severeSymptoms = emptyList(),
            maintenanceMode = false
        )
        assertTrue(insights.any { it.title.contains("growth streak") })
    }

    @Test
    fun `high wasted time generates insight`() {
        val insights = engine.generateInsights(
            hasLearningToday = true,
            growthScoreTrend = listOf(60.0),
            wastedTimeRatio = 0.4,
            productiveRatio = 0.3,
            sleepDebtHours = 0.0,
            lowHydration = false,
            lowProteinDays = 0,
            lowFiberDays = 0,
            routineSimilarityDays = 0,
            noNewActivitiesDays = 0,
            noMovementToday = false,
            noReflectionToday = false,
            exerciseMinutes = 30.0,
            sleepHours = 8.0,
            fatigueLevel = 2,
            severeSymptoms = emptyList(),
            maintenanceMode = false
        )
        assertTrue(insights.any { it.title.contains("Wasted time") })
    }

    @Test
    fun `severe symptom generates critical insight`() {
        val insights = engine.generateInsights(
            hasLearningToday = true,
            growthScoreTrend = listOf(60.0),
            wastedTimeRatio = 0.1,
            productiveRatio = 0.3,
            sleepDebtHours = 0.0,
            lowHydration = false,
            lowProteinDays = 0,
            lowFiberDays = 0,
            routineSimilarityDays = 0,
            noNewActivitiesDays = 0,
            noMovementToday = false,
            noReflectionToday = false,
            exerciseMinutes = 30.0,
            sleepHours = 8.0,
            fatigueLevel = 2,
            severeSymptoms = listOf("chest pain"),
            maintenanceMode = false
        )
        assertTrue(insights.any { it.severity == "CRITICAL" && it.title.contains("Severe symptom") })
    }

    @Test
    fun `maintenance mode suppresses loop warning`() {
        val insights = engine.generateInsights(
            hasLearningToday = false,
            growthScoreTrend = listOf(30.0),
            wastedTimeRatio = 0.1,
            productiveRatio = 0.3,
            sleepDebtHours = 0.0,
            lowHydration = false,
            lowProteinDays = 0,
            lowFiberDays = 0,
            routineSimilarityDays = 5,
            noNewActivitiesDays = 10,
            noMovementToday = false,
            noReflectionToday = false,
            exerciseMinutes = 30.0,
            sleepHours = 8.0,
            fatigueLevel = 2,
            severeSymptoms = emptyList(),
            maintenanceMode = true
        )
        assertFalse(insights.any { it.title.contains("Routine loop") })
    }

    @Test
    fun `createInsightEntity has correct cooldown`() {
        val template = InsightEngine.InsightTemplate(
            title = "Test",
            message = "Test message",
            severity = "INFO",
            category = "TIME",
            actionLabel = "Action",
            cooldownMs = 3600_000L
        )
        val entity = engine.createInsightEntity(template)
        assertEquals("Test", entity.title)
        assertEquals("INFO", entity.severity)
        assertTrue(entity.cooldownUntil > System.currentTimeMillis())
    }
}
