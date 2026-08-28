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
