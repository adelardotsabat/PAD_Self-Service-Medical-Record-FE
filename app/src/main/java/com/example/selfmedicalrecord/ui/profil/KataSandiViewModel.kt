package com.example.selfmedicalrecord.ui.profil

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

data class KataSandiUiState(
    val isLoading: Boolean = false,
    @StringRes val currentError: Int? = null,
    @StringRes val newError: Int? = null,
    @StringRes val confirmError: Int? = null,
    @StringRes val formError: Int? = null
)

class KataSandiViewModel(
    private val repository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KataSandiUiState())
    val uiState: StateFlow<KataSandiUiState> = _uiState.asStateFlow()

    private val _saved = Channel<Unit>(Channel.BUFFERED)
    val saved: Flow<Unit> = _saved.receiveAsFlow()

    fun onInputChanged() {
        _uiState.update { it.copy(currentError = null, newError = null, confirmError = null, formError = null) }
    }

    fun save(current: String, new: String, confirm: String) {
        if (_uiState.value.isLoading) return

        val currentError = Validators.password(current)
        val newError = Validators.newPassword(new)
        val confirmError = Validators.confirmPassword(new, confirm)
        if (currentError != null || newError != null || confirmError != null) {
            _uiState.update {
                it.copy(currentError = currentError, newError = newError, confirmError = confirmError, formError = null)
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            repository.changePassword(current, new)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _saved.send(Unit)
                }
                .onFailure {
                    // Penyebab paling umum: kata sandi saat ini salah. Pesan rinci menyusul saat API siap.
                    _uiState.update {
                        it.copy(isLoading = false, currentError = R.string.security_current_wrong)
                    }
                }
        }
    }
}
