/*
 * Copyright 2024 Soul Track Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.github.dailytrack.engine

import io.github.dailytrack.data.db.entity.SessionEntity
import kotlin.math.sqrt

class RoutineLoopEngine {
    private val similarityThreshold = 0.75
    private val minDaysForLoop = 3
    private val comfortZoneSimilarity = 0.65
    private val comfortZoneNovelty = 20.0

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
        if (vec1.isEmpty() && vec2.isEmpty()) return 1.0
        if (vec1.isEmpty() || vec2.isEmpty()) return 0.0

        val keys = vec1.keys + vec2.keys
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

    fun calculateCategoryDiversity(categoryDurations: Map<Long, Long>): Double {
        if (categoryDurations.isEmpty()) return 0.0
        val totalDuration = categoryDurations.values.sum().toDouble()
        if (totalDuration == 0.0) return 0.0

        val proportions = categoryDurations.values.map { it.toDouble() / totalDuration }
        val entropy = -proportions.sumOf { p ->
            if (p > 0) p * kotlin.math.ln(p) / kotlin.math.ln(categoryDurations.size.toDouble()) else 0.0
        }
        return entropy * 100.0
    }

    fun calculateNoveltyScore(
        currentCategories: Set<Long>,
        recentCategories: List<Set<Long>>,
        currentDuration: Long
    ): Double {
        if (currentDuration == 0L) return 0.0

        var novelty = 0.0

        val activityLevel = when {
            currentDuration > 28800000L -> 30.0
            currentDuration > 14400000L -> 40.0
            currentDuration > 7200000L -> 50.0
            currentDuration > 3600000L -> 60.0
            currentDuration > 1800000L -> 70.0
            else -> 80.0
        }
        novelty += activityLevel

        if (recentCategories.isNotEmpty()) {
            val allRecentCategories = recentCategories.flatten().toSet()
            val newCategories = currentCategories - allRecentCategories
            val categoryNovelty = when {
                newCategories.size >= 3 -> 30.0
                newCategories.size == 2 -> 20.0
                newCategories.size == 1 -> 10.0
                else -> 0.0
            }
            novelty += categoryNovelty

            val recentSizeHistory = recentCategories.map { it.size }
            val avgRecentSize = if (recentSizeHistory.isNotEmpty()) recentSizeHistory.average() else 0.0
            val sizeVariety = when {
                currentCategories.size > avgRecentSize + 2 -> 20.0
                currentCategories.size > avgRecentSize + 1 -> 15.0
                currentCategories.size > avgRecentSize -> 10.0
                else -> 0.0
            }
            novelty += sizeVariety
        } else {
            novelty += 30.0
        }

        return novelty.coerceIn(0.0, 100.0)
    }

    fun detectLoop(
        dailyVectors: List<Map<String, Double>>,
        growthScores: List<Double>,
        noveltyScores: List<Double>,
        daysThreshold: Int = minDaysForLoop
    ): LoopDetectionResult {
        if (dailyVectors.size < 2) {
            return LoopDetectionResult(isLoopDetected = false, similarity = 0.0, consecutiveDays = 0)
        }

        val activeDays = dailyVectors.filter { it.isNotEmpty() }
        if (activeDays.size < 2) {
            return LoopDetectionResult(isLoopDetected = false, similarity = 0.0, consecutiveDays = 0)
        }

        var consecutiveSimilarDays = 0
        var maxConsecutiveSimilar = 0
        var currentConsecutive = 0
        var maxSimilarity = 0.0
        var totalSimilarity = 0.0
        var comparisonCount = 0

        for (i in 1 until dailyVectors.size) {
            val vec1 = dailyVectors[i - 1]
            val vec2 = dailyVectors[i]

            if (vec1.isEmpty() || vec2.isEmpty()) {
                currentConsecutive = 0
                continue
            }

            val similarity = cosineSimilarity(vec1, vec2)
            maxSimilarity = maxOf(maxSimilarity, similarity)
            totalSimilarity += similarity
            comparisonCount++

            if (similarity >= similarityThreshold) {
                currentConsecutive++
                maxConsecutiveSimilar = maxOf(maxConsecutiveSimilar, currentConsecutive)
            } else {
                currentConsecutive = 0
            }
        }

        consecutiveSimilarDays = maxConsecutiveSimilar
        val avgSimilarity = if (comparisonCount > 0) totalSimilarity / comparisonCount else 0.0

        val recentGrowthAvg = if (growthScores.isNotEmpty())
            growthScores.takeLast(daysThreshold).average() else 0.0
        val recentNoveltyAvg = if (noveltyScores.isNotEmpty())
            noveltyScores.takeLast(daysThreshold).average() else 0.0

        val growthDeclining = if (growthScores.size >= daysThreshold) {
            val recent = growthScores.takeLast(daysThreshold).average()
            val older = growthScores.dropLast(daysThreshold).takeLast(daysThreshold).average()
            recent < older * 0.9
        } else false

        val isLoopDetected = consecutiveSimilarDays >= daysThreshold &&
                avgSimilarity >= similarityThreshold

        val comfortZoneWarning = (avgSimilarity >= comfortZoneSimilarity && recentNoveltyAvg < comfortZoneNovelty) ||
                (consecutiveSimilarDays >= 2 && avgSimilarity >= 0.80 && growthDeclining)

        return LoopDetectionResult(
            isLoopDetected = isLoopDetected,
            similarity = avgSimilarity,
            consecutiveDays = consecutiveSimilarDays,
            comfortZoneWarning = comfortZoneWarning
        )
    }
}

data class LoopDetectionResult(
    val isLoopDetected: Boolean,
    val similarity: Double,
    val consecutiveDays: Int,
    val comfortZoneWarning: Boolean = false
)
