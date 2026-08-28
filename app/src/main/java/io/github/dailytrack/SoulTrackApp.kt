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


package io.github.dailytrack

import android.app.Application
import io.github.dailytrack.data.api.QuotesApi
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
        QuotesApi.init(this)
        DeadlineCheckWorker.schedule(this)
    }
}
