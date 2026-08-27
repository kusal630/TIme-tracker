package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val age: Int = 0,
    val sex: String = "",
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val activityLevel: String = "MODERATE",
    val dietaryPreferences: String = "",
    val allergies: String = "",
    val medicalConditions: String = "",
    val medications: String = "",
    val isPregnant: Boolean = false,
    val isNursing: Boolean = false,
    val hasFluidRestriction: Boolean = false,
    val doctorApprovedLimits: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
