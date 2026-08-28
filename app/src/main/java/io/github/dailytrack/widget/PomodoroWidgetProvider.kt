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


package io.github.dailytrack.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.dailytrack.R
import io.github.dailytrack.service.PomodoroForegroundService
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

                val updateIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE
                }
                context.sendBroadcast(updateIntent)
            }

            ACTION_STOP -> {
                val stopIntent = Intent(context, PomodoroForegroundService::class.java).apply {
                    action = PomodoroForegroundService.ACTION_STOP
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
                val stopIntent = Intent(context, PomodoroForegroundService::class.java).apply {
                    action = PomodoroForegroundService.ACTION_STOP
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(stopIntent)
                } else {
                    context.startService(stopIntent)
                }

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
        val workColor = prefs.getInt("work_color", 0xFFE94560.toInt())

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

            if (!isBreak) {
                val progress = (elapsed.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
                val fromR = android.graphics.Color.red(workColor).toFloat()
                val fromG = android.graphics.Color.green(workColor).toFloat()
                val fromB = android.graphics.Color.blue(workColor).toFloat()
                val toR = android.graphics.Color.red(0xFFFFAB40.toInt()).toFloat()
                val toG = android.graphics.Color.green(0xFFFFAB40.toInt()).toFloat()
                val toB = android.graphics.Color.blue(0xFFFFAB40.toInt()).toFloat()
                val r = (fromR + (toR - fromR) * progress).toInt()
                val g = (fromG + (toG - fromG) * progress).toInt()
                val b = (fromB + (toB - fromB) * progress).toInt()
                val timerColor = android.graphics.Color.rgb(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
                views.setTextColor(R.id.widget_pom_timer, timerColor)
            } else {
                views.setTextColor(R.id.widget_pom_timer, 0xFFFFAB40.toInt())
            }
        } else {
            views.setTextViewText(R.id.widget_pom_timer, "25:00")
            views.setTextViewText(R.id.widget_pom_status, "Ready to focus")
            views.setViewVisibility(R.id.widget_pom_btn_start, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.widget_pom_btn_stop, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_pom_btn_reset, android.view.View.GONE)
            views.setTextColor(R.id.widget_pom_timer, android.graphics.Color.WHITE)
        }

        val startIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
            action = ACTION_START
        }
        val startPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 12, startIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_pom_btn_start, startPendingIntent)

        val stopIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 13, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_pom_btn_stop, stopPendingIntent)

        val resetIntent = Intent(context, PomodoroWidgetProvider::class.java).apply {
            action = ACTION_RESET
        }
        val resetPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 14, resetIntent,
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
