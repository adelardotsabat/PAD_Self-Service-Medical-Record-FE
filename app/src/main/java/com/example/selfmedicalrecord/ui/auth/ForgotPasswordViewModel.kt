package com.example.selfmedicalrecord.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.auth.AuthRepository
import com.example.selfmedicalrecord.data.remote.ApiException
import com.example.selfmedicalrecord.data.remote.NetworkException
import com.example.selfmedicalrecord.utils.Validators
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tiga langkah lupa sandi: email -> kode 6 digit -> kata sandi baru. */
enum class ForgotStep { EMAIL, CODE, NEW_PASSWORD }

data class ForgotPasswordUiState(
    val step: ForgotStep = ForgotStep.EMAIL,
    /** Email yang sudah dikirimi kode (terisi sejak langkah kode). */
    val email: String = "",
    val isLoading: Boolean = false,
    /** Sisa detik sebelum kode boleh dikirim ulang; 0 berarti boleh. */
    val resendSeconds: Int = 0,
    @StringRes val emailError: Int? = null,
    @StringRes val codeError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmError: Int? = null,
    @StringRes val formError: Int? = null
)

/** Kejadian sekali: kata sandi berhasil diganti. */
data object ResetCompleted

class ForgotPasswordViewModel(
    private val repository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    // Channel agar dialog sukses hanya muncul sekali (tidak terulang saat layar diputar).
    private val _events = Channel<ResetCompleted>(Channel.BUFFERED)
    val events: Flow<ResetCompleted> = _events.receiveAsFlow()

    // Token sekali pakai dari langkah kode; hanya disimpan di memori.
    private var resetToken: String? = null
    private var cooldownJob: Job? = null

    fun onInputChanged() {
        _uiState.update {
            it.copy(emailError = null, codeError = null, passwordError = null, confirmError = null, formError = null)
        }
    }

    /** Langkah 1: kirim kode ke email. */
    fun sendCode(email: String) {
        if (_uiState.value.isLoading) return

        val emailError = Validators.email(email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError, formError = null) }
            return
        }

        val target = email.trim()
        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            repository.requestPasswordReset(target)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, step = ForgotStep.CODE, email = target)
                    }
                    startCooldown()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, formError = error.toFormError()) }
                }
        }
    }

    /** Langkah 2: tukar kode dengan token reset. */
    fun verifyCode(code: String) {
        val state = _uiState.value
        if (state.isLoading) return

        val codeError = when {
            code.isBlank() -> R.string.error_code_required
            code.length != CODE_LENGTH || !code.all { it.isDigit() } -> R.string.error_code_format
            else -> null
        }
        if (codeError != null) {
            _uiState.update { it.copy(codeError = codeError, formError = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            repository.verifyPasswordResetCode(state.email, code)
                .onSuccess { token ->
                    resetToken = token
                    cooldownJob?.cancel()
                    _uiState.update { it.copy(isLoading = false, step = ForgotStep.NEW_PASSWORD) }
                }
                .onFailure { error ->
                    _uiState.update {
                        if (error is ApiException && error.httpCode == 422) {
                            it.copy(isLoading = false, codeError = R.string.error_code_invalid)
                        } else {
                            it.copy(isLoading = false, formError = error.toFormError())
                        }
                    }
                }
        }
    }

    /** Kirim ulang kode di langkah 2 (hanya jika hitung mundur sudah habis). */
    fun resendCode() {
        val state = _uiState.value
        if (state.isLoading || state.resendSeconds > 0) return

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            repository.requestPasswordReset(state.email)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    startCooldown()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, formError = error.toFormError()) }
                }
        }
    }

    /** Langkah 3: simpan kata sandi baru. */
    fun resetPassword(password: String, confirmation: String) {
        val state = _uiState.value
        val token = resetToken
        if (state.isLoading || token == null) return

        val passwordError = Validators.newPassword(password)
        val confirmError = Validators.confirmPassword(password, confirmation)
        if (passwordError != null || confirmError != null) {
            _uiState.update {
                it.copy(passwordError = passwordError, confirmError = confirmError, formError = null)
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            repository.resetPassword(state.email, token, password)
                .onSuccess {
                    resetToken = null
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(ResetCompleted)
                }
                .onFailure { error ->
                    _uiState.update {
                        if (error is ApiException && error.httpCode == 422) {
                            it.copy(isLoading = false, formError = R.string.error_reset_expired)
                        } else {
                            it.copy(isLoading = false, formError = error.toFormError())
                        }
                    }
                }
        }
    }

    /**
     * Tombol kembali. Dari langkah kode, kembali ke langkah email (true = sudah ditangani di sini).
     * Dari langkah lain, false: Activity yang menutup layar.
     */
    fun goBack(): Boolean {
        if (_uiState.value.step != ForgotStep.CODE) return false
        cooldownJob?.cancel()
        _uiState.update {
            it.copy(step = ForgotStep.EMAIL, resendSeconds = 0, codeError = null, formError = null)
        }
        return true
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (left in RESEND_COOLDOWN_SECONDS downTo 1) {
                _uiState.update { it.copy(resendSeconds = left) }
                delay(1_000)
            }
            _uiState.update { it.copy(resendSeconds = 0) }
        }
    }

    private fun Throwable.toFormError(): Int = when {
        this is NetworkException -> R.string.error_network
        this is ApiException && httpCode == 429 -> R.string.error_resend_wait
        else -> R.string.error_reset_failed
    }

    private companion object {
        const val CODE_LENGTH = 6
        const val RESEND_COOLDOWN_SECONDS = 60
    }
}
