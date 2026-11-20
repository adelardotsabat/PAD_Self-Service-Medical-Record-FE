package com.example.selfmedicalrecord.data.admin

import kotlinx.coroutines.flow.StateFlow

/** Dilempar [AdminRepository.addUser] saat email sudah dipakai akun lain. */
class EmailTakenException : Exception("Email sudah terdaftar")

/**
 * Kontrak data untuk area admin.
 *
 * Saat ini hanya ada [FakeAdminRepository]. Backend Laravel BELUM punya endpoint admin
 * (konfigurasi OpenMRS, log aktivitas, kelola user), jadi semua di sini data contoh.
 * Begitu endpoint-nya ada, buat implementasi Remote dan ganti di ServiceLocator.
 */
interface AdminRepository {
    /** Daftar akun; berubah otomatis saat [addUser] / [deleteUser] dipanggil. */
    val users: StateFlow<List<AdminUser>>

    suspend fun summary(): Result<AdminSummary>
    suspend fun endpointConfig(): Result<EndpointConfig>
    suspend fun saveEndpointConfig(config: EndpointConfig): Result<Unit>
    suspend fun pingEndpoint(config: EndpointConfig): Result<Unit>
    suspend fun integrations(): Result<List<IntegrationEndpoint>>
    suspend fun logs(): Result<List<LogEntry>>
    suspend fun userActivities(): Result<List<UserActivity>>

    /** Gagal dengan [EmailTakenException] bila email sudah terdaftar. */
    suspend fun addUser(name: String, email: String, password: String, role: UserRole): Result<Unit>
    suspend fun deleteUser(id: Long): Result<Unit>
}
