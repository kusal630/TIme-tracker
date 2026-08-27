package io.github.dailytrack.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NutritionEngineTest {
    private lateinit var engine: NutritionEngine

    @Before
    fun setup() {
        engine = NutritionEngine()
    }

    @Test
    fun `zero totals gives low quality score`() {
        val totals = NutritionEngine.NutritionTotals(
            calories = 0.0, protein = 0.0, carbs = 0.0, fat = 0.0,
            saturatedFat = 0.0, sugar = 0.0, addedSugar = 0.0, fiber = 0.0,
            sodium = 0.0, iron = 0.0, calcium = 0.0, vitaminD = 0.0,
            vitaminB12 = 0.0, folate = 0.0
        )
        val result = engine.calculateQualityScore(totals)
        assertTrue("Score should be low for zero intake", result.score < 0.5)
    }

    @Test
    fun `good nutrition gives high score`() {
        val totals = NutritionEngine.NutritionTotals(
            calories = 2000.0, protein = 60.0, carbs = 250.0, fat = 70.0,
            saturatedFat = 20.0, sugar = 30.0, addedSugar = 10.0, fiber = 30.0,
            sodium = 1500.0, iron = 18.0, calcium = 1000.0, vitaminD = 20.0,
            vitaminB12 = 2.4, folate = 400.0
        )
        val result = engine.calculateQualityScore(totals)
        assertTrue(result.score > 0.7)
    }

    @Test
    fun `high added sugar reduces score`() {
        val goodTotals = NutritionEngine.NutritionTotals(
            calories = 2000.0, protein = 50.0, carbs = 250.0, fat = 65.0,
            saturatedFat = 20.0, sugar = 30.0, addedSugar = 10.0, fiber = 25.0,
            sodium = 2000.0, iron = 10.0, calcium = 800.0, vitaminD = 15.0,
            vitaminB12 = 2.0, folate = 300.0
        )
        val badTotals = goodTotals.copy(addedSugar = 80.0)
        val goodResult = engine.calculateQualityScore(goodTotals)
        val badResult = engine.calculateQualityScore(badTotals)
        assertTrue(goodResult.score > badResult.score)
    }

    @Test
    fun `hydration score calculation`() {
        assertEquals(1.0, engine.calculateHydrationScore(2000.0, 2000.0, 2000.0), 0.01)
        assertEquals(0.5, engine.calculateHydrationScore(1000.0, 1000.0, 2000.0), 0.01)
        assertEquals(0.0, engine.calculateHydrationScore(0.0, 0.0, 2000.0), 0.01)
    }

    @Test
    fun `low intake risk detection`() {
        val totals = NutritionEngine.NutritionTotals(
            calories = 500.0, protein = 10.0, carbs = 50.0, fat = 15.0,
            saturatedFat = 5.0, sugar = 10.0, addedSugar = 5.0, fiber = 5.0,
            sodium = 500.0, iron = 2.0, calcium = 200.0, vitaminD = 2.0,
            vitaminB12 = 0.3, folate = 50.0
        )
        val risks = engine.calculateLowIntakeRisk(totals)
        assertTrue("PROTEIN" in risks)
        assertTrue("FIBER" in risks)
        assertTrue("IRON" in risks)
        assertTrue("CALCIUM" in risks)
    }

    @Test
    fun `no risk with adequate intake`() {
        val totals = NutritionEngine.NutritionTotals(
            calories = 2000.0, protein = 60.0, carbs = 250.0, fat = 70.0,
            saturatedFat = 20.0, sugar = 30.0, addedSugar = 10.0, fiber = 30.0,
            sodium = 1500.0, iron = 18.0, calcium = 1000.0, vitaminD = 20.0,
            vitaminB12 = 2.4, folate = 400.0
        )
        val risks = engine.calculateLowIntakeRisk(totals)
        assertTrue(risks.isEmpty())
    }
}
