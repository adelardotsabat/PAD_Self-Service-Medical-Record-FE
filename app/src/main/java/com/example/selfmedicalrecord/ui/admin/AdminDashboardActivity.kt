package com.example.selfmedicalrecord.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.databinding.ActivityAdminDashboardBinding
import com.example.selfmedicalrecord.ui.auth.SessionRouter
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/** Beranda admin: ringkasan sistem dan pintu ke menu administrasi. */
class AdminDashboardActivity : AdminActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val viewModel: AdminDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!ensureAdmin()) return

        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        val name = ServiceLocator.tokenStore.name()?.takeIf { it.isNotBlank() }
            ?: getString(R.string.admin_default_name)
        binding.tvGreeting.text = getString(R.string.admin_greeting, name)

        binding.btnNotifications.setOnClickListener {
            Snackbar.make(binding.root, R.string.feature_coming_soon, Snackbar.LENGTH_SHORT).show()
        }
        binding.cardConfig.setOnClickListener { open(EndpointActivity::class.java) }
        binding.cardMonitor.setOnClickListener { open(IntegrationStatusActivity::class.java) }
        binding.cardLog.setOnClickListener { open(ActivityLogActivity::class.java) }
        binding.cardUsers.setOnClickListener { open(UserListActivity::class.java) }
        binding.cardApi.setOnClickListener { open(IntegrationStatusActivity::class.java) }
        binding.tvLogout.setOnClickListener { logout() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun open(target: Class<*>) = startActivity(Intent(this, target))

    private fun render(state: AdminDashboardUiState) {
        val summary = state.summary ?: return
        binding.tvStatus.setText(
            if (summary.isOptimal) R.string.admin_status_optimal else R.string.admin_status_degraded
        )
        binding.tvUptime.text = getString(R.string.admin_uptime, summary.uptime)
        binding.tvApiSync.text = getString(R.string.admin_last_sync, summary.lastSync)
        if (summary.apiActive) {
            binding.tvApiBadge.bindBadge(
                getString(R.string.admin_active), R.color.badge_green_bg, R.color.badge_green_text
            )
        } else {
            binding.tvApiBadge.bindBadge(
                getString(R.string.admin_inactive), R.color.badge_red_bg, R.color.badge_red_text
            )
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            ServiceLocator.authRepository.logout()
            startActivity(SessionRouter.loggedOutIntent(this@AdminDashboardActivity))
            finish()
        }
    }
}
