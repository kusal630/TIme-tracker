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
        val hasAnyActivity = learningMinutes > 0 || productiveMinutes > 0 ||
                exerciseMinutes > 0 || sleepHours > 0 || socialMinutes > 0

        if (!hasAnyActivity && consecutiveDaysActive == 0) {
            return GrowthScoreResult(
                totalScore = 0.0,
                learningComponent = 0.0,
                productiveComponent = 0.0,
                exerciseComponent = 0.0,
                sleepComponent = 0.0,
                socialComponent = 0.0,
                noveltyComponent = 0.0,
                consistencyComponent = 0.0
            )
        }

        val learningScore = cappedNormalized(learningMinutes, TARGET_LEARNING_MINUTES)
        val productiveScore = cappedNormalized(productiveMinutes, TARGET_PRODUCTIVE_MINUTES)
        val exerciseScore = cappedNormalized(exerciseMinutes, TARGET_EXERCISE_MINUTES)
        val socialScore = cappedNormalized(socialMinutes, TARGET_SOCIAL_MINUTES)

        val sleepScore = when {
            sleepHours <= 0.0 -> 0.0
            sleepHours in 6.0..9.0 -> 1.0
            sleepHours in 5.0..<6.0 -> 0.6
            sleepHours in 9.0..<10.0 -> 0.8
            sleepHours < 5.0 -> 0.3
            else -> 0.5
        }

        val noveltyVal = if (hasAnyActivity) noveltyScore.coerceIn(0.0, 1.0) else 0.0
        val consistencyVal = (consecutiveDaysActive.toDouble() / 7.0).coerceAtMost(1.0)

        val learningComponent = weights.learning * learningScore * 100.0
        val productiveComponent = weights.productive * productiveScore * 100.0
        val exerciseComponent = weights.exercise * exerciseScore * 100.0
        val sleepComponent = weights.sleep * sleepScore * 100.0
        val socialComponent = weights.social * socialScore * 100.0
        val noveltyComponent = weights.novelty * noveltyVal * 100.0
        val consistencyComponent = weights.consistency * consistencyVal * 100.0

        val baseScore = learningComponent + productiveComponent + exerciseComponent +
                sleepComponent + socialComponent + noveltyComponent + consistencyComponent

        val balanceBonus = calculateBalanceBonus(learningScore, productiveScore, exerciseScore)
        val intensityBonus = calculateIntensityBonus(productiveMinutes, learningMinutes)

        val totalScore = (baseScore + balanceBonus + intensityBonus).coerceIn(0.0, 100.0)

        return GrowthScoreResult(
            totalScore = totalScore,
            learningComponent = learningComponent,
            productiveComponent = productiveComponent,
            exerciseComponent = exerciseComponent,
            sleepComponent = sleepComponent,
            socialComponent = socialComponent,
            noveltyComponent = noveltyComponent,
            consistencyComponent = consistencyComponent
        )
    }

    private fun calculateBalanceBonus(learning: Double, productive: Double, exercise: Double): Double {
        val minScore = minOf(learning, productive, exercise)
        return when {
            minScore >= 0.7 -> 3.0
            minScore >= 0.5 -> 2.0
            minScore >= 0.3 -> 1.0
            else -> 0.0
        }
    }

    private fun calculateIntensityBonus(productiveMinutes: Double, learningMinutes: Double): Double {
        val totalFocus = productiveMinutes + learningMinutes
        return when {
            totalFocus >= 360.0 -> 2.0
            totalFocus >= 240.0 -> 1.5
            totalFocus >= 180.0 -> 1.0
            else -> 0.0
        }
    }

    private fun cappedNormalized(value: Double, target: Double): Double {
        if (value <= 0.0) return 0.0
        return (value / target).coerceAtMost(1.0)
    }

    companion object {
        const val TARGET_LEARNING_MINUTES = 120.0
        const val TARGET_PRODUCTIVE_MINUTES = 240.0
        const val TARGET_EXERCISE_MINUTES = 60.0
        const val TARGET_SOCIAL_MINUTES = 30.0
    }
}
