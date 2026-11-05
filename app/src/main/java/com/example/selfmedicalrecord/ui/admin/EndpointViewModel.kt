package com.example.selfmedicalrecord.ui.admin

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.EndpointConfig
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EndpointUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isPinging: Boolean = false,
    /** Nilai awal untuk mengisi form; null setelah ditampilkan ([EndpointViewModel.onConfigShown]). */
    val config: EndpointConfig? = null,
    @StringRes val urlError: Int? = null,
    @StringRes val portError: Int? = null,
    @StringRes val apiKeyError: Int? = null
) {
    val isBusy: Boolean get() = isLoading || isSaving || isPinging
}

class EndpointViewModel(
    private val repository: AdminRepository = ServiceLocator.adminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EndpointUiState())
    val uiState: StateFlow<EndpointUiState> = _uiState.asStateFlow()

    private val _messages = Channel<Int>(Channel.BUFFERED)
    /** Pesan sekali tampil (id string), untuk Snackbar. */
    val messages: Flow<Int> = _messages.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.endpointConfig()
                .onSuccess { config ->
                    _uiState.update { it.copy(isLoading = false, config = config) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _messages.send(R.string.admin_error_load_failed)
                }
        }
    }

    fun onInputChanged() {
        _uiState.update { it.copy(urlError = null, portError = null, apiKeyError = null) }
    }

    fun onConfigShown() {
        _uiState.update { it.copy(config = null) }
    }

    fun save(url: String, port: String, apiKey: String, enabled: Boolean) {
        if (_uiState.value.isBusy) return
        val config = validated(url, port, apiKey, enabled) ?: return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = repository.saveEndpointConfig(config)
            _uiState.update { it.copy(isSaving = false) }
            _messages.send(
                if (result.isSuccess) R.string.admin_endpoint_saved else R.string.admin_error_save_failed
            )
        }
    }

    fun ping(url: String, port: String, apiKey: String, enabled: Boolean) {
        if (_uiState.value.isBusy) return
        val config = validated(url, port, apiKey, enabled) ?: return
        _uiState.update { it.copy(isPinging = true) }
        viewModelScope.launch {
            val result = repository.pingEndpoint(config)
            _uiState.update { it.copy(isPinging = false) }
            _messages.send(
                if (result.isSuccess) R.string.admin_endpoint_ping_ok else R.string.admin_endpoint_ping_failed
            )
        }
    }

    private fun validated(url: String, port: String, apiKey: String, enabled: Boolean): EndpointConfig? {
        val urlError = AdminValidators.serverUrl(url)
        val portError = AdminValidators.port(port)
        val keyError = AdminValidators.apiKey(apiKey)
        _uiState.update { it.copy(urlError = urlError, portError = portError, apiKeyError = keyError) }
        if (urlError != null || portError != null || keyError != null) return null
        return EndpointConfig(url.trim(), port.trim(), apiKey.trim(), enabled)
    }
}
