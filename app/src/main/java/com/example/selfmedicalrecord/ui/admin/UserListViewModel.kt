package com.example.selfmedicalrecord.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.AdminUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    fun onQueryChanged(value: String) {
        query.value = value
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteUser(id) }
    }
}
