package io.github.dailytrack.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.dailytrack.R
import io.github.dailytrack.service.TimerForegroundService

class TimerWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_START = "io.github.dailytrack.ACTION_WIDGET_START"
        const val ACTION_STOP = "io.github.dailytrack.ACTION_WIDGET_STOP"
        const val ACTION_RESET = "io.github.dailytrack.ACTION_WIDGET_RESET"
        const val ACTION_UPDATE = "io.github.dailytrack.ACTION_WIDGET_UPDATE"

        fun updateWidgets(context: Context) {
            val intent = Intent(context, TimerWidgetProvider::class.java).apply {
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
                val startIntent = Intent(context, TimerForegroundService::class.java).apply {
                    putExtra(TimerForegroundService.EXTRA_START_TIME, System.currentTimeMillis())
                    putExtra(TimerForegroundService.EXTRA_TITLE, "Quick Session")
                    putExtra(TimerForegroundService.EXTRA_CATEGORY, "")
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(startIntent)
                } else {
                    context.startService(startIntent)
                }

                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("is_running", true)
                    .putLong("start_time", System.currentTimeMillis())
                    .putString("session_name", "Quick Session")
                    .apply()

                val updateIntent = Intent(context, TimerWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE
                }
                context.sendBroadcast(updateIntent)
            }

            ACTION_STOP -> {
                val stopIntent = Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_STOP
                }
                context.startService(stopIntent)

                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("is_running", false).apply()

                val updateIntent = Intent(context, TimerWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE
                }
                context.sendBroadcast(updateIntent)
            }

            ACTION_RESET -> {
                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("is_running", false)
                    .putLong("start_time", 0L)
                    .putString("session_name", "Ready to start")
                    .apply()

                val updateIntent = Intent(context, TimerWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE
                }
                context.sendBroadcast(updateIntent)
            }

            ACTION_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = android.content.ComponentName(context, TimerWidgetProvider::class.java)
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
        val views = RemoteViews(context.packageName, R.layout.widget_timer)

        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val isRunning = prefs.getBoolean("is_running", false)
        val startTime = prefs.getLong("start_time", 0L)
        val sessionName = prefs.getString("session_name", "Ready to start") ?: "Ready to start"

        views.setTextViewText(R.id.widget_session_name, sessionName)

        if (isRunning && startTime > 0) {
            val elapsed = System.currentTimeMillis() - startTime
            val hours = elapsed / 3600000
            val minutes = (elapsed % 3600000) / 60000
            val seconds = (elapsed % 60000) / 1000
            val timeStr = String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

            views.setTextViewText(R.id.widget_timer, timeStr)
            views.setViewVisibility(R.id.widget_btn_start, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_btn_stop, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.widget_btn_reset, android.view.View.VISIBLE)
        } else {
            views.setTextViewText(R.id.widget_timer, "00:00:00")
            views.setViewVisibility(R.id.widget_btn_start, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.widget_btn_stop, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_btn_reset, android.view.View.GONE)
        }

        val startIntent = Intent(context, TimerWidgetProvider::class.java).apply {
            action = ACTION_START
        }
        val startPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 0, startIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_start, startPendingIntent)

        val stopIntent = Intent(context, TimerWidgetProvider::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 1, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_stop, stopPendingIntent)

        val resetIntent = Intent(context, TimerWidgetProvider::class.java).apply {
            action = ACTION_RESET
        }
        val resetPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 2, resetIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_reset, resetPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
