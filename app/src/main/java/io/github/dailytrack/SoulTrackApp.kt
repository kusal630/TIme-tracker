package io.github.dailytrack

import android.app.Application
import io.github.dailytrack.data.db.AppDatabase
import io.github.dailytrack.sync.SyncManager
import io.github.dailytrack.sync.SyncRepository
import io.github.dailytrack.worker.DeadlineCheckWorker

class SoulTrackApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val syncRepository: SyncRepository by lazy {
        SyncRepository(this, database)
    }

    val syncManager: SyncManager by lazy {
        SyncManager(this, syncRepository)
    }

    override fun onCreate() {
        super.onCreate()
        DeadlineCheckWorker.schedule(this)
    }
}
