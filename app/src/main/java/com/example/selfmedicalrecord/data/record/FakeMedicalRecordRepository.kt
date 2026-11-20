package com.example.selfmedicalrecord.data.record

import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.TimeZone

/** Data contoh dari desain, disimpan di memori, dengan jeda kecil untuk meniru jaringan. */
class FakeMedicalRecordRepository(seedDemoData: Boolean = true) : MedicalRecordRepository {

    private val records = mutableListOf<MedicalRecord>()
    private var nextPersonalId = 1L

    init {
        if (seedDemoData) {
            records += listOf(
                MedicalRecord(
                    id = "c1",
                    facilityName = "Klinik Panasea",
                    visitDateMillis = utcMillis(2026, Calendar.SEPTEMBER, 20),
                    diagnosis = "Hipertensi Ringan",
                    complaint = "Pusing berputar, leher bagian belakang terasa kaku dan tegang, insomnia beberapa malam",
                    medications = "Amlodipin, Vitamin B Complex"
                ),
                MedicalRecord(
                    id = "c2",
                    facilityName = "Klinik Husada",
                    visitDateMillis = utcMillis(2026, Calendar.SEPTEMBER, 11),
                    diagnosis = "Maag",
                    complaint = "Nyeri ulu hati terasa melilit, mual, nafsu makan menurun",
                    medications = "Omeprazole"
                ),
                MedicalRecord(
                    id = "c3",
                    facilityName = "Medical Center",
                    visitDateMillis = utcMillis(2026, Calendar.SEPTEMBER, 11),
                    diagnosis = "Alergi",
                    complaint = "Gatal-gatal merah dan bentol di lengan dan leher setelah mengonsumsi makanan laut",
                    medications = "Cetirizine, Salep"
                )
            )
        }
    }

    override suspend fun getRecords(): Result<List<MedicalRecord>> {
        delay(600)
        return Result.success(records.toList())
    }

    override suspend fun addRecord(draft: MedicalRecordDraft): Result<Unit> {
        delay(500)
        val id = nextPersonalId++
        records += MedicalRecord(
            id = "p$id",
            facilityName = draft.facilityName,
            visitDateMillis = draft.visitDateMillis,
            diagnosis = null,
            complaint = draft.complaint,
            medications = draft.medications,
            personalId = id
        )
        return Result.success(Unit)
    }

    override suspend fun updateRecord(personalId: Long, draft: MedicalRecordDraft): Result<Unit> {
        delay(500)
        val index = records.indexOfFirst { it.personalId == personalId }
        if (index < 0) return Result.failure(IllegalStateException("Catatan tidak ditemukan"))
        records[index] = records[index].copy(
            facilityName = draft.facilityName,
            visitDateMillis = draft.visitDateMillis,
            complaint = draft.complaint,
            medications = draft.medications
        )
        return Result.success(Unit)
    }

    private fun utcMillis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day)
        }.timeInMillis
}
