package io.github.dailytrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.dailytrack.DailyTrackApp
import io.github.dailytrack.data.db.entity.*
import io.github.dailytrack.data.repository.*
import io.github.dailytrack.engine.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as DailyTrackApp).database
    private val sessionRepo = SessionRepository(db.sessionDao())
    private val categoryRepo = CategoryRepository(db.categoryDao())
    private val foodRepo = FoodEntryRepository(db.foodEntryDao())
    private val drinkRepo = DrinkEntryRepository(db.drinkEntryDao())
    private val insightRepo = InsightRepository(db.insightDao())
    private val moodRepo = MoodCheckInRepository(db.moodCheckInDao())
    private val symptomRepo = SymptomEntryRepository(db.symptomEntryDao())
    private val bodyMetricRepo = BodyMetricRepository(db.bodyMetricDao())
    private val profileRepo = UserProfileRepository(db.userProfileDao())

    private val timeEngine = TimeCoverageEngine()
    private val growthEngine = GrowthEngine()
    private val insightEngine = InsightEngine()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _activeSession = MutableStateFlow<SessionEntity?>(null)
    val activeSession: StateFlow<SessionEntity?> = _activeSession

    private val _todaySessions = MutableStateFlow<List<SessionEntity>>(emptyList())
    val todaySessions: StateFlow<List<SessionEntity>> = _todaySessions

    private val _todayCoverage = MutableStateFlow<TimeCoverage?>(null)
    val todayCoverage: StateFlow<TimeCoverage?> = _todayCoverage

    private val _todayWater = MutableStateFlow(0.0)
    val todayWater: StateFlow<Double> = _todayWater

    private val _todayCalories = MutableStateFlow(0.0)
    val todayCalories: StateFlow<Double> = _todayCalories

    private val _todayProtein = MutableStateFlow(0.0)
    val todayProtein: StateFlow<Double> = _todayProtein

    private val _todayFiber = MutableStateFlow(0.0)
    val todayFiber: StateFlow<Double> = _todayFiber

    private val _growthScore = MutableStateFlow(0.0)
    val growthScore: StateFlow<Double> = _growthScore

    private val _insights = MutableStateFlow<List<InsightEntity>>(emptyList())
    val insights: StateFlow<List<InsightEntity>> = _insights

    private val _activeInsights = MutableStateFlow<List<InsightEntity>>(emptyList())
    val activeInsights: StateFlow<List<InsightEntity>> = _activeInsights

    init {
        viewModelScope.launch {
            categoryRepo.initializeDefaults()
        }

        viewModelScope.launch {
            sessionRepo.getActiveSession().collect { session ->
                _activeSession.value = session
            }
        }

        viewModelScope.launch {
            _selectedDate.collect { date -> loadDayData(date) }
        }
    }

    private fun loadDayData(date: LocalDate) {
        val zone = ZoneId.systemDefault()
        val (dayStart, dayEnd) = getDayRange(date, zone)

        viewModelScope.launch {
            sessionRepo.getSessionsForDay(dayStart, dayEnd).collect { sessions ->
                _todaySessions.value = sessions
                _todayCoverage.value = timeEngine.calculateCoverage(sessions, date, zone)
            }
        }

        viewModelScope.launch {
            drinkRepo.getTotalWaterMlForDay(dayStart, dayEnd).collect { water ->
                _todayWater.value = water ?: 0.0
            }
        }

        viewModelScope.launch {
            foodRepo.getTotalCaloriesForDay(dayStart, dayEnd).collect { cal ->
                _todayCalories.value = cal ?: 0.0
            }
        }

        viewModelScope.launch {
            foodRepo.getTotalProteinForDay(dayStart, dayEnd).collect { protein ->
                _todayProtein.value = protein ?: 0.0
            }
        }

        viewModelScope.launch {
            foodRepo.getTotalFiberForDay(dayStart, dayEnd).collect { fiber ->
                _todayFiber.value = fiber ?: 0.0
            }
        }

        viewModelScope.launch {
            calculateGrowthScore()
        }

        viewModelScope.launch {
            insightRepo.getActiveInsights().collect { insights ->
                _activeInsights.value = insights
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun startSession(title: String, categoryId: Long?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            sessionRepo.stopActiveSession(now)

            val session = SessionEntity(
                title = title.ifBlank { "Active Session" },
                categoryId = if (categoryId == 0L) null else categoryId,
                type = "ACTIVITY",
                startTime = now,
                isActive = true,
                source = "TIMER",
                timezoneId = ZoneId.systemDefault().id
            )
            sessionRepo.insert(session)
        }
    }

    fun stopSession() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            sessionRepo.stopActiveSession(now)
        }
    }

    fun logWater(volumeMl: Double) {
        if (volumeMl <= 0) return
        viewModelScope.launch {
            val entry = DrinkEntryEntity(
                timestamp = System.currentTimeMillis(),
                drinkType = "WATER",
                volumeMl = volumeMl
            )
            drinkRepo.insert(entry)
        }
    }

    fun logFood(name: String, mealType: String, calories: Double, protein: Double, fiber: Double) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val entry = FoodEntryEntity(
                timestamp = System.currentTimeMillis(),
                mealType = mealType,
                foodName = name,
                caloriesKcal = calories,
                proteinG = protein,
                fiberG = fiber
            )
            foodRepo.insert(entry)
        }
    }

    fun logMood(mood: Int, energy: Int, stress: Int, note: String) {
        viewModelScope.launch {
            val checkIn = MoodCheckInEntity(
                timestamp = System.currentTimeMillis(),
                mood = mood,
                energy = energy,
                stress = stress,
                note = note
            )
            moodRepo.insert(checkIn)
        }
    }

    fun dismissInsight(id: Long) {
        viewModelScope.launch {
            insightRepo.dismiss(id)
        }
    }

    private suspend fun calculateGrowthScore() {
        val zone = ZoneId.systemDefault()
        val today = _selectedDate.value
        val (dayStart, dayEnd) = getDayRange(today, zone)

        val sessions = sessionRepo.getSessionsForDaySync(dayStart, dayEnd)
        val learningMinutes = sessions.filter { it.type == "LEARNING" }
            .sumOf { ((it.endTime ?: dayEnd) - it.startTime) / 60000.0 }
        val productiveMinutes = sessions.filter { it.type == "ACTIVITY" }
            .sumOf { ((it.endTime ?: dayEnd) - it.startTime) / 60000.0 }
        val exerciseMinutes = sessions.filter { it.type == "EXERCISE" }
            .sumOf { ((it.endTime ?: dayEnd) - it.startTime) / 60000.0 }
        val socialMinutes = sessions.filter { it.type == "SOCIAL" }
            .sumOf { ((it.endTime ?: dayEnd) - it.startTime) / 60000.0 }

        val sleepSession = sessions.find { it.type == "SLEEP" }
        val sleepHours = if (sleepSession != null) {
            ((sleepSession.endTime ?: dayEnd) - sleepSession.startTime) / 3600000.0
        } else 0.0

        val nutritionQuality = if (_todayCalories.value > 0) {
            val proteinScore = (_todayProtein.value / 50.0).coerceIn(0.0, 1.0)
            val fiberScore = (_todayFiber.value / 25.0).coerceIn(0.0, 1.0)
            (proteinScore + fiberScore) / 2.0
        } else 0.0

        val hydrationScore = (_todayWater.value / 2000.0).coerceIn(0.0, 1.0)

        val hasReflection = false
        val noveltyScore = 0.5
        val goalProgress = 0.5

        val result = growthEngine.calculateGrowthScore(
            learningMinutes = learningMinutes,
            productiveMinutes = productiveMinutes,
            exerciseMinutes = exerciseMinutes,
            sleepHours = sleepHours,
            nutritionQualityScore = nutritionQuality,
            hydrationScore = hydrationScore,
            socialMinutes = socialMinutes,
            hasReflection = hasReflection,
            noveltyScore = noveltyScore,
            goalProgress = goalProgress
        )

        _growthScore.value = result.totalScore
    }

    private fun getDayRange(date: LocalDate, zone: ZoneId, dayStartHour: Int = 0): Pair<Long, Long> {
        val start = date.atStartOfDay(zone).plusHours(dayStartHour.toLong()).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).plusHours(dayStartHour.toLong()).toInstant().toEpochMilli()
        return start to end
    }
}
