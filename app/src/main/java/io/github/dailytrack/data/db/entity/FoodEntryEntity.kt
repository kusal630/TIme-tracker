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

@Entity(tableName = "food_entries")
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val mealType: String = "SNACK",
    val foodName: String,
    val portionQuantity: Double = 1.0,
    val portionUnit: String = "serving",
    val notes: String = "",
    val photoUri: String? = null,
    val barcode: String? = null,
    val source: String = "MANUAL",
    val caloriesKcal: Double = 0.0,
    val proteinG: Double = 0.0,
    val carbohydrateG: Double = 0.0,
    val fatG: Double = 0.0,
    val saturatedFatG: Double = 0.0,
    val sugarG: Double = 0.0,
    val addedSugarG: Double = 0.0,
    val fiberG: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val caffeineMg: Double = 0.0,
    val alcoholG: Double = 0.0,
    val ironMg: Double = 0.0,
    val calciumMg: Double = 0.0,
    val magnesiumMg: Double = 0.0,
    val potassiumMg: Double = 0.0,
    val vitaminDMcg: Double = 0.0,
    val vitaminB12Mcg: Double = 0.0,
    val folateMcg: Double = 0.0,
    val vitaminCMg: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
