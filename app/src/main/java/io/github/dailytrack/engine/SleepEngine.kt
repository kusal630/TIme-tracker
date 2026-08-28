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

data class SleepDebtResult(
    val targetHours: Double,
    val actualHours: Double,
    val debtHours: Double,
    val isAcuteLowSleep: Boolean,
    val isChronicDebt: Boolean
)

data class RecoveryResult(
    val needsRest: Boolean,
    val highExertion: Boolean,
    val lowSleep: Boolean,
    val highFatigue: Boolean,
    val warningMessage: String?
)

class SleepEngine {
    fun calculateSleepDebt(
        lastNightHours: Double,
        recentNightHours: List<Double>,
        targetHours: Double = 8.0
    ): SleepDebtResult {
        val isAcuteLowSleep = lastNightHours < targetHours - 2.0
        val weekDeficit = recentNightHours.map { targetHours - it }.filter { it > 0 }.sum()
        val isChronicDebt = weekDeficit > targetHours

        return SleepDebtResult(
            targetHours = targetHours,
            actualHours = lastNightHours,
            debtHours = weekDeficit,
            isAcuteLowSleep = isAcuteLowSleep,
            isChronicDebt = isChronicDebt
        )
    }

    fun assessRecovery(
        exerciseMinutesToday: Double,
        sleepHours: Double,
        fatigueLevel: Int,
        painLevel: Int,
        stressLevel: Int
    ): RecoveryResult {
        val highExertion = exerciseMinutesToday > 90.0
        val lowSleep = sleepHours < 6.0
        val highFatigue = fatigueLevel >= 4

        val warningMessage = when {
            highExertion && lowSleep && highFatigue ->
                "You have low sleep and high exertion. Your recovery may be reduced. Consider lighter training, hydration, food, and rest. If you feel pain or symptoms, stop and seek professional advice."
            highExertion && lowSleep ->
                "Your recent training load and recovery indicators suggest you may need more rest."
            lowSleep && fatigueLevel >= 3 ->
                "Low sleep may reduce recovery and increase fatigue. Consider prioritizing rest."
            painLevel >= 4 && highExertion ->
                "You reported pain with high training load. Consider reducing intensity and consulting a healthcare professional if this persists."
            else -> null
        }

        return RecoveryResult(
            needsRest = lowSleep && highExertion,
            highExertion = highExertion,
            lowSleep = lowSleep,
            highFatigue = highFatigue,
            warningMessage = warningMessage
        )
    }
}
