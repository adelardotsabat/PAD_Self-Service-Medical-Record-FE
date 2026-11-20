package com.example.selfmedicalrecord.data.food

import android.net.Uri
import kotlinx.coroutines.delay

/**
 * Hasil contoh dari desain, tidak membaca foto sama sekali. Jeda kecil meniru proses analisis.
 * Ganti dengan implementasi AI nanti; cukup ubah satu baris di ServiceLocator.
 */
class FakeFoodCalorieRepository : FoodCalorieRepository {

    override suspend fun estimate(imageUri: Uri): Result<CalorieEstimate> {
        delay(600)
        return Result.success(
            CalorieEstimate(
                totalMinKcal = 490,
                totalMaxKcal = 595,
                accuracyPercent = 98,
                items = listOf(
                    FoodItemEstimate("Ikan Salmon Panggang", 220, 250),
                    FoodItemEstimate("Nasi", 120, 150),
                    FoodItemEstimate("Alpukat", 60, 80),
                    FoodItemEstimate("Jagung Manis", 30, 40),
                    FoodItemEstimate("Saus/Dressing", 30, 60),
                    FoodItemEstimate("Tomat Ceri", 10, 15)
                )
            )
        )
    }
}
