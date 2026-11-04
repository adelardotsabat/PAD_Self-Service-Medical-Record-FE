package com.example.selfmedicalrecord.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Daftar endpoint backend Laravel. Base URL berakhir di /api/, jadi path ditulis tanpa awalan. */
interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<AuthData>>

    @POST("logout")
    suspend fun logout(): Response<ApiEnvelope<Any>>
}
