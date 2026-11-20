package com.example.selfmedicalrecord.data.auth

import com.example.selfmedicalrecord.data.remote.ApiService
import com.example.selfmedicalrecord.data.remote.ForgotPasswordRequest
import com.example.selfmedicalrecord.data.remote.LoginRequest
import com.example.selfmedicalrecord.data.remote.RegisterRequest
import com.example.selfmedicalrecord.data.remote.ResendCodeRequest
import com.example.selfmedicalrecord.data.remote.ResetPasswordRequest
import com.example.selfmedicalrecord.data.remote.TokenStore
import com.example.selfmedicalrecord.data.remote.UpdateProfileRequest
import com.example.selfmedicalrecord.data.remote.VerifyEmailRequest
import com.example.selfmedicalrecord.data.remote.VerifyResetCodeRequest
import com.example.selfmedicalrecord.data.remote.formatApiDate
import com.example.selfmedicalrecord.data.remote.safeApiCall

/**
 * Implementasi yang memanggil backend Laravel.
 *
 * Tersambung: login, logout, register, verifikasi email, kirim ulang kode, simpan data diri.
 * Lupa sandi 3 langkah juga sudah tersambung. Masih diteruskan ke [fallback] (Fake): ubah sandi
 * saat sudah login (backend belum punya endpoint-nya).
 *
 * [onSessionChanged] dipanggil setelah login berhasil dan setelah keluar, supaya data profil
 * tersimpan milik akun sebelumnya dibersihkan.
 */
class RemoteAuthRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore,
    private val fallback: AuthRepository = FakeAuthRepository(),
    private val onSessionChanged: () -> Unit = {}
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> =
        safeApiCall { api.login(LoginRequest(email, password)) }.mapCatching { envelope ->
            val token = envelope.data?.token
            check(!token.isNullOrBlank()) { "Server tidak mengirim token" }
            val user = envelope.data?.user
            tokenStore.save(token, user?.role?.roleName, user?.name, user?.email)
            onSessionChanged()
        }

    override suspend fun logout(): Result<Unit> {
        // Beri tahu server (abaikan kegagalan), lalu selalu hapus token di perangkat.
        safeApiCall { api.logout() }
        tokenStore.clear()
        onSessionChanged()
        return Result.success(Unit)
    }

    override suspend fun register(email: String, password: String): Result<Unit> =
        safeApiCall { api.register(RegisterRequest(email, password)) }.map { }

    override suspend fun verifyEmail(email: String, code: String): Result<Unit> =
        safeApiCall { api.verifyEmail(VerifyEmailRequest(email, code)) }.map { }

    override suspend fun resendVerificationCode(email: String): Result<Unit> =
        safeApiCall { api.resendEmailCode(ResendCodeRequest(email)) }.map { }

    override suspend fun saveProfile(profile: PatientProfile): Result<Unit> {
        val birthDate = formatApiDate(profile.birthDateMillis)
        val body = UpdateProfileRequest(
            name = profile.fullName,
            nik = profile.nik,
            gender = profile.gender,
            dateOfBirth = birthDate
        )
        return safeApiCall { api.updateProfile(body) }.map { }
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> =
        safeApiCall { api.forgotPassword(ForgotPasswordRequest(email)) }.map { }

    override suspend fun verifyPasswordResetCode(email: String, code: String): Result<String> =
        safeApiCall { api.verifyResetCode(VerifyResetCodeRequest(email, code)) }.mapCatching { envelope ->
            val token = envelope.data?.resetToken
            check(!token.isNullOrBlank()) { "Server tidak mengirim token reset" }
            token
        }

    override suspend fun resetPassword(
        email: String,
        resetToken: String,
        newPassword: String
    ): Result<Unit> = safeApiCall {
        api.resetPassword(ResetPasswordRequest(email, resetToken, newPassword, newPassword))
    }.map { }

    override suspend fun changePassword(currentPassword: String, newPassword: String) =
        fallback.changePassword(currentPassword, newPassword)
}
