package com.example.selfmedicalrecord.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Menambahkan header ke setiap request:
 * - Accept: application/json  (supaya Laravel membalas JSON, bukan halaman HTML)
 * - Authorization: Bearer <token>  (jika sudah login)
 *
 * Jika server membalas 401 pada request yang membawa token, token dihapus
 * (sesi habis atau dicabut). Layar yang mengamati [tokenStore] akan kembali ke Login.
 */
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.token()
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .apply { if (token != null) header("Authorization", "Bearer $token") }
            .build()

        val response = chain.proceed(request)
        if (response.code == 401 && token != null) {
            tokenStore.clear()
        }
        return response
    }
}
