package com.example.selfmedicalrecord.data.food

/** Perkiraan kalori satu jenis makanan di foto, berupa rentang (kkal). */
data class FoodItemEstimate(
    val name: String,
    val minKcal: Int,
    val maxKcal: Int
)

/**
 * Hasil estimasi kalori dari satu foto makanan.
 * Total disimpan terpisah (bukan dijumlahkan dari [items]) karena model AI nantinya
 * memberi rentang total sendiri, dan menjumlahkan rentang per item tidak selalu sama.
 */
data class CalorieEstimate(
    val totalMinKcal: Int,
    val totalMaxKcal: Int,
    val accuracyPercent: Int,
    val items: List<FoodItemEstimate>
)
