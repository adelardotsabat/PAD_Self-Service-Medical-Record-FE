package com.example.selfmedicalrecord.ui.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.admin.UserRole
import com.example.selfmedicalrecord.databinding.ActivityAddUserBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.example.selfmedicalrecord.utils.hideKeyboard
import com.example.selfmedicalrecord.utils.showMessage
import kotlinx.coroutines.launch

/**
 * Tambah User Baru. Backend hanya mengenal SATU peran per akun, jadi dua saklar "Hak Akses"
 * saling melengkapi (admin menyala = pasien mati) dan selalu sejalan dengan dropdown peran.
 */
class AddUserActivity : AdminActivity() {

    private lateinit var binding: ActivityAddUserBinding
    private val viewModel: AddUserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!ensureAdmin()) return

        binding = ActivityAddUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        listOf(binding.etName, binding.etEmail, binding.etPassword, binding.etConfirm)
            .forEach { it.doAfterTextChanged { viewModel.onInputChanged() } }

        val roles = listOf(UserRole.ADMIN, UserRole.USER)
        binding.actvRole.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, roles.map { getString(roleLabel(it)) })
        )
        binding.actvRole.setOnItemClickListener { _, _, position, _ ->
            viewModel.onRoleSelected(roles[position])
        }
        binding.switchAdmin.setOnCheckedChangeListener { _, checked ->
            viewModel.onRoleSelected(if (checked) UserRole.ADMIN else UserRole.USER)
        }
        binding.switchPatient.setOnCheckedChangeListener { _, checked ->
            viewModel.onRoleSelected(if (checked) UserRole.USER else UserRole.ADMIN)
        }

        binding.btnSave.setOnClickListener {
            hideKeyboard()
            viewModel.save(
                name = binding.etName.text?.toString().orEmpty(),
                email = binding.etEmail.text?.toString().orEmpty(),
                password = binding.etPassword.text?.toString().orEmpty(),
                confirm = binding.etConfirm.text?.toString().orEmpty()
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun roleLabel(role: UserRole) =
        if (role == UserRole.ADMIN) R.string.admin_role_admin else R.string.admin_role_user

    private fun render(state: AddUserUiState) {
        binding.tilName.error = state.nameError?.let { getString(it) }
        binding.tilEmail.error = state.emailError?.let { getString(it) }
        binding.tilPassword.error = state.passwordError?.let { getString(it) }
        binding.tilConfirm.error = state.confirmError?.let { getString(it) }
        binding.tvFormError.showMessage(state.formError)

        binding.actvRole.setText(getString(roleLabel(state.role)), false)
        val isAdmin = state.role == UserRole.ADMIN
        if (binding.switchAdmin.isChecked != isAdmin) binding.switchAdmin.isChecked = isAdmin
        if (binding.switchPatient.isChecked == isAdmin) binding.switchPatient.isChecked = !isAdmin

        binding.btnSave.isEnabled = !state.isSaving
        binding.btnSave.setText(if (state.isSaving) R.string.action_loading else R.string.admin_user_save)

        if (state.isSaved) {
            Toast.makeText(this, R.string.admin_user_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
