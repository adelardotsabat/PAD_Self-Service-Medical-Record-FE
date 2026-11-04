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

data class AuthData(
    val token: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    val user: UserDto? = null
)

data class UserDto(
    val name: String? = null,
    val email: String? = null
)
