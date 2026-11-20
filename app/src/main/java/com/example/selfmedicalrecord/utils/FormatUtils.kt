package com.example.selfmedicalrecord.utils

import java.util.Locale

private val ID_LOCALE: Locale = Locale.forLanguageTag("id-ID")

/** Satu angka desimal dengan koma, mis. 18,6. */
fun Float.formatOneDecimal(): String = String.format(ID_LOCALE, "%.1f", this)

/** Bilangan bulat ditampilkan tanpa desimal (45), selain itu satu desimal (47,6). */
fun Float.formatCompact(): String =
    if (this == toInt().toFloat()) toInt().toString() else formatOneDecimal()
