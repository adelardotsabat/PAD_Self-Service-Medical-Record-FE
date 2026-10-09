package com.example.selfmedicalrecord.data.health

import com.example.selfmedicalrecord.utils.DayUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Penyimpanan di memori untuk pengembangan UI tanpa backend. Data hilang saat proses aplikasi mati.
 *
 * @param seedDemoData isi 7 hari terakhir dengan data contoh supaya grafik Beranda terlihat.
 *                     Set `false` untuk melihat tampilan kosong (pengguna baru).
 */
class FakeHealthRepository(seedDemoData: Boolean = true) : HealthRepository {

    private val _bodyRecords = MutableStateFlow<List<BodyRecord>>(emptyList())
    override val bodyRecords: StateFlow<List<BodyRecord>> = _bodyRecords.asStateFlow()

    private val _bloodPressureRecords = MutableStateFlow<List<BloodPressureRecord>>(emptyList())
    override val bloodPressureRecords: StateFlow<List<BloodPressureRecord>> =
        _bloodPressureRecords.asStateFlow()

    init {
        if (seedDemoData) seed()
    }

    override suspend fun addBodyRecord(record: BodyRecord): Result<Unit> {
        delay(FAKE_LATENCY_MS)
        // sortedBy stabil, jadi urutan input untuk tanggal yang sama tetap terjaga.
        _bodyRecords.update { list -> (list + record).sortedBy { it.epochDay } }
        return Result.success(Unit)
    }

    override suspend fun addBloodPressureRecord(record: BloodPressureRecord): Result<Unit> {
        delay(FAKE_LATENCY_MS)
        _bloodPressureRecords.update { list -> (list + record).sortedBy { it.epochDay } }
        return Result.success(Unit)
    }

    private fun seed() {
        val today = DayUtils.todayEpochDay()
        val weights = listOf(46.6f, 47.0f, 46.8f, 47.6f, 47.4f, 48.0f, 47.6f)
        val systolic = listOf(110, 130, 118, 135, 142, 120, 128)
        val diastolic = listOf(75, 80, 78, 88, 85, 75, 80)

        _bodyRecords.value = weights.mapIndexed { i, weight ->
            BodyRecord(epochDay = today - (weights.lastIndex - i), weightKg = weight, heightCm = 160f)
        }
        _bloodPressureRecords.value = systolic.mapIndexed { i, sys ->
            BloodPressureRecord(
                epochDay = today - (systolic.lastIndex - i),
                systolic = sys,
                diastolic = diastolic[i]
            )
        }
    }

    private companion object {
        const val FAKE_LATENCY_MS = 500L
    }
}
