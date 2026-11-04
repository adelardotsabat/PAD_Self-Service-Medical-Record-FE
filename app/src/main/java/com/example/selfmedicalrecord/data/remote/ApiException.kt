package com.example.selfmedicalrecord.data.remote

import com.google.gson.Gson
import com.google.gson.JsonParseException
import java.io.IOException
import retrofit2.Response

/** Server membalas, tetapi dengan kode gagal (401, 403, 422, 429, 500, ...). */
class ApiException(
    val httpCode: Int,
    override val message: String,
    val fieldErrors: Map<String, List<String>> = emptyMap()
) : Exception(message)

/** Tidak bisa menghubungi server (tidak ada internet, server mati, alamat salah). */
class NetworkException(cause: Throwable) : Exception(cause.message, cause)

private val gson = Gson()

/**
 * Menjalankan satu panggilan Retrofit dan mengubah hasilnya menjadi [Result].
 * Sukses -> [ApiEnvelope]; gagal -> [ApiException] atau [NetworkException].
 */
internal suspend fun <T> safeApiCall(
    call: suspend () -> Response<ApiEnvelope<T>>
): Result<ApiEnvelope<T>> {
    val response = try {
        call()
    } catch (e: IOException) {
        return Result.failure(NetworkException(e))
    }

    if (response.isSuccessful) {
        val body = response.body()
        return if (body != null && body.success) {
            Result.success(body)
        } else {
            Result.failure(ApiException(response.code(), body?.message ?: "Respons server kosong"))
        }
    }

    val envelope = try {
        gson.fromJson(response.errorBody()?.string(), ApiEnvelope::class.java)
    } catch (e: JsonParseException) {
        null
    }
    return Result.failure(
        ApiException(
            httpCode = response.code(),
            message = envelope?.message ?: "Terjadi kesalahan (${response.code()})",
            fieldErrors = envelope?.errors.orEmpty()
        )
    )
}
