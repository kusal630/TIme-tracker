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
