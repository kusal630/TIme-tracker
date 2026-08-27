package io.github.dailytrack.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TimeCoverageEngineTest {
    private lateinit var engine: TimeCoverageEngine

    @Before
    fun setup() {
        engine = TimeCoverageEngine()
    }

    @Test
    fun `empty sessions returns zero coverage`() {
        val date = java.time.LocalDate.of(2024, 1, 15)
        val zone = java.time.ZoneId.of("UTC")
        val result = engine.calculateCoverage(emptyList(), date, zone)

        assertEquals(0L, result.trackedSeconds)
        assertEquals(result.totalDaySeconds, result.untrackedSeconds)
        assertEquals(0.0, result.trackedRatio, 0.001)
    }

    @Test
    fun `single session calculates duration correctly`() {
        val date = java.time.LocalDate.of(2024, 1, 15)
        val zone = java.time.ZoneId.of("UTC")
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()

        val session = io.github.dailytrack.data.db.entity.SessionEntity(
            id = 1,
            type = "ACTIVITY",
            startTime = dayStart + 3600_000,
            endTime = dayStart + 7200_000,
            isActive = false
        )

        val result = engine.calculateCoverage(listOf(session), date, zone)
        assertEquals(3600L, result.trackedSeconds)
        assertEquals(result.totalDaySeconds - 3600, result.untrackedSeconds)
    }

    @Test
    fun `sleep session counted correctly`() {
        val date = java.time.LocalDate.of(2024, 1, 15)
        val zone = java.time.ZoneId.of("UTC")
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()

        val session = io.github.dailytrack.data.db.entity.SessionEntity(
            id = 1,
            type = "SLEEP",
            startTime = dayStart,
            endTime = dayStart + 28800_000,
            isActive = false
        )

        val result = engine.calculateCoverage(listOf(session), date, zone)
        assertEquals(28800L, result.sleepSeconds)
        assertEquals(28800L, result.trackedSeconds)
    }

    @Test
    fun `exercise session counted correctly`() {
        val date = java.time.LocalDate.of(2024, 1, 15)
        val zone = java.time.ZoneId.of("UTC")
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()

        val session = io.github.dailytrack.data.db.entity.SessionEntity(
            id = 1,
            type = "EXERCISE",
            startTime = dayStart + 3600_000,
            endTime = dayStart + 5400_000,
            isActive = false
        )

        val result = engine.calculateCoverage(listOf(session), date, zone)
        assertEquals(1800L, result.exerciseSeconds)
    }

    @Test
    fun `multiple sessions sum correctly`() {
        val date = java.time.LocalDate.of(2024, 1, 15)
        val zone = java.time.ZoneId.of("UTC")
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()

        val sessions = listOf(
            io.github.dailytrack.data.db.entity.SessionEntity(
                id = 1, type = "SLEEP", startTime = dayStart, endTime = dayStart + 28800_000, isActive = false
            ),
            io.github.dailytrack.data.db.entity.SessionEntity(
                id = 2, type = "LEARNING", startTime = dayStart + 30000_000, endTime = dayStart + 33600_000, isActive = false
            ),
            io.github.dailytrack.data.db.entity.SessionEntity(
                id = 3, type = "EXERCISE", startTime = dayStart + 36000_000, endTime = dayStart + 37800_000, isActive = false
            )
        )

        val result = engine.calculateCoverage(sessions, date, zone)
        assertEquals(28800L, result.sleepSeconds)
        assertEquals(3600L, result.learningSeconds)
        assertEquals(1800L, result.exerciseSeconds)
        assertEquals(34200L, result.trackedSeconds)
    }
}
