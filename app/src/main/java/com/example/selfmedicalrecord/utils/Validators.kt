package com.example.selfmedicalrecord.utils

import android.util.Patterns
import androidx.annotation.StringRes
import com.example.selfmedicalrecord.R

/**
 * Validasi input form. Mengembalikan id string pesan error, atau null jika valid.
 */
object Validators {

    const val MIN_PASSWORD_LENGTH = 8
    const val NIK_LENGTH = 16

    @StringRes
    fun email(value: String): Int? = when {
        value.isBlank() -> R.string.error_email_required
        !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches() -> R.string.error_email_invalid
        else -> null
    }

    @StringRes
    fun password(value: String): Int? =
        if (value.isEmpty()) R.string.error_password_required else null

    @StringRes
    fun newPassword(value: String): Int? = when {
        value.isEmpty() -> R.string.error_password_required
        value.length < MIN_PASSWORD_LENGTH -> R.string.error_password_too_short
        else -> null
    }

    @StringRes
    fun confirmPassword(password: String, confirmation: String): Int? = when {
        confirmation.isEmpty() -> R.string.error_confirm_required
        confirmation != password -> R.string.error_confirm_mismatch
        else -> null
    }

    @StringRes
    fun fullName(value: String): Int? =
        if (value.isBlank()) R.string.error_full_name_required else null

    @StringRes
    fun nik(value: String): Int? = when {
        value.isBlank() -> R.string.error_nik_required
        value.length != NIK_LENGTH || !value.all { it.isDigit() } -> R.string.error_nik_invalid
        else -> null
    }
}
