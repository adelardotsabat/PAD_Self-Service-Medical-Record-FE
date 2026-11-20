package com.example.selfmedicalrecord.data.admin

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Data contoh (sesuai desain Figma) supaya UI admin bisa dicoba tanpa backend. */
class FakeAdminRepository : AdminRepository {

    private val _users = MutableStateFlow(
        listOf(
            AdminUser(1, "Andi", "andi@email.com", UserRole.ADMIN, AccountStatus.ACTIVE),
            AdminUser(2, "Siti Rahma", "siti@email.com", UserRole.USER, AccountStatus.ACTIVE),
            AdminUser(3, "Budi Santoso", "budi@email.com", UserRole.USER, AccountStatus.PENDING)
        )
    )
    override val users: StateFlow<List<AdminUser>> = _users.asStateFlow()

    private var nextUserId = 4L

    private var config = EndpointConfig(
        serverUrl = "https://api.satusehat.kemkes.go.id/v2",
        port = "443",
        apiKey = "ss_live_9a8f27bcd0134e",
        enabled = true
    )

    override suspend fun summary(): Result<AdminSummary> {
        delay(300)
        return Result.success(
            AdminSummary(isOptimal = true, uptime = "99.9%", apiActive = true, lastSync = "2 menit lalu")
        )
    }

    override suspend fun endpointConfig(): Result<EndpointConfig> {
        delay(300)
        return Result.success(config)
    }

    override suspend fun saveEndpointConfig(config: EndpointConfig): Result<Unit> {
        delay(600)
        this.config = config
        return Result.success(Unit)
    }

    override suspend fun pingEndpoint(config: EndpointConfig): Result<Unit> {
        delay(900)
        return if (config.enabled) Result.success(Unit)
        else Result.failure(IllegalStateException("Koneksi dinonaktifkan"))
    }

    override suspend fun integrations(): Result<List<IntegrationEndpoint>> {
        delay(300)
        return Result.success(
            listOf(
                IntegrationEndpoint(1, "Satusehat FHIR API", "fhir.kemkes.go.id/v1", "5 menit lalu", IntegrationState.CONNECTED),
                IntegrationEndpoint(2, "BPJS Claims Gateway", "api.bpjs-kesehatan.go.id", "5 menit lalu", IntegrationState.FAILED),
                IntegrationEndpoint(3, "Sistem Informasi Rujukan", "sisrute.kemkes.go.id/api", "5 menit lalu", IntegrationState.WAITING),
                IntegrationEndpoint(4, "Sistem Manajemen Vaksin", "vaksin.kemkes.go.id/v2", "5 menit lalu", IntegrationState.CONNECTED)
            )
        )
    }

    override suspend fun logs(): Result<List<LogEntry>> {
        delay(300)
        return Result.success(
            listOf(
                LogEntry(1, "10:24", "Admin IT", "Mengubah URL FHIR Endpoint", LogCategory.SYSTEM),
                LogEntry(2, "09:41", "Admin", "Berhasil masuk ke aplikasi", LogCategory.LOGIN),
                LogEntry(3, "08:12", "Siti Rahma", "Menginput data BMI baru", LogCategory.DATA),
                LogEntry(4, "07:30", "Sistem", "Sinkronisasi data pengguna gagal", LogCategory.ERROR)
            )
        )
    }

    override suspend fun userActivities(): Result<List<UserActivity>> {
        delay(300)
        return Result.success(
            listOf(
                UserActivity(1, "Andi", UserRole.ADMIN, "10 menit lalu", true),
                UserActivity(2, "Budi", UserRole.USER, "Kemarin", false),
                UserActivity(3, "Dewi", UserRole.USER, "2 jam lalu", true),
                UserActivity(4, "Eko", UserRole.USER, "3 hari lalu", false)
            )
        )
    }

    override suspend fun addUser(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<Unit> {
        delay(600)
        val exists = _users.value.any { it.email.equals(email, ignoreCase = true) }
        if (exists) return Result.failure(EmailTakenException())
        // Kata sandi sengaja tidak disimpan: ini hanya data contoh.
        _users.update { list ->
            list + AdminUser(nextUserId++, name, email, role, AccountStatus.PENDING)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteUser(id: Long): Result<Unit> {
        _users.update { list -> list.filterNot { it.id == id } }
        return Result.success(Unit)
    }
}
