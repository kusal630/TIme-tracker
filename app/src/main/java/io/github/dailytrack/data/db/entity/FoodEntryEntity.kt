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
