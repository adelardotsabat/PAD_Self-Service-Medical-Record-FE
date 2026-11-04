package com.example.selfmedicalrecord.data.auth

import com.example.selfmedicalrecord.data.remote.ApiService
import com.example.selfmedicalrecord.data.remote.LoginRequest
import com.example.selfmedicalrecord.data.remote.TokenStore
import com.example.selfmedicalrecord.data.remote.safeApiCall

/**
 * Implementasi yang memanggil backend Laravel.
 *
 * Tahap ini baru login dan logout yang sungguhan. Fungsi lain masih diteruskan ke [fallback]
 * (Fake) sampai tahap berikutnya (register + kode verifikasi, lupa sandi 3 langkah, ubah sandi).
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
            tokenStore.save(token)
        }

    override suspend fun logout(): Result<Unit> {
        // Beri tahu server (abaikan kegagalan), lalu selalu hapus token di perangkat.
        safeApiCall { api.logout() }
        tokenStore.clear()
        return Result.success(Unit)
    }

    override suspend fun register(email: String, password: String) =
        fallback.register(email, password)

    override suspend fun saveProfile(profile: PatientProfile) =
        fallback.saveProfile(profile)

    override suspend fun requestPasswordReset(email: String) =
        fallback.requestPasswordReset(email)

    override suspend fun changePassword(currentPassword: String, newPassword: String) =
        fallback.changePassword(currentPassword, newPassword)
}
