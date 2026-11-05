package com.example.selfmedicalrecord.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.auth.AuthRepository
import com.example.selfmedicalrecord.data.auth.PatientProfile
import com.example.selfmedicalrecord.data.remote.ApiException
import com.example.selfmedicalrecord.data.remote.NetworkException
import com.example.selfmedicalrecord.utils.Validators
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Register: buat akun, verifikasi email dengan kode, lalu lengkapi data diri. */
enum class RegisterStep { ACCOUNT, VERIFY, PROFILE }

data class RegisterUiState(
    val step: RegisterStep = RegisterStep.ACCOUNT,
    val isLoading: Boolean = false,
    val birthDateMillis: Long? = null,

    // Langkah 1
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,

    // Langkah 2 (verifikasi)
    /** Email tujuan kode, ditampilkan di teks petunjuk. */
    val verifyEmail: String = "",
    @StringRes val codeError: Int? = null,
    /** Sisa detik sebelum kode boleh dikirim ulang; 0 = boleh. */
    val resendSeconds: Int = 0,

    // Langkah 3 (data diri)
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
        codeError = null,
        fullNameError = null,
        nikError = null,
        birthDateError = null,
        formError = null
    )
}

class RegisterViewModel(
    private val repository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Disimpan di memori saja (tidak ditulis ke disk), dipakai untuk login otomatis
    // setelah email terverifikasi.
    private var pendingEmail = ""
    private var pendingPassword = ""
    private var cooldownJob: Job? = null

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
                    pendingEmail = email.trim()
                    pendingPassword = password
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            step = RegisterStep.VERIFY,
                            verifyEmail = pendingEmail
                        )
                    }
                    startResendCooldown()
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        if (error is ApiException && error.httpCode == 422 &&
                            error.fieldErrors.containsKey("email")
                        ) {
                            state.copy(isLoading = false, emailError = R.string.error_email_taken)
                        } else {
                            state.copy(isLoading = false, formError = registerErrorRes(error))
                        }
                    }
                }
        }
    }

    /** Memverifikasi kode; jika benar, langsung login supaya data diri bisa disimpan. */
    fun submitCode(code: String) {
        if (_uiState.value.isLoading) return

        val codeError = when {
            code.isBlank() -> R.string.error_code_required
            code.length != CODE_LENGTH || !code.all { it.isDigit() } -> R.string.error_code_format
            else -> null
        }
        if (codeError != null) {
            _uiState.update { state -> state.copy(codeError = codeError, formError = null) }
            return
        }

        _uiState.update { state -> state.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            repository.verifyEmail(pendingEmail, code)
                .onSuccess {
                    repository.login(pendingEmail, pendingPassword)
                        .onSuccess {
                            pendingPassword = ""
                            _uiState.update { state ->
                                state.copy(isLoading = false, step = RegisterStep.PROFILE)
                            }
                        }
                        .onFailure { error ->
                            _uiState.update { state ->
                                state.copy(isLoading = false, formError = loginErrorRes(error))
                            }
                        }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        if (error is ApiException && error.httpCode == 422) {
                            state.copy(isLoading = false, codeError = R.string.error_code_invalid)
                        } else {
                            state.copy(isLoading = false, formError = registerErrorRes(error))
                        }
                    }
                }
        }
    }

    fun resendCode() {
        val state = _uiState.value
        if (state.isLoading || state.resendSeconds > 0) return

        viewModelScope.launch {
            repository.resendVerificationCode(pendingEmail)
                .onSuccess { startResendCooldown() }
                .onFailure { error ->
                    val message = if (error is ApiException && error.httpCode == 429) {
                        R.string.error_resend_wait
                    } else {
                        registerErrorRes(error)
                    }
                    _uiState.update { s -> s.copy(formError = message) }
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
                .onFailure { error ->
                    _uiState.update { state ->
                        val message = if (error is NetworkException) {
                            R.string.error_network
                        } else {
                            R.string.error_save_profile_failed
                        }
                        state.copy(isLoading = false, formError = message)
                    }
                }
        }
    }

    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (second in RESEND_COOLDOWN_SECONDS downTo 1) {
                _uiState.update { state -> state.copy(resendSeconds = second) }
                delay(1_000)
            }
            _uiState.update { state -> state.copy(resendSeconds = 0) }
        }
    }

    @StringRes
    private fun registerErrorRes(error: Throwable): Int = when {
        error is NetworkException -> R.string.error_network
        error is ApiException && error.httpCode == 503 -> R.string.error_email_send_failed
        else -> R.string.error_register_failed
    }

    @StringRes
    private fun loginErrorRes(error: Throwable): Int =
        if (error is NetworkException) R.string.error_network else R.string.error_login_failed

    private companion object {
        const val CODE_LENGTH = 6
        const val RESEND_COOLDOWN_SECONDS = 60
    }
}
