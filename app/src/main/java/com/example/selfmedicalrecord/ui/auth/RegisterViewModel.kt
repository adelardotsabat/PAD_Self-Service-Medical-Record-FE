package com.example.selfmedicalrecord.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.auth.AuthRepository
import com.example.selfmedicalrecord.data.auth.FakeAuthRepository
import com.example.selfmedicalrecord.data.auth.PatientProfile
import com.example.selfmedicalrecord.utils.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Register terdiri dari dua langkah: membuat akun, lalu melengkapi data diri. */
enum class RegisterStep { ACCOUNT, PROFILE }

data class RegisterUiState(
    val step: RegisterStep = RegisterStep.ACCOUNT,
    val isLoading: Boolean = false,
    val birthDateMillis: Long? = null,

    // Langkah 1
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,

    // Langkah 2
    @StringRes val fullNameError: Int? = null,
    @StringRes val nikError: Int? = null,
    @StringRes val birthDateError: Int? = null,

    /** Error umum dari server, ditampilkan di atas tombol submit langkah yang aktif. */
    @StringRes val formError: Int? = null,

    /** True setelah data diri tersimpan; Activity lanjut ke halaman utama. */
    val isFinished: Boolean = false
) {
    fun withoutErrors() = copy(
        emailError = null,
        passwordError = null,
        confirmPasswordError = null,
        fullNameError = null,
        nikError = null,
        birthDateError = null,
        formError = null
    )
}

class RegisterViewModel(
    private val repository: AuthRepository = FakeAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onInputChanged() {
        _uiState.update { state -> state.withoutErrors() }
    }

    fun onBirthDateSelected(millis: Long) {
        _uiState.update { state -> state.copy(birthDateMillis = millis, birthDateError = null) }
    }

    fun submitAccount(email: String, password: String, confirmPassword: String) {
        if (_uiState.value.isLoading) return

        val emailError = Validators.email(email)
        val passwordError = Validators.newPassword(password)
        val confirmError = Validators.confirmPassword(password, confirmPassword)
        if (emailError != null || passwordError != null || confirmError != null) {
            _uiState.update { state ->
                state.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError,
                    formError = null
                )
            }
            return
        }

        _uiState.update { state -> state.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            repository.register(email.trim(), password)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(isLoading = false, step = RegisterStep.PROFILE)
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(isLoading = false, formError = R.string.error_register_failed)
                    }
                }
        }
    }

    fun submitProfile(fullName: String, nik: String, gender: String?) {
        val current = _uiState.value
        if (current.isLoading) return

        val birthDate = current.birthDateMillis
        val fullNameError = Validators.fullName(fullName)
        val nikError = Validators.nik(nik)
        val birthDateError = if (birthDate == null) R.string.error_birth_date_required else null
        if (fullNameError != null || nikError != null || birthDate == null) {
            _uiState.update { state ->
                state.copy(
                    fullNameError = fullNameError,
                    nikError = nikError,
                    birthDateError = birthDateError,
                    formError = null
                )
            }
            return
        }

        _uiState.update { state -> state.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            val profile = PatientProfile(
                fullName = fullName.trim(),
                nik = nik,
                gender = gender,
                birthDateMillis = birthDate
            )
            repository.saveProfile(profile)
                .onSuccess {
                    _uiState.update { state -> state.copy(isLoading = false, isFinished = true) }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(isLoading = false, formError = R.string.error_save_profile_failed)
                    }
                }
        }
    }
}
