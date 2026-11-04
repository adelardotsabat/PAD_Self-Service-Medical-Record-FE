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

    fun save(token: String) = prefs.edit { putString(KEY_TOKEN, token) }

    fun token(): String? = prefs.getString(KEY_TOKEN, null)

    fun hasToken(): Boolean = !token().isNullOrBlank()

    fun clear() = prefs.edit { remove(KEY_TOKEN) }

    private companion object {
        const val FILE_NAME = "session"
        const val KEY_TOKEN = "token"
    }
}
