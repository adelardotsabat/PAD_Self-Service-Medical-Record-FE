package com.example.selfmedicalrecord.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Helper tanggal berbasis epoch day (hari sejak 1970-01-01), selaras dengan MaterialDatePicker
 * yang mengembalikan tengah malam UTC. Dengan begitu tanggal tidak bergeser oleh zona waktu,
 * dan tidak butuh java.time (minSdk 24).
 */
object DayUtils {

    const val DAY_MS = 86_400_000L

    private val ID = Locale.forLanguageTag("id-ID")
    private val UTC = TimeZone.getTimeZone("UTC")
    private val WEEKDAYS = arrayOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    /** Hari ini menurut kalender lokal perangkat. */
    fun todayEpochDay(): Long {
        val local = Calendar.getInstance()
        val utc = Calendar.getInstance(UTC).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }
        return utc.timeInMillis / DAY_MS
    }

    /** Tengah malam UTC untuk [epochDay]; format yang dipakai MaterialDatePicker. */
    fun toUtcMillis(epochDay: Long): Long = epochDay * DAY_MS

    fun fromUtcMillis(millis: Long): Long = Math.floorDiv(millis, DAY_MS)

    fun format(epochDay: Long, pattern: String): String =
        SimpleDateFormat(pattern, ID).apply { timeZone = UTC }.format(Date(toUtcMillis(epochDay)))

    /** Singkatan hari (Sen..Min). 1 Jan 1970 adalah hari Kamis. */
    fun weekdayShort(epochDay: Long): String =
        WEEKDAYS[Math.floorMod(epochDay + 3, 7L).toInt()]
}
