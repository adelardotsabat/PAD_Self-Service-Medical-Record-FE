package com.example.selfmedicalrecord.ui.auth

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.ActivityForgotPasswordBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.example.selfmedicalrecord.utils.hideKeyboard
import com.example.selfmedicalrecord.utils.showMessage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Lupa kata sandi dalam tiga langkah di satu layar: email -> kode 6 digit -> kata sandi baru.
 * Dibuka dari LoginActivity.
 */
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        // Isi otomatis dengan email yang sudah diketik di layar Login (jika ada).
        if (savedInstanceState == null) {
            binding.etEmail.setText(intent.getStringExtra(EXTRA_EMAIL).orEmpty().trim())
        }

        binding.btnBack.setOnClickListener { handleBack() }
        binding.tvBackToLogin.setOnClickListener { finish() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = handleBack()
        })

        listOf(binding.etEmail, binding.etCode, binding.etPassword, binding.etConfirm)
            .forEach { it.doAfterTextChanged { viewModel.onInputChanged() } }

        binding.btnSend.setOnClickListener { sendCode() }
        binding.etEmail.setOnEditorActionListener(doneAction { sendCode() })

        binding.btnVerify.setOnClickListener { verifyCode() }
        binding.etCode.setOnEditorActionListener(doneAction { verifyCode() })
        binding.tvResend.setOnClickListener { viewModel.resendCode() }

        binding.btnResetPassword.setOnClickListener { resetPassword() }
        binding.etConfirm.setOnEditorActionListener(doneAction { resetPassword() })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { render(it) } }
                launch { viewModel.events.collect { showDoneDialog() } }
            }
        }
    }

    private fun handleBack() {
        hideKeyboard()
        if (!viewModel.goBack()) finish()
    }

    private fun doneAction(action: () -> Unit) =
        android.widget.TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                action()
                true
            } else {
                false
            }
        }

    private fun sendCode() {
        hideKeyboard()
        viewModel.sendCode(binding.etEmail.text?.toString().orEmpty())
    }

    private fun verifyCode() {
        hideKeyboard()
        viewModel.verifyCode(binding.etCode.text?.toString().orEmpty().trim())
    }

    private fun resetPassword() {
        hideKeyboard()
        viewModel.resetPassword(
            password = binding.etPassword.text?.toString().orEmpty(),
            confirmation = binding.etConfirm.text?.toString().orEmpty()
        )
    }

    private fun render(state: ForgotPasswordUiState) {
        binding.groupEmail.isVisible = state.step == ForgotStep.EMAIL
        binding.groupCode.isVisible = state.step == ForgotStep.CODE
        binding.groupPassword.isVisible = state.step == ForgotStep.NEW_PASSWORD

        when (state.step) {
            ForgotStep.EMAIL -> {
                binding.tvTitle.setText(R.string.forgot_title)
                binding.tvSubtitle.setText(R.string.forgot_subtitle)
            }
            ForgotStep.CODE -> {
                binding.tvTitle.setText(R.string.forgot_code_title)
                binding.tvSubtitle.text = getString(R.string.forgot_code_subtitle, state.email)
            }
            ForgotStep.NEW_PASSWORD -> {
                binding.tvTitle.setText(R.string.forgot_new_title)
                binding.tvSubtitle.setText(R.string.forgot_new_subtitle)
            }
        }

        binding.tilEmail.error = state.emailError?.let { getString(it) }
        binding.tilCode.error = state.codeError?.let { getString(it) }
        binding.tilPassword.error = state.passwordError?.let { getString(it) }
        binding.tilConfirm.error = state.confirmError?.let { getString(it) }
        binding.tvFormError.showMessage(state.formError)

        val loading = state.isLoading
        binding.btnSend.isEnabled = !loading
        binding.btnSend.setText(if (loading) R.string.action_loading else R.string.forgot_send)
        binding.btnVerify.isEnabled = !loading
        binding.btnVerify.setText(if (loading) R.string.action_loading else R.string.forgot_continue)
        binding.btnResetPassword.isEnabled = !loading
        binding.btnResetPassword.setText(
            if (loading) R.string.action_loading else R.string.forgot_save_password
        )

        val canResend = state.resendSeconds == 0 && !loading
        binding.tvResend.isEnabled = canResend
        binding.tvResend.alpha = if (canResend) 1f else 0.5f
        binding.tvResend.text = if (state.resendSeconds > 0) {
            getString(R.string.verify_resend_wait, state.resendSeconds)
        } else {
            getString(R.string.verify_resend)
        }
    }

    private fun showDoneDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.forgot_done_title)
            .setMessage(R.string.forgot_done_message)
            .setCancelable(false)
            .setPositiveButton(R.string.forgot_ok) { _, _ -> finish() }
            .show()
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}
