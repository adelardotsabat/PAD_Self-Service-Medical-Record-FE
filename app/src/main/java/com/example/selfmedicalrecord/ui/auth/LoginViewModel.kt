package com.example.selfmedicalrecord.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.auth.AuthRepository
import com.example.selfmedicalrecord.data.auth.FakeAuthRepository
import com.example.selfmedicalrecord.utils.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    /** Error umum (mis. kredensial salah), ditampilkan di atas tombol Masuk. */
    @StringRes val formError: Int? = null,
    val isSuccess: Boolean = false
)

/**
 * Parameter repository punya nilai default supaya bisa dibuat lewat `by viewModels()`.
 * Saat API siap, ganti default ini (atau pakai ViewModelProvider.Factory / Hilt).
 */
class LoginViewModel(
    private val repository: AuthRepository = FakeAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Dipanggil saat pengguna mengetik, supaya pesan error hilang. */
    fun onInputChanged() {
        _uiState.update { state ->
            state.copy(emailError = null, passwordError = null, formError = null)
        }
    }

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return

        val emailError = Validators.email(email)
        val passwordError = Validators.password(password)
        if (emailError != null || passwordError != null) {
            _uiState.update { state ->
                state.copy(emailError = emailError, passwordError = passwordError, formError = null)
            }
            return
        }

        _uiState.update { state -> state.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            repository.login(email.trim(), password)
                .onSuccess {
                    _uiState.update { state -> state.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(isLoading = false, formError = R.string.error_login_failed)
                    }
                }
        }
    }
}
