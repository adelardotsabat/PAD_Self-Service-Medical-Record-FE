package com.example.selfmedicalrecord.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.admin.AccountStatus
import com.example.selfmedicalrecord.data.admin.AdminUser
import com.example.selfmedicalrecord.databinding.ActivityUserListBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/** Manajemen Pengguna: ringkasan akun, cari, tambah, serta aktifkan / nonaktifkan akun. */
class UserListActivity : AdminActivity() {

    private lateinit var binding: ActivityUserListBinding
    private val viewModel: UserListViewModel by viewModels()
    // Baris tidak bisa diklik: layar "Data Pengguna" masih data contoh dan tidak sesuai daftar asli.
    private val adapter = UserAdapter(
        onItemClick = null,
        onMoreClick = { anchor, user -> showMenu(anchor, user) }
    )

    private var isLoading = true
    private var isEmpty = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!ensureAdmin()) return

        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.rvUsers.adapter = adapter
        binding.etSearch.doAfterTextChanged { viewModel.onQueryChanged(it?.toString().orEmpty()) }
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddUserActivity::class.java))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.users.collect { users ->
                        adapter.submitList(users)
                        isEmpty = users.isEmpty()
                        renderEmpty()
                    }
                }
                launch {
                    viewModel.isLoading.collect { loading ->
                        isLoading = loading
                        renderEmpty()
                    }
                }
                launch {
                    viewModel.messages.collect { messageRes ->
                        Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_LONG).show()
                    }
                }
                launch {
                    viewModel.stats.collect { stats ->
                        binding.tvStatTotal.text = stats.total.toString()
                        binding.tvStatActive.text = stats.active.toString()
                        binding.tvStatInactive.text = stats.inactive.toString()
                    }
                }
            }
        }
    }

    // Muat ulang tiap layar tampil, supaya akun baru dari Tambah User langsung muncul.
    override fun onStart() {
        super.onStart()
        viewModel.load()
    }

    /** "Pengguna tidak ditemukan" jangan sempat muncul selama data masih dimuat. */
    private fun renderEmpty() {
        binding.tvEmpty.isVisible = !isLoading && isEmpty
    }

    private fun showMenu(anchor: android.view.View, user: AdminUser) {
        val isActive = user.status == AccountStatus.ACTIVE
        PopupMenu(this, anchor).apply {
            menu.add(
                0, MENU_TOGGLE, 0,
                if (isActive) R.string.admin_user_deactivate else R.string.admin_user_activate
            )
            setOnMenuItemClickListener { item ->
                if (item.itemId == MENU_TOGGLE) confirmToggle(user, activate = !isActive)
                true
            }
            show()
        }
    }

    private fun confirmToggle(user: AdminUser, activate: Boolean) {
        MaterialAlertDialogBuilder(this)
            .setTitle(if (activate) R.string.admin_user_activate_title else R.string.admin_user_deactivate_title)
            .setMessage(
                getString(
                    if (activate) R.string.admin_user_activate_message else R.string.admin_user_deactivate_message,
                    user.name
                )
            )
            .setNegativeButton(R.string.admin_cancel, null)
            .setPositiveButton(
                if (activate) R.string.admin_action_activate else R.string.admin_action_deactivate
            ) { _, _ -> viewModel.setActive(user, activate) }
            .show()
    }

    private companion object {
        const val MENU_TOGGLE = 1
    }
}
