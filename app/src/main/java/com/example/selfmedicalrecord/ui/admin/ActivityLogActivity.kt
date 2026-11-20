package com.example.selfmedicalrecord.ui.admin

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.ActivityActivityLogBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import kotlinx.coroutines.launch

/** Log Aktivitas: jejak audit dengan filter kategori. */
class ActivityLogActivity : AdminActivity() {

    private lateinit var binding: ActivityActivityLogBinding
    private val viewModel: ActivityLogViewModel by viewModels()
    private val adapter = LogAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!ensureAdmin()) return

        binding = ActivityActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.rvLogs.adapter = adapter

        binding.chipAll.setOnClickListener { viewModel.onFilterSelected(LogFilter.ALL) }
        binding.chipLogin.setOnClickListener { viewModel.onFilterSelected(LogFilter.LOGIN) }
        binding.chipData.setOnClickListener { viewModel.onFilterSelected(LogFilter.DATA) }
        binding.chipSystem.setOnClickListener { viewModel.onFilterSelected(LogFilter.SYSTEM) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    styleChip(binding.chipAll, state.filter == LogFilter.ALL)
                    styleChip(binding.chipLogin, state.filter == LogFilter.LOGIN)
                    styleChip(binding.chipData, state.filter == LogFilter.DATA)
                    styleChip(binding.chipSystem, state.filter == LogFilter.SYSTEM)
                    adapter.submitList(state.entries)
                    binding.tvEmpty.isVisible = state.entries.isEmpty()
                }
            }
        }
    }

    private fun styleChip(chip: TextView, selected: Boolean) {
        chip.setBackgroundResource(if (selected) R.drawable.bg_pill_active else R.drawable.bg_chip_outline)
        chip.setTextColor(
            ContextCompat.getColor(this, if (selected) R.color.white else R.color.text_primary)
        )
    }
}
