package com.example.selfmedicalrecord.data.health

/**
 * Hari disimpan sebagai epoch day (jumlah hari sejak 1970-01-01, tanpa zona waktu) supaya
 * tanggal yang dipilih pengguna tidak bergeser karena zona waktu. Lihat [com.example.selfmedicalrecord.utils.DayUtils].
 */
data class BodyRecord(
    val epochDay: Long,
    val weightKg: Float,
    val heightCm: Float
)

data class BloodPressureRecord(
    val epochDay: Long,
    val systolic: Int,
    val diastolic: Int
)
