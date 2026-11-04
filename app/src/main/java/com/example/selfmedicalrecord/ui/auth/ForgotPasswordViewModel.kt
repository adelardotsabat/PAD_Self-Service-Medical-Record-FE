package com.example.selfmedicalrecord.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.auth.AuthRepository
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.utils.Validators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    @StringRes val emailError: Int? = null,
    @StringRes val formError: Int? = null
)

/** Kejadian sekali: tautan berhasil dikirim ke [email]. */
data class ResetLinkSent(val email: String)

class ForgotPasswordViewModel(
    private val repository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    // Channel agar dialog sukses hanya muncul sekali (tidak terulang saat layar diputar).
    private val _events = Channel<ResetLinkSent>(Channel.BUFFERED)
    val events: Flow<ResetLinkSent> = _events.receiveAsFlow()

    fun onInputChanged() {
        _uiState.update { it.copy(emailError = null, formError = null) }
    }

    fun sendLink(email: String) {
        if (_uiState.value.isLoading) return

        val emailError = Validators.email(email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError, formError = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            repository.requestPasswordReset(email.trim())
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(ResetLinkSent(email.trim()))
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, formError = R.string.error_reset_failed)
                    }
                }
        }
    }
}
