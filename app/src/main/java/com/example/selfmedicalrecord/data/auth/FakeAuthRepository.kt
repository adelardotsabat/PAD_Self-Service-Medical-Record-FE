package com.example.selfmedicalrecord.data.auth

import kotlinx.coroutines.delay

/**
 * Implementasi palsu untuk pengembangan UI tanpa backend.
 *
 * Semua permintaan berhasil setelah jeda singkat (supaya state loading terlihat),
 * kecuali login dengan kata sandi [DEMO_WRONG_PASSWORD], untuk mencoba tampilan error.
 */
class FakeAuthRepository : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        delay(FAKE_LATENCY_MS)
        return if (password == DEMO_WRONG_PASSWORD) {
            Result.failure(IllegalArgumentException("Kredensial salah"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        delay(FAKE_LATENCY_MS)
        return Result.success(Unit)
    }

    override suspend fun saveProfile(profile: PatientProfile): Result<Unit> {
        delay(FAKE_LATENCY_MS)
        return Result.success(Unit)
    }

    private companion object {
        const val FAKE_LATENCY_MS = 800L
        const val DEMO_WRONG_PASSWORD = "salah"
    }
}
