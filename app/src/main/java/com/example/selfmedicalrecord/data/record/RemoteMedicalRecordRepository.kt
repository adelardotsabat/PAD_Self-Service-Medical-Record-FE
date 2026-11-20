package com.example.selfmedicalrecord.data.record

import com.example.selfmedicalrecord.data.remote.ApiService
import com.example.selfmedicalrecord.data.remote.ClinicEncounterDto
import com.example.selfmedicalrecord.data.remote.PersonalRecordDto
import com.example.selfmedicalrecord.data.remote.PersonalRecordRequest
import com.example.selfmedicalrecord.data.remote.formatApiDate
import com.example.selfmedicalrecord.data.remote.parseApiDate
import com.example.selfmedicalrecord.data.remote.safeApiCall

/**
 * Riwayat dari backend. Menggabungkan dua sumber:
 * - Catatan mandiri (GET/POST/PUT /personal-medical-records): dibuat pengguna, bisa diubah.
 * - Catatan klinik (GET /medical-records, dari OpenMRS): hanya dibaca. Bersifat tambahan: jika
 *   gagal (layanan klinik belum dikonfigurasi, NIK belum terdaftar, dsb.), daftar tetap tampil
 *   dari catatan mandiri saja.
 *
 * Yang dimuat maksimal [PAGE_SIZE] catatan terbaru per sumber.
 */
class RemoteMedicalRecordRepository(private val api: ApiService) : MedicalRecordRepository {

    override suspend fun getRecords(): Result<List<MedicalRecord>> {
        val personal = safeApiCall { api.personalRecords(PAGE_SIZE, 1) }
            .getOrElse { return Result.failure(it) }
            .data?.data.orEmpty()
            .mapNotNull { it.toDomain() }

        val clinic = safeApiCall { api.clinicRecords(1, PAGE_SIZE) }
            .getOrNull()?.data?.data.orEmpty()
            .mapIndexedNotNull { index, dto -> dto.toDomain(index) }

        return Result.success((personal + clinic).sortedByDescending { it.visitDateMillis })
    }

    override suspend fun addRecord(draft: MedicalRecordDraft): Result<Unit> =
        safeApiCall { api.addPersonalRecord(draft.toRequest()) }.map { }

    override suspend fun updateRecord(personalId: Long, draft: MedicalRecordDraft): Result<Unit> =
        safeApiCall { api.updatePersonalRecord(personalId, draft.toRequest()) }.map { }

    private fun MedicalRecordDraft.toRequest() = PersonalRecordRequest(
        visitDate = formatApiDate(visitDateMillis),
        facilityName = facilityName,
        complaint = complaint,
        // Selalu dikirim (boleh kosong) supaya obat bisa dihapus saat mengubah catatan.
        medications = medications
    )

    private fun PersonalRecordDto.toDomain(): MedicalRecord? {
        val date = parseApiDate(visitDate) ?: return null
        return MedicalRecord(
            id = "p$id",
            facilityName = facilityName.orEmpty(),
            visitDateMillis = date,
            diagnosis = null,
            complaint = complaint.orEmpty(),
            medications = medications.orEmpty(),
            personalId = id
        )
    }

    private fun ClinicEncounterDto.toDomain(index: Int): MedicalRecord? {
        val date = parseApiDate(visitDate) ?: return null
        return MedicalRecord(
            id = "c${encounterUuid ?: "$index-$visitDate"}",
            facilityName = facilityName.orEmpty(),
            visitDateMillis = date,
            diagnosis = diagnosis,
            complaint = complaint.orEmpty(),
            medications = medications.orEmpty().mapNotNull { it.drug?.takeIf { d -> d.isNotBlank() } }
                .joinToString(", ")
        )
    }

    private companion object {
        const val PAGE_SIZE = 100
    }
}
