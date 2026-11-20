package com.example.selfmedicalrecord.ui.result

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.food.CalorieEstimate
import com.example.selfmedicalrecord.data.food.FoodCalorieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HasilKaloriUiState(
    val isLoading: Boolean = true,
    val estimate: CalorieEstimate? = null,
    val hasError: Boolean = false
)

class HasilKaloriViewModel(
    private val repository: FoodCalorieRepository = ServiceLocator.foodCalorieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HasilKaloriUiState())
    val uiState: StateFlow<HasilKaloriUiState> = _uiState.asStateFlow()

    private var imageUri: Uri? = null
    private var job: Job? = null

    /** Aman dipanggil berulang (mis. setelah rotasi): foto yang sama tidak dianalisis ulang. */
    fun start(uri: Uri) {
        if (imageUri == uri) return
        imageUri = uri
        load()
    }

    fun retry() = load()

    private fun load() {
        val uri = imageUri ?: return
        job?.cancel()
        _uiState.value = HasilKaloriUiState(isLoading = true)
        job = viewModelScope.launch {
            repository.estimate(uri)
                .onSuccess { _uiState.value = HasilKaloriUiState(isLoading = false, estimate = it) }
                .onFailure { _uiState.value = HasilKaloriUiState(isLoading = false, hasError = true) }
        }
    }
}
