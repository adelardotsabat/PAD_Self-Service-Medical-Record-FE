package com.example.selfmedicalrecord.data.admin

import com.example.selfmedicalrecord.data.remote.AdminUserStatusRequest
import com.example.selfmedicalrecord.data.remote.ApiService
import com.example.selfmedicalrecord.data.remote.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repository admin yang disambungkan ke backend SECARA BERTAHAP.
 *
 * Fungsi yang sudah di-override di sini memakai API sungguhan; sisanya diteruskan ke
 * [fallback] (data contoh) sampai fiturnya disambungkan satu per satu.
 *
 * Sudah tersambung: [logs], [users] / [refreshUsers], [userStats], [setUserActive].
 * Belum ada endpoint di backend: [addUser], [deleteUser] (gagal dengan [FeatureUnavailableException]).
 */
class RemoteAdminRepository(
    private val api: ApiService,
    private val fallback: AdminRepository = FakeAdminRepository()
) : AdminRepository by fallback {

    private val _users = MutableStateFlow<List<AdminUser>>(emptyList())
    override val users: StateFlow<List<AdminUser>> = _users.asStateFlow()

    /**
     * 100 log terbaru (aktivitas + error integrasi) dari GET admin/logs.
     * Filter Login/Data/Sistem dilakukan di layar, jadi cukup satu panggilan.
     */
    override suspend fun logs(): Result<List<LogEntry>> =
        safeApiCall { api.adminLogs(category = "ALL", perPage = 100, page = 1) }
            .map { envelope -> envelope.data?.items.orEmpty().map { it.toLogEntry() } }

    /** 100 akun terbaru dari GET admin/users (pencarian di layar bekerja pada daftar ini). */
    override suspend fun refreshUsers(): Result<Unit> =
        safeApiCall { api.adminUsers(perPage = 100, page = 1) }
            .map { envelope ->
                _users.value = envelope.data?.data.orEmpty().map { it.toAdminUser() }
            }

    /** Jumlah akun dihitung server atas SEMUA akun (dari GET admin/dashboard). */
    override suspend fun userStats(): Result<UserStats> =
        safeApiCall { api.adminDashboard() }
            .mapCatching { envelope ->
                val counts = envelope.data?.users ?: error("Ringkasan pengguna kosong")
                UserStats(total = counts.total, active = counts.active, inactive = counts.inactive)
            }

    override suspend fun setUserActive(id: Long, active: Boolean): Result<Unit> =
        safeApiCall { api.adminSetUserStatus(id, AdminUserStatusRequest(active)) }
            .map { envelope ->
                val updated = envelope.data?.toAdminUser()
                _users.update { list ->
                    list.map { user ->
                        when {
                            user.id != id -> user
                            updated != null -> updated
                            else -> user.copy(
                                status = if (active) AccountStatus.ACTIVE else AccountStatus.INACTIVE
                            )
                        }
                    }
                }
            }

    // Backend belum punya endpoint tambah / hapus user.
    override suspend fun addUser(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<Unit> = Result.failure(FeatureUnavailableException())

    override suspend fun deleteUser(id: Long): Result<Unit> =
        Result.failure(FeatureUnavailableException())
}
