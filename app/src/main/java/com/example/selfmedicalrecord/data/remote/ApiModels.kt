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

data class UserDto(
    val name: String? = null,
    val email: String? = null,
    /** Backend memuat relasi role: {"role_id": 2, "role_name": "ADMIN"}. */
    val role: RoleDto? = null
)

data class RoleDto(
    @SerializedName("role_name") val roleName: String? = null
)
