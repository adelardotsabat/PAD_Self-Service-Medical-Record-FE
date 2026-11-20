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
import com.example.selfmedicalrecord.data.admin.AdminUser
import com.example.selfmedicalrecord.databinding.ActivityUserListBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/** Daftar User: cari, tambah, dan hapus akun. */
class UserListActivity : AdminActivity() {

    private lateinit var binding: ActivityUserListBinding
    private val viewModel: UserListViewModel by viewModels()
    private val adapter = UserAdapter { anchor, user -> showMenu(anchor, user) }

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
        binding.tvActivityLink.setOnClickListener {
            startActivity(Intent(this, UserActivityActivity::class.java))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { users ->
                    adapter.submitList(users)
                    binding.tvEmpty.isVisible = users.isEmpty()
                }
            }
        }
    }

    private fun showMenu(anchor: android.view.View, user: AdminUser) {
        PopupMenu(this, anchor).apply {
            menu.add(0, MENU_DELETE, 0, R.string.admin_user_delete)
            setOnMenuItemClickListener { item ->
                if (item.itemId == MENU_DELETE) confirmDelete(user)
                true
            }
            show()
        }
    }

    private fun confirmDelete(user: AdminUser) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.admin_user_delete_title)
            .setMessage(getString(R.string.admin_user_delete_message, user.name))
            .setNegativeButton(R.string.admin_cancel, null)
            .setPositiveButton(R.string.admin_delete) { _, _ -> viewModel.delete(user.id) }
            .show()
    }

    private companion object {
        const val MENU_DELETE = 1
    }
}
