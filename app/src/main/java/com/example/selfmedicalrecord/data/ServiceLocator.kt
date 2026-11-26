package com.example.selfmedicalrecord.data

import android.content.Context
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.RemoteAdminRepository
import com.example.selfmedicalrecord.data.auth.AuthRepository
import com.example.selfmedicalrecord.data.auth.RemoteAuthRepository
import com.example.selfmedicalrecord.data.food.FakeFoodCalorieRepository
import com.example.selfmedicalrecord.data.food.FoodCalorieRepository
import com.example.selfmedicalrecord.data.health.HealthRepository
import com.example.selfmedicalrecord.data.health.RemoteHealthRepository
import com.example.selfmedicalrecord.data.profile.ProfileRepository
import com.example.selfmedicalrecord.data.record.MedicalRecordRepository
import com.example.selfmedicalrecord.data.record.RemoteMedicalRecordRepository
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
        RemoteAuthRepository(api, tokenStore, onSessionChanged = {
            profileRepository.clear()
            healthRepository.clear()
        })
    }

    // Sudah tersambung ke backend: data akun (/me) dan simpan profil.
    val profileRepository: ProfileRepository by lazy { RemoteProfileRepository(api, tokenStore) }

    // Admin disambungkan bertahap: Log Aktivitas sudah ke backend, sisanya masih data contoh.
    val adminRepository: AdminRepository by lazy { RemoteAdminRepository(api) }

    // Sudah tersambung ke backend: berat/tinggi (BMI) dan tensi lewat /health-records.
    val healthRepository: HealthRepository by lazy { RemoteHealthRepository(api) }

    // Sudah tersambung ke backend: catatan mandiri + catatan klinik (OpenMRS).
    val medicalRecordRepository: MedicalRecordRepository by lazy { RemoteMedicalRecordRepository(api) }

    // Masih Fake sampai tahap berikutnya.
    val foodCalorieRepository: FoodCalorieRepository by lazy { FakeFoodCalorieRepository() }
}
