package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.CategoryDao
import io.github.dailytrack.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getActiveCategories(): Flow<List<CategoryEntity>> = categoryDao.getActiveCategories()
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> = categoryDao.getCategoriesByType(type)
    suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)
    suspend fun insert(category: CategoryEntity): Long = categoryDao.insert(category)
    suspend fun update(category: CategoryEntity) = categoryDao.update(category)
    suspend fun archive(id: Long) = categoryDao.archive(id)
    suspend fun delete(category: CategoryEntity) = categoryDao.delete(category)
    suspend fun count(): Int = categoryDao.count()

    suspend fun initializeDefaults() {
        if (categoryDao.count() == 0) {
            val defaults = listOf(
                CategoryEntity(name = "Deep Work", type = "PRODUCTIVE", growthContribution = "HIGH", color = 0xFF1B5E20, icon = "work"),
                CategoryEntity(name = "Study/Learning", type = "LEARNING", growthContribution = "HIGH", color = 0xFF0D47A1, icon = "school"),
                CategoryEntity(name = "Skill Practice", type = "LEARNING", growthContribution = "HIGH", color = 0xFF1565C0, icon = "fitness"),
                CategoryEntity(name = "Planning", type = "PRODUCTIVE", growthContribution = "MEDIUM", color = 0xFF2E7D32, icon = "plan"),
                CategoryEntity(name = "Chores/Errands", type = "PRODUCTIVE", growthContribution = "LOW", color = 0xFF388E3C, icon = "chores"),
                CategoryEntity(name = "Creative Work", type = "PRODUCTIVE", growthContribution = "HIGH", color = 0xFF43A047, icon = "creative"),
                CategoryEntity(name = "Reading", type = "LEARNING", growthContribution = "HIGH", color = 0xFF1976D2, icon = "book"),
                CategoryEntity(name = "Course/Lesson", type = "LEARNING", growthContribution = "HIGH", color = 0xFF1E88E5, icon = "course"),
                CategoryEntity(name = "Research", type = "LEARNING", growthContribution = "HIGH", color = 0xFF2196F3, icon = "research"),
                CategoryEntity(name = "Language Practice", type = "LEARNING", growthContribution = "HIGH", color = 0xFF42A5F5, icon = "language"),
                CategoryEntity(name = "Music Practice", type = "LEARNING", growthContribution = "HIGH", color = 0xFF64B5F6, icon = "music"),
                CategoryEntity(name = "Running", type = "EXERCISE", growthContribution = "HIGH", color = 0xFFE65100, icon = "run"),
                CategoryEntity(name = "Walking", type = "EXERCISE", growthContribution = "MEDIUM", color = 0xFFF57C00, icon = "walk"),
                CategoryEntity(name = "Strength Training", type = "EXERCISE", growthContribution = "HIGH", color = 0xFFEF6C00, icon = "strength"),
                CategoryEntity(name = "Stretching/Mobility", type = "EXERCISE", growthContribution = "MEDIUM", color = 0xFFFF9800, icon = "stretch"),
                CategoryEntity(name = "Sports", type = "EXERCISE", growthContribution = "HIGH", color = 0xFFFFA726, icon = "sports"),
                CategoryEntity(name = "Meals", type = "NEUTRAL", growthContribution = "NONE", color = 0xFF795548, icon = "meal"),
                CategoryEntity(name = "Break", type = "NEUTRAL", growthContribution = "NONE", color = 0xFF9E9E9E, icon = "break"),
                CategoryEntity(name = "Personal Care", type = "NEUTRAL", growthContribution = "NONE", color = 0xFF757575, icon = "care"),
                CategoryEntity(name = "Commute", type = "NEUTRAL", growthContribution = "NONE", color = 0xFF607D8B, icon = "commute"),
                CategoryEntity(name = "Family/Responsibilities", type = "SOCIAL", growthContribution = "MEDIUM", color = 0xFFAD1457, icon = "family"),
                CategoryEntity(name = "Rest", type = "RECOVERY", growthContribution = "LOW", color = 0xFF4CAF50, icon = "rest"),
                CategoryEntity(name = "Meditation", type = "RECOVERY", growthContribution = "MEDIUM", color = 0xFF66BB6A, icon = "meditation"),
                CategoryEntity(name = "Relaxation", type = "RECOVERY", growthContribution = "LOW", color = 0xFF81C784, icon = "relax"),
                CategoryEntity(name = "Social Media Scrolling", type = "WASTED", growthContribution = "NONE", comfortRisk = "HIGH", color = 0xFFB71C1C, icon = "phone"),
                CategoryEntity(name = "Procrastination", type = "WASTED", growthContribution = "NONE", comfortRisk = "HIGH", color = 0xFFC62828, icon = "procrastinate"),
                CategoryEntity(name = "Excessive Entertainment", type = "WASTED", growthContribution = "NONE", comfortRisk = "MEDIUM", color = 0xFFD32F2F, icon = "tv"),
                CategoryEntity(name = "Aimless Browsing", type = "WASTED", growthContribution = "NONE", comfortRisk = "HIGH", color = 0xFFE53935, icon = "browse"),
                CategoryEntity(name = "Sleep", type = "SLEEP", growthContribution = "NONE", color = 0xFF311B92, icon = "sleep"),
                CategoryEntity(name = "Nap", type = "SLEEP", growthContribution = "NONE", color = 0xFF4527A0, icon = "nap"),
                CategoryEntity(name = "Family Time", type = "SOCIAL", growthContribution = "MEDIUM", color = 0xFF880E4F, icon = "familytime"),
                CategoryEntity(name = "Friends", type = "SOCIAL", growthContribution = "MEDIUM", color = 0xFFAD1457, icon = "friends"),
                CategoryEntity(name = "Community", type = "SOCIAL", growthContribution = "MEDIUM", color = 0xFFC2185B, icon = "community"),
            )
            defaults.forEach { categoryDao.insert(it) }
        }
    }
}
