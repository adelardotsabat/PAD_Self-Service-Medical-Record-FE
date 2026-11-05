package com.example.selfmedicalrecord.data.remote

import android.content.Context
import androidx.core.content.edit

/**
 * Menyimpan token login di penyimpanan privat aplikasi (tidak bisa dibaca app lain).
 * TODO: untuk rilis, pertimbangkan enkripsi tambahan (Android Keystore).
 */
class TokenStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun save(token: String, role: String? = null, name: String? = null) = prefs.edit {
        putString(KEY_TOKEN, token)
        putString(KEY_ROLE, role)
        putString(KEY_NAME, name)
    }

    /** "ADMIN" / "USER" dari backend, atau null bila tidak diketahui. */
    fun role(): String? = prefs.getString(KEY_ROLE, null)

    fun isAdmin(): Boolean = role().equals(ROLE_ADMIN, ignoreCase = true)

    fun name(): String? = prefs.getString(KEY_NAME, null)

    fun token(): String? = prefs.getString(KEY_TOKEN, null)

    fun hasToken(): Boolean = !token().isNullOrBlank()

    fun clear() = prefs.edit { clear() }

    private companion object {
        const val FILE_NAME = "session"
        const val KEY_TOKEN = "token"
        const val KEY_ROLE = "role"
        const val KEY_NAME = "name"
        const val ROLE_ADMIN = "ADMIN"
    }
}
