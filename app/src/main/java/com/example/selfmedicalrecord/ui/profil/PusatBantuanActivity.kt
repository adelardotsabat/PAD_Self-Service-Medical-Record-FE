package com.example.selfmedicalrecord.ui.profil

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.ActivityPusatBantuanBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/** Daftar pertanyaan umum yang bisa dicari dan dibuka-tutup. */
class PusatBantuanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPusatBantuanBinding
    private val viewModel: PusatBantuanViewModel by viewModels()
    private val adapter = FaqAdapter { id -> viewModel.onToggle(id) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPusatBantuanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        binding.rvFaq.adapter = adapter
        binding.btnBack.setOnClickListener { finish() }
        binding.etSearch.doAfterTextChanged { viewModel.onQueryChanged(it?.toString().orEmpty()) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.items)
                    binding.tvEmpty.isVisible = state.items.isEmpty()
                }
            }
        }
    }
}
