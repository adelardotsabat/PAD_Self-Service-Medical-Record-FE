package com.example.selfmedicalrecord.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.MainActivity
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.ActivityLoginBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.example.selfmedicalrecord.utils.hideKeyboard
import com.example.selfmedicalrecord.utils.showMessage
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        // TODO: jika sesi login masih valid (token tersimpan), langsung buka MainActivity.

        setupListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.etEmail.doAfterTextChanged { viewModel.onInputChanged() }
        binding.etPassword.doAfterTextChanged { viewModel.onInputChanged() }
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }

        binding.btnLogin.setOnClickListener { submit() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Belum ada layarnya; lihat daftar pertanyaan terbuka di PR.
        binding.tvForgotPassword.setOnClickListener { showComingSoon() }
        binding.btnGoogle.setOnClickListener { showComingSoon() }
    }

    private fun submit() {
        hideKeyboard()
        viewModel.login(
            email = binding.etEmail.text?.toString().orEmpty(),
            password = binding.etPassword.text?.toString().orEmpty()
        )
    }

    private fun render(state: LoginUiState) {
        binding.tilEmail.error = state.emailError?.let { getString(it) }
        binding.tilPassword.error = state.passwordError?.let { getString(it) }
        binding.tvFormError.showMessage(state.formError)

        binding.btnLogin.isEnabled = !state.isLoading
        binding.btnLogin.setText(if (state.isLoading) R.string.action_loading else R.string.action_login)

        if (state.isSuccess) {
            val intent = Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun showComingSoon() {
        Snackbar.make(binding.root, R.string.feature_coming_soon, Snackbar.LENGTH_SHORT).show()
    }
}
