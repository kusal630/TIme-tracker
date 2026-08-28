package io.github.dailytrack.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import io.github.dailytrack.MainActivity
import io.github.dailytrack.widget.PomodoroWidgetProvider
import kotlinx.coroutines.*

class PomodoroForegroundService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var startTime: Long = 0L
    private var durationMinutes: Int = 25
    private var todoTitle: String = ""
    private var isBreak: Boolean = false
    private var notificationManager: NotificationManager? = null
    private var timerJob: Job? = null
    private var workColor: Int = 0xFFE94560.toInt()

    companion object {
        const val CHANNEL_WORK = "soultrack_work"
        const val CHANNEL_BREAK = "soultrack_break"
        const val NOTIFICATION_ID = 1002
        const val ACTION_STOP = "io.github.dailytrack.STOP_POMODORO"
        const val ACTION_COMPLETE = "io.github.dailytrack.COMPLETE_POMODORO"
        const val ACTION_COMPLETE_WORK = "io.github.dailytrack.COMPLETE_POMODORO_WORK"
        const val ACTION_COMPLETE_BREAK = "io.github.dailytrack.COMPLETE_POMODORO_BREAK"
        const val EXTRA_START_TIME = "start_time"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_TODO_TITLE = "todo_title"
        const val EXTRA_IS_BREAK = "is_break"
        const val EXTRA_WORK_COLOR = "work_color"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        notificationManager = getSystemService(NotificationManager::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                timerJob?.cancel()
                val prefs = getSharedPreferences("pom_widget_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("is_running", false).apply()
                val stopBroadcast = Intent(ACTION_COMPLETE)
                stopBroadcast.setPackage(packageName)
                sendBroadcast(stopBroadcast)
                notificationManager?.cancel(NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                PomodoroWidgetProvider.updateWidgets(this)
                return START_NOT_STICKY
            }
            ACTION_COMPLETE -> {
                timerJob?.cancel()
                playCompletionSound()
                val prefs = getSharedPreferences("pom_widget_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("is_running", false).apply()
                val completeBroadcast = Intent(ACTION_COMPLETE)
                completeBroadcast.setPackage(packageName)
                sendBroadcast(completeBroadcast)
                notificationManager?.cancel(NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                PomodoroWidgetProvider.updateWidgets(this)
                return START_NOT_STICKY
            }
            ACTION_COMPLETE_WORK -> {
                timerJob?.cancel()
                val prefs = getSharedPreferences("pom_widget_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("is_running", false).apply()
                val workBroadcast = Intent(ACTION_COMPLETE_WORK)
                workBroadcast.setPackage(packageName)
                sendBroadcast(workBroadcast)
                notificationManager?.cancel(NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                PomodoroWidgetProvider.updateWidgets(this)
                return START_NOT_STICKY
            }
            ACTION_COMPLETE_BREAK -> {
                timerJob?.cancel()
                val prefs = getSharedPreferences("pom_widget_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("is_running", false).apply()
                val breakBroadcast = Intent(ACTION_COMPLETE_BREAK)
                breakBroadcast.setPackage(packageName)
                sendBroadcast(breakBroadcast)
                notificationManager?.cancel(NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                PomodoroWidgetProvider.updateWidgets(this)
                return START_NOT_STICKY
            }
        }

        startTime = intent?.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
            ?: System.currentTimeMillis()
        durationMinutes = intent?.getIntExtra(EXTRA_DURATION, 25) ?: 25
        todoTitle = intent?.getStringExtra(EXTRA_TODO_TITLE) ?: ""
        isBreak = intent?.getBooleanExtra(EXTRA_IS_BREAK, false) ?: false
        workColor = intent?.getIntExtra(EXTRA_WORK_COLOR, 0xFFE94560.toInt()) ?: 0xFFE94560.toInt()

        val prefs = getSharedPreferences("pom_widget_prefs", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_running", true)
            .putLong("start_time", startTime)
            .putInt("duration", durationMinutes)
            .putBoolean("is_break", isBreak)
            .apply()

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        startNotificationUpdates()
        PomodoroWidgetProvider.updateWidgets(this)
        return START_STICKY
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
        val completeIntent = PendingIntent.getBroadcast(
            this, 2,
            Intent(this, PomodoroForegroundService::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val elapsed = System.currentTimeMillis() - startTime
        val totalMillis = durationMinutes * 60 * 1000L
        val remaining = (totalMillis - elapsed).coerceAtLeast(0)
        val minutes = remaining / 60000
        val seconds = (remaining % 60000) / 1000
        val timeStr = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

        val channel = if (isBreak) CHANNEL_BREAK else CHANNEL_WORK
        val title = if (isBreak) "Break Time" else "Focus Time"
        val subtitle = if (todoTitle.isNotBlank()) todoTitle else "Stay focused!"

        val notificationColor = if (isBreak) {
            0xFFFFAB40.toInt()
        } else {
            val progress = elapsed.toFloat() / totalMillis.toFloat()
            val fromR = Color.red(workColor)
            val fromG = Color.green(workColor)
            val fromB = Color.blue(workColor)
            val toR = Color.red(0xFFFFAB40.toInt())
            val toG = Color.green(0xFFFFAB40.toInt())
            val toB = Color.blue(0xFFFFAB40.toInt())
            val r = (fromR + (toR - fromR) * progress).toInt()
            val g = (fromG + (toG - fromG) * progress).toInt()
            val b = (fromB + (toB - fromB) * progress).toInt()
            Color.rgb(r, g, b)
        }

        return NotificationCompat.Builder(this, channel)
            .setContentTitle("$title: $timeStr")
            .setContentText(subtitle)
            .setSmallIcon(if (isBreak) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setUsesChronometer(false)
            .setShowWhen(false)
            .setColor(notificationColor)
            .setColorized(true)
            .addAction(android.R.drawable.ic_media_pause, if (isBreak) "Skip Break" else "Complete", completeIntent)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun playCompletionSound() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java) as Vibrator
        }

        val pattern = if (isBreak) {
            longArrayOf(0, 200, 100, 200, 100, 400)
        } else {
            longArrayOf(0, 300, 150, 300, 150, 300, 150, 600)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }

        val sound = if (isBreak) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        val ringtone = RingtoneManager.getRingtone(this, sound)
        ringtone?.play()

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                ringtone?.stop()
            } catch (_: Exception) {}
        }, 3000)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val workChannel = NotificationChannel(
                CHANNEL_WORK,
                "Focus Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active focus session timer"
                setShowBadge(false)
                lightColor = 0xFFE94560.toInt()
                enableLights(true)
            }

            val breakChannel = NotificationChannel(
                CHANNEL_BREAK,
                "Break Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active break timer"
                setShowBadge(false)
                lightColor = 0xFFFFAB40.toInt()
                enableLights(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(workChannel)
            notificationManager.createNotificationChannel(breakChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timerJob?.cancel()
        val prefs = getSharedPreferences("pom_widget_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("is_running", false).apply()
        notificationManager?.cancel(NOTIFICATION_ID)
        scope.cancel()
        super.onDestroy()
    }
}
