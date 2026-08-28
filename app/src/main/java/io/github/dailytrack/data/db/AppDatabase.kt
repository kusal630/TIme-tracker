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


package io.github.dailytrack.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.dailytrack.data.db.dao.*
import io.github.dailytrack.data.db.entity.*

@Database(
    entities = [
        CategoryEntity::class,
        SessionEntity::class,
        TagEntity::class,
        SessionTagCrossRef::class,
        FoodEntryEntity::class,
        DrinkEntryEntity::class,
        BodyMetricEntity::class,
        SymptomEntryEntity::class,
        MoodCheckInEntity::class,
        JournalEntryEntity::class,
        LifeEventEntity::class,
        InsightEntity::class,
        UserProfileEntity::class,
        TodoEntity::class,
        PomodoroSessionEntity::class,
        SavedQuoteEntity::class,
        SubtaskEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun sessionDao(): SessionDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun drinkEntryDao(): DrinkEntryDao
    abstract fun bodyMetricDao(): BodyMetricDao
    abstract fun symptomEntryDao(): SymptomEntryDao
    abstract fun moodCheckInDao(): MoodCheckInDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun lifeEventDao(): LifeEventDao
    abstract fun insightDao(): InsightDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun todoDao(): TodoDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
    abstract fun savedQuoteDao(): SavedQuoteDao
    abstract fun subtaskDao(): SubtaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE todos ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS subtasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        todoId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soultrack.db"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
