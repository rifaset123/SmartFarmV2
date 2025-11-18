package com.example.smartfarm.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartfarm.data.model.Notification
import com.example.smartfarm.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.*
import com.example.smartfarm.data.remote.response.NotifResponseItem
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repo: NotificationRepository
) : ViewModel() {

    private val _items = MutableLiveData<List<NotifResponseItem>>(emptyList())
    val items: LiveData<List<NotifResponseItem>> = _items

    val loading = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)

    private var bearer: String? = null

    fun load(bearerToken: String?) = viewModelScope.launch {
        bearer = bearerToken
        loading.value = true

        repo.list(bearerToken)
            .onSuccess { list ->
                _items.value = list
                    .filterNotNull() // buang yang null dari API
                    .sortedByDescending { it.createdAt } // paling baru di atas
            }
            .onFailure { e ->
                error.value = e.message
            }

        loading.value = false
    }

    fun markRead(id: String?) = viewModelScope.launch {
        if (id == null) return@launch

        // optional: call ke API mark read di repo, lalu update UI lokal
        _items.value = _items.value?.map {
            if (it.id == id) it.copy(readStatus = true) else it
        }
    }
}