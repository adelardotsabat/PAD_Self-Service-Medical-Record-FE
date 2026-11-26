package com.example.selfmedicalrecord.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.AdminUser
import com.example.selfmedicalrecord.data.admin.UserStats
import com.example.selfmedicalrecord.data.remote.ApiException
import com.example.selfmedicalrecord.data.remote.NetworkException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserListViewModel(
    private val repository: AdminRepository = ServiceLocator.adminRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    val users: StateFlow<List<AdminUser>> = combine(repository.users, query) { list, q ->
        val key = q.trim()
        if (key.isEmpty()) list
        else list.filter { it.name.contains(key, ignoreCase = true) || it.email.contains(key, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _stats = MutableStateFlow(UserStats())
    val stats: StateFlow<UserStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Pesan singkat (resource string) untuk Snackbar: hasil aksi atau kegagalan. */
    private val _messages = Channel<Int>(Channel.BUFFERED)
    val messages: Flow<Int> = _messages.receiveAsFlow()

    fun onQueryChanged(value: String) {
        query.value = value
    }

    /** Muat daftar akun dan ringkasannya. Dipanggil tiap layar tampil (mis. kembali dari Tambah User). */
    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.refreshUsers().onFailure { error ->
                _messages.trySend(loadErrorMessage(error))
            }
            repository.userStats().onSuccess { _stats.value = it }
            _isLoading.value = false
        }
    }

    /** Aktifkan atau nonaktifkan akun; hasilnya langsung tampil di daftar dan ringkasan. */
    fun setActive(user: AdminUser, active: Boolean) {
        viewModelScope.launch {
            repository.setUserActive(user.id, active)
                .onSuccess {
                    _messages.trySend(
                        if (active) R.string.admin_user_activated else R.string.admin_user_deactivated
                    )
                    repository.userStats().onSuccess { _stats.value = it }
                }
                .onFailure { error -> _messages.trySend(statusErrorMessage(error)) }
        }
    }

    private fun loadErrorMessage(error: Throwable): Int =
        if (error is NetworkException) R.string.error_network else R.string.admin_error_load_failed

    private fun statusErrorMessage(error: Throwable): Int = when {
        error is NetworkException -> R.string.error_network
        // Backend: 403 = mengubah akun sendiri, 422 = menonaktifkan admin aktif terakhir.
        error is ApiException && error.httpCode == 403 -> R.string.admin_error_self_status
        error is ApiException && error.httpCode == 422 -> R.string.admin_error_last_admin
        else -> R.string.admin_error_save_failed
    }
}
