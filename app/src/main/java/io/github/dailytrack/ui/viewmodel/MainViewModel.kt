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


package io.github.dailytrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.dailytrack.SoulTrackApp
import io.github.dailytrack.data.db.entity.*
import io.github.dailytrack.data.repository.*
import io.github.dailytrack.engine.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as SoulTrackApp).database
    private val sessionRepo = SessionRepository(db.sessionDao())
    private val categoryRepo = CategoryRepository(db.categoryDao())
    private val insightRepo = InsightRepository(db.insightDao())
    private val todoRepo = TodoRepository(db.todoDao(), db.subtaskDao())
    private val pomodoroRepo = PomodoroRepository(db.pomodoroSessionDao())
    private val savedQuoteRepo = SavedQuoteRepository(db.savedQuoteDao())
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

    private val _growthResult = MutableStateFlow<GrowthScoreResult?>(null)
    val growthResult: StateFlow<GrowthScoreResult?> = _growthResult

    private val _activeInsights = MutableStateFlow<List<InsightEntity>>(emptyList())
    val activeInsights: StateFlow<List<InsightEntity>> = _activeInsights

    private val _loopStatus = MutableStateFlow<LoopDetectionResult?>(null)
    val loopStatus: StateFlow<LoopDetectionResult?> = _loopStatus

    private val _activeTodos = MutableStateFlow<List<TodoEntity>>(emptyList())
    val activeTodos: StateFlow<List<TodoEntity>> = _activeTodos

    private val _completedTodos = MutableStateFlow<List<TodoEntity>>(emptyList())
    val completedTodos: StateFlow<List<TodoEntity>> = _completedTodos

    private val _activeTodoCount = MutableStateFlow(0)
    val activeTodoCount: StateFlow<Int> = _activeTodoCount

    private val _completedTodoCount = MutableStateFlow(0)
    val completedTodoCount: StateFlow<Int> = _completedTodoCount

    private val _highPriorityCount = MutableStateFlow(0)
    val highPriorityCount: StateFlow<Int> = _highPriorityCount

    private val _overdueCount = MutableStateFlow(0)
    val overdueCount: StateFlow<Int> = _overdueCount

    private val _todayPomodoros = MutableStateFlow<List<PomodoroSessionEntity>>(emptyList())
    val todayPomodoros: StateFlow<List<PomodoroSessionEntity>> = _todayPomodoros

    private val _activePomodoro = MutableStateFlow<PomodoroSessionEntity?>(null)
    val activePomodoro: StateFlow<PomodoroSessionEntity?> = _activePomodoro

    private val _savedQuotes = MutableStateFlow<List<SavedQuoteEntity>>(emptyList())
    val savedQuotes: StateFlow<List<SavedQuoteEntity>> = _savedQuotes

    private val _weeklyStats = MutableStateFlow(WeeklyStats())
    val weeklyStats: StateFlow<WeeklyStats> = _weeklyStats

    private val _dailyProductivity = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val dailyProductivity: StateFlow<List<Pair<String, Float>>> = _dailyProductivity

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
            todoRepo.getActiveTodos().collect { todos ->
                _activeTodos.value = todos
            }
        }

        viewModelScope.launch {
            todoRepo.getCompletedTodos().collect { todos ->
                _completedTodos.value = todos
            }
        }

        viewModelScope.launch {
            todoRepo.getHighPriorityCount().collect { count ->
                _highPriorityCount.value = count
            }
        }

        viewModelScope.launch {
            todoRepo.getOverdueCount(System.currentTimeMillis()).collect { count ->
                _overdueCount.value = count
            }
        }

        viewModelScope.launch {
            _selectedDate.collect { date ->
                loadDayData(date)
                val zone = ZoneId.systemDefault()
                val (dayStart, dayEnd) = getDayRange(date, zone)
                todoRepo.getTodayActiveTodoCount(dayStart, dayEnd).collect { count ->
                    _activeTodoCount.value = count
                }
            }
        }

        viewModelScope.launch {
            _selectedDate.collect { date ->
                val zone = ZoneId.systemDefault()
                val (dayStart, dayEnd) = getDayRange(date, zone)
                todoRepo.getTodayCompletedTodoCount(dayStart, dayEnd).collect { count ->
                    _completedTodoCount.value = count
                }
            }
        }

        viewModelScope.launch {
            savedQuoteRepo.getAllSavedQuotes().collect { quotes ->
                _savedQuotes.value = quotes
            }
        }
    }

    private fun loadDayData(date: LocalDate) {
        val zone = ZoneId.systemDefault()
        val (dayStart, dayEnd) = getDayRange(date, zone)

        viewModelScope.launch {
            sessionRepo.getSessionsForDay(dayStart, dayEnd).collect { sessions ->
                _todaySessions.value = sessions
                _todayCoverage.value = timeEngine.calculateCoverage(sessions, _categories.value, date, zone)
                calculateGrowthScore()
                detectLoop()
                generateInsights()
                calculateDailyProductivity()
            }
        }

        viewModelScope.launch {
            pomodoroRepo.getPomodorosForDay(dayStart, dayEnd).collect { pomodoros ->
                _todayPomodoros.value = pomodoros
            }
        }

        viewModelScope.launch {
            calculateWeeklyStats()
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
                title = title.ifBlank { category?.name ?: "Active Session" },
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

    fun reloadAll() {
        loadDayData(_selectedDate.value)
    }

    fun dismissInsight(id: Long) {
        viewModelScope.launch {
            insightRepo.dismiss(id)
        }
    }

    fun addTodo(title: String, description: String, categoryId: Long?, deadline: Long?, estimatedMinutes: Int, priority: Int = 0) {
        viewModelScope.launch {
            val todo = TodoEntity(
                title = title,
                description = description,
                categoryId = categoryId,
                deadline = deadline,
                estimatedMinutes = estimatedMinutes,
                priority = priority
            )
            todoRepo.insert(todo)
        }
    }

    fun completeTodo(id: Long) {
        viewModelScope.launch {
            todoRepo.completeTodo(id)
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoRepo.delete(todo)
            todoRepo.deleteAllSubtasksForTodo(todo.id)
        }
    }

    fun deleteCompletedTodos() {
        viewModelScope.launch {
            val completed = _completedTodos.value
            completed.forEach { todo ->
                todoRepo.delete(todo)
                todoRepo.deleteAllSubtasksForTodo(todo.id)
            }
        }
    }

    fun updateTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoRepo.update(todo)
        }
    }

    fun startPomodoro(todoId: Long?, categoryId: Long?, durationMinutes: Int) {
        viewModelScope.launch {
            val pomodoro = PomodoroSessionEntity(
                todoId = todoId,
                categoryId = categoryId,
                startTime = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                type = "WORK"
            )
            val id = pomodoroRepo.insert(pomodoro)
            _activePomodoro.value = pomodoroRepo.getPomodoroById(id)
        }
    }

    fun completePomodoro() {
        viewModelScope.launch {
            val pomodoro = _activePomodoro.value ?: return@launch
            pomodoroRepo.completePomodoro(pomodoro.id, System.currentTimeMillis())
            _activePomodoro.value = null

            if (pomodoro.todoId != null) {
                val todo = todoRepo.getTodoById(pomodoro.todoId)
                if (todo != null) {
                    todoRepo.update(todo.copy(
                        actualMinutes = todo.actualMinutes + pomodoro.durationMinutes,
                        pomodoroCount = todo.pomodoroCount + 1,
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }
        }
    }

    fun startPomodoroBreak(durationMinutes: Int) {
        viewModelScope.launch {
            val pomodoro = PomodoroSessionEntity(
                startTime = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                type = "BREAK"
            )
            pomodoroRepo.insert(pomodoro)
        }
    }

    fun saveQuote(text: String, author: String) {
        viewModelScope.launch {
            val existing = savedQuoteRepo.getQuoteByText(text)
            if (existing == null) {
                savedQuoteRepo.insert(SavedQuoteEntity(text = text, author = author))
            }
        }
    }

    fun deleteSavedQuote(quote: SavedQuoteEntity) {
        viewModelScope.launch {
            savedQuoteRepo.delete(quote)
        }
    }

    fun isQuoteSaved(text: String): Boolean {
        return _savedQuotes.value.any { it.text == text }
    }

    fun getSubtasksForTodo(todoId: Long): Flow<List<SubtaskEntity>> {
        return todoRepo.getSubtasksForTodo(todoId)
    }

    fun addSubtask(todoId: Long, title: String) {
        viewModelScope.launch {
            todoRepo.insertSubtask(
                SubtaskEntity(
                    todoId = todoId,
                    title = title,
                    sortOrder = 0
                )
            )
        }
    }

    fun toggleSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch {
            todoRepo.setSubtaskCompleted(subtask.id, !subtask.isCompleted)
        }
    }

    fun deleteSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch {
            todoRepo.deleteSubtask(subtask)
        }
    }

    private suspend fun calculateGrowthScore() {
        val zone = ZoneId.systemDefault()
        val today = _selectedDate.value
        val (dayStart, dayEnd) = getDayRange(today, zone)

        val sessions = sessionRepo.getSessionsForDaySync(dayStart, dayEnd)
        val cats = _categories.value
        val pomodoros = pomodoroRepo.getPomodorosForDaySync(dayStart, dayEnd)

        var learningMinutes = 0.0
        var productiveMinutes = 0.0
        var exerciseMinutes = 0.0
        var socialMinutes = 0.0
        var sleepHours = 0.0

        for (session in sessions) {
            if (session.isActive) continue
            val duration = ((session.endTime ?: dayEnd) - session.startTime) / 60000.0
            val cat = session.categoryId?.let { cats[it] }
            val catType = cat?.type ?: session.type

            when (catType) {
                "LEARNING" -> learningMinutes += duration
                "PRODUCTIVE" -> productiveMinutes += duration
                "ACTIVITY" -> productiveMinutes += duration
                "EXERCISE" -> exerciseMinutes += duration
                "SOCIAL" -> socialMinutes += duration
                "SLEEP" -> sleepHours += duration / 60.0
            }
        }

        for (pomodoro in pomodoros) {
            if (pomodoro.type == "WORK" && pomodoro.isCompleted) {
                productiveMinutes += pomodoro.durationMinutes
            }
        }

        val todoMinutes = todoRepo.getTotalCompletedMinutesInRange(dayStart, dayEnd)
        productiveMinutes += todoMinutes

        var consecutiveDaysActive = 0
        for (i in 0..6) {
            val date = today.minusDays(i.toLong())
            val (dStart, dEnd) = getDayRange(date, zone)
            val daySessions = sessionRepo.getSessionsForDaySync(dStart, dEnd).filter { !it.isActive }
            val dayPomodoros = pomodoroRepo.getPomodorosForDaySync(dStart, dEnd).filter { it.isCompleted }
            if (daySessions.isNotEmpty() || dayPomodoros.isNotEmpty()) {
                consecutiveDaysActive++
            } else {
                break
            }
        }

        val noveltyScore = when {
            sessions.isEmpty() -> 0.0
            sessions.size > 5 -> 0.9
            sessions.size > 3 -> 0.7
            sessions.size > 1 -> 0.5
            else -> 0.3
        }

        val result = growthEngine.calculateGrowthScore(
            learningMinutes = learningMinutes,
            productiveMinutes = productiveMinutes,
            exerciseMinutes = exerciseMinutes,
            sleepHours = sleepHours,
            socialMinutes = socialMinutes,
            noveltyScore = noveltyScore,
            consecutiveDaysActive = consecutiveDaysActive
        )

        _growthScore.value = result.totalScore
        _growthResult.value = result
    }

    private suspend fun detectLoop() {
        val zone = ZoneId.systemDefault()
        val today = _selectedDate.value

        val dailyVectors = mutableListOf<Map<String, Double>>()
        val growthScores = mutableListOf<Double>()
        val noveltyScores = mutableListOf<Double>()
        val recentCategorySets = mutableListOf<Set<Long>>()

        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val (dayStart, dayEnd) = getDayRange(date, zone)
            val sessions = sessionRepo.getSessionsForDaySync(dayStart, dayEnd)
            val pomodoros = pomodoroRepo.getPomodorosForDaySync(dayStart, dayEnd)

            val categoryDurations = mutableMapOf<Long, Long>()
            val dayCategories = mutableSetOf<Long>()
            for (session in sessions) {
                val catId = session.categoryId ?: continue
                val duration = (session.endTime ?: dayEnd) - session.startTime
                categoryDurations[catId] = (categoryDurations[catId] ?: 0L) + duration
                dayCategories.add(catId)
            }

            for (pomodoro in pomodoros) {
                if (pomodoro.isCompleted && pomodoro.categoryId != null) {
                    val duration = pomodoro.durationMinutes * 60000L
                    categoryDurations[pomodoro.categoryId] = (categoryDurations[pomodoro.categoryId] ?: 0L) + duration
                    dayCategories.add(pomodoro.categoryId)
                }
            }

            val vector = routineEngine.calculateDailyRoutineVector(sessions, categoryDurations)
            dailyVectors.add(vector)

            val totalDuration = categoryDurations.values.sum()
            val dayNovelty = routineEngine.calculateNoveltyScore(
                currentCategories = dayCategories,
                recentCategories = recentCategorySets.toList(),
                currentDuration = totalDuration
            )
            noveltyScores.add(dayNovelty)
            recentCategorySets.add(dayCategories)
            growthScores.add(_growthScore.value)
        }

        _loopStatus.value = routineEngine.detectLoop(dailyVectors, growthScores, noveltyScores)
    }

    private suspend fun generateInsights() {
        val zone = ZoneId.systemDefault()
        val today = _selectedDate.value
        val (dayStart, dayEnd) = getDayRange(today, zone)

        val sessions = sessionRepo.getSessionsForDaySync(dayStart, dayEnd)
        val cats = _categories.value
        val pomodoros = pomodoroRepo.getPomodorosForDaySync(dayStart, dayEnd)

        val hasLearning = sessions.any { session ->
            val catType = session.categoryId?.let { cats[it]?.type }
            catType == "LEARNING" || session.type == "LEARNING"
        }

        val hasExercise = sessions.any { session ->
            val catType = session.categoryId?.let { cats[it]?.type }
            catType == "EXERCISE" || session.type == "EXERCISE"
        }

        var totalProductiveMinutes = 0.0
        var totalWastedMinutes = 0.0
        var totalBreakMinutes = 0.0

        for (session in sessions) {
            if (session.isActive) continue
            val duration = ((session.endTime ?: dayEnd) - session.startTime) / 60000.0
            val cat = session.categoryId?.let { cats[it] }
            val catType = cat?.type ?: session.type

            when (catType) {
                "PRODUCTIVE", "LEARNING", "ACTIVITY" -> totalProductiveMinutes += duration
                "WASTED" -> totalWastedMinutes += duration
                "NEUTRAL" -> totalBreakMinutes += duration
            }
        }

        for (pomodoro in pomodoros) {
            if (pomodoro.isCompleted) {
                when (pomodoro.type) {
                    "WORK" -> totalProductiveMinutes += pomodoro.durationMinutes
                    "BREAK" -> totalBreakMinutes += pomodoro.durationMinutes
                }
            }
        }

        val todoMinutes = todoRepo.getTotalCompletedMinutesInRange(dayStart, dayEnd)
        totalProductiveMinutes += todoMinutes

        val yesterday = today.minusDays(1)
        val (yesterdayStart, yesterdayEnd) = getDayRange(yesterday, zone)
        val yesterdaySessions = sessionRepo.getSessionsForDaySync(yesterdayStart, yesterdayEnd)
        var yesterdayWastedMinutes = 0.0
        for (session in yesterdaySessions) {
            if (session.isActive) continue
            val cat = session.categoryId?.let { cats[it] }
            val catType = cat?.type ?: session.type
            if (catType == "WASTED") {
                yesterdayWastedMinutes += ((session.endTime ?: yesterdayEnd) - session.startTime) / 60000.0
            }
        }

        val loop = _loopStatus.value
        val growthScore = _growthScore.value

        val templates = insightEngine.generateInsights(
            hasLearningToday = hasLearning,
            growthScoreTrend = listOf(growthScore),
            wastedTimeRatio = if (totalProductiveMinutes > 0) totalWastedMinutes / totalProductiveMinutes else 0.0,
            productiveRatio = if (totalProductiveMinutes + totalWastedMinutes > 0) totalProductiveMinutes / (totalProductiveMinutes + totalWastedMinutes) else 0.0,
            sleepDebtHours = 0.0,
            lowHydration = false,
            lowProteinDays = 0,
            lowFiberDays = 0,
            routineSimilarityDays = loop?.consecutiveDays ?: 0,
            noNewActivitiesDays = if (loop?.isLoopDetected == true) 5 else 0,
            noMovementToday = !hasExercise,
            noReflectionToday = false,
            exerciseMinutes = sessions.filter { !it.isActive && (it.type == "EXERCISE" || cats[it.categoryId]?.type == "EXERCISE") }.sumOf {
                ((it.endTime ?: dayEnd) - it.startTime) / 60000.0
            },
            sleepHours = sessions.filter { !it.isActive && (it.type == "SLEEP" || cats[it.categoryId]?.type == "SLEEP") }.sumOf {
                ((it.endTime ?: dayEnd) - it.startTime) / 3600000.0
            },
            fatigueLevel = 2,
            severeSymptoms = emptyList(),
            maintenanceMode = false
        )

        val smartInsights = generateSmartInsights(
            totalProductiveMinutes = totalProductiveMinutes,
            totalWastedMinutes = totalWastedMinutes,
            totalBreakMinutes = totalBreakMinutes,
            yesterdayWastedMinutes = yesterdayWastedMinutes,
            activeTodoCount = _activeTodoCount.value,
            completedTodoCount = _completedTodoCount.value,
            pomodoroCount = pomodoros.count { it.type == "WORK" && it.isCompleted }
        )

        val now = System.currentTimeMillis()
        for (template in templates + smartInsights) {
            val insight = insightEngine.createInsightEntity(template)
            insightRepo.insertIfNotCoolingDown(insight)
        }
    }

    private fun generateSmartInsights(
        totalProductiveMinutes: Double,
        totalWastedMinutes: Double,
        totalBreakMinutes: Double,
        yesterdayWastedMinutes: Double,
        activeTodoCount: Int,
        completedTodoCount: Int,
        pomodoroCount: Int
    ): List<InsightEngine.InsightTemplate> {
        val insights = mutableListOf<InsightEngine.InsightTemplate>()

        if (totalWastedMinutes > totalProductiveMinutes && totalProductiveMinutes > 0) {
            insights.add(InsightEngine.InsightTemplate(
                title = "Time Waste Alert",
                message = "You've wasted ${totalWastedMinutes.toInt()} minutes today, which is more than your productive time (${totalProductiveMinutes.toInt()} min). Try to refocus on your goals.",
                severity = "CRITICAL",
                category = "PRODUCTIVITY",
                actionLabel = "Start a productive session"
            ))
        }

        if (totalWastedMinutes > yesterdayWastedMinutes && yesterdayWastedMinutes > 0) {
            insights.add(InsightEngine.InsightTemplate(
                title = "Increasing Waste Pattern",
                message = "Today's wasted time (${totalWastedMinutes.toInt()} min) is higher than yesterday (${yesterdayWastedMinutes.toInt()} min). Consider adjusting your routine.",
                severity = "WARNING",
                category = "PRODUCTIVITY",
                actionLabel = "Review your schedule"
            ))
        }

        if (activeTodoCount > 5) {
            insights.add(InsightEngine.InsightTemplate(
                title = "Too Many Pending Tasks",
                message = "You have $activeTodoCount pending tasks. Focus on completing a few before adding more.",
                severity = "CAUTION",
                category = "TODO",
                actionLabel = "Complete a task"
            ))
        }

        if (completedTodoCount == 0 && activeTodoCount > 0) {
            insights.add(InsightEngine.InsightTemplate(
                title = "No Tasks Completed Today",
                message = "You haven't completed any tasks today. Try to finish at least one task.",
                severity = "WARNING",
                category = "TODO",
                actionLabel = "Complete a task"
            ))
        }

        if (pomodoroCount >= 8) {
            insights.add(InsightEngine.InsightTemplate(
                title = "Great Focus Today!",
                message = "You've completed $pomodoroCount pomodoros today. Keep up the excellent work!",
                severity = "INFO",
                category = "PRODUCTIVITY",
                actionLabel = "Continue working"
            ))
        } else if (pomodoroCount < 3 && totalProductiveMinutes > 60) {
            insights.add(InsightEngine.InsightTemplate(
                title = "Try Pomodoro Technique",
                message = "Consider using the Pomodoro timer to structure your work. It can help improve focus.",
                severity = "INFO",
                category = "PRODUCTIVITY",
                actionLabel = "Start a pomodoro"
            ))
        }

        if (totalBreakMinutes > totalProductiveMinutes && totalProductiveMinutes > 0) {
            insights.add(InsightEngine.InsightTemplate(
                title = "Too Much Break Time",
                message = "You've spent ${totalBreakMinutes.toInt()} minutes on breaks, which is more than your productive time. Try to balance better.",
                severity = "WARNING",
                category = "PRODUCTIVITY",
                actionLabel = "Get back to work"
            ))
        }

        return insights
    }

    private suspend fun calculateWeeklyStats() {
        val zone = ZoneId.systemDefault()
        val today = _selectedDate.value

        var totalProductive = 0
        var totalWasted = 0
        var totalFree = 0
        var totalCompletedTodos = 0

        for (i in 0..6) {
            val date = today.minusDays(i.toLong())
            val (dayStart, dayEnd) = getDayRange(date, zone)
            val sessions = sessionRepo.getSessionsForDaySync(dayStart, dayEnd)
            val cats = _categories.value

            for (session in sessions) {
                if (session.isActive) continue
                val duration = ((session.endTime ?: dayEnd) - session.startTime) / 60000
                val cat = session.categoryId?.let { cats[it] }
                val catType = cat?.type ?: session.type

                when (catType) {
                    "PRODUCTIVE", "LEARNING", "ACTIVITY", "EXERCISE" -> totalProductive += duration.toInt()
                    "WASTED" -> totalWasted += duration.toInt()
                    "NEUTRAL" -> totalFree += duration.toInt()
                }
            }

            totalCompletedTodos += todoRepo.getCompletedCountInRange(dayStart, dayEnd)
        }

        _weeklyStats.value = WeeklyStats(
            totalProductiveMinutes = totalProductive,
            totalWastedMinutes = totalWasted,
            totalFreeMinutes = totalFree,
            completedTodos = totalCompletedTodos,
            activeTodos = _activeTodoCount.value,
            averageProductivePerDay = totalProductive / 7
        )
    }

    private suspend fun calculateDailyProductivity() {
        val zone = ZoneId.systemDefault()
        val today = _selectedDate.value
        val cats = _categories.value
        val dailyData = mutableListOf<Pair<String, Float>>()

        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val (dayStart, dayEnd) = getDayRange(date, zone)
            val sessions = sessionRepo.getSessionsForDaySync(dayStart, dayEnd)
            val pomodoros = pomodoroRepo.getPomodorosForDaySync(dayStart, dayEnd)

            var productiveMinutes = 0f

            for (session in sessions) {
                if (session.isActive) continue
                val duration = ((session.endTime ?: dayEnd) - session.startTime) / 60000f
                val cat = session.categoryId?.let { cats[it] }
                val catType = cat?.type ?: session.type
                if (catType in listOf("PRODUCTIVE", "LEARNING", "ACTIVITY", "EXERCISE")) {
                    productiveMinutes += duration
                }
            }

            for (p in pomodoros) {
                if (p.isCompleted && p.type == "WORK") {
                    productiveMinutes += p.durationMinutes
                }
            }

            val dayOfWeek = date.dayOfWeek.value % 7
            val label = dayLabels[(dayOfWeek + 6) % 7]
            dailyData.add(label to productiveMinutes)
        }

        _dailyProductivity.value = dailyData
    }

    private fun getDayRange(date: LocalDate, zone: ZoneId, dayStartHour: Int = 0): Pair<Long, Long> {
        val start = date.atStartOfDay(zone).plusHours(dayStartHour.toLong()).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).plusHours(dayStartHour.toLong()).toInstant().toEpochMilli()
        return start to end
    }
}

data class WeeklyStats(
    val totalProductiveMinutes: Int = 0,
    val totalWastedMinutes: Int = 0,
    val totalFreeMinutes: Int = 0,
    val completedTodos: Int = 0,
    val activeTodos: Int = 0,
    val averageProductivePerDay: Int = 0
)
