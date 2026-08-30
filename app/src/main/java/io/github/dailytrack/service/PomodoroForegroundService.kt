package io.github.dailytrack.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.NotificationManager.Policy
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import io.github.dailytrack.MainActivity
import io.github.dailytrack.SoulTrackApp
import io.github.dailytrack.data.db.entity.SessionEntity
import io.github.dailytrack.widget.PomodoroWidgetProvider
import kotlinx.coroutines.*

class PomodoroForegroundService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private var sedentaryJob: Job? = null
    private var notificationManager: NotificationManager? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var workDurationMinutes: Int = 25
    private var breakDurationMinutes: Int = 5
    private var longBreakDurationMinutes: Int = 15
    private var todoTitle: String = ""
    private var currentSessionNumber: Int = 0
    private var isBreak: Boolean = false
    private var isLongBreak: Boolean = false
    private var startTime: Long = 0L
    private var workColor: Int = 0xFFE94560.toInt()
    private var dndWasEnabled: Boolean = false

    companion object {
        const val CHANNEL_WORK = "soultrack_work"
        const val CHANNEL_BREAK = "soultrack_break"
        const val CHANNEL_SEDENTARY = "soultrack_sedentary"
        const val CHANNEL_TRANSITION = "soultrack_transition"
        const val NOTIFICATION_ID = 1002
        const val SEDENTARY_NOTIFICATION_ID = 1003
        const val TRANSITION_NOTIFICATION_ID = 1005

        const val ACTION_STOP = "io.github.dailytrack.STOP_POMODORO"
        const val ACTION_COMPLETE_WORK = "io.github.dailytrack.COMPLETE_POMODORO_WORK"
        const val ACTION_COMPLETE_BREAK = "io.github.dailytrack.COMPLETE_POMODORO_BREAK"
        const val ACTION_START_NEW = "io.github.dailytrack.START_NEW_POMODORO"

        const val EXTRA_START_TIME = "start_time"
        const val EXTRA_WORK_DURATION = "work_duration"
        const val EXTRA_BREAK_DURATION = "break_duration"
        const val EXTRA_TODO_TITLE = "todo_title"
        const val EXTRA_WORK_COLOR = "work_color"
        const val EXTRA_SESSION_NUMBER = "session_number"
        const val EXTRA_IS_BREAK = "is_break"
        const val EXTRA_IS_LONG_BREAK = "is_long_break"
        const val EXTRA_LONG_BREAK_DURATION = "long_break_duration"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        notificationManager = getSystemService(NotificationManager::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopAll()
                return START_NOT_STICKY
            }
            ACTION_COMPLETE_WORK -> {
                onWorkSessionComplete()
                return START_STICKY
            }
            ACTION_COMPLETE_BREAK -> {
                onBreakComplete()
                return START_STICKY
            }
            ACTION_START_NEW -> {
                currentSessionNumber = 0
                isBreak = false
                isLongBreak = false
                startWorkSession(
                    workDuration = intent.getIntExtra(EXTRA_WORK_DURATION, workDurationMinutes),
                    title = intent.getStringExtra(EXTRA_TODO_TITLE) ?: todoTitle
                )
                return START_STICKY
            }
        }

        workDurationMinutes = intent?.getIntExtra(EXTRA_WORK_DURATION, 25) ?: 25
        breakDurationMinutes = intent?.getIntExtra(EXTRA_BREAK_DURATION, 5) ?: 5
        longBreakDurationMinutes = intent?.getIntExtra(EXTRA_LONG_BREAK_DURATION, 15) ?: 15
        todoTitle = intent?.getStringExtra(EXTRA_TODO_TITLE) ?: ""
        workColor = intent?.getIntExtra(EXTRA_WORK_COLOR, 0xFFE94560.toInt()) ?: 0xFFE94560.toInt()
        currentSessionNumber = intent?.getIntExtra(EXTRA_SESSION_NUMBER, 0) ?: 0
        isBreak = intent?.getBooleanExtra(EXTRA_IS_BREAK, false) ?: false
        isLongBreak = intent?.getBooleanExtra(EXTRA_IS_LONG_BREAK, false) ?: false

        startWorkSession(workDurationMinutes, todoTitle)
        return START_STICKY
    }

    private fun startWorkSession(workDuration: Int, title: String) {
        timerJob?.cancel()
        sedentaryJob?.cancel()
        isBreak = false
        isLongBreak = false
        workDurationMinutes = workDuration
        todoTitle = title
        startTime = System.currentTimeMillis()

        startTrackingSession(title)
        enableDnd()
        acquireWakeLock()
        playTransitionSound(isWork = true)
        showTransitionNotification("Focus session $currentSessionNumber started", "Stay focused for $workDuration minutes")

        updatePrefs()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        startNotificationUpdates()
        PomodoroWidgetProvider.updateWidgets(this)
    }

    private fun startBreakSession(durationMinutes: Int, isLong: Boolean) {
        timerJob?.cancel()
        sedentaryJob?.cancel()
        isBreak = true
        isLongBreak = isLong
        breakDurationMinutes = durationMinutes
        startTime = System.currentTimeMillis()

        stopTrackingSession()
        disableDnd()
        acquireWakeLock()
        playTransitionSound(isWork = false)
        val breakLabel = if (isLong) "Long break" else "Short break"
        showBreakQuoteNotification("$breakLabel started", "Relax for $durationMinutes minutes")

        updatePrefs()
        val breakNotification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, breakNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, breakNotification)
        }
        startNotificationUpdates()
        scheduleSedentaryReminder()
        scheduleEyeBlinkReminder()
        PomodoroWidgetProvider.updateWidgets(this)
    }

    private fun onWorkSessionComplete() {
        timerJob?.cancel()
        sedentaryJob?.cancel()
        playCompletionVibration()

        val db = (applicationContext as SoulTrackApp).database
        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            db.pomodoroSessionDao().completeActiveSession(now)
        }

        currentSessionNumber++
        showTransitionNotification(
            "Session $currentSessionNumber completed!",
            "Great work! Time for a break."
        )

        scope.launch {
            delay(2000L)
            if (currentSessionNumber % 4 == 0) {
                startBreakSession(longBreakDurationMinutes, isLong = true)
            } else {
                startBreakSession(breakDurationMinutes, isLong = false)
            }
        }
    }

    private fun onBreakComplete() {
        timerJob?.cancel()
        sedentaryJob?.cancel()
        playCompletionVibration()

        val db = (applicationContext as SoulTrackApp).database
        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            db.pomodoroSessionDao().completeActiveSession(now)
        }

        showTransitionNotification(
            "Break ended",
            "Ready for the next focus session?"
        )

        scope.launch {
            delay(2000L)
            startWorkSession(workDurationMinutes, todoTitle)
        }
    }

    private fun stopAll() {
        timerJob?.cancel()
        sedentaryJob?.cancel()
        stopTrackingSession()
        disableDnd()
        releaseWakeLock()
        notificationManager?.cancel(NOTIFICATION_ID)
        notificationManager?.cancel(SEDENTARY_NOTIFICATION_ID)
        notificationManager?.cancel(TRANSITION_NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        PomodoroWidgetProvider.updateWidgets(this)
    }

    private fun startTrackingSession(title: String) {
        val db = (applicationContext as SoulTrackApp).database
        CoroutineScope(Dispatchers.IO).launch {
            db.sessionDao().deactivateAllSessions(System.currentTimeMillis())

            val deepWorkCat = db.categoryDao().getIdByName("Deep Work")
            val catId = deepWorkCat ?: db.categoryDao().getIdByName("Productive")
            val catType = if (deepWorkCat != null) "PRODUCTIVE" else "PRODUCTIVE"

            val session = SessionEntity(
                title = title.ifBlank { "Pomodoro Focus" },
                categoryId = catId,
                type = catType,
                startTime = System.currentTimeMillis(),
                isActive = true,
                source = "POMODORO",
                timezoneId = java.time.ZoneId.systemDefault().id
            )
            val sessionId = db.sessionDao().insert(session)

            val prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE)
            prefs.edit()
                .putBoolean("is_running", true)
                .putLong("start_time", System.currentTimeMillis())
                .putLong("session_id", sessionId)
                .putString("session_name", "Pomodoro Focus")
                .putString("category_name", "Deep Work")
                .putString("category_type", "PRODUCTIVE")
                .apply()

            val intent = Intent(this@PomodoroForegroundService, TimerForegroundService::class.java).apply {
                putExtra(TimerForegroundService.EXTRA_START_TIME, System.currentTimeMillis())
                putExtra(TimerForegroundService.EXTRA_TITLE, title.ifBlank { "Pomodoro Focus" })
                putExtra(TimerForegroundService.EXTRA_CATEGORY, "Deep Work")
                putExtra(TimerForegroundService.EXTRA_CATEGORY_TYPE, "PRODUCTIVE")
                putExtra(TimerForegroundService.EXTRA_IS_POMODORO, true)
            }
            withContext(Dispatchers.Main) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        }
    }

    private fun stopTrackingSession() {
        val db = (applicationContext as SoulTrackApp).database
        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            db.sessionDao().stopActiveSession(now)
        }

        val prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_running", false)
            .putLong("session_id", -1L)
            .apply()

        stopService(Intent(this, TimerForegroundService::class.java))
    }

    private fun acquireWakeLock() {
        releaseWakeLock()
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "soultrack:pomodoro_transition"
        ).apply {
            acquire(10_000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun enableDnd() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return

        dndWasEnabled = nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
    }

    private fun disableDnd() {
        if (!dndWasEnabled) return
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        dndWasEnabled = false
    }

    private fun playTransitionSound(isWork: Boolean) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java) as Vibrator
        }

        val pattern = if (isWork) {
            longArrayOf(0, 300, 100, 300, 100, 500)
        } else {
            longArrayOf(0, 200, 100, 200, 100, 300)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }

        val sound = if (isWork) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        try {
            val ringtone = RingtoneManager.getRingtone(this, sound)
            ringtone?.play()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try { ringtone?.stop() } catch (_: Exception) {}
            }, 3000)
        } catch (_: Exception) {}
    }

    private fun playCompletionVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java) as Vibrator
        }

        val pattern = longArrayOf(0, 400, 100, 400, 100, 400, 100, 600)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun showTransitionNotification(title: String, text: String) {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_TRANSITION)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager?.notify(TRANSITION_NOTIFICATION_ID, notification)
    }

    private fun showBreakQuoteNotification(title: String, text: String) {
        val breakQuotes = listOf(
            "Rest when you're weary. Refresh and renewal await.",
            "A short rest fuels a powerful comeback.",
            "Step back to leap forward.",
            "The pause between notes makes the music.",
            "Even the sun rests before rising again.",
            "Breathe. You've earned this moment.",
            "Stillness is not laziness \u2014 it's strategy.",
            "Recharge now, conquer later.",
            "Great things take breaks too.",
            "Your mind processes miracles in silence."
        )

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quote = breakQuotes.random()
        val notification = NotificationCompat.Builder(this, CHANNEL_TRANSITION)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$quote\n\n$text")
                    .setSummaryText(text)
            )
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager?.notify(TRANSITION_NOTIFICATION_ID, notification)
    }

    private fun startNotificationUpdates() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                val notification = buildNotification()
                notificationManager?.notify(NOTIFICATION_ID, notification)
                PomodoroWidgetProvider.updateWidgets(this@PomodoroForegroundService)
                delay(1000)
            }
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val action = if (isBreak) ACTION_COMPLETE_BREAK else ACTION_COMPLETE_WORK
        val completeIntent = PendingIntent.getService(
            this, 2,
            Intent(this, PomodoroForegroundService::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 3,
            Intent(this, PomodoroForegroundService::class.java).apply { this.action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val elapsed = System.currentTimeMillis() - startTime
        val totalMillis = (if (isBreak) breakDurationMinutes else workDurationMinutes) * 60 * 1000L
        val remaining = (totalMillis - elapsed).coerceAtLeast(0)
        val minutes = remaining / 60000
        val seconds = (remaining % 60000) / 1000
        val timeStr = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

        val channel = if (isBreak) CHANNEL_BREAK else CHANNEL_WORK
        val title = when {
            isBreak && isLongBreak -> "Long Break"
            isBreak -> "Short Break"
            else -> "Focus Session $currentSessionNumber"
        }
        val subtitle = when {
            isBreak -> "Relax before next session"
            todoTitle.isNotBlank() -> todoTitle
            else -> "Stay focused!"
        }

        val sessionLabel = if (isBreak) {
            "Break (${currentSessionNumber}/4)"
        } else {
            "Session ${currentSessionNumber + 1}/4"
        }

        return NotificationCompat.Builder(this, channel)
            .setContentTitle("$title: $timeStr")
            .setContentText(subtitle)
            .setSubText(sessionLabel)
            .setSmallIcon(if (isBreak) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setUsesChronometer(false)
            .setShowWhen(false)
            .setColor(if (isBreak) 0xFFFFAB40.toInt() else workColor)
            .setColorized(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (isBreak) "Skip Break" else "Complete",
                completeIntent
            )
            .addAction(android.R.drawable.ic_delete, "Stop", stopIntent)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updatePrefs() {
        val prefs = getSharedPreferences("pom_widget_prefs", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_running", true)
            .putLong("start_time", startTime)
            .putInt("duration", if (isBreak) breakDurationMinutes else workDurationMinutes)
            .putBoolean("is_break", isBreak)
            .putInt("work_color", workColor)
            .putInt("session_number", currentSessionNumber)
            .putBoolean("is_long_break", isLongBreak)
            .apply()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val workChannel = NotificationChannel(
                CHANNEL_WORK, "Focus Sessions", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active focus session timer"
                setShowBadge(false)
                lightColor = 0xFFE94560.toInt()
                enableLights(true)
            }

            val breakChannel = NotificationChannel(
                CHANNEL_BREAK, "Break Timer", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active break timer"
                setShowBadge(false)
                lightColor = 0xFFFFAB40.toInt()
                enableLights(true)
            }

            val sedentaryChannel = NotificationChannel(
                CHANNEL_SEDENTARY, "Movement Reminders", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to stand and stretch during breaks"
                setShowBadge(true)
                lightColor = 0xFF4CAF50.toInt()
                enableLights(true)
                enableVibration(true)
            }

            val transitionChannel = NotificationChannel(
                CHANNEL_TRANSITION, "Session Transitions", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when sessions start or end"
                setShowBadge(true)
                lightColor = 0xFFE94560.toInt()
                enableLights(true)
                enableVibration(true)
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(workChannel)
            nm.createNotificationChannel(breakChannel)
            nm.createNotificationChannel(sedentaryChannel)
            nm.createNotificationChannel(transitionChannel)
        }
    }

    private fun scheduleSedentaryReminder() {
        sedentaryJob?.cancel()
        sedentaryJob = scope.launch {
            delay(30_000L)
            if (isActive && isBreak) {
                showSedentaryNotification()
            }
        }
    }

    private fun showSedentaryNotification() {
        val tips = listOf(
            "Stand up and stretch your arms above your head!",
            "Take a walk around the room for 2 minutes",
            "Do 10 squats to get your blood flowing",
            "Stretch your neck: roll it slowly in circles",
            "Touch your toes - hold for 15 seconds",
            "Do some calf raises to activate your legs",
            "Open a window and take 5 deep breaths",
            "Shake out your hands and wrists"
        )

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_SEDENTARY)
            .setContentTitle("Time to Move!")
            .setContentText(tips.random())
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager?.notify(SEDENTARY_NOTIFICATION_ID, notification)
    }

    private fun scheduleEyeBlinkReminder() {
        scope.launch {
            delay(20_000L)
            if (isActive && isBreak) {
                showEyeBlinkNotification()
            }
        }
    }

    private fun showEyeBlinkNotification() {
        val tips = listOf(
            "Blink your eyes 20 times to reduce dryness and strain.",
            "Close your eyes for 10 seconds, then focus on something far away.",
            "Look at something 20 feet away for 20 seconds (20-20-20 rule).",
            "Gently press your closed eyelids for 5 seconds, then release.",
            "Roll your eyes clockwise 5 times, then counter-clockwise 5 times.",
            "Place your palms over your eyes for 30 seconds of darkness rest.",
            "Focus on a distant object for 15 seconds, then something close. Repeat 3 times."
        )

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_SEDENTARY)
            .setContentTitle("Eye Care Break")
            .setContentText(tips.random())
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager?.notify(SEDENTARY_NOTIFICATION_ID + 20, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timerJob?.cancel()
        sedentaryJob?.cancel()
        disableDnd()
        releaseWakeLock()
        val prefs = getSharedPreferences("pom_widget_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("is_running", false).apply()
        notificationManager?.cancel(NOTIFICATION_ID)
        notificationManager?.cancel(SEDENTARY_NOTIFICATION_ID)
        notificationManager?.cancel(TRANSITION_NOTIFICATION_ID)
        scope.cancel()
        super.onDestroy()
    }
}
