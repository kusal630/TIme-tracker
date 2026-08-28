package io.github.dailytrack.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.dailytrack.R
import io.github.dailytrack.data.api.QuotesApi
import io.github.dailytrack.SoulTrackApp
import kotlinx.coroutines.*

class QuoteWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_NEXT_QUOTE = "io.github.dailytrack.ACTION_QUOTE_WIDGET_NEXT"
        const val ACTION_SAVE_QUOTE = "io.github.dailytrack.ACTION_QUOTE_WIDGET_SAVE"
        const val ACTION_UPDATE = "io.github.dailytrack.ACTION_QUOTE_WIDGET_UPDATE"

        fun updateWidgets(context: Context) {
            val intent = Intent(context, QuoteWidgetProvider::class.java).apply {
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
            ACTION_NEXT_QUOTE -> {
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    val quote = QuotesApi.getRandomQuote()
                    val prefs = context.getSharedPreferences("quote_widget_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("quote_text", quote.text)
                        .putString("quote_author", quote.author)
                        .apply()

                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val componentName = android.content.ComponentName(context, QuoteWidgetProvider::class.java)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                    for (appWidgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                }
            }

            ACTION_SAVE_QUOTE -> {
                val prefs = context.getSharedPreferences("quote_widget_prefs", Context.MODE_PRIVATE)
                val quoteText = prefs.getString("quote_text", "") ?: ""
                val quoteAuthor = prefs.getString("quote_author", "") ?: ""

                if (quoteText.isNotBlank()) {
                    val db = (context.applicationContext as SoulTrackApp).database
                    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                        val existing = db.savedQuoteDao().getQuoteByText(quoteText)
                        if (existing == null) {
                            db.savedQuoteDao().insert(
                                io.github.dailytrack.data.db.entity.SavedQuoteEntity(
                                    text = quoteText,
                                    author = quoteAuthor
                                )
                            )
                        }
                    }
                }
            }

            ACTION_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = android.content.ComponentName(context, QuoteWidgetProvider::class.java)
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
        val views = RemoteViews(context.packageName, R.layout.widget_quote)

        val prefs = context.getSharedPreferences("quote_widget_prefs", Context.MODE_PRIVATE)
        var quoteText = prefs.getString("quote_text", "") ?: ""
        var quoteAuthor = prefs.getString("quote_author", "") ?: ""

        if (quoteText.isBlank()) {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                val quote = QuotesApi.getRandomQuote()
                prefs.edit()
                    .putString("quote_text", quote.text)
                    .putString("quote_author", quote.author)
                    .apply()

                val appWidgetManager2 = AppWidgetManager.getInstance(context)
                val componentName = android.content.ComponentName(context, QuoteWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager2.getAppWidgetIds(componentName)
                for (appWidgetId2 in appWidgetIds) {
                    val views2 = RemoteViews(context.packageName, R.layout.widget_quote)
                    views2.setTextViewText(R.id.widget_quote_text, "\"${quote.text}\"")
                    views2.setTextViewText(R.id.widget_quote_author, "— ${quote.author}")
                    appWidgetManager2.updateAppWidget(appWidgetId2, views2)
                }
            }
            quoteText = "Loading inspiration..."
            quoteAuthor = ""
        }

        views.setTextViewText(R.id.widget_quote_text, "\"$quoteText\"")
        views.setTextViewText(R.id.widget_quote_author, if (quoteAuthor.isNotBlank()) "— $quoteAuthor" else "")

        val nextIntent = Intent(context, QuoteWidgetProvider::class.java).apply {
            action = ACTION_NEXT_QUOTE
        }
        val nextPendingIntent = android.app.PendingIntent.getBroadcast(
            context, 0, nextIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

        val saveIntent = Intent(context, QuoteWidgetProvider::class.java).apply {
            action = ACTION_SAVE_QUOTE
        }
        val savePendingIntent = android.app.PendingIntent.getBroadcast(
            context, 1, saveIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_save, savePendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
