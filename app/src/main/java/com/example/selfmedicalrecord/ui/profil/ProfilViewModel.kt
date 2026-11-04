package com.example.selfmedicalrecord.ui.profil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.data.auth.AuthRepository
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.profile.FakeProfileRepository
import com.example.selfmedicalrecord.data.profile.ProfileRepository
import com.example.selfmedicalrecord.data.profile.UserProfile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ProfilViewModel(
    profileRepository: ProfileRepository = FakeProfileRepository(),
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    /** Profil terkini; kartu di tab Profil ikut berubah saat Kelola Profil menyimpan. */
    val profile: StateFlow<UserProfile> = profileRepository.profile

    // Kejadian sekali: selesai keluar -> Fragment membuka layar Welcome.
    private val _loggedOut = Channel<Unit>(Channel.BUFFERED)
    val loggedOut: Flow<Unit> = _loggedOut.receiveAsFlow()

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loggedOut.send(Unit)
        }
    }
}
