package com.example.selfmedicalrecord.data.record

/** Sumber data riwayat kesehatan. UI hanya tahu interface ini, bukan dari mana datanya. */
interface MedicalRecordRepository {
    suspend fun getRecords(): Result<List<MedicalRecord>>

    /** Menambah catatan mandiri baru. */
    suspend fun addRecord(draft: MedicalRecordDraft): Result<Unit>

    /** Mengubah catatan mandiri dengan [personalId] tertentu. */
    suspend fun updateRecord(personalId: Long, draft: MedicalRecordDraft): Result<Unit>
}
