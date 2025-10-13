package com.example.smartfarm.ui.daily_informations.input_daily

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.data.repository.DailyDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyInputViewModel @Inject constructor(
    private val repository: DailyDataRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun submitDailyActivity(bearer: String?, req: DailyData) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addDailyActivity(bearer, req)
                .onSuccess {
                    _message.value = "Aktivitas harian berhasil disimpan"
                }
                .onFailure { e ->
                    _message.value = e.message ?: "Gagal menyimpan aktivitas harian"
                }
            _isLoading.value = false
        }
    }
}