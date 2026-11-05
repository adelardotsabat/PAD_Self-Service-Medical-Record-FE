package com.example.selfmedicalrecord.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/** Daftar endpoint backend Laravel. Base URL berakhir di /api/, jadi path ditulis tanpa awalan. */
interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<AuthData>>

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiEnvelope<Any>>

    @POST("email/verify")
    suspend fun verifyEmail(@Body body: VerifyEmailRequest): Response<ApiEnvelope<Any>>

    @POST("email/resend-code")
    suspend fun resendEmailCode(@Body body: ResendCodeRequest): Response<ApiEnvelope<Any>>

    @PUT("profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<ApiEnvelope<Any>>

    @POST("logout")
    suspend fun logout(): Response<ApiEnvelope<Any>>
}
