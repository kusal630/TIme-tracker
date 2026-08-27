package io.github.dailytrack.engine

import io.github.dailytrack.data.db.entity.SessionEntity
import kotlin.math.sqrt

class RoutineLoopEngine {
    private val similarityThreshold = 0.85

    fun calculateDailyRoutineVector(
        sessions: List<SessionEntity>,
        categoryDurations: Map<Long, Long>
    ): Map<String, Double> {
        val totalDuration = categoryDurations.values.sum().toDouble()
        if (totalDuration == 0.0) return emptyMap()
        return categoryDurations.map { (categoryId, duration) ->
            "cat_$categoryId" to duration.toDouble() / totalDuration
        }.toMap()
    }

    fun cosineSimilarity(vec1: Map<String, Double>, vec2: Map<String, Double>): Double {
        val keys = vec1.keys + vec2.keys
        if (keys.isEmpty()) return 1.0

        var dotProduct = 0.0
        var norm1 = 0.0
        var norm2 = 0.0

        for (key in keys) {
            val v1 = vec1[key] ?: 0.0
            val v2 = vec2[key] ?: 0.0
            dotProduct += v1 * v2
            norm1 += v1 * v1
            norm2 += v2 * v2
        }

        val denominator = sqrt(norm1) * sqrt(norm2)
        return if (denominator == 0.0) 0.0 else dotProduct / denominator
    }

    fun detectLoop(
        dailyVectors: List<Map<String, Double>>,
        growthScores: List<Double>,
        noveltyScores: List<Double>,
        daysThreshold: Int = 3
    ): LoopDetectionResult {
        if (dailyVectors.size < daysThreshold) {
            return LoopDetectionResult(isLoopDetected = false, similarity = 0.0, consecutiveDays = 0)
        }

        var consecutiveSimilarDays = 0
        var maxSimilarity = 0.0

        for (i in 1 until dailyVectors.size) {
            val similarity = cosineSimilarity(dailyVectors[i - 1], dailyVectors[i])
            maxSimilarity = maxOf(maxSimilarity, similarity)

            if (similarity >= similarityThreshold) {
                consecutiveSimilarDays++
            } else {
                consecutiveSimilarDays = 0
            }
        }

        val recentGrowthAvg = if (growthScores.isNotEmpty())
            growthScores.takeLast(daysThreshold).average() else 50.0
        val recentNoveltyAvg = if (noveltyScores.isNotEmpty())
            noveltyScores.takeLast(daysThreshold).average() else 50.0

        val isLoopDetected = consecutiveSimilarDays >= daysThreshold &&
                recentGrowthAvg < 40.0 &&
                recentNoveltyAvg < 30.0

        return LoopDetectionResult(
            isLoopDetected = isLoopDetected,
            similarity = maxSimilarity,
            consecutiveDays = consecutiveSimilarDays
        )
    }
}

data class LoopDetectionResult(
    val isLoopDetected: Boolean,
    val similarity: Double,
    val consecutiveDays: Int
)
