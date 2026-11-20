package com.example.selfmedicalrecord.ui.riwayat

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.record.MedicalRecordDraft
import com.example.selfmedicalrecord.data.record.MedicalRecordRepository
import com.example.selfmedicalrecord.data.remote.ApiException
import com.example.selfmedicalrecord.data.remote.NetworkException
import com.example.selfmedicalrecord.utils.DayUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InputRiwayatUiState(
    /** Tanggal terpilih, epoch milidetik UTC (tengah malam). */
    val dateMillis: Long? = null,
    val isSaving: Boolean = false,
    @StringRes val dateError: Int? = null,
    @StringRes val facilityError: Int? = null,
    @StringRes val complaintError: Int? = null,
    /** Pesan dari server atau jaringan, tampil di atas tombol Simpan. */
    val formError: String? = null
)

class InputRiwayatViewModel(
    private val repository: MedicalRecordRepository = ServiceLocator.medicalRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InputRiwayatUiState())
    val uiState: StateFlow<InputRiwayatUiState> = _uiState.asStateFlow()

    private val _saved = Channel<Unit>(Channel.BUFFERED)
    val saved = _saved.receiveAsFlow()

    fun onDateSelected(millis: Long) {
        _uiState.update { it.copy(dateMillis = millis, dateError = null, formError = null) }
    }

    fun onInputChanged() {
        _uiState.update {
            it.copy(facilityError = null, complaintError = null, formError = null)
        }
    }

    /** [personalId] null = catatan baru; selain itu mengubah catatan tersebut. */
    fun save(personalId: Long?, facility: String, complaint: String, medications: String) {
        val state = _uiState.value
        if (state.isSaving) return

        val date = state.dateMillis
        val dateError = when {
            date == null -> R.string.riwayat_input_error_date_required
            DayUtils.fromUtcMillis(date) > DayUtils.todayEpochDay() -> R.string.riwayat_input_error_date_future
            else -> null
        }
        val facilityError = if (facility.isBlank()) R.string.riwayat_input_error_facility_required else null
        val complaintError = if (complaint.isBlank()) R.string.riwayat_input_error_complaint_required else null
        if (date == null || dateError != null || facilityError != null || complaintError != null) {
            _uiState.update {
                it.copy(dateError = dateError, facilityError = facilityError, complaintError = complaintError)
            }
            return
        }

        val draft = MedicalRecordDraft(
            visitDateMillis = date,
            facilityName = facility.trim(),
            complaint = complaint.trim(),
            medications = medications.trim()
        )
        _uiState.update { it.copy(isSaving = true, formError = null) }
        viewModelScope.launch {
            val result = if (personalId == null) {
                repository.addRecord(draft)
            } else {
                repository.updateRecord(personalId, draft)
            }
            result
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    _saved.send(Unit)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, formError = messageFor(error)) }
                }
        }
    }

    private fun messageFor(error: Throwable): String = when (error) {
        // Pesan validasi dari backend sudah berbahasa Indonesia untuk field catatan mandiri.
        is ApiException -> error.fieldErrors.values.flatten().firstOrNull() ?: error.message
        is NetworkException -> "Tidak bisa terhubung ke server. Periksa koneksi internet kamu."
        else -> error.message ?: "Data gagal disimpan. Coba lagi"
    }
}
