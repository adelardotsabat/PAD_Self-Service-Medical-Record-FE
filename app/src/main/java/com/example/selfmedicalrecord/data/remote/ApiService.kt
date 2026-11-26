package com.example.selfmedicalrecord.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

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

    /** Riwayat pengukuran milik pengguna, terbaru dulu, dibagi per halaman. */
    @GET("health-records")
    suspend fun healthRecords(
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): Response<ApiEnvelope<PageDto<HealthRecordDto>>>

    @POST("health-records")
    suspend fun addHealthRecord(@Body body: CreateHealthRecordRequest): Response<ApiEnvelope<HealthRecordDto>>

    /** Catatan mandiri milik pengguna, terbaru dulu. */
    @GET("personal-medical-records")
    suspend fun personalRecords(
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): Response<ApiEnvelope<PageDto<PersonalRecordDto>>>

    @POST("personal-medical-records")
    suspend fun addPersonalRecord(@Body body: PersonalRecordRequest): Response<ApiEnvelope<PersonalRecordDto>>

    @PUT("personal-medical-records/{id}")
    suspend fun updatePersonalRecord(
        @Path("id") id: Long,
        @Body body: PersonalRecordRequest
    ): Response<ApiEnvelope<PersonalRecordDto>>

    /** Catatan klinik (OpenMRS). Bisa gagal 503 bila layanan klinik belum siap. */
    @GET("medical-records")
    suspend fun clinicRecords(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<ApiEnvelope<ClinicRecordsData>>

    @PUT("profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<ApiEnvelope<Any>>

    @POST("logout")
    suspend fun logout(): Response<ApiEnvelope<Any>>

    // ---- Admin (hanya akun ADMIN; selain itu backend membalas 403) ----

    /** Log gabungan. category: ALL, LOGIN, DATA, SYSTEM, ERROR. per_page maksimal 100. */
    @GET("admin/logs")
    suspend fun adminLogs(
        @Query("category") category: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): Response<ApiEnvelope<AdminLogsData>>

    /** Daftar akun (terbaru dulu). per_page maksimal 100. */
    @GET("admin/users")
    suspend fun adminUsers(
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): Response<ApiEnvelope<PageDto<AdminUserDto>>>

    /** Aktifkan / nonaktifkan akun. 403 = akun sendiri, 422 = admin aktif terakhir. */
    @PATCH("admin/users/{id}/status")
    suspend fun adminSetUserStatus(
        @Path("id") id: Long,
        @Body body: AdminUserStatusRequest
    ): Response<ApiEnvelope<AdminUserDto>>

    /** Ringkasan admin; dipakai untuk jumlah akun total/aktif/nonaktif. */
    @GET("admin/dashboard")
    suspend fun adminDashboard(): Response<ApiEnvelope<AdminDashboardData>>
}
