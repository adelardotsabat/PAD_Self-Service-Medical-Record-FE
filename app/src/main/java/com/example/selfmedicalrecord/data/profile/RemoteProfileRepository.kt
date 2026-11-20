package com.example.selfmedicalrecord.data.profile

import com.example.selfmedicalrecord.data.remote.ApiService
import com.example.selfmedicalrecord.data.remote.TokenStore
import com.example.selfmedicalrecord.data.remote.UpdateProfileRequest
import com.example.selfmedicalrecord.data.remote.formatApiDate
import com.example.selfmedicalrecord.data.remote.parseApiDate
import com.example.selfmedicalrecord.data.remote.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Profil dari backend (GET /me dan PUT /profile).
 *
 * Batasan backend saat ini:
 * - NIK tidak dikirim balik oleh /me (disembunyikan), jadi hanya terisi selama sesi ini setelah
 *   pengguna menyimpannya dari Kelola Profil.
 * - Alamat belum punya kolom di backend, jadi hanya disimpan di memori selama aplikasi hidup.
 * - Email tidak dikirim saat menyimpan: mengubahnya membuat backend menandai akun belum
 *   terverifikasi dan meminta kode baru, alur yang belum ada di Kelola Profil.
 */
class RemoteProfileRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore
) : ProfileRepository {

    private val _profile = MutableStateFlow(seed())
    override val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    /** Nama dan email yang disimpan saat login, supaya layar langsung punya isi sebelum /me selesai. */
    private fun seed() = UserProfile(
        fullName = tokenStore.name().orEmpty(),
        email = tokenStore.email().orEmpty()
    )

    override suspend fun refresh(): Result<Unit> =
        safeApiCall { api.me() }.mapCatching { envelope ->
            val user = checkNotNull(envelope.data) { "Server tidak mengirim data akun" }
            _profile.update { current ->
                current.copy(
                    fullName = user.name.orEmpty(),
                    email = user.email.orEmpty(),
                    gender = user.gender,
                    birthDateMillis = parseApiDate(user.dateOfBirth)
                )
            }
        }

    override suspend fun updateProfile(profile: UserProfile): Result<Unit> {
        val body = UpdateProfileRequest(
            name = profile.fullName,
            nik = profile.nik.ifBlank { null },
            gender = profile.gender,
            dateOfBirth = profile.birthDateMillis?.let { formatApiDate(it) }
        )
        return safeApiCall { api.updateProfile(body) }.map {
            // Email tetap yang dari server; semua field lain mengikuti yang baru disimpan.
            _profile.value = profile.copy(email = _profile.value.email)
        }
    }

    override fun clear() {
        _profile.value = seed()
    }
}
