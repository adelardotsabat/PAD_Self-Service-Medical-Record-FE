package com.example.selfmedicalrecord.ui.admin

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.EmailTakenException
import com.example.selfmedicalrecord.data.admin.UserRole
import com.example.selfmedicalrecord.utils.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddUserUiState(
    val isSaving: Boolean = false,
    val role: UserRole = UserRole.USER,
    @StringRes val nameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmError: Int? = null,
    @StringRes val formError: Int? = null,
    val isSaved: Boolean = false
)

class AddUserViewModel(
    private val repository: AdminRepository = ServiceLocator.adminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUserUiState())
    val uiState: StateFlow<AddUserUiState> = _uiState.asStateFlow()

    fun onInputChanged() {
        _uiState.update {
            it.copy(nameError = null, emailError = null, passwordError = null, confirmError = null, formError = null)
        }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(role = role) }
    }

    fun save(name: String, email: String, password: String, confirm: String) {
        if (_uiState.value.isSaving) return

        val nameError = Validators.fullName(name)
        val emailError = Validators.email(email)
        val passwordError = Validators.newPassword(password)
        val confirmError = Validators.confirmPassword(password, confirm)
        if (listOf(nameError, emailError, passwordError, confirmError).any { it != null }) {
            _uiState.update {
                it.copy(
                    nameError = nameError, emailError = emailError,
                    passwordError = passwordError, confirmError = confirmError, formError = null
                )
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, formError = null) }
        viewModelScope.launch {
            repository.addUser(name.trim(), email.trim(), password, _uiState.value.role)
                .onSuccess { _uiState.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { error ->
                    val message = if (error is EmailTakenException) {
                        R.string.admin_error_email_taken
                    } else {
                        R.string.admin_error_save_failed
                    }
                    _uiState.update { it.copy(isSaving = false, formError = message) }
                }
        }
    }
}
