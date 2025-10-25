package com.example.smartfarm.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartfarm.data.model.Notification
import com.example.smartfarm.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.*
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repo: NotificationRepository
) : ViewModel() {

    private val _items = MutableLiveData<List<Notification>>(emptyList())
    val items: LiveData<List<Notification>> = _items

    val loading = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)

    private var bearer: String? = null

    fun load(bearerToken: String?) = viewModelScope.launch {
        bearer = bearerToken
        loading.value = true
        repo.list(bearerToken)
            .onSuccess { _items.value =
                it.sortedByDescending { notif -> notif?.createdAt } as List<Notification>?
            }
            .onFailure { error.value = it.message }
        loading.value = false
    }

    fun markRead(id: String?) = viewModelScope.launch {
        // Optimistic UI
        _items.value = _items.value.orEmpty().map { if (it.id == id) it.copy(isRead = true) else it }
        repo.markRead(bearer, id).onFailure { err -> error.value = err.message }
    }
}