package io.github.dailytrack.data.model

import java.time.LocalDate
import java.time.ZoneId

data class DayBoundary(
    val dayStartHour: Int = 0,
    val timezoneId: String = ZoneId.systemDefault().id
) {
    fun getDayRange(date: LocalDate, zone: ZoneId = ZoneId.of(timezoneId)): Pair<Long, Long> {
        val start = date.atStartOfDay(zone)
            .plusHours(dayStartHour.toLong())
            .toInstant()
            .toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone)
            .plusHours(dayStartHour.toLong())
            .toInstant()
            .toEpochMilli()
        return start to end
    }

    fun getDateForTimestamp(timestamp: Long, zone: ZoneId = ZoneId.of(timezoneId)): LocalDate {
        val adjusted = timestamp - (dayStartHour * 3600_000L)
        return java.time.Instant.ofEpochMilli(adjusted).atZone(zone).toLocalDate()
    }
}

enum class ProductivityType {
    PRODUCTIVE, NEUTRAL, WASTED, SLEEP, LEARNING, EXERCISE, SOCIAL, RECOVERY, FOOD_DRINK, CUSTOM
}

enum class SessionType {
    ACTIVITY, SLEEP, EXERCISE, LEARNING, SOCIAL, FOOD_PREPARATION, REST, CUSTOM
}

enum class GrowthContribution {
    HIGH, MEDIUM, LOW, NONE
}

enum class ComfortRisk {
    HIGH, MEDIUM, LOW
}

enum class InsightSeverity {
    INFO, CAUTION, WARNING, CRITICAL
}

enum class InsightCategory {
    TIME, PRODUCTIVITY, GROWTH, ROUTINE, SLEEP, EXERCISE, NUTRITION, HYDRATION, BODY, MOOD, SAFETY
}

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK, CUSTOM
}

enum class DrinkType {
    WATER, TEA, COFFEE, MILK, JUICE, SODA, ENERGY_DRINK, ALCOHOL, SOUP, OTHER
}

enum class SeverityLevel {
    MILD, MODERATE, SEVERE
}

enum class BodySystemStatus {
    GOOD, CAUTION, ATTENTION, UNKNOWN
}

enum class ActivityLevel {
    SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE
}
