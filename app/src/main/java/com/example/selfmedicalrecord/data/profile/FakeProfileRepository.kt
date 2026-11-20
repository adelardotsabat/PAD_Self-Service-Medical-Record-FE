package com.example.selfmedicalrecord.data.profile

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementasi palsu: data disimpan di memori dan dipakai bersama semua layar,
 * sehingga perubahan di Kelola Profil langsung terlihat di tab Profil.
 *
 * TODO: pindahkan ke ServiceLocator setelah branch self-assessment di-merge.
 */
class FakeProfileRepository : ProfileRepository {

    override val profile: StateFlow<UserProfile> = sharedProfile.asStateFlow()

    override suspend fun updateProfile(profile: UserProfile): Result<Unit> {
        delay(FAKE_LATENCY_MS)
        sharedProfile.value = profile
        return Result.success(Unit)
    }

    override suspend fun refresh(): Result<Unit> = Result.success(Unit)

    override fun clear() = Unit

    private companion object {
        const val FAKE_LATENCY_MS = 600L

        // Satu penyimpanan untuk seluruh proses aplikasi.
        val sharedProfile = MutableStateFlow(
            UserProfile(fullName = "Pengguna", email = "pengguna@email.com")
        )
    }
}
