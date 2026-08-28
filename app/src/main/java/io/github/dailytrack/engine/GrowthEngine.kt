package io.github.dailytrack.engine

data class GrowthScoreResult(
    val totalScore: Double,
    val learningComponent: Double,
    val productiveComponent: Double,
    val exerciseComponent: Double,
    val sleepComponent: Double,
    val socialComponent: Double,
    val noveltyComponent: Double,
    val consistencyComponent: Double
)

data class GrowthWeights(
    val learning: Double = 0.25,
    val productive: Double = 0.25,
    val exercise: Double = 0.20,
    val sleep: Double = 0.15,
    val social: Double = 0.05,
    val novelty: Double = 0.05,
    val consistency: Double = 0.05
)

class GrowthEngine(private val weights: GrowthWeights = GrowthWeights()) {

    fun calculateGrowthScore(
        learningMinutes: Double,
        productiveMinutes: Double,
        exerciseMinutes: Double,
        sleepHours: Double,
        socialMinutes: Double,
        noveltyScore: Double,
        consecutiveDaysActive: Int
    ): GrowthScoreResult {
        val learningScore = normalize(learningMinutes, 0.0, 120.0)
        val productiveScore = normalize(productiveMinutes, 0.0, 480.0)
        val exerciseScore = normalize(exerciseMinutes, 0.0, 60.0)
        val sleepScore = normalize(sleepHours, 0.0, 8.0).coerceAtMost(1.0)
        val socialScore = normalize(socialMinutes, 0.0, 60.0)
        val noveltyVal = noveltyScore.coerceIn(0.0, 1.0)
        val consistencyVal = normalize(consecutiveDaysActive.toDouble(), 0.0, 7.0).coerceAtMost(1.0)

        val learningComponent = weights.learning * learningScore * 100.0
        val productiveComponent = weights.productive * productiveScore * 100.0
        val exerciseComponent = weights.exercise * exerciseScore * 100.0
        val sleepComponent = weights.sleep * sleepScore * 100.0
        val socialComponent = weights.social * socialScore * 100.0
        val noveltyComponent = weights.novelty * noveltyVal * 100.0
        val consistencyComponent = weights.consistency * consistencyVal * 100.0

        val totalScore = learningComponent + productiveComponent + exerciseComponent +
                sleepComponent + socialComponent + noveltyComponent + consistencyComponent

        return GrowthScoreResult(
            totalScore = totalScore.coerceIn(0.0, 100.0),
            learningComponent = learningComponent,
            productiveComponent = productiveComponent,
            exerciseComponent = exerciseComponent,
            sleepComponent = sleepComponent,
            socialComponent = socialComponent,
            noveltyComponent = noveltyComponent,
            consistencyComponent = consistencyComponent
        )
    }

    private fun normalize(value: Double, min: Double, max: Double): Double {
        return ((value - min) / (max - min)).coerceIn(0.0, 1.0)
    }
}
