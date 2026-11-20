package com.example.selfmedicalrecord.ui.admin

import android.util.Patterns
import androidx.annotation.StringRes
import com.example.selfmedicalrecord.R

/** Validasi form konfigurasi endpoint. Mengembalikan id string error, atau null jika valid. */
object AdminValidators {

    @StringRes
    fun serverUrl(value: String): Int? {
        val url = value.trim()
        return when {
            url.isEmpty() -> R.string.admin_error_url_required
            !(url.startsWith("https://") || url.startsWith("http://")) ||
                !Patterns.WEB_URL.matcher(url).matches() -> R.string.admin_error_url_invalid
            else -> null
        }
    }

    @StringRes
    fun port(value: String): Int? {
        val port = value.trim().toIntOrNull()
        return when {
            value.isBlank() -> R.string.admin_error_port_required
            port == null || port !in 1..65535 -> R.string.admin_error_port_invalid
            else -> null
        }
    }

    @StringRes
    fun apiKey(value: String): Int? =
        if (value.isBlank()) R.string.admin_error_api_key_required else null
}
