package com.example.selfmedicalrecord.ui.admin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.databinding.ActivityUserActivityBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import kotlinx.coroutines.launch

/** Data Pengguna: kapan terakhir aktif dan apakah akunnya sedang aktif. */
class UserActivityActivity : AdminActivity() {

    private lateinit var binding: ActivityUserActivityBinding
    private val viewModel: UserActivityViewModel by viewModels()
    private val adapter = UserActivityAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!ensureAdmin()) return

        binding = ActivityUserActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.rvUsers.adapter = adapter
        binding.etSearch.doAfterTextChanged { viewModel.onQueryChanged(it?.toString().orEmpty()) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect { items ->
                    adapter.submitList(items)
                    binding.tvEmpty.isVisible = items.isEmpty()
                }
            }
        }
    }
}
