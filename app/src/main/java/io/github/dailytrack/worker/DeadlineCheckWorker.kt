package io.github.dailytrack.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import io.github.dailytrack.MainActivity
import io.github.dailytrack.SoulTrackApp
import java.util.concurrent.TimeUnit

class DeadlineCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "soultrack_deadlines"
        const val WORK_NAME = "deadline_check"
        
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<DeadlineCheckWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        val db = (applicationContext as SoulTrackApp).database
        val todoDao = db.todoDao()
        
        val now = System.currentTimeMillis()
        val upcomingDeadline = now + 24 * 60 * 60 * 1000
        
        val upcomingTodos = todoDao.getTodosWithDeadlineApproaching(now, upcomingDeadline)
        
        if (upcomingTodos.isNotEmpty()) {
            createNotificationChannel()
            
            for (todo in upcomingTodos) {
                val hoursUntilDeadline = ((todo.deadline!! - now) / (60 * 60 * 1000)).toInt()
                
                sendNotification(
                    todoId = todo.id.toInt(),
                    title = "Task Deadline Approaching",
                    message = "\"${todo.title}\" is due in $hoursUntilDeadline hours"
                )
            }
        }
        
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Deadlines",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for approaching task deadlines"
            }
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(todoId: Int, title: String, message: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TODO_ID", todoId.toLong())
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            todoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
        notificationManager.notify(todoId, notification)
    }
}
