package com.example.selfmedicalrecord.data.admin

import com.example.selfmedicalrecord.data.remote.AdminUserDto

/**
 * Backend hanya mengenal aktif / nonaktif (is_active), jadi status "Menunggu" tidak dipakai.
 * Nama bisa kosong di backend (akun baru), maka dipakai bagian depan email.
 */
internal fun AdminUserDto.toAdminUser(): AdminUser = AdminUser(
    id = userId,
    name = name?.takeIf { it.isNotBlank() } ?: email.orEmpty().substringBefore('@'),
    email = email.orEmpty(),
    role = if (role.equals("ADMIN", ignoreCase = true)) UserRole.ADMIN else UserRole.USER,
    status = if (isActive) AccountStatus.ACTIVE else AccountStatus.INACTIVE,
    lastSeen = parseIsoMillis(lastSeenAt)?.let { relativeTimeId(it) }
)
