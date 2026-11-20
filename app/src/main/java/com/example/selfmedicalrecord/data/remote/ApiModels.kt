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

/** Satu pengukuran dari /health-records. Pengukuran BMI mengisi tinggi+berat, tensi mengisi sistolik+diastolik. */
data class HealthRecordDto(
    @SerializedName("health_record_id") val id: Long = 0,
    /** Server mengirim angka desimal sebagai teks ("160.00"); Gson mengubahnya ke Float. */
    val height: Float? = null,
    val weight: Float? = null,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    /** Tanggal pengukuran yyyy-MM-dd menurut zona waktu Asia/Jakarta. */
    @SerializedName("recorded_date") val recordedDate: String? = null
)

/** Bungkus paginasi Laravel: daftar ada di `data`. */
data class PageDto<T>(
    val data: List<T>? = null,
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1
)

/** Field null tidak ikut dikirim. Kirim pasangan (tinggi+berat) atau (sistolik+diastolik), tidak boleh sebagian. */
data class CreateHealthRecordRequest(
    val height: Float? = null,
    val weight: Float? = null,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    /** ISO-8601 UTC, mis. 2026-11-13T05:00:00Z. */
    @SerializedName("recorded_at") val recordedAt: String
)

/** Catatan mandiri dari /personal-medical-records. */
data class PersonalRecordDto(
    @SerializedName("personal_medical_record_id") val id: Long = 0,
    /** yyyy-MM-dd */
    @SerializedName("visit_date") val visitDate: String? = null,
    @SerializedName("facility_name") val facilityName: String? = null,
    val complaint: String? = null,
    val medications: String? = null
)

data class PersonalRecordRequest(
    @SerializedName("visit_date") val visitDate: String,
    @SerializedName("facility_name") val facilityName: String,
    val complaint: String,
    val medications: String
)

/** Bungkus respons /medical-records: daftar ada di `data.data`. */
data class ClinicRecordsData(
    val data: List<ClinicEncounterDto>? = null
)

data class ClinicEncounterDto(
    @SerializedName("encounter_uuid") val encounterUuid: String? = null,
    @SerializedName("visit_date") val visitDate: String? = null,
    @SerializedName("facility_name") val facilityName: String? = null,
    val diagnosis: String? = null,
    val complaint: String? = null,
    val medications: List<ClinicMedicationDto>? = null
)

data class ClinicMedicationDto(
    val drug: String? = null
)
