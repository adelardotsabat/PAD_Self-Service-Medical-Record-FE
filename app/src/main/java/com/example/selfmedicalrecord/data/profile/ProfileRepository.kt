package com.example.selfmedicalrecord.data.profile

import kotlinx.coroutines.flow.StateFlow

/** Sumber data profil. UI hanya tahu interface ini, bukan dari mana datanya. */
interface ProfileRepository {
    /** Profil terkini. Berubah otomatis setelah [updateProfile] berhasil. */
    val profile: StateFlow<UserProfile>

    suspend fun updateProfile(profile: UserProfile): Result<Unit>

    /** Mengambil data terbaru dari server. Aman dipanggil berulang. */
    suspend fun refresh(): Result<Unit>

    /** Mengosongkan data (saat login atau keluar) supaya tidak bocor ke akun berikutnya. */
    fun clear()
}
