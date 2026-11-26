package com.example.selfmedicalrecord.data.admin

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Membaca waktu ISO 8601 dari Laravel (berakhiran "Z" atau "+07:00", dengan/tanpa pecahan
 * detik, mis. 2026-11-27T06:10:00.000000Z) menjadi epoch milidetik. Null bila tidak terbaca.
 */
internal fun parseIsoMillis(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    val normalized = value.trim()
        .replace(Regex("\\.\\d+"), "")
        .let { if (it.endsWith("Z")) it.dropLast(1) + "+00:00" else it }
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(normalized)?.time
    }.getOrNull()
}

/** "Baru saja", "10 menit lalu", "2 jam lalu", "3 hari lalu"; lebih dari seminggu -> tanggal. */
internal fun relativeTimeId(millis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = (now - millis).coerceAtLeast(0L)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        diff < minute -> "Baru saja"
        diff < hour -> "${diff / minute} menit lalu"
        diff < day -> "${diff / hour} jam lalu"
        diff < 7 * day -> "${diff / day} hari lalu"
        else -> SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(java.util.Date(millis))
    }
}
