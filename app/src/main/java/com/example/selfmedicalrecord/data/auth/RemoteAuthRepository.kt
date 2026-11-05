package com.example.selfmedicalrecord.data.auth

import com.example.selfmedicalrecord.data.remote.ApiService
import com.example.selfmedicalrecord.data.remote.LoginRequest
import com.example.selfmedicalrecord.data.remote.RegisterRequest
import com.example.selfmedicalrecord.data.remote.ResendCodeRequest
import com.example.selfmedicalrecord.data.remote.TokenStore
import com.example.selfmedicalrecord.data.remote.UpdateProfileRequest
import com.example.selfmedicalrecord.data.remote.VerifyEmailRequest
import com.example.selfmedicalrecord.data.remote.safeApiCall
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Implementasi yang memanggil backend Laravel.
 *
 * Tersambung: login, logout, register, verifikasi email, kirim ulang kode, simpan data diri.
 * Masih diteruskan ke [fallback] (Fake): lupa sandi (3 langkah) dan ubah sandi.
 */
class RemoteAuthRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore,
    private val fallback: AuthRepository = FakeAuthRepository()
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> =
        safeApiCall { api.login(LoginRequest(email, password)) }.mapCatching { envelope ->
            val token = envelope.data?.token
            check(!token.isNullOrBlank()) { "Server tidak mengirim token" }
            tokenStore.save(token, envelope.data?.user?.role?.roleName, envelope.data?.user?.name)
        }

    override suspend fun logout(): Result<Unit> {
        // Beri tahu server (abaikan kegagalan), lalu selalu hapus token di perangkat.
        safeApiCall { api.logout() }
        tokenStore.clear()
        return Result.success(Unit)
    }

    override suspend fun register(email: String, password: String): Result<Unit> =
        safeApiCall { api.register(RegisterRequest(email, password)) }.map { }

    override suspend fun verifyEmail(email: String, code: String): Result<Unit> =
        safeApiCall { api.verifyEmail(VerifyEmailRequest(email, code)) }.map { }

    override suspend fun resendVerificationCode(email: String): Result<Unit> =
        safeApiCall { api.resendEmailCode(ResendCodeRequest(email)) }.map { }

    override suspend fun saveProfile(profile: PatientProfile): Result<Unit> {
        // Tanggal lahir dari date picker adalah tengah malam UTC, jadi format dengan UTC
        // agar tanggalnya tidak bergeser sehari.
        val birthDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(profile.birthDateMillis))
        val body = UpdateProfileRequest(
            name = profile.fullName,
            nik = profile.nik,
            gender = profile.gender,
            dateOfBirth = birthDate
        )
        return safeApiCall { api.updateProfile(body) }.map { }
    }

    override suspend fun requestPasswordReset(email: String) =
        fallback.requestPasswordReset(email)

    override suspend fun changePassword(currentPassword: String, newPassword: String) =
        fallback.changePassword(currentPassword, newPassword)
}
