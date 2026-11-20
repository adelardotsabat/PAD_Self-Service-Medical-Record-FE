package com.example.selfmedicalrecord.ui.riwayat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.record.MedicalRecord
import com.example.selfmedicalrecord.data.record.MedicalRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RiwayatUiState(
    val isLoading: Boolean = true,
    val records: List<MedicalRecord> = emptyList(),
    val hasError: Boolean = false
)

class RiwayatViewModel(
    private val repository: MedicalRecordRepository = ServiceLocator.medicalRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiwayatUiState())
    val uiState: StateFlow<RiwayatUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = RiwayatUiState(isLoading = true)
        viewModelScope.launch {
            repository.getRecords()
                .onSuccess { list ->
                    // Terbaru di atas; untuk tanggal yang sama urutan dari sumber dipertahankan.
                    _uiState.value = RiwayatUiState(
                        isLoading = false,
                        records = list.sortedByDescending { it.visitDateMillis }
                    )
                }
                .onFailure {
                    _uiState.value = RiwayatUiState(isLoading = false, hasError = true)
                }
        }
    }
}
