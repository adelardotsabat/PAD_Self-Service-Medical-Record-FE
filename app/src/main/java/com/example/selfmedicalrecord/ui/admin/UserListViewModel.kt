package com.example.selfmedicalrecord.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.admin.AdminRepository
import com.example.selfmedicalrecord.data.admin.AccountStatus
import com.example.selfmedicalrecord.data.admin.AdminUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Jumlah akun untuk kartu ringkasan (dihitung dari seluruh akun, bukan hasil pencarian). */
data class UserStats(val total: Int = 0, val active: Int = 0, val inactive: Int = 0)

class UserListViewModel(
    private val repository: AdminRepository = ServiceLocator.adminRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    val users: StateFlow<List<AdminUser>> = combine(repository.users, query) { list, q ->
        val key = q.trim()
        if (key.isEmpty()) list
        else list.filter { it.name.contains(key, ignoreCase = true) || it.email.contains(key, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<UserStats> = repository.users
        .map { list ->
            UserStats(
                total = list.size,
                active = list.count { it.status == AccountStatus.ACTIVE },
                inactive = list.count { it.status == AccountStatus.INACTIVE }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserStats())

    fun onQueryChanged(value: String) {
        query.value = value
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteUser(id) }
    }
}
