package com.example.selfmedicalrecord.ui.user

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.health.BloodPressureRecord
import com.example.selfmedicalrecord.data.health.BodyRecord
import com.example.selfmedicalrecord.data.health.HealthRepository
import com.example.selfmedicalrecord.domain.health.BloodPressureCategory
import com.example.selfmedicalrecord.domain.health.BloodPressureClassifier
import com.example.selfmedicalrecord.utils.DayUtils
import com.example.selfmedicalrecord.utils.HealthValidators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InputTab { BMI, BLOOD_PRESSURE }

data class InputUiState(
    val tab: InputTab = InputTab.BMI,
    /** Tanggal pengukuran (dipakai kedua tab); default hari ini. */
    val dateEpochDay: Long = DayUtils.todayEpochDay(),
    val isSaving: Boolean = false,
    @StringRes val weightError: Int? = null,
    @StringRes val heightError: Int? = null,
    @StringRes val systolicError: Int? = null,
    @StringRes val diastolicError: Int? = null
)

/** Kejadian sekali pakai yang ditangani Fragment (navigasi, dialog, snackbar). */
sealed interface InputEvent {
    /** BMI tersimpan; buka halaman hasil. */
    data class BmiSaved(val weightKg: Float, val heightCm: Float) : InputEvent

    data class BloodPressureSaved(
        val systolic: Int,
        val diastolic: Int,
        val category: BloodPressureCategory
    ) : InputEvent

    data object SaveFailed : InputEvent
}

class InputDataKesehatanViewModel(
    private val repository: HealthRepository = ServiceLocator.healthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState.asStateFlow()

    private val _events = Channel<InputEvent>(Channel.BUFFERED)
    val events: Flow<InputEvent> = _events.receiveAsFlow()

    fun selectTab(tab: InputTab) {
        _uiState.update { it.copy(tab = tab) }
    }

    fun onDateSelected(epochDay: Long) {
        _uiState.update { it.copy(dateEpochDay = epochDay) }
    }

    /** Dipanggil saat pengguna mengetik, supaya pesan error hilang. */
    fun onInputChanged() {
        _uiState.update {
            it.copy(weightError = null, heightError = null, systolicError = null, diastolicError = null)
        }
    }

    fun saveBmi(weightRaw: String, heightRaw: String) {
        val current = _uiState.value
        if (current.isSaving) return

        val weightError = HealthValidators.weight(weightRaw)
        val heightError = HealthValidators.height(heightRaw)
        if (weightError != null || heightError != null) {
            _uiState.update { it.copy(weightError = weightError, heightError = heightError) }
            return
        }

        val weight = HealthValidators.parseDecimal(weightRaw) ?: return
        val height = HealthValidators.parseDecimal(heightRaw) ?: return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.addBodyRecord(BodyRecord(current.dateEpochDay, weight, height))
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(InputEvent.BmiSaved(weight, height))
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(InputEvent.SaveFailed)
                }
        }
    }

    fun saveBloodPressure(systolicRaw: String, diastolicRaw: String) {
        val current = _uiState.value
        if (current.isSaving) return

        val systolicError = HealthValidators.systolic(systolicRaw)
        val systolic = if (systolicError == null) systolicRaw.trim().toInt() else null
        val diastolicError = HealthValidators.diastolic(diastolicRaw, systolic)
        if (systolicError != null || diastolicError != null || systolic == null) {
            _uiState.update { it.copy(systolicError = systolicError, diastolicError = diastolicError) }
            return
        }
        val diastolic = diastolicRaw.trim().toInt()

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.addBloodPressureRecord(BloodPressureRecord(current.dateEpochDay, systolic, diastolic))
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(
                        InputEvent.BloodPressureSaved(
                            systolic, diastolic, BloodPressureClassifier.classify(systolic, diastolic)
                        )
                    )
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(InputEvent.SaveFailed)
                }
        }
    }
}
