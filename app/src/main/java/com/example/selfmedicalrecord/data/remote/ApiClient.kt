package com.example.selfmedicalrecord.data.remote

import com.example.selfmedicalrecord.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Membuat [ApiService]. Alamat server diatur di app/build.gradle.kts (BASE_URL). */
object ApiClient {

    fun create(tokenStore: TokenStore): ApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenStore))
            .apply {
                if (BuildConfig.DEBUG) {
                    // BASIC: hanya baris permintaan/status. Jangan BODY, karena akan
                    // menulis kata sandi dan token ke Logcat.
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
