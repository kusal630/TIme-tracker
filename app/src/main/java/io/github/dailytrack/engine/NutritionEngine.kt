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

class NutritionEngine {
    data class NutritionTotals(
        val calories: Double,
        val protein: Double,
        val carbs: Double,
        val fat: Double,
        val saturatedFat: Double,
        val sugar: Double,
        val addedSugar: Double,
        val fiber: Double,
        val sodium: Double,
        val iron: Double,
        val calcium: Double,
        val vitaminD: Double,
        val vitaminB12: Double,
        val folate: Double
    )

    data class NutritionQualityResult(
        val score: Double,
        val proteinScore: Double,
        val fiberScore: Double,
        val addedSugarScore: Double,
        val sodiumScore: Double,
        val micronutrientScore: Double
    )

    fun calculateQualityScore(totals: NutritionTotals): NutritionQualityResult {
        val proteinScore = (totals.protein / 50.0).coerceIn(0.0, 1.0)
        val fiberScore = (totals.fiber / 25.0).coerceIn(0.0, 1.0)
        val addedSugarScore = 1.0 - (totals.addedSugar / 50.0).coerceIn(0.0, 1.0)
        val sodiumScore = 1.0 - (totals.sodium / 2300.0).coerceIn(0.0, 1.0)

        val ironScore = (totals.iron / 18.0).coerceIn(0.0, 1.0)
        val calciumScore = (totals.calcium / 1000.0).coerceIn(0.0, 1.0)
        val vitD = (totals.vitaminD / 20.0).coerceIn(0.0, 1.0)
        val vitB12 = (totals.vitaminB12 / 2.4).coerceIn(0.0, 1.0)
        val fol = (totals.folate / 400.0).coerceIn(0.0, 1.0)
        val micronutrientScore = (ironScore + calciumScore + vitD + vitB12 + fol) / 5.0

        val score = (proteinScore * 0.25 + fiberScore * 0.20 + addedSugarScore * 0.20 +
                sodiumScore * 0.15 + micronutrientScore * 0.20)

        return NutritionQualityResult(
            score = score.coerceIn(0.0, 1.0),
            proteinScore = proteinScore,
            fiberScore = fiberScore,
            addedSugarScore = addedSugarScore,
            sodiumScore = sodiumScore,
            micronutrientScore = micronutrientScore
        )
    }

    fun calculateHydrationScore(waterMl: Double, totalVolumeMl: Double, targetMl: Double): Double {
        val effectiveMl = maxOf(waterMl, totalVolumeMl * 0.7)
        return (effectiveMl / targetMl).coerceIn(0.0, 1.0)
    }

    fun calculateLowIntakeRisk(totals: NutritionTotals): List<String> {
        val risks = mutableListOf<String>()
        if (totals.protein < 30.0) risks.add("PROTEIN")
        if (totals.fiber < 15.0) risks.add("FIBER")
        if (totals.iron < 8.0) risks.add("IRON")
        if (totals.calcium < 500.0) risks.add("CALCIUM")
        if (totals.vitaminD < 10.0) risks.add("VITAMIN_D")
        if (totals.vitaminB12 < 1.2) risks.add("VITAMIN_B12")
        if (totals.folate < 200.0) risks.add("FOLATE")
        return risks
    }
}
