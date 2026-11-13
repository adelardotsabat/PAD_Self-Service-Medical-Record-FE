package com.example.selfmedicalrecord.data.remote

import com.google.gson.annotations.SerializedName

/** Bungkus standar semua respons backend: {success, message, data, errors?}. */
data class ApiEnvelope<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null,
    /** Hanya ada saat validasi gagal (HTTP 422): nama field -> daftar pesan. */
    val errors: Map<String, List<String>>? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class VerifyEmailRequest(
    val email: String,
    val code: String
)

data class ResendCodeRequest(
    val email: String
)

/** Field null tidak ikut dikirim (Gson), cocok dengan aturan "sometimes" di backend. */
data class UpdateProfileRequest(
    val name: String? = null,
    val nik: String? = null,
    /** "MALE" atau "FEMALE". */
    val gender: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null
)

data class AuthData(
    val token: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    val user: UserDto? = null
)

data class ForgotPasswordRequest(
    val email: String
)

data class VerifyResetCodeRequest(
    val email: String,
    val code: String
)

/** Data balasan /password/verify-code: token sekali pakai untuk langkah terakhir. */
data class ResetTokenData(
    @SerializedName("reset_token") val resetToken: String? = null
)

data class ResetPasswordRequest(
    val email: String,
    @SerializedName("reset_token") val resetToken: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String
)

/**
 * Data akun dari /login dan /me. NIK dan telepon SENGAJA tidak dikirim backend
 * (disembunyikan di model User), jadi tidak ada di sini.
 */
data class UserDto(
    val name: String? = null,
    val email: String? = null,
    /** "MALE" atau "FEMALE"; null jika belum diisi. */
    val gender: String? = null,
    /** Format yyyy-MM-dd; null jika belum diisi. */
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    /** Backend memuat relasi role: {"role_id": 2, "role_name": "ADMIN"}. */
    val role: RoleDto? = null
)

data class RoleDto(
    @SerializedName("role_name") val roleName: String? = null
)
