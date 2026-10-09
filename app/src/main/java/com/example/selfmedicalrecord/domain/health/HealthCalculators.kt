package com.example.selfmedicalrecord.domain.health

import androidx.annotation.StringRes
import com.example.selfmedicalrecord.R
import kotlin.math.roundToInt

/** Warna status pada badge dan angka: hijau (baik), kuning (perlu perhatian), merah (waspada). */
enum class StatusTone { GOOD, CAUTION, ALERT }

enum class BmiCategory(@StringRes val label: Int, val tone: StatusTone) {
    UNDERWEIGHT(R.string.bmi_underweight, StatusTone.CAUTION),
    IDEAL(R.string.bmi_ideal, StatusTone.GOOD),
    OVERWEIGHT(R.string.bmi_overweight, StatusTone.CAUTION),
    OBESE(R.string.bmi_obese, StatusTone.ALERT)
}

/**
 * Batas BMI mengikuti rancangan (rentang normal 18,5-22,9), yaitu kriteria Asia-Pasifik.
 * TODO(tim): konfirmasi ke pihak medis apakah memakai kriteria ini atau kriteria Kemenkes.
 */
object BmiCalculator {

    const val IDEAL_MIN = 18.5f
    const val IDEAL_MAX = 22.9f
    private const val OVERWEIGHT_FROM = 23.0f
    private const val OBESE_FROM = 25.0f

    fun bmi(weightKg: Float, heightCm: Float): Float {
        val heightM = heightCm / 100f
        return weightKg / (heightM * heightM)
    }

    /** Kategori dihitung dari nilai yang dibulatkan 1 desimal, sama dengan yang tampil di layar. */
    fun category(bmi: Float): BmiCategory {
        val shown = (bmi * 10).roundToInt() / 10f
        return when {
            shown < IDEAL_MIN -> BmiCategory.UNDERWEIGHT
            shown < OVERWEIGHT_FROM -> BmiCategory.IDEAL
            shown < OBESE_FROM -> BmiCategory.OVERWEIGHT
            else -> BmiCategory.OBESE
        }
    }
}

enum class BloodPressureCategory(
    @StringRes val label: Int,
    val tone: StatusTone,
    /** True jika perlu menampilkan peringatan setelah data disimpan. */
    val isAnomaly: Boolean
) {
    LOW(R.string.bp_low, StatusTone.CAUTION, true),
    NORMAL(R.string.bp_normal, StatusTone.GOOD, false),
    ELEVATED(R.string.bp_elevated, StatusTone.CAUTION, false),
    HIGH(R.string.bp_high, StatusTone.ALERT, true)
}

/**
 * Klasifikasi sederhana untuk dewasa, ditampilkan sebagai informasi, bukan diagnosis.
 * TODO(tim): batas ini HARUS dikonfirmasi pihak medis sebelum rilis. Perhatikan juga contoh
 *  di Figma (132/80 berlabel "Normal") yang berbeda dari klasifikasi ini.
 */
object BloodPressureClassifier {

    fun classify(systolic: Int, diastolic: Int): BloodPressureCategory = when {
        systolic < 90 || diastolic < 60 -> BloodPressureCategory.LOW
        systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.HIGH
        systolic >= 120 || diastolic >= 80 -> BloodPressureCategory.ELEVATED
        else -> BloodPressureCategory.NORMAL
    }
}
