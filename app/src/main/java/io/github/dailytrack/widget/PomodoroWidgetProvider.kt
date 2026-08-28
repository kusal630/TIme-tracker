package io.github.dailytrack.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.dailytrack.R
import io.github.dailytrack.SoulTrackApp
import io.github.dailytrack.service.PomodoroForegroundService
import io.github.dailytrack.data.db.entity.PomodoroSessionEntity
import kotlinx.coroutines.*

class PomodoroWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_START = "io.github.dailytrack.ACTION_POM_WIDGET_START"
        const val ACTION_STOP = "io.github.dailytrack.ACTION_POM_WIDGET_STOP"
        const val ACTION_RESET = "io.github.dailytrack.ACTION_POM_WIDGET_RESET"
        const val ACTION_UPDATE = "io.github.dailytrack.ACTION_POM_WIDGET_UPDATE"

        fun updateWidgets(context: Context) {
            val intent = Intent(context, PomodoroWidgetProvider::class.java).apply {
                action = ACTION_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_START -> {
                val prefs = context.getSharedPreferences("pom_widget_prefs", Context.MODE_PRIVATE)
                val startTime = System.currentTimeMillis()
                val duration = 25

                val serviceIntent = Intent(context, PomodoroForegroundService::class.java).apply {
                    putExtra(PomodoroForegroundService.EXTRA_START_TIME, startTime)
                    putExtra(PomodoroForegroundService.EXTRA_DURATION, duration)
                    putExtra(PomodoroForegroundService.EXTRA_TODO_TITLE, "Quick Focus")
                    putExtra(PomodoroForegroundService.EXTRA_IS_BREAK, false)
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                prefs.edit()
                    .putBoolean("is_running", true)
                    .putLong("start_time", startTime)
                    .putInt("duration", duration)
                    .putBoolean("is_break", false)
                    .apply()

                val db = (context.applicationContext as SoulTrackApp).database
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    db.pomodoroSessionDao().insert(
                        PomodoroSessionEntity(
                            startTime = startTime,
                            durationMinutes = duration,
                            type = "WORK"
                        )
                    )
                }

                val updateIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE
                }
                context.sendBroadcast(updateIntent)
            }

            ACTION_STOP -> {
                val stopIntent = Intent(context, PomodoroForegroundService::class.java).apply {
                    action = PomodoroForegroundService.ACTION_COMPLETE
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(stopIntent)
                } else {
                    context.startService(stopIntent)
                }

                val prefs = context.getSharedPreferences("pom_widget_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("is_running", false).apply()

                val updateIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE
                }
                context.sendBroadcast(updateIntent)
            }

            ACTION_RESET -> {
                val prefs = context.getSharedPreferences("pom_widget_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("is_running", false)
                    .putLong("start_time", 0L)
                    .putInt("duration", 25)
                    .putBoolean("is_break", false)
                    .apply()

                val updateIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE
                }
                context.sendBroadcast(updateIntent)
            }

            ACTION_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = android.content.ComponentName(context, PomodoroWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_pomodoro)

        val prefs = context.getSharedPreferences("pom_widget_prefs", Context.MODE_PRIVATE)
        val isRunning = prefs.getBoolean("is_running", false)
        val startTime = prefs.getLong("start_time", 0L)
        val duration = prefs.getInt("duration", 25)
        val isBreak = prefs.getBoolean("is_break", false)

        val db = (context.applicationContext as SoulTrackApp).database
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            val todayStart = java.time.LocalDate.now()
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            val todayEnd = java.time.LocalDate.now().plusDays(1)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            val sessions = db.pomodoroSessionDao().getPomodorosForDaySync(todayStart, todayEnd)
            val workCount = sessions.count { it.type == "WORK" && it.isCompleted }

            views.setTextViewText(R.id.widget_pom_session_count, "Sessions: $workCount")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        if (isRunning && startTime > 0) {
            val elapsed = System.currentTimeMillis() - startTime
            val totalMillis = duration * 60 * 1000L
            val remaining = (totalMillis - elapsed).coerceAtLeast(0)
            val minutes = remaining / 60000
            val seconds = (remaining % 60000) / 1000
            val timeStr = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

            views.setTextViewText(R.id.widget_pom_timer, timeStr)
            views.setTextViewText(R.id.widget_pom_status, if (isBreak) "Break Time" else "Focusing...")
            views.setViewVisibility(R.id.widget_pom_btn_start, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_pom_btn_stop, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.widget_pom_btn_reset, android.view.View.VISIBLE)
        } else {
            views.setTextViewText(R.id.widget_pom_timer, "25:00")
            views.setTextViewText(R.id.widget_pom_status, "Ready to focus")
            views.setViewVisibility(R.id.widget_pom_btn_start, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.widget_pom_btn_stop, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_pom_btn_reset, android.view.View.GONE)
        }

        val startIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
            action = ACTION_START
        }
        val startPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 0, startIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_pom_btn_start, startPendingIntent)

        val stopIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 1, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_pom_btn_stop, stopPendingIntent)

        val resetIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
            action = ACTION_RESET
        }
        val resetPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 2, resetIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_pom_btn_reset, resetPendingIntent)

        val rootClickIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
            action = ACTION_UPDATE
        }
        val rootPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 20, rootClickIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_pom_root, rootPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
