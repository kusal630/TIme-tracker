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


package io.github.dailytrack.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.github.dailytrack.MainActivity
import io.github.dailytrack.widget.TimerWidgetProvider
import kotlinx.coroutines.*

class TimerForegroundService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var startTime: Long = 0L
    private var sessionTitle: String = ""
    private var categoryName: String = ""
    private var categoryType: String = ""
    private var notificationManager: NotificationManager? = null
    private var timerJob: Job? = null

    companion object {
        const val CHANNEL_ID = "soultrack_timer"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "io.github.dailytrack.STOP_TIMER"
        const val EXTRA_START_TIME = "start_time"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_CATEGORY_TYPE = "category_type"

        fun getCategoryColor(type: String, isWaste: Boolean = false): Int {
            if (isWaste) return 0xFFF44336.toInt()
            return when (type) {
                "PRODUCTIVE", "LEARNING" -> 0xFF4CAF50.toInt()
                "EXERCISE" -> 0xFF2196F3.toInt()
                "SLEEP" -> 0xFF9C27B0.toInt()
                "SOCIAL" -> 0xFFFF9800.toInt()
                "RECOVERY", "REST" -> 0xFF607D8B.toInt()
                "WASTED" -> 0xFFF44336.toInt()
                else -> 0xFF757575.toInt()
            }
        }

        fun getCategoryLabel(type: String, isWaste: Boolean = false): String {
            if (isWaste) return "Waste"
            return when (type) {
                "PRODUCTIVE", "LEARNING" -> "Productive"
                "EXERCISE" -> "Exercise"
                "SLEEP" -> "Sleep"
                "SOCIAL" -> "Social"
                "RECOVERY", "REST" -> "Rest"
                "WASTED" -> "Waste"
                else -> "Activity"
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        notificationManager = getSystemService(NotificationManager::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                timerJob?.cancel()
                val prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("is_running", false).apply()
                notificationManager?.cancel(NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                TimerWidgetProvider.updateWidgets(this)
                return START_NOT_STICKY
            }
        }

        startTime = intent?.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
            ?: System.currentTimeMillis()
        sessionTitle = intent?.getStringExtra(EXTRA_TITLE) ?: "Active Session"
        categoryName = intent?.getStringExtra(EXTRA_CATEGORY) ?: ""
        categoryType = intent?.getStringExtra(EXTRA_CATEGORY_TYPE) ?: ""

        val prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_running", true)
            .putLong("start_time", startTime)
            .putString("session_name", sessionTitle)
            .putString("category_name", categoryName)
            .apply()

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        startNotificationUpdates()
        TimerWidgetProvider.updateWidgets(this)
        return START_STICKY
    }

    private fun startNotificationUpdates() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                val notification = buildNotification()
                notificationManager?.notify(NOTIFICATION_ID, notification)
                TimerWidgetProvider.updateWidgets(this@TimerForegroundService)
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

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, TimerForegroundService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val elapsed = System.currentTimeMillis() - startTime
        val hours = elapsed / 3600000
        val minutes = (elapsed % 3600000) / 60000
        val seconds = (elapsed % 60000) / 1000
        val timeStr = String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

        val catColor = getCategoryColor(categoryType)
        val catLabel = getCategoryLabel(categoryType)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(sessionTitle.ifBlank { "Active Session" })
            .setContentText("Running: $timeStr")
            .setSubText(catLabel)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setUsesChronometer(false)
            .setShowWhen(false)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(catColor)
            .setStyle(
                NotificationCompat.DecoratedCustomViewStyle()
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active timer session"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timerJob?.cancel()
        val prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("is_running", false).apply()
        notificationManager?.cancel(NOTIFICATION_ID)
        scope.cancel()
        super.onDestroy()
    }
}
