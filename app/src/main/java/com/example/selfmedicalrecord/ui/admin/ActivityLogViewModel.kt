package com.example.selfmedicalrecord.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.LogCategory
import com.example.selfmedicalrecord.data.admin.LogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LogFilter { ALL, LOGIN, DATA, SYSTEM }

data class ActivityLogUiState(
    val filter: LogFilter = LogFilter.ALL,
    val entries: List<LogEntry> = emptyList()
)

class ActivityLogViewModel(
    private val repository: AdminRepository = ServiceLocator.adminRepository
) : ViewModel() {

    private var all: List<LogEntry> = emptyList()

    private val _uiState = MutableStateFlow(ActivityLogUiState())
    val uiState: StateFlow<ActivityLogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.logs().onSuccess { logs ->
                all = logs
                _uiState.update { it.copy(entries = filtered(it.filter)) }
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
