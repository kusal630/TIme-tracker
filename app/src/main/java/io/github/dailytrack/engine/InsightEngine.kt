package io.github.dailytrack.engine

import io.github.dailytrack.data.db.entity.InsightEntity

class InsightEngine {
    data class InsightTemplate(
        val title: String,
        val message: String,
        val severity: String,
        val category: String,
        val actionLabel: String,
        val cooldownMs: Long = 24 * 60 * 60 * 1000L
    )

    fun generateInsights(
        hasLearningToday: Boolean,
        growthScoreTrend: List<Double>,
        wastedTimeRatio: Double,
        productiveRatio: Double,
        sleepDebtHours: Double,
        lowHydration: Boolean,
        lowProteinDays: Int,
        lowFiberDays: Int,
        routineSimilarityDays: Int,
        noNewActivitiesDays: Int,
        noMovementToday: Boolean,
        noReflectionToday: Boolean,
        exerciseMinutes: Double,
        sleepHours: Double,
        fatigueLevel: Int,
        severeSymptoms: List<String>,
        maintenanceMode: Boolean
    ): List<InsightTemplate> {
        val insights = mutableListOf<InsightTemplate>()

        if (!hasLearningToday) {
            insights.add(
                InsightTemplate(
                    title = "No learning logged today",
                    message = "You have not logged any learning today. Even 10 minutes of reading or practice can support growth.",
                    severity = "CAUTION",
                    category = "GROWTH",
                    actionLabel = "Log a learning session"
                )
            )
        }

        if (growthScoreTrend.size >= 3 && growthScoreTrend.takeLast(3).all { it < 40.0 }) {
            insights.add(
                InsightTemplate(
                    title = "Low growth streak",
                    message = "Your growth score has been low for several days. Consider one small meaningful action today.",
                    severity = "WARNING",
                    category = "GROWTH",
                    actionLabel = "Plan a growth activity"
                )
            )
        }

        if (wastedTimeRatio > 0.3) {
            insights.add(
                InsightTemplate(
                    title = "Wasted time is high",
                    message = "Wasted time is high today. You still have time to recover the day with one focused block.",
                    severity = "CAUTION",
                    category = "TIME",
                    actionLabel = "Start a focus session"
                )
            )
        }

        if (productiveRatio < 0.2 && growthScoreTrend.size >= 3) {
            insights.add(
                InsightTemplate(
                    title = "Low productivity",
                    message = "Productivity has been below your target. Try scheduling one short high-focus session.",
                    severity = "CAUTION",
                    category = "PRODUCTIVITY",
                    actionLabel = "Schedule focus time"
                )
            )
        }

        if (sleepDebtHours > 8.0) {
            insights.add(
                InsightTemplate(
                    title = "Sleep debt accumulated",
                    message = "You have accumulated ${sleepDebtHours.toInt()} hours of sleep debt over the last 7 days. Consider prioritizing rest.",
                    severity = "WARNING",
                    category = "SLEEP",
                    actionLabel = "Adjust sleep schedule"
                )
            )
        }

        if (lowHydration) {
            insights.add(
                InsightTemplate(
                    title = "Hydration is low",
                    message = "If you have no fluid restriction, consider drinking water. Staying hydrated supports energy and focus.",
                    severity = "CAUTION",
                    category = "HYDRATION",
                    actionLabel = "Log water intake"
                )
            )
        }

        if (lowProteinDays >= 3) {
            insights.add(
                InsightTemplate(
                    title = "Protein intake has been low",
                    message = "Consider adding legumes, dairy, eggs, tofu, fish, or meat alternatives to your meals.",
                    severity = "CAUTION",
                    category = "NUTRITION",
                    actionLabel = "Review nutrition"
                )
            )
        }

        if (lowFiberDays >= 3) {
            insights.add(
                InsightTemplate(
                    title = "Fiber intake appears low",
                    message = "Vegetables, fruits, whole grains, legumes, nuts, and seeds may help increase fiber intake.",
                    severity = "CAUTION",
                    category = "NUTRITION",
                    actionLabel = "Review nutrition"
                )
            )
        }

        if (routineSimilarityDays >= 3 && !maintenanceMode) {
            insights.add(
                InsightTemplate(
                    title = "Routine loop detected",
                    message = "Your routine has looked very similar for several days. If this is intentional, that is okay. If you feel stuck, try one new activity.",
                    severity = "CAUTION",
                    category = "ROUTINE",
                    actionLabel = "Try something new"
                )
            )
        }

        if (noNewActivitiesDays >= 7 && !maintenanceMode) {
            insights.add(
                InsightTemplate(
                    title = "Stagnation pattern",
                    message = "You may be in a stagnation pattern. Consider trying one new habit or task.",
                    severity = "CAUTION",
                    category = "GROWTH",
                    actionLabel = "Explore new activities"
                )
            )
        }

        if (noMovementToday) {
            insights.add(
                InsightTemplate(
                    title = "No movement logged today",
                    message = "You have been inactive for a long time. A short walk or stretch may help.",
                    severity = "INFO",
                    category = "EXERCISE",
                    actionLabel = "Log movement"
                )
            )
        }

        if (exerciseMinutes > 90 && sleepHours < 6) {
            insights.add(
                InsightTemplate(
                    title = "Recovery warning",
                    message = "You have low sleep and high exertion. Consider lighter training and rest. If you feel pain, stop and seek professional advice.",
                    severity = "WARNING",
                    category = "SLEEP",
                    actionLabel = "Prioritize rest"
                )
            )
        }

        for (symptom in severeSymptoms) {
            insights.add(
                InsightTemplate(
                    title = "Severe symptom logged",
                    message = "You logged severe $symptom. If this is an emergency, please seek immediate medical attention.",
                    severity = "CRITICAL",
                    category = "SAFETY",
                    actionLabel = "Seek help if needed",
                    cooldownMs = 4 * 60 * 60 * 1000L
                )
            )
        }

        return insights
    }

    fun createInsightEntity(template: InsightTemplate): InsightEntity {
        val now = System.currentTimeMillis()
        return InsightEntity(
            timestamp = now,
            severity = template.severity,
            category = template.category,
            title = template.title,
            message = template.message,
            actionLabel = template.actionLabel,
            cooldownUntil = now + template.cooldownMs
        )
    }
}
