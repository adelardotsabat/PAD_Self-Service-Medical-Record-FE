package com.example.selfmedicalrecord.data.health

import com.example.selfmedicalrecord.data.remote.ApiService
import com.example.selfmedicalrecord.data.remote.CreateHealthRecordRequest
import com.example.selfmedicalrecord.data.remote.HealthRecordDto
import com.example.selfmedicalrecord.data.remote.parseApiDate
import com.example.selfmedicalrecord.data.remote.safeApiCall
import com.example.selfmedicalrecord.utils.DayUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Data kesehatan dari backend (GET dan POST /health-records).
 *
 * Catatan:
 * - Data dimuat sampai [MAX_PAGES] halaman terbaru (maks sekitar 500 pengukuran).
 * - Backend menyimpan satu baris per pengukuran: BMI mengisi tinggi dan berat, tensi mengisi
 *   sistolik dan diastolik. Kategori BMI dan peringatan tensi versi server tidak dipakai; app
 *   tetap menghitung label sendiri (lihat TODO(tim) di HealthCalculators).
 * - Tanggal pengukuran dibaca dari `recorded_date` (zona Asia/Jakarta). Perangkat di zona waktu
 *   lain bisa melihat selisih sehari di sekitar tengah malam.
 */
class RemoteHealthRepository(private val api: ApiService) : HealthRepository {

    private val _bodyRecords = MutableStateFlow<List<BodyRecord>>(emptyList())
    override val bodyRecords: StateFlow<List<BodyRecord>> = _bodyRecords.asStateFlow()

    private val _bloodPressureRecords = MutableStateFlow<List<BloodPressureRecord>>(emptyList())
    override val bloodPressureRecords: StateFlow<List<BloodPressureRecord>> =
        _bloodPressureRecords.asStateFlow()

    override suspend fun refresh(): Result<Unit> {
        val all = mutableListOf<HealthRecordDto>()
        var page = 1
        while (page <= MAX_PAGES) {
            val envelope = safeApiCall { api.healthRecords(PAGE_SIZE, page) }
                .getOrElse { return Result.failure(it) }
            val pageData = envelope.data ?: break
            all += pageData.data.orEmpty()
            if (pageData.currentPage >= pageData.lastPage) break
            page++
        }

        // Server mengirim terbaru dulu; app butuh terlama dulu (urutan input tetap terjaga).
        val ascending = all.asReversed()
        _bodyRecords.value = ascending.mapNotNull { dto ->
            val day = epochDayOf(dto) ?: return@mapNotNull null
            val height = dto.height ?: return@mapNotNull null
            val weight = dto.weight ?: return@mapNotNull null
            BodyRecord(day, weight, height)
        }.sortedBy { it.epochDay }
        _bloodPressureRecords.value = ascending.mapNotNull { dto ->
            val day = epochDayOf(dto) ?: return@mapNotNull null
            val systolic = dto.systolic ?: return@mapNotNull null
            val diastolic = dto.diastolic ?: return@mapNotNull null
            BloodPressureRecord(day, systolic, diastolic)
        }.sortedBy { it.epochDay }
        return Result.success(Unit)
    }

    override suspend fun addBodyRecord(record: BodyRecord): Result<Unit> {
        val body = CreateHealthRecordRequest(
            height = record.heightCm,
            weight = record.weightKg,
            recordedAt = recordedAtFor(record.epochDay)
        )
        return safeApiCall { api.addHealthRecord(body) }.map {
            _bodyRecords.update { list -> (list + record).sortedBy { it.epochDay } }
        }
    }

    override suspend fun addBloodPressureRecord(record: BloodPressureRecord): Result<Unit> {
        val body = CreateHealthRecordRequest(
            systolic = record.systolic,
            diastolic = record.diastolic,
            recordedAt = recordedAtFor(record.epochDay)
        )
        return safeApiCall { api.addHealthRecord(body) }.map {
            _bloodPressureRecords.update { list -> (list + record).sortedBy { it.epochDay } }
        }
    }

    override fun clear() {
        _bodyRecords.value = emptyList()
        _bloodPressureRecords.value = emptyList()
    }

    private fun epochDayOf(dto: HealthRecordDto): Long? =
        parseApiDate(dto.recordedDate)?.let { DayUtils.fromUtcMillis(it) }

    /**
     * Waktu pengukuran untuk dikirim ke server (ISO-8601 UTC).
     * - Hari ini: waktu sekarang dikurangi semenit, karena server menolak waktu di masa depan
     *   dan jam perangkat bisa sedikit lebih cepat.
     * - Hari lain: pukul 12.00 WIB di tanggal itu, supaya tanggalnya tidak bergeser.
     */
    private fun recordedAtFor(epochDay: Long): String {
        val millis = if (epochDay == DayUtils.todayEpochDay()) {
            System.currentTimeMillis() - ONE_MINUTE_MS
        } else {
            epochDay * DayUtils.DAY_MS + NOON_WIB_IN_UTC_MS
        }
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millis))
    }

    private companion object {
        const val PAGE_SIZE = 100
        const val MAX_PAGES = 5
        const val ONE_MINUTE_MS = 60_000L

        /** 12.00 WIB (UTC+7) = 05.00 UTC, dihitung dari tengah malam UTC tanggal yang sama. */
        const val NOON_WIB_IN_UTC_MS = 5 * 3_600_000L
    }
}
