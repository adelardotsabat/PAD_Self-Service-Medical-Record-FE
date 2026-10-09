package com.example.selfmedicalrecord.data.health

import kotlinx.coroutines.flow.StateFlow

/**
 * Sumber data kesehatan yang dibagi oleh Beranda (membaca) dan Input Data (menulis).
 *
 * Daftar selalu terurut menaik berdasarkan tanggal; untuk tanggal yang sama, urutan
 * mengikuti waktu input (elemen terakhir = input terbaru). Saat API Laravel/OpenMRS siap,
 * buat implementasi baru dan ganti di [com.example.selfmedicalrecord.data.ServiceLocator].
 */
interface HealthRepository {
    val bodyRecords: StateFlow<List<BodyRecord>>
    val bloodPressureRecords: StateFlow<List<BloodPressureRecord>>

    suspend fun addBodyRecord(record: BodyRecord): Result<Unit>
    suspend fun addBloodPressureRecord(record: BloodPressureRecord): Result<Unit>
}
