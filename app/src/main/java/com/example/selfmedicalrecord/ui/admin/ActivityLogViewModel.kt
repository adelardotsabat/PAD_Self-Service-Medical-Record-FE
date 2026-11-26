package com.example.selfmedicalrecord.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.LogCategory
import com.example.selfmedicalrecord.data.admin.LogEntry
import com.example.selfmedicalrecord.data.remote.NetworkException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LogFilter { ALL, LOGIN, DATA, SYSTEM }

data class ActivityLogUiState(
    val filter: LogFilter = LogFilter.ALL,
    val entries: List<LogEntry> = emptyList(),
    val isLoading: Boolean = true
)

class ActivityLogViewModel(
    private val repository: AdminRepository = ServiceLocator.adminRepository
) : ViewModel() {

    private var all: List<LogEntry> = emptyList()

    private val _uiState = MutableStateFlow(ActivityLogUiState())
    val uiState: StateFlow<ActivityLogUiState> = _uiState.asStateFlow()

    /** Pesan gagal muat (resource string), ditampilkan sekali oleh layar. */
    private val _errors = Channel<Int>(Channel.BUFFERED)
    val errors: Flow<Int> = _errors.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.logs()
                .onSuccess { logs ->
                    all = logs
                    _uiState.update { it.copy(isLoading = false, entries = filtered(it.filter)) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _errors.trySend(
                        if (error is NetworkException) R.string.error_network
                        else R.string.admin_error_load_failed
                    )
                }
        }
    }

    fun onFilterSelected(filter: LogFilter) {
        _uiState.update { it.copy(filter = filter, entries = filtered(filter)) }
    }

    /** Filter "Sistem" juga memuat kejadian error, karena error berasal dari sistem. */
    private fun filtered(filter: LogFilter): List<LogEntry> = when (filter) {
        LogFilter.ALL -> all
        LogFilter.LOGIN -> all.filter { it.category == LogCategory.LOGIN }
        LogFilter.DATA -> all.filter { it.category == LogCategory.DATA }
        LogFilter.SYSTEM -> all.filter {
            it.category == LogCategory.SYSTEM || it.category == LogCategory.ERROR
        }
    }
}
