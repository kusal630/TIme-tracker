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

data class BodySystemCard(
    val name: String,
    val status: String,
    val contributingFactors: List<String>,
    val trendArrow: String,
    val explanation: String,
    val suggestedAction: String,
    val disclaimer: String = "This is not medical advice. Consult a healthcare professional for concerns."
)

class BodySystemEngine {
    fun generateCards(
        sleepDebtHours: Double,
        lowRecovery: Boolean,
        lowCalorieIntake: Boolean,
        highSugarIntake: Boolean,
        lowMovement: Boolean,
        highRestingHR: Boolean,
        goodExercise: Boolean,
        goodSleep: Boolean,
        painAfterExercise: Boolean,
        noMovementDays: Int,
        lowFiber: Boolean,
        lowWater: Boolean,
        highSodium: Boolean,
        hasConstipation: Boolean,
        lowIron: Boolean,
        lowCalcium: Boolean,
        lowVitD: Boolean,
        lowB12: Boolean,
        lowFolate: Boolean,
        persistentLowMood: Boolean,
        highStress: Boolean,
        lowSleepHighWasted: Boolean,
        goodLearning: Boolean
    ): List<BodySystemCard> {
        val cards = mutableListOf<BodySystemCard>()

        val sleepFactors = mutableListOf<String>()
        var sleepStatus = "Good"
        if (sleepDebtHours > 8) { sleepStatus = "Attention"; sleepFactors.add("Sleep debt: ${sleepDebtHours.toInt()}h") }
        if (lowRecovery) { sleepStatus = "Caution"; sleepFactors.add("Low recovery after exercise") }
        if (sleepFactors.isEmpty()) sleepFactors.addAll(listOf("Adequate sleep", "Good recovery patterns"))
        cards.add(BodySystemCard(
            name = "Sleep & Recovery",
            status = sleepStatus,
            contributingFactors = sleepFactors,
            trendArrow = if (sleepDebtHours > 8) "↓" else "→",
            explanation = "Sleep debt may reduce focus, mood, and physical recovery.",
            suggestedAction = "Aim for consistent sleep and rest when needed."
        ))

        val energyFactors = mutableListOf<String>()
        var energyStatus = "Unknown"
        if (lowCalorieIntake) { energyStatus = "Attention"; energyFactors.add("Low energy intake logged") }
        if (highSugarIntake) { energyStatus = "Caution"; energyFactors.add("High sugar intake") }
        if (energyFactors.isEmpty()) energyFactors.add("Insufficient data")
        cards.add(BodySystemCard(
            name = "Energy & Metabolism",
            status = energyStatus,
            contributingFactors = energyFactors,
            trendArrow = "→",
            explanation = "Low energy intake and high sugar may affect energy levels throughout the day.",
            suggestedAction = "Consider balanced meals with protein, complex carbs, and healthy fats."
        ))

        val cardioFactors = mutableListOf<String>()
        var cardioStatus = "Unknown"
        if (lowMovement && highRestingHR) { cardioStatus = "Caution"; cardioFactors.add("Low movement with elevated resting heart rate") }
        if (goodExercise) { cardioStatus = "Good"; cardioFactors.add("Regular exercise logged") }
        if (cardioFactors.isEmpty()) cardioFactors.add("Insufficient data")
        cards.add(BodySystemCard(
            name = "Cardiovascular & Fitness",
            status = cardioStatus,
            contributingFactors = cardioFactors,
            trendArrow = if (goodExercise) "↑" else "→",
            explanation = "Regular movement supports cardiovascular health.",
            suggestedAction = "Aim for regular moderate movement as tolerated."
        ))

        val musculoFactors = mutableListOf<String>()
        var musculoStatus = "Unknown"
        if (painAfterExercise) { musculoStatus = "Attention"; musculoFactors.add("Reported pain after exercise") }
        if (noMovementDays > 3) { musculoStatus = "Caution"; musculoFactors.add("No movement for $noMovementDays days") }
        if (musculoFactors.isEmpty()) musculoFactors.add("Insufficient data")
        cards.add(BodySystemCard(
            name = "Musculoskeletal & Movement",
            status = musculoStatus,
            contributingFactors = musculoFactors,
            trendArrow = if (noMovementDays > 3) "↓" else "→",
            explanation = "Regular movement supports musculoskeletal health.",
            suggestedAction = "Gentle stretching or walking may help."
        ))

        val digestFactors = mutableListOf<String>()
        var digestStatus = "Unknown"
        if (lowFiber && lowWater && hasConstipation) { digestStatus = "Attention"; digestFactors.add("Low fiber and water with constipation") }
        if (highSodium && lowWater) { digestStatus = "Caution"; digestFactors.add("High sodium with low water") }
        if (digestFactors.isEmpty()) digestFactors.add("Insufficient data")
        cards.add(BodySystemCard(
            name = "Digestive & Hydration",
            status = digestStatus,
            contributingFactors = digestFactors,
            trendArrow = "→",
            explanation = "Adequate fiber and water support digestive health.",
            suggestedAction = "Consider vegetables, fruits, whole grains, and adequate fluids."
        ))

        val nutrientFactors = mutableListOf<String>()
        var nutrientStatus = "Unknown"
        if (lowIron) nutrientFactors.add("Low iron intake")
        if (lowCalcium) nutrientFactors.add("Low calcium intake")
        if (lowVitD) nutrientFactors.add("Low vitamin D intake")
        if (lowB12) nutrientFactors.add("Low vitamin B12 intake")
        if (lowFolate) nutrientFactors.add("Low folate intake")
        if (nutrientFactors.isNotEmpty()) nutrientStatus = "Caution"
        cards.add(BodySystemCard(
            name = "Nutrition & Micronutrients",
            status = nutrientStatus,
            contributingFactors = nutrientFactors.ifEmpty { listOf("Insufficient data") },
            trendArrow = "→",
            explanation = "Long-term low intake of key micronutrients may affect overall health.",
            suggestedAction = "Review your diet or consult a healthcare professional."
        ))

        val moodFactors = mutableListOf<String>()
        var moodStatus = "Unknown"
        if (persistentLowMood) { moodStatus = "Attention"; moodFactors.add("Persistent low mood reported") }
        if (highStress) { moodStatus = "Caution"; moodFactors.add("High stress reported") }
        if (moodFactors.isEmpty()) moodFactors.add("Insufficient data")
        cards.add(BodySystemCard(
            name = "Mood & Stress",
            status = moodStatus,
            contributingFactors = moodFactors,
            trendArrow = "→",
            explanation = "Persistent low mood or high stress may benefit from attention and support.",
            suggestedAction = "Consider reflection, social connection, or professional support if needed."
        ))

        val cogFactors = mutableListOf<String>()
        var cogStatus = "Unknown"
        if (lowSleepHighWasted) { cogStatus = "Caution"; cogFactors.add("Low sleep with high wasted time") }
        if (goodLearning) { cogStatus = "Good"; cogFactors.add("Good learning activity") }
        if (cogFactors.isEmpty()) cogFactors.add("Insufficient data")
        cards.add(BodySystemCard(
            name = "Cognitive Focus",
            status = cogStatus,
            contributingFactors = cogFactors,
            trendArrow = if (goodLearning) "↑" else "→",
            explanation = "Sleep, focus, and learning activity influence cognitive performance.",
            suggestedAction = "Prioritize sleep and focused learning blocks."
        ))

        return cards
    }
}
