package io.github.dailytrack

import android.app.Application
import io.github.dailytrack.data.db.AppDatabase

class DailyTrackApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
