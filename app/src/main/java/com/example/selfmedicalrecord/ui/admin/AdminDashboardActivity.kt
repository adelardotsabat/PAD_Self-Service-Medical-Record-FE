package com.example.selfmedicalrecord.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.databinding.ActivityAdminDashboardBinding
import com.example.selfmedicalrecord.ui.auth.SessionRouter
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import kotlinx.coroutines.launch

/** Beranda admin: sapaan dan pintu ke menu administrasi. */
class AdminDashboardActivity : AdminActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

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

        binding.cardEndpoint.setOnClickListener { open(EndpointActivity::class.java) }
        binding.cardLog.setOnClickListener { open(ActivityLogActivity::class.java) }
        binding.cardUsers.setOnClickListener { open(UserListActivity::class.java) }
        binding.cardAddUser.setOnClickListener { open(AddUserActivity::class.java) }
        binding.tvLogout.setOnClickListener { logout() }
    }

    private fun open(target: Class<*>) = startActivity(Intent(this, target))

    private fun logout() {
        lifecycleScope.launch {
            ServiceLocator.authRepository.logout()
            startActivity(SessionRouter.loggedOutIntent(this@AdminDashboardActivity))
            finish()
        }
    }
}
