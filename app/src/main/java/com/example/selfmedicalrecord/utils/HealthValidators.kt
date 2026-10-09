package com.example.selfmedicalrecord.utils

import androidx.annotation.StringRes
import com.example.selfmedicalrecord.R

/** Validasi form input data kesehatan. Mengembalikan id string pesan error, atau null jika valid. */
object HealthValidators {

    const val WEIGHT_MIN = 2f
    const val WEIGHT_MAX = 500f
    const val HEIGHT_MIN = 30f
    const val HEIGHT_MAX = 250f
    const val SYSTOLIC_MIN = 50
    const val SYSTOLIC_MAX = 300
    const val DIASTOLIC_MIN = 30
    const val DIASTOLIC_MAX = 200

    /** Menerima titik maupun koma sebagai pemisah desimal. */
    fun parseDecimal(raw: String): Float? = raw.trim().replace(',', '.').toFloatOrNull()

    @StringRes
    fun weight(raw: String): Int? = decimalInRange(
        raw, WEIGHT_MIN, WEIGHT_MAX, R.string.error_weight_required, R.string.error_weight_range
    )

    @StringRes
    fun height(raw: String): Int? = decimalInRange(
        raw, HEIGHT_MIN, HEIGHT_MAX, R.string.error_height_required, R.string.error_height_range
    )

    @StringRes
    fun systolic(raw: String): Int? = intInRange(
        raw, SYSTOLIC_MIN, SYSTOLIC_MAX, R.string.error_systolic_required, R.string.error_systolic_range
    )

    @StringRes
    fun diastolic(raw: String, systolic: Int?): Int? {
        val basic = intInRange(
            raw, DIASTOLIC_MIN, DIASTOLIC_MAX,
            R.string.error_diastolic_required, R.string.error_diastolic_range
        )
        if (basic != null) return basic
        val dia = raw.trim().toInt()
        return if (systolic != null && dia >= systolic) R.string.error_diastolic_above_systolic else null
    }

    @StringRes
    private fun decimalInRange(
        raw: String, min: Float, max: Float,
        @StringRes requiredError: Int, @StringRes rangeError: Int
    ): Int? {
        if (raw.isBlank()) return requiredError
        val value = parseDecimal(raw) ?: return R.string.error_number_invalid
        return if (value in min..max) null else rangeError
    }

    @StringRes
    private fun intInRange(
        raw: String, min: Int, max: Int,
        @StringRes requiredError: Int, @StringRes rangeError: Int
    ): Int? {
        if (raw.isBlank()) return requiredError
        val value = raw.trim().toIntOrNull() ?: return R.string.error_number_invalid
        return if (value in min..max) null else rangeError
    }
}
