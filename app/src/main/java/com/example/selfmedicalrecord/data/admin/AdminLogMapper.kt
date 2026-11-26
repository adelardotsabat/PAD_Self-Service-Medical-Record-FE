package com.example.selfmedicalrecord.data.admin

import com.example.selfmedicalrecord.data.remote.AdminLogDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val INTEGRATION_PREFIX = "integration_"

/** Mengubah baris log dari backend menjadi [LogEntry] siap tampil. */
internal fun AdminLogDto.toLogEntry(): LogEntry = LogEntry(
    id = parseLogId(id),
    time = formatLogTime(timestamp),
    actor = actorName(),
    message = logMessage(action, description),
    category = runCatching { LogCategory.valueOf(category.orEmpty().uppercase(Locale.US)) }
        .getOrDefault(LogCategory.SYSTEM)
)

/**
 * Log aktivitas memakai id angka; log integrasi ("integration_5") dibuat negatif
 * supaya keduanya tidak pernah bentrok sebagai id Long.
 */
private fun parseLogId(raw: String?): Long {
    if (raw.isNullOrBlank()) return 0L
    if (raw.startsWith(INTEGRATION_PREFIX)) {
        val n = raw.removePrefix(INTEGRATION_PREFIX).toLongOrNull()
        return if (n != null) -n else raw.hashCode().toLong()
    }
    return raw.toLongOrNull() ?: raw.hashCode().toLong()
}

private fun AdminLogDto.actorName(): String = when {
    // Backend memakai "System" saat tidak ada nama; bila ada email, tampilkan email.
    actor.isNullOrBlank() || actor == "System" -> actorEmail ?: "Sistem"
    else -> actor
}

/** Hari ini -> "10:24"; hari lain -> "26 Nov 10:24". Memakai zona waktu perangkat. */
private fun formatLogTime(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return "-"
    val millis = parseIsoMillis(timestamp) ?: return timestamp.take(16).replace('T', ' ')
    val date = Date(millis)

    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { time = date }
    val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

    val pattern = if (sameDay) "HH:mm" else "d MMM HH:mm"
    return SimpleDateFormat(pattern, Locale("id", "ID")).format(date)
}

/** Deskripsi dari backend berbahasa Inggris; aksi yang umum diterjemahkan. */
private fun logMessage(action: String?, description: String?): String = when (action) {
    "LOGIN" ->
        if (description?.contains("Google", ignoreCase = true) == true) "Berhasil masuk dengan Google"
        else "Berhasil masuk ke aplikasi"
    "LOGOUT" -> "Keluar dari aplikasi"
    "PROFILE_UPDATE" -> "Memperbarui profil"
    "PROFILE_PHOTO_UPLOAD" -> "Mengunggah foto profil"
    "PROFILE_PHOTO_DELETE" -> "Menghapus foto profil"
    "HEALTH_RECORD_CREATE" -> "Menambah data kesehatan"
    "HEALTH_RECORD_UPDATE" -> "Mengubah data kesehatan"
    "HEALTH_RECORD_DELETE" -> "Menghapus data kesehatan"
    "PERSONAL_RECORD_CREATE" -> "Menambah riwayat pelayanan"
    "PERSONAL_RECORD_UPDATE" -> "Mengubah riwayat pelayanan"
    "PERSONAL_RECORD_DELETE" -> "Menghapus riwayat pelayanan"
    "OPENMRS_CONFIG_UPDATE" -> "Mengubah konfigurasi endpoint"
    "ADMIN_OPENMRS_TEST_CONNECTION" -> "Menguji koneksi endpoint"
    "ADMIN_USER_STATUS_CHANGE" ->
        if (description?.contains("deactivated", ignoreCase = true) == true) "Menonaktifkan akun pengguna"
        else "Mengaktifkan akun pengguna"
    "ADMIN_SESSION_REVOKE", "ADMIN_SESSION_REVOKE_ALL" -> "Mencabut sesi pengguna"
    "ADMIN_USER_VIEW" -> "Melihat detail pengguna"
    "ADMIN_HEALTH_RECORDS_VIEW" -> "Melihat data kesehatan pengguna"
    "ADMIN_PERSONAL_RECORDS_VIEW" -> "Melihat riwayat pelayanan pengguna"
    "ADMIN_SYSTEM_LOGS_VIEW" -> "Melihat log sistem"
    // Log error integrasi dan aksi yang belum dikenal: tampilkan apa adanya.
    else -> description?.takeIf { it.isNotBlank() } ?: action.orEmpty()
}
