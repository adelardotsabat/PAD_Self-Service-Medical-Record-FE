package com.example.selfmedicalrecord.data.auth

/**
 * Kontrak untuk autentikasi dan pendaftaran.
 *
 * Saat ini hanya diimplementasikan oleh [FakeAuthRepository]. Ketika API Laravel siap,
 * buat implementasi baru (Retrofit) dan ganti yang dipakai ViewModel; UI tidak perlu diubah.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>

    /** Langkah 1 register: membuat akun dengan email dan kata sandi. */
    suspend fun register(email: String, password: String): Result<Unit>

    /** Langkah 2 register: melengkapi data diri pasien. */
    suspend fun saveProfile(profile: PatientProfile): Result<Unit>
}

data class PatientProfile(
    val fullName: String,
    val nik: String,
    /** Null jika pengguna tidak memilih. */
    val gender: String?,
    /** Tanggal lahir sebagai epoch millis (UTC, tengah malam), apa adanya dari date picker. */
    val birthDateMillis: Long
)
