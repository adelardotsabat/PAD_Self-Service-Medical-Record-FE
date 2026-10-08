package com.example.selfmedicalrecord.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.MainActivity
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.ActivityRegisterBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.example.selfmedicalrecord.utils.hideKeyboard
import com.example.selfmedicalrecord.utils.setRequiredLabel
import com.example.selfmedicalrecord.utils.showMessage
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    // MaterialDatePicker mengembalikan tengah malam UTC, jadi format juga dengan UTC
    // agar tanggal yang tampil tidak bergeser sehari.
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("id-ID")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        binding.tvLabelFullName.setRequiredLabel(R.string.label_full_name)
        binding.tvLabelNik.setRequiredLabel(R.string.label_nik)
        binding.tvLabelBirthDate.setRequiredLabel(R.string.label_birth_date)

        setupListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun setupListeners() {
        // Catatan: di langkah 2 akun sudah terbuat, jadi back keluar dari layar register.
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.tvLogin.setOnClickListener { finish() }

        listOf(
            binding.etEmail,
            binding.etPassword,
            binding.etConfirmPassword,
            binding.etFullName,
            binding.etNik
        ).forEach { field -> field.doAfterTextChanged { viewModel.onInputChanged() } }

        binding.etConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitAccount()
                true
            } else {
                false
            }
        }

        binding.btnRegister.setOnClickListener { submitAccount() }
        binding.btnSave.setOnClickListener { submitProfile() }

        binding.etBirthDate.setOnClickListener { showDatePicker() }
        binding.tilBirthDate.setEndIconOnClickListener { showDatePicker() }
    }

    private fun submitAccount() {
        hideKeyboard()
        viewModel.submitAccount(
            email = binding.etEmail.text?.toString().orEmpty(),
            password = binding.etPassword.text?.toString().orEmpty(),
            confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()
        )
    }

    private fun submitProfile() {
        hideKeyboard()
        viewModel.submitProfile(
            fullName = binding.etFullName.text?.toString().orEmpty(),
            nik = binding.etNik.text?.toString().orEmpty(),
            gender = binding.actGender.text?.toString()?.takeIf { it.isNotBlank() }
        )
    }

    private fun showDatePicker() {
        hideKeyboard()
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now()) // tanggal lahir tidak boleh di masa depan
            .build()
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.label_birth_date)
            .setCalendarConstraints(constraints)
        viewModel.uiState.value.birthDateMillis?.let { builder.setSelection(it) }
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { millis -> viewModel.onBirthDateSelected(millis) }
        picker.show(supportFragmentManager, BIRTH_DATE_PICKER_TAG)
    }

    private fun render(state: RegisterUiState) {
        val isAccountStep = state.step == RegisterStep.ACCOUNT
        binding.groupAccount.isVisible = isAccountStep
        binding.groupProfile.isVisible = !isAccountStep

        // Langkah 1
        binding.tilEmail.error = state.emailError?.let { getString(it) }
        binding.tilPassword.error = state.passwordError?.let { getString(it) }
        binding.tilConfirmPassword.error = state.confirmPasswordError?.let { getString(it) }
        binding.tvAccountError.showMessage(state.formError.takeIf { isAccountStep })
        binding.btnRegister.isEnabled = !state.isLoading
        binding.btnRegister.setText(
            if (state.isLoading) R.string.action_loading else R.string.action_register
        )

        // Langkah 2
        binding.tilFullName.error = state.fullNameError?.let { getString(it) }
        binding.tilNik.error = state.nikError?.let { getString(it) }
        binding.tilBirthDate.error = state.birthDateError?.let { getString(it) }
        binding.etBirthDate.setText(
            state.birthDateMillis?.let { dateFormat.format(Date(it)) }.orEmpty()
        )
        binding.tvProfileError.showMessage(state.formError.takeIf { !isAccountStep })
        binding.btnSave.isEnabled = !state.isLoading
        binding.btnSave.setText(
            if (state.isLoading) R.string.action_loading else R.string.action_save
        )

        if (state.isFinished) {
            val intent = Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    private companion object {
        const val BIRTH_DATE_PICKER_TAG = "birth_date_picker"
    }
}
