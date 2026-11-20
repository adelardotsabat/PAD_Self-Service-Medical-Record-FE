package com.example.selfmedicalrecord.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.IntegrationEndpoint
import com.example.selfmedicalrecord.data.admin.IntegrationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IntegrationUiState(val endpoints: List<IntegrationEndpoint> = emptyList()) {
    val total: Int get() = endpoints.size
    val connected: Int get() = endpoints.count { it.state == IntegrationState.CONNECTED }
    val failed: Int get() = endpoints.count { it.state == IntegrationState.FAILED }
}

class IntegrationViewModel(
    private val repository: AdminRepository = ServiceLocator.adminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntegrationUiState())
    val uiState: StateFlow<IntegrationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.integrations().onSuccess { _uiState.value = IntegrationUiState(it) }
        }
    }
}
