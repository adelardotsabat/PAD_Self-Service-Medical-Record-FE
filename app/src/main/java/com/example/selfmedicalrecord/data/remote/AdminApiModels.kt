package com.example.selfmedicalrecord.data.remote

import com.google.gson.annotations.SerializedName

/** Isi `data` dari GET admin/logs. */
data class AdminLogsData(
    val items: List<AdminLogDto>? = null
)

/**
 * Satu baris log gabungan. [id] bisa angka ("12") untuk log aktivitas atau
 * "integration_5" untuk log integrasi yang gagal, jadi dibaca sebagai teks.
 */
data class AdminLogDto(
    val id: String? = null,
    /** ISO 8601 dengan zona waktu, mis. 2026-11-27T06:10:00+07:00. */
    val timestamp: String? = null,
    val actor: String? = null,
    @SerializedName("actor_email") val actorEmail: String? = null,
    /** LOGIN, DATA, SYSTEM, atau ERROR. */
    val category: String? = null,
    val level: String? = null,
    val action: String? = null,
    val description: String? = null
)

/** Satu akun di GET admin/users (data pribadi seperti NIK sengaja tidak dikirim backend). */
data class AdminUserDto(
    @SerializedName("user_id") val userId: Long = 0,
    val name: String? = null,
    val email: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    /** ISO 8601 UTC, null bila belum pernah terlihat aktif. */
    @SerializedName("last_seen_at") val lastSeenAt: String? = null,
    /** "ADMIN" atau "USER". */
    val role: String? = null
)

/** Isi PATCH admin/users/{id}/status. */
data class AdminUserStatusRequest(
    @SerializedName("is_active") val isActive: Boolean
)

/** Isi `data` dari GET admin/dashboard (baru bagian `users` yang dipakai). */
data class AdminDashboardData(
    val users: AdminUserCountsDto? = null
)

data class AdminUserCountsDto(
    val total: Int = 0,
    val active: Int = 0,
    val inactive: Int = 0
)
