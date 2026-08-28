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
    private val insightRepo = InsightRepository(db.insightDao())
    private val routineEngine = RoutineLoopEngine()

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

    private val _categories = MutableStateFlow<Map<Long, CategoryEntity>>(emptyMap())
    val categories: StateFlow<Map<Long, CategoryEntity>> = _categories

    private val _growthScore = MutableStateFlow(0.0)
    val growthScore: StateFlow<Double> = _growthScore

    private val _activeInsights = MutableStateFlow<List<InsightEntity>>(emptyList())
    val activeInsights: StateFlow<List<InsightEntity>> = _activeInsights

    private val _loopStatus = MutableStateFlow<LoopDetectionResult?>(null)
    val loopStatus: StateFlow<LoopDetectionResult?> = _loopStatus

    init {
        viewModelScope.launch {
            categoryRepo.initializeDefaults()
        }

        viewModelScope.launch {
            categoryRepo.getAllCategories().collect { cats ->
                _categories.value = cats.associateBy { it.id }
            }
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
                _todayCoverage.value = timeEngine.calculateCoverage(sessions, _categories.value, date, zone)
            }
        }

        viewModelScope.launch {
            calculateGrowthScore()
            detectLoop()
        }

        viewModelScope.launch {
            generateInsights()
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun startSession(title: String, categoryId: Long?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            sessionRepo.stopActiveSession(now)

            val cats = _categories.value
            val category = categoryId?.let { cats[it] }
            val sessionType = when (category?.type) {
                "SLEEP" -> "SLEEP"
                "EXERCISE" -> "EXERCISE"
                "LEARNING" -> "LEARNING"
                "SOCIAL" -> "SOCIAL"
                "RECOVERY" -> "REST"
                else -> "ACTIVITY"
            }

            val session = SessionEntity(
                title = title.ifBlank { "Active Session" },
                categoryId = if (categoryId == 0L) null else categoryId,
                type = sessionType,
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
        val cats = _categories.value

        var learningMinutes = 0.0
        var productiveMinutes = 0.0
        var exerciseMinutes = 0.0
        var socialMinutes = 0.0
        var sleepHours = 0.0

        for (session in sessions) {
            val duration = ((session.endTime ?: dayEnd) - session.startTime) / 60000.0
            val cat = session.categoryId?.let { cats[it] }
            val catType = cat?.type ?: session.type

            when (catType) {
                "LEARNING" -> learningMinutes += duration
                "PRODUCTIVE" -> productiveMinutes += duration
                "EXERCISE" -> exerciseMinutes += duration
                "SOCIAL" -> socialMinutes += duration
                "SLEEP" -> sleepHours += duration / 60.0
                "ACTIVITY" -> productiveMinutes += duration
            }
        }

        val hasReflection = sessions.isNotEmpty()
        val noveltyScore = if (sessions.size > 2) 0.7 else 0.3
        val goalProgress = (productiveMinutes / 60.0).coerceAtMost(1.0)

        val result = growthEngine.calculateGrowthScore(
            learningMinutes = learningMinutes,
            productiveMinutes = productiveMinutes,
            exerciseMinutes = exerciseMinutes,
            sleepHours = sleepHours,
            nutritionQualityScore = 0.5,
            hydrationScore = 0.5,
            socialMinutes = socialMinutes,
            hasReflection = hasReflection,
            noveltyScore = noveltyScore,
            goalProgress = goalProgress
        )

        _growthScore.value = result.totalScore
    }

    private suspend fun detectLoop() {
        val zone = ZoneId.systemDefault()
        val today = _selectedDate.value
        val cats = _categories.value

        val dailyVectors = mutableListOf<Map<String, Double>>()
        val growthScores = mutableListOf<Double>()

        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val (dayStart, dayEnd) = getDayRange(date, zone)
            val sessions = sessionRepo.getSessionsForDaySync(dayStart, dayEnd)

            val categoryDurations = mutableMapOf<Long, Long>()
            for (session in sessions) {
                val catId = session.categoryId ?: continue
                val duration = (session.endTime ?: dayEnd) - session.startTime
                categoryDurations[catId] = (categoryDurations[catId] ?: 0L) + duration
            }

            val vector = routineEngine.calculateDailyRoutineVector(sessions, categoryDurations)
            dailyVectors.add(vector)
            growthScores.add(30.0)
        }

        val noveltyScores = List(7) { 30.0 }
        _loopStatus.value = routineEngine.detectLoop(dailyVectors, growthScores, noveltyScores)
    }

    private suspend fun generateInsights() {
        val zone = ZoneId.systemDefault()
        val today = _selectedDate.value
        val (dayStart, dayEnd) = getDayRange(today, zone)

        val sessions = sessionRepo.getSessionsForDaySync(dayStart, dayEnd)
        val cats = _categories.value

        val hasLearning = sessions.any { session ->
            val catType = session.categoryId?.let { cats[it]?.type }
            catType == "LEARNING" || session.type == "LEARNING"
        }

        val hasExercise = sessions.any { session ->
            val catType = session.categoryId?.let { cats[it]?.type }
            catType == "EXERCISE" || session.type == "EXERCISE"
        }

        val totalMinutes = sessions.sumOf { s ->
            ((s.endTime ?: dayEnd) - s.startTime) / 60000.0
        }

        val loop = _loopStatus.value
        val growthScore = _growthScore.value

        val templates = insightEngine.generateInsights(
            hasLearningToday = hasLearning,
            growthScoreTrend = listOf(growthScore),
            wastedTimeRatio = 0.0,
            productiveRatio = if (totalMinutes > 0) (totalMinutes / 1440.0) else 0.0,
            sleepDebtHours = 0.0,
            lowHydration = false,
            lowProteinDays = 0,
            lowFiberDays = 0,
            routineSimilarityDays = loop?.consecutiveDays ?: 0,
            noNewActivitiesDays = if (loop?.isLoopDetected == true) 5 else 0,
            noMovementToday = !hasExercise,
            noReflectionToday = false,
            exerciseMinutes = sessions.filter { it.type == "EXERCISE" }.sumOf {
                ((it.endTime ?: dayEnd) - it.startTime) / 60000.0
            },
            sleepHours = sessions.filter { it.type == "SLEEP" }.sumOf {
                ((it.endTime ?: dayEnd) - it.startTime) / 3600000.0
            },
            fatigueLevel = 2,
            severeSymptoms = emptyList(),
            maintenanceMode = false
        )

        val now = System.currentTimeMillis()
        for (template in templates) {
            val insight = insightEngine.createInsightEntity(template)
            insightRepo.insertIfNotCoolingDown(insight)
        }
    }

    private fun getDayRange(date: LocalDate, zone: ZoneId, dayStartHour: Int = 0): Pair<Long, Long> {
        val start = date.atStartOfDay(zone).plusHours(dayStartHour.toLong()).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).plusHours(dayStartHour.toLong()).toInstant().toEpochMilli()
        return start to end
    }
}
