package io.github.dailytrack.engine

import io.github.dailytrack.data.db.entity.SessionEntity

data class GrowthScoreResult(
    val totalScore: Double,
    val learningComponent: Double,
    val productiveComponent: Double,
    val exerciseComponent: Double,
    val sleepComponent: Double,
    val nutritionComponent: Double,
    val hydrationComponent: Double,
    val socialComponent: Double,
    val reflectionComponent: Double,
    val noveltyComponent: Double,
    val goalComponent: Double
)

data class GrowthWeights(
    val learning: Double = 0.20,
    val productive: Double = 0.20,
    val exercise: Double = 0.15,
    val sleep: Double = 0.15,
    val nutrition: Double = 0.10,
    val hydration: Double = 0.05,
    val social: Double = 0.05,
    val reflection: Double = 0.05,
    val novelty: Double = 0.05,
    val goal: Double = 0.05
)

class GrowthEngine(private val weights: GrowthWeights = GrowthWeights()) {

    fun calculateGrowthScore(
        learningMinutes: Double,
        productiveMinutes: Double,
        exerciseMinutes: Double,
        sleepHours: Double,
        nutritionQualityScore: Double,
        hydrationScore: Double,
        socialMinutes: Double,
        hasReflection: Boolean,
        noveltyScore: Double,
        goalProgress: Double
    ): GrowthScoreResult {
        val learningComponent = weights.learning * (learningMinutes.coerceIn(0.0, 180.0) / 180.0) * 100.0
        val productiveComponent = weights.productive * (productiveMinutes.coerceIn(0.0, 600.0) / 600.0) * 100.0
        val exerciseComponent = weights.exercise * (exerciseMinutes.coerceIn(0.0, 120.0) / 120.0) * 100.0
        val sleepComponent = weights.sleep * (sleepHours.coerceIn(0.0, 10.0) / 8.0).coerceAtMost(1.0) * 100.0
        val nutritionComponent = weights.nutrition * nutritionQualityScore.coerceIn(0.0, 1.0) * 100.0
        val hydrationComponent = weights.hydration * hydrationScore.coerceIn(0.0, 1.0) * 100.0
        val socialComponent = weights.social * (socialMinutes.coerceIn(0.0, 120.0) / 120.0) * 100.0
        val reflectionComponent = weights.reflection * (if (hasReflection) 1.0 else 0.0) * 100.0
        val noveltyComponent = weights.novelty * noveltyScore.coerceIn(0.0, 1.0) * 100.0
        val goalComponent = weights.goal * goalProgress.coerceIn(0.0, 1.0) * 100.0

        val totalScore = learningComponent + productiveComponent + exerciseComponent +
                sleepComponent + nutritionComponent + hydrationComponent +
                socialComponent + reflectionComponent + noveltyComponent + goalComponent

        return GrowthScoreResult(
            totalScore = totalScore.coerceIn(0.0, 100.0),
            learningComponent = learningComponent,
            productiveComponent = productiveComponent,
            exerciseComponent = exerciseComponent,
            sleepComponent = sleepComponent,
            nutritionComponent = nutritionComponent,
            hydrationComponent = hydrationComponent,
            socialComponent = socialComponent,
            reflectionComponent = reflectionComponent,
            noveltyComponent = noveltyComponent,
            goalComponent = goalComponent
        )
    }
}
