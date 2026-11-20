package com.example.selfmedicalrecord.data.food

import android.net.Uri

/** Sumber estimasi kalori. UI hanya tahu interface ini, bukan dari mana hasilnya. */
interface FoodCalorieRepository {
    suspend fun estimate(imageUri: Uri): Result<CalorieEstimate>
}
