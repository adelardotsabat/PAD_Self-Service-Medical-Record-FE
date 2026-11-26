package com.example.selfmedicalrecord.data.admin

import kotlinx.coroutines.flow.StateFlow

/** Dilempar saat fitur belum disediakan backend (mis. tambah/hapus user). */
class FeatureUnavailableException : Exception("Fitur belum tersedia di server")

/** Dilempar [AdminRepository.addUser] saat email sudah dipakai akun lain. */
class EmailTakenException : Exception("Email sudah terdaftar")

/**
 * Kontrak data untuk area admin.
 *
 * Disambungkan bertahap ke backend lewat [RemoteAdminRepository]; yang belum disambungkan
 * masih memakai data contoh dari [FakeAdminRepository].
 * Tiap fungsi yang sudah tersambung ditandai di KDoc RemoteAdminRepository.
 */
interface AdminRepository {
    /** Daftar akun; berubah otomatis saat [addUser] / [deleteUser] dipanggil. */
    val users: StateFlow<List<AdminUser>>

    /** Memuat ulang [users] dari sumbernya (backend). Data contoh tidak perlu memuat ulang. */
    suspend fun refreshUsers(): Result<Unit> = Result.success(Unit)

    /** Jumlah akun total/aktif/nonaktif untuk kartu ringkasan. */
    suspend fun userStats(): Result<UserStats> {
        val list = users.value
        return Result.success(
            UserStats(
                total = list.size,
                active = list.count { it.status == AccountStatus.ACTIVE },
                inactive = list.count { it.status == AccountStatus.INACTIVE }
            )
        )
    }

    /** Aktifkan / nonaktifkan akun. Backend menolak akun sendiri (403) dan admin aktif terakhir (422). */
    suspend fun setUserActive(id: Long, active: Boolean): Result<Unit>

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
