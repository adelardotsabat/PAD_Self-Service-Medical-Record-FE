package com.example.selfmedicalrecord.data.remote

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun apiDateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    isLenient = false
    timeZone = TimeZone.getTimeZone("UTC")
}

/**
 * Epoch millis (tengah malam UTC, seperti hasil date picker) -> "yyyy-MM-dd" untuk backend.
 * Memakai UTC supaya tanggalnya tidak bergeser sehari.
 */
fun formatApiDate(millis: Long): String = apiDateFormat().format(Date(millis))

/** "yyyy-MM-dd" dari backend -> epoch millis UTC; null bila kosong atau formatnya tidak dikenal. */
fun parseApiDate(value: String?): Long? {
    val text = value?.take(10)?.takeIf { it.isNotBlank() } ?: return null
    return try {
        apiDateFormat().parse(text)?.time
    } catch (e: ParseException) {
        null
    }
}
