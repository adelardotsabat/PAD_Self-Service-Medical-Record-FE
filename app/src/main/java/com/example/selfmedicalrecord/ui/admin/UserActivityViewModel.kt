package com.example.selfmedicalrecord.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.UserActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserActivityViewModel(
    private val repository: AdminRepository = ServiceLocator.adminRepository
) : ViewModel() {

    private var all: List<UserActivity> = emptyList()
    private var query = ""

    private val _items = MutableStateFlow<List<UserActivity>>(emptyList())
    val items: StateFlow<List<UserActivity>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userActivities().onSuccess { list ->
                all = list
                apply()
            }
        }
    }

    /** Pencarian cocok ke nama atau peran (admin / user). */
    fun onQueryChanged(value: String) {
        query = value.trim()
        apply()
    }

    private fun apply() {
        _items.value = if (query.isEmpty()) all else all.filter {
            it.name.contains(query, ignoreCase = true) || it.role.name.contains(query, ignoreCase = true)
        }
    }
}
