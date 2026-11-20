package com.example.selfmedicalrecord.domain.health

import androidx.annotation.StringRes
import com.example.selfmedicalrecord.R

/** Warna status pada badge dan angka: hijau (baik), kuning (perlu perhatian), merah (waspada). */
enum class StatusTone { GOOD, CAUTION, ALERT }

enum class BmiCategory(@StringRes val label: Int, val tone: StatusTone) {
    UNDERWEIGHT(R.string.bmi_underweight, StatusTone.CAUTION),
    IDEAL(R.string.bmi_ideal, StatusTone.GOOD),
    OVERWEIGHT(R.string.bmi_overweight, StatusTone.CAUTION)
}

/**
 * Batas BMI disamakan dengan backend (SelfAssessmentService::bmiCategory):
 * di bawah 18,5 kurang, 18,5 sampai di bawah 25 ideal, 25 ke atas berlebih.
 * Kategori dihitung dari nilai penuh (tanpa pembulatan), sama seperti server.
 */
object BmiCalculator {

    const val IDEAL_MIN = 18.5f

    /** Batas atas yang ditampilkan di layar (ideal berlaku sampai tepat di bawah 25,0). */
    const val IDEAL_MAX = 24.9f
    private const val OVERWEIGHT_FROM = 25.0f

    fun bmi(weightKg: Float, heightCm: Float): Float {
        val heightM = heightCm / 100f
        return weightKg / (heightM * heightM)
    }

    fun category(bmi: Float): BmiCategory = when {
        bmi < IDEAL_MIN -> BmiCategory.UNDERWEIGHT
        bmi < OVERWEIGHT_FROM -> BmiCategory.IDEAL
        else -> BmiCategory.OVERWEIGHT
    }
}

enum class BloodPressureCategory(
    @StringRes val label: Int,
    val tone: StatusTone,
    /** True jika perlu menampilkan peringatan setelah data disimpan. */
    val isAnomaly: Boolean
) {
    NORMAL(R.string.bp_normal, StatusTone.GOOD, false),
    ELEVATED(R.string.bp_elevated, StatusTone.CAUTION, true),
    HIGH(R.string.bp_high, StatusTone.ALERT, true),
    URGENT(R.string.bp_urgent, StatusTone.ALERT, true)
}

/**
 * Klasifikasi disamakan dengan backend (SelfAssessmentService::assessBloodPressure):
 * lebih dari 180/120 darurat, 140/90 ke atas tinggi, 130/80 ke atas meningkat, sisanya normal.
 * Backend tidak punya kategori tekanan rendah, jadi di sini juga tidak ada.
 * Ditampilkan sebagai informasi, bukan diagnosis.
 */
object BloodPressureClassifier {

    fun classify(systolic: Int, diastolic: Int): BloodPressureCategory = when {
        systolic > 180 || diastolic > 120 -> BloodPressureCategory.URGENT
        systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.HIGH
        systolic >= 130 || diastolic >= 80 -> BloodPressureCategory.ELEVATED
        else -> BloodPressureCategory.NORMAL
    }
}
