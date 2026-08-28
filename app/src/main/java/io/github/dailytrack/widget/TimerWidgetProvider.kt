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
import io.github.dailytrack.SoulTrackApp
import io.github.dailytrack.data.api.QuotesApi
import io.github.dailytrack.data.db.entity.SessionEntity
import io.github.dailytrack.service.TimerForegroundService
import kotlinx.coroutines.*

class TimerWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_START = "io.github.dailytrack.ACTION_WIDGET_START"
        const val ACTION_STOP = "io.github.dailytrack.ACTION_WIDGET_STOP"
        const val ACTION_RESET = "io.github.dailytrack.ACTION_WIDGET_RESET"
        const val ACTION_UPDATE = "io.github.dailytrack.ACTION_WIDGET_UPDATE"
        const val ACTION_CYCLE_CATEGORY = "io.github.dailytrack.ACTION_CYCLE_CATEGORY"
        const val ACTION_NEXT_QUOTE = "io.github.dailytrack.ACTION_NEXT_QUOTE"

        private val widgetCategories = listOf(
            Pair("Productive", "PRODUCTIVE"),
            Pair("Learning", "LEARNING"),
            Pair("Exercise", "EXERCISE"),
            Pair("Social", "SOCIAL"),
            Pair("Wasted", "WASTED")
        )

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
                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                val categoryIndex = prefs.getInt("category_index", 0)
                val (catName, catType) = widgetCategories[categoryIndex]

                val startIntent = Intent(context, TimerForegroundService::class.java).apply {
                    putExtra(TimerForegroundService.EXTRA_START_TIME, System.currentTimeMillis())
                    putExtra(TimerForegroundService.EXTRA_TITLE, "Quick Session")
                    putExtra(TimerForegroundService.EXTRA_CATEGORY, catName)
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(startIntent)
                } else {
                    context.startService(startIntent)
                }

                prefs.edit()
                    .putBoolean("is_running", true)
                    .putLong("start_time", System.currentTimeMillis())
                    .putString("session_name", "Quick Session")
                    .putString("category_name", catName)
                    .putString("category_type", catType)
                    .apply()

                val db = (context.applicationContext as SoulTrackApp).database
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    val session = SessionEntity(
                        title = "Quick Session",
                        categoryId = null,
                        type = when (catType) {
                            "LEARNING" -> "LEARNING"
                            "EXERCISE" -> "EXERCISE"
                            "SOCIAL" -> "SOCIAL"
                            "WASTED" -> "WASTED"
                            else -> "ACTIVITY"
                        },
                        startTime = System.currentTimeMillis(),
                        isActive = true,
                        source = "WIDGET",
                        timezoneId = java.time.ZoneId.systemDefault().id
                    )
                    db.sessionDao().insert(session)
                }

                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    val quote = QuotesApi.getRandomQuote()
                    val quotePrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                    quotePrefs.edit()
                        .putString("widget_quote", quote.text)
                        .putString("widget_quote_author", quote.author)
                        .apply()

                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val componentName = android.content.ComponentName(context, TimerWidgetProvider::class.java)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                    for (appWidgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                }

                val updateIntent = Intent(context, TimerWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE
                }
                context.sendBroadcast(updateIntent)
            }

            ACTION_STOP -> {
                val stopIntent = Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_STOP
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(stopIntent)
                } else {
                    context.startService(stopIntent)
                }

                val db = (context.applicationContext as SoulTrackApp).database
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    db.sessionDao().deactivateAllSessions()
                }

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
                    .putString("category_name", "")
                    .putString("category_type", "")
                    .apply()

                val updateIntent = Intent(context, TimerWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE
                }
                context.sendBroadcast(updateIntent)
            }

            ACTION_CYCLE_CATEGORY -> {
                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                var index = prefs.getInt("category_index", 0)
                index = (index + 1) % widgetCategories.size
                prefs.edit().putInt("category_index", index).apply()

                val updateIntent = Intent(context, TimerWidgetProvider::class.java).apply {
                    action = ACTION_UPDATE
                }
                context.sendBroadcast(updateIntent)
            }

            ACTION_NEXT_QUOTE -> {
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    val quote = QuotesApi.getRandomQuote()
                    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("widget_quote", quote.text)
                        .putString("widget_quote_author", quote.author)
                        .apply()

                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val componentName = android.content.ComponentName(context, TimerWidgetProvider::class.java)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                    for (appWidgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                }
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
        val categoryName = prefs.getString("category_name", "") ?: ""
        val categoryIndex = prefs.getInt("category_index", 0)
        val quoteText = prefs.getString("widget_quote", "") ?: ""
        val quoteAuthor = prefs.getString("widget_quote_author", "") ?: ""

        views.setTextViewText(R.id.widget_session_name, sessionName)
        views.setTextViewText(R.id.widget_category, widgetCategories[categoryIndex].first)

        if (quoteText.isNotBlank()) {
            views.setTextViewText(R.id.widget_quote, "\"$quoteText\"")
            views.setTextViewText(R.id.widget_quote_author, "— $quoteAuthor")
            views.setViewVisibility(R.id.widget_quote, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.widget_quote_author, android.view.View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_quote, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_quote_author, android.view.View.GONE)
        }

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
            views.setViewVisibility(R.id.widget_category, android.view.View.GONE)
        } else {
            views.setTextViewText(R.id.widget_timer, "00:00:00")
            views.setViewVisibility(R.id.widget_btn_start, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.widget_btn_stop, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_btn_reset, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_category, android.view.View.VISIBLE)
        }

        val categoryClickIntent = Intent(context, TimerWidgetProvider::class.java).apply {
            action = ACTION_CYCLE_CATEGORY
        }
        val categoryPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 10, categoryClickIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_category, categoryPendingIntent)

        val quoteClickIntent = Intent(context, TimerWidgetProvider::class.java).apply {
            action = ACTION_NEXT_QUOTE
        }
        val quotePendingIntent = android.app.PendingIntent.getBroadcast(
            context, 11, quoteClickIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_quote, quotePendingIntent)

        val startIntent = Intent(context, TimerWidgetProvider::class.java).apply {
            action = ACTION_START
        }
        val startPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 12, startIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_start, startPendingIntent)

        val stopIntent = Intent(context, TimerWidgetProvider::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 13, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_stop, stopPendingIntent)

        val resetIntent = Intent(context, TimerWidgetProvider::class.java).apply {
            action = ACTION_RESET
        }
        val resetPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 14, resetIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_reset, resetPendingIntent)

        val rootClickIntent = Intent(context, TimerWidgetProvider::class.java).apply {
            action = ACTION_UPDATE
        }
        val rootPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 20, rootClickIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, rootPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
