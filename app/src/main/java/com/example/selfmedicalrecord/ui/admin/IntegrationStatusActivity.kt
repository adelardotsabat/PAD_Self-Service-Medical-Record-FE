package com.example.selfmedicalrecord.ui.admin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.databinding.ActivityIntegrationStatusBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import kotlinx.coroutines.launch

/** Status Integrasi: ringkasan dan daftar koneksi ke sistem medis eksternal. */
class IntegrationStatusActivity : AdminActivity() {

    private lateinit var binding: ActivityIntegrationStatusBinding
    private val viewModel: IntegrationViewModel by viewModels()
    private val adapter = IntegrationAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!ensureAdmin()) return

        binding = ActivityIntegrationStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.rvEndpoints.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvTotal.text = state.total.toString()
                    binding.tvConnected.text = state.connected.toString()
                    binding.tvFailed.text = state.failed.toString()
                    adapter.submitList(state.endpoints)
                }
            }
        }
    }
}
