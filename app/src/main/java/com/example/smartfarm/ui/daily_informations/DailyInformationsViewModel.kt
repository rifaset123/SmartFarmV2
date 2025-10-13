package com.example.smartfarm.ui.daily_informations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfarm.data.remote.response.GetDailyResponseItem
import com.example.smartfarm.data.repository.DailyDataRepository
import com.example.smartfarm.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyInformationsViewModel @Inject constructor(
    private val repository: DailyDataRepository
) : ViewModel() {

    private val _items = MutableLiveData<List<GetDailyResponseItem>>(emptyList())
    val items: LiveData<List<GetDailyResponseItem>> = _items

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _state = MutableLiveData<UiState<List<GetDailyResponseItem>>>()
    val state: LiveData<UiState<List<GetDailyResponseItem>>> = _state

    fun load(cageId: String, bearer: String?) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            repository.getDailyActivities(bearer, cageId)
                .onSuccess { list -> _state.value = UiState.Success(list) }
                .onFailure { e -> _state.value = UiState.Failure(e.message) }
        }
    }
}