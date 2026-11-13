package com.example.selfmedicalrecord.data

import android.content.Context
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.FakeAdminRepository
import com.example.selfmedicalrecord.data.auth.AuthRepository
import com.example.selfmedicalrecord.data.auth.RemoteAuthRepository
import com.example.selfmedicalrecord.data.food.FakeFoodCalorieRepository
import com.example.selfmedicalrecord.data.food.FoodCalorieRepository
import com.example.selfmedicalrecord.data.health.FakeHealthRepository
import com.example.selfmedicalrecord.data.health.HealthRepository
import com.example.selfmedicalrecord.data.profile.ProfileRepository
import com.example.selfmedicalrecord.data.profile.RemoteProfileRepository
import com.example.selfmedicalrecord.data.remote.ApiClient
import com.example.selfmedicalrecord.data.remote.ApiService
import com.example.selfmedicalrecord.data.remote.TokenStore

/**
 * Penyedia dependensi sederhana (satu instance dipakai bersama seluruh app).
 * Harus dipanggil [init] sekali dari Application sebelum dipakai.
 * Di sinilah memilih Fake atau Remote untuk tiap repository.
 */
object ServiceLocator {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val tokenStore: TokenStore by lazy { TokenStore(appContext) }
    private val api: ApiService by lazy { ApiClient.create(tokenStore) }

    // Sudah tersambung ke backend: login, register, verifikasi, lupa sandi, logout.
    val authRepository: AuthRepository by lazy {
        RemoteAuthRepository(api, tokenStore, onSessionChanged = { profileRepository.clear() })
    }

    // Sudah tersambung ke backend: data akun (/me) dan simpan profil.
    val profileRepository: ProfileRepository by lazy { RemoteProfileRepository(api, tokenStore) }

    // Backend belum punya endpoint admin, jadi masih Fake.
    val adminRepository: AdminRepository by lazy { FakeAdminRepository() }

    // Masih Fake sampai tahap berikutnya.
    val healthRepository: HealthRepository by lazy { FakeHealthRepository(seedDemoData = true) }
    val foodCalorieRepository: FoodCalorieRepository by lazy { FakeFoodCalorieRepository() }
}
