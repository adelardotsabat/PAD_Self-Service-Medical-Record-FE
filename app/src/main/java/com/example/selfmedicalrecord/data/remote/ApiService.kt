package com.example.selfmedicalrecord.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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

    @POST("password/forgot")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<ApiEnvelope<Any>>

    @POST("password/verify-code")
    suspend fun verifyResetCode(@Body body: VerifyResetCodeRequest): Response<ApiEnvelope<ResetTokenData>>

    @POST("password/reset")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<ApiEnvelope<Any>>

    /** Data akun yang sedang login. Backend mengirim objek user langsung di `data`. */
    @GET("me")
    suspend fun me(): Response<ApiEnvelope<UserDto>>

    @PUT("profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<ApiEnvelope<Any>>

    @POST("logout")
    suspend fun logout(): Response<ApiEnvelope<Any>>
}
