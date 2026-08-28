package io.github.dailytrack.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
        SavedQuoteEntity::class
    ],
    version = 2,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soultrack.db"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
