package com.example.selfmedicalrecord.ui.result

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.ActivityHasilBmiBinding
import com.example.selfmedicalrecord.domain.health.BmiCalculator
import com.example.selfmedicalrecord.domain.health.BmiCategory
import com.example.selfmedicalrecord.ui.common.bindStatusBadge
import com.example.selfmedicalrecord.ui.common.textColorRes
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.example.selfmedicalrecord.utils.formatCompact
import com.example.selfmedicalrecord.utils.formatOneDecimal

/** Menampilkan hasil perhitungan BMI dari berat dan tinggi yang dikirim lewat Intent. */
class HasilBmiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHasilBmiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val weightKg = intent.getFloatExtra(EXTRA_WEIGHT_KG, 0f)
        val heightCm = intent.getFloatExtra(EXTRA_HEIGHT_CM, 0f)
        if (weightKg <= 0f || heightCm <= 0f) {
            finish() // dibuka tanpa data yang valid
            return
        }

        binding = ActivityHasilBmiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRecalculate.setOnClickListener { finish() }

        bind(weightKg, heightCm)
    }

    private fun bind(weightKg: Float, heightCm: Float) {
        val bmi = BmiCalculator.bmi(weightKg, heightCm)
        val category = BmiCalculator.category(bmi)
        val bmiText = bmi.formatOneDecimal()
        val categoryText = getString(category.label)

        binding.tvBmiValue.text = bmiText
        binding.tvBmiValue.setTextColor(ContextCompat.getColor(this, category.tone.textColorRes()))
        bindStatusBadge(
            badge = binding.badgeBmiCategory,
            dot = binding.dotBmiCategory,
            label = binding.tvBmiCategory,
            tone = category.tone,
            text = categoryText
        )

        binding.tvWeight.text = getString(R.string.result_value_kg, weightKg.formatCompact())
        binding.tvHeight.text = getString(R.string.result_value_cm, heightCm.formatCompact())
        binding.tvNormalRange.text = getString(
            R.string.result_normal_range,
            BmiCalculator.IDEAL_MIN.formatOneDecimal(),
            BmiCalculator.IDEAL_MAX.formatOneDecimal()
        )
        binding.markerLabel.text = bmiText
        binding.tvNote.setText(noteFor(category))
        binding.tvSummary.text = getString(R.string.result_summary, bmiText, categoryText)

        positionMarker(bmi)
    }

    /** Menaruh penanda di bar sesuai nilai BMI pada skala [SCALE_MIN]..[SCALE_MAX]. */
    private fun positionMarker(bmi: Float) {
        binding.rangeContainer.doOnLayout { container ->
            val fraction = ((bmi - SCALE_MIN) / (SCALE_MAX - SCALE_MIN)).coerceIn(0f, 1f)
            val center = fraction * container.width

            val dot = binding.markerDot
            dot.translationX = (center - dot.width / 2f).coerceIn(0f, (container.width - dot.width).toFloat())

            // Label dijaga agar tidak keluar dari kartu saat BMI mendekati ujung skala.
            val label = binding.markerLabel
            label.translationX =
                (center - label.width / 2f).coerceIn(0f, (container.width - label.width).toFloat())
        }
    }

    @StringRes
    private fun noteFor(category: BmiCategory): Int = when (category) {
        BmiCategory.UNDERWEIGHT -> R.string.result_note_underweight
        BmiCategory.IDEAL -> R.string.result_note_ideal
        BmiCategory.OVERWEIGHT -> R.string.result_note_overweight
        BmiCategory.OBESE -> R.string.result_note_obese
    }

    companion object {
        const val EXTRA_WEIGHT_KG = "extra_weight_kg"
        const val EXTRA_HEIGHT_CM = "extra_height_cm"

        /** Skala bar: bobot segmen di layout (3,5 / 4,5 / 2 / 5) menjumlah 15 dari 15 sampai 30. */
        private const val SCALE_MIN = 15f
        private const val SCALE_MAX = 30f
    }
}
