package com.example.selfmedicalrecord.data.record

/**
 * Satu catatan pelayanan kesehatan.
 * Berasal dari dua sumber: catatan klinik (OpenMRS, hanya bisa dibaca) dan catatan mandiri
 * yang dibuat pengguna sendiri (bisa diubah).
 *
 * @param visitDateMillis tanggal kunjungan, epoch milidetik UTC.
 * @param diagnosis hanya ada di catatan klinik; null untuk catatan mandiri.
 * @param medications teks bebas, mis. "Amlodipin, Vitamin B Complex"; kosong jika tidak ada.
 * @param personalId id catatan mandiri di backend; null untuk catatan klinik.
 */
data class MedicalRecord(
    val id: String,
    val facilityName: String,
    val visitDateMillis: Long,
    val diagnosis: String?,
    val complaint: String,
    val medications: String,
    val personalId: Long? = null
) {
    /** Hanya catatan mandiri yang boleh diubah pengguna. */
    val isEditable: Boolean get() = personalId != null
}

/** Isian form Input Riwayat Kesehatan. */
data class MedicalRecordDraft(
    val visitDateMillis: Long,
    val facilityName: String,
    val complaint: String,
    val medications: String
)
