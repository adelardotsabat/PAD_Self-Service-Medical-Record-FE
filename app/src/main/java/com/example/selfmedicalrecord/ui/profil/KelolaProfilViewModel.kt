package com.example.selfmedicalrecord.ui.profil

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.profile.ProfileRepository
import com.example.selfmedicalrecord.data.profile.UserProfile
import com.example.selfmedicalrecord.utils.Validators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KelolaProfilUiState(
    val isLoading: Boolean = false,
    /** Tanggal lahir terpilih (epoch millis UTC). Disimpan di sini karena field-nya hanya menampilkan. */
    val birthDateMillis: Long? = null,
    @StringRes val nameError: Int? = null,
    @StringRes val birthDateError: Int? = null,
    @StringRes val nikError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val formError: Int? = null
)

class KelolaProfilViewModel(
    private val repository: ProfileRepository = ServiceLocator.profileRepository
) : ViewModel() {

    /** Profil saat layar dibuka, dipakai Activity untuk mengisi form pertama kali. */
    val initialProfile: UserProfile = repository.profile.value

    private val _uiState = MutableStateFlow(KelolaProfilUiState(birthDateMillis = initialProfile.birthDateMillis))
    val uiState: StateFlow<KelolaProfilUiState> = _uiState.asStateFlow()

    // Kejadian sekali: perubahan tersimpan -> Activity menutup layar.
    private val _saved = Channel<Unit>(Channel.BUFFERED)
    val saved: Flow<Unit> = _saved.receiveAsFlow()

    fun onBirthDateSelected(millis: Long) {
        _uiState.update { it.copy(birthDateMillis = millis, birthDateError = null) }
    }

    /** Dipanggil saat pengguna mengetik, supaya pesan error hilang. */
    fun onInputChanged() {
        _uiState.update {
            it.copy(nameError = null, nikError = null, emailError = null, formError = null)
        }
    }

    fun save(name: String, gender: String?, nik: String, address: String, email: String) {
        val state = _uiState.value
        if (state.isLoading) return

        val nameError = Validators.fullName(name)
        val nikError = Validators.nik(nik)
        // Email tidak bisa diubah dari layar ini (backend meminta verifikasi ulang), jadi tidak divalidasi.
        val emailError: Int? = null
        val birthError = if (state.birthDateMillis == null) R.string.error_birth_date_required else null
        if (nameError != null || nikError != null || emailError != null || birthError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    nikError = nikError,
                    emailError = emailError,
                    birthDateError = birthError,
                    formError = null
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            val updated = UserProfile(
                fullName = name.trim(),
                gender = gender,
                birthDateMillis = state.birthDateMillis,
                nik = nik.trim(),
                address = address.trim(),
                email = email.trim()
            )
            repository.updateProfile(updated)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _saved.send(Unit)
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, formError = R.string.manage_save_failed) }
                }
        }
    }
}
