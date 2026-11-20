package com.example.selfmedicalrecord.ui.result

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.food.CalorieEstimate
import com.example.selfmedicalrecord.databinding.ActivityHasilKaloriBinding
import com.example.selfmedicalrecord.databinding.ItemFoodEstimateBinding
import com.example.selfmedicalrecord.utils.applyFigmaShadow
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.example.selfmedicalrecord.utils.decodeSampledBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Hasil estimasi kalori dari foto makanan. Foto datang lewat [Intent.getData]
 * (file hasil kamera atau content:// dari galeri). Tombol back kembali ke layar scan.
 */
class HasilKaloriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHasilKaloriBinding
    private val viewModel: HasilKaloriViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val photoUri = intent.data
        if (photoUri == null) {
            finish() // dibuka tanpa foto
            return
        }

        binding = ActivityHasilKaloriBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()
        binding.cardTotal.applyFigmaShadow(6f)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRetry.setOnClickListener { viewModel.retry() }

        showPhoto(photoUri)
        viewModel.start(photoUri)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun showPhoto(uri: Uri) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                decodeSampledBitmap(applicationContext, uri, PHOTO_MAX_SIDE_PX)
            }
            if (bitmap != null) binding.ivFood.setImageBitmap(bitmap)
        }
    }

    private fun render(state: HasilKaloriUiState) {
        binding.progress.isVisible = state.isLoading
        binding.layoutError.isVisible = state.hasError
        binding.layoutContent.isVisible = state.estimate != null
        state.estimate?.let(::bindEstimate)
    }

    private fun bindEstimate(estimate: CalorieEstimate) {
        binding.tvTotal.text = getString(
            R.string.kalori_result_total_value,
            estimate.totalMinKcal,
            estimate.totalMaxKcal
        )
        binding.tvAccuracy.text = getString(R.string.kalori_result_accuracy, estimate.accuracyPercent)

        binding.layoutItems.removeAllViews()
        estimate.items.forEach { item ->
            val row = ItemFoodEstimateBinding.inflate(layoutInflater, binding.layoutItems, false)
            row.tvName.text = item.name
            row.tvKcal.text = getString(R.string.kalori_result_item_value, item.minKcal, item.maxKcal)
            binding.layoutItems.addView(row.root)
        }
    }

    companion object {
        private const val PHOTO_MAX_SIDE_PX = 1280

        /** [photoUri] boleh file:// (hasil kamera) atau content:// (galeri); izin baca ikut diteruskan. */
        fun newIntent(context: Context, photoUri: Uri): Intent =
            Intent(context, HasilKaloriActivity::class.java).apply {
                data = photoUri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
    }
}
