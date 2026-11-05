package com.example.selfmedicalrecord.data.admin

enum class UserRole { ADMIN, USER }

enum class AccountStatus { ACTIVE, PENDING }

/** Akun yang dikelola admin (layar Daftar User). */
data class AdminUser(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRole,
    val status: AccountStatus
)

/** Ringkasan aktivitas satu pengguna (layar Data Pengguna). */
data class UserActivity(
    val id: Long,
    val name: String,
    val role: UserRole,
    /** Sudah berbentuk teks siap tampil, mis. "10 menit lalu". */
    val lastSeen: String,
    val isActive: Boolean
)

data class EndpointConfig(
    val serverUrl: String,
    val port: String,
    val apiKey: String,
    val enabled: Boolean
)

enum class IntegrationState { CONNECTED, FAILED, WAITING }

data class IntegrationEndpoint(
    val id: Long,
    val name: String,
    val url: String,
    val lastSync: String,
    val state: IntegrationState
)

enum class LogCategory { LOGIN, DATA, SYSTEM, ERROR }

data class LogEntry(
    val id: Long,
    val time: String,
    val actor: String,
    val message: String,
    val category: LogCategory
)

data class AdminSummary(
    val isOptimal: Boolean,
    val uptime: String,
    val apiActive: Boolean,
    val lastSync: String
)
