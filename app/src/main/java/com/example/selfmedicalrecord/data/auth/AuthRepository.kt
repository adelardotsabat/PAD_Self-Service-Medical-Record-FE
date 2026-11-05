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

    /** Memverifikasi email dengan kode 6 digit yang dikirim server setelah [register]. */
    suspend fun verifyEmail(email: String, code: String): Result<Unit>

    /** Meminta kode verifikasi baru (server membatasi satu kali per 60 detik). */
    suspend fun resendVerificationCode(email: String): Result<Unit>

    /** Langkah terakhir register: melengkapi data diri pasien (pengguna sudah login). */
    suspend fun saveProfile(profile: PatientProfile): Result<Unit>

    /** Meminta tautan pengaturan ulang kata sandi dikirim ke [email]. */
    suspend fun requestPasswordReset(email: String): Result<Unit>

    /** Mengganti kata sandi saat sudah masuk. Gagal jika [currentPassword] salah. */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    /** Keluar dari akun. Kelak: hapus token tersimpan dan beri tahu server. */
    suspend fun logout(): Result<Unit>
}

data class PatientProfile(
    val fullName: String,
    val nik: String,
    /** "MALE" atau "FEMALE"; null jika pengguna tidak memilih. */
    val gender: String?,
    /** Tanggal lahir sebagai epoch millis (UTC, tengah malam), apa adanya dari date picker. */
    val birthDateMillis: Long
)
