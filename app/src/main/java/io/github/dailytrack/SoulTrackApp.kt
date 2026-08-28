package io.github.dailytrack

import android.app.Application
import io.github.dailytrack.data.db.AppDatabase
import io.github.dailytrack.worker.DeadlineCheckWorker

class SoulTrackApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        DeadlineCheckWorker.schedule(this)
    }
}
