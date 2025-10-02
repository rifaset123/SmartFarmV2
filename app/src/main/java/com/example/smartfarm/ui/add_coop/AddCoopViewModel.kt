package com.example.smartfarm.ui.add_coop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfarm.data.model.AddCage
import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.data.remote.response.AddCageResponse
import com.example.smartfarm.data.remote.retrofit.config.cage.CageConfig
import com.example.smartfarm.data.repository.KandangRepository
import com.example.smartfarm.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCoopViewModel @Inject constructor() : ViewModel() {

    private val _addCageState = MutableLiveData<UiState<AddCageResponse>>()
    val addCageState: LiveData<UiState<AddCageResponse>> = _addCageState

    fun addCage(token: String, initialPopulation: Int, cageArea: Double, deviceId: String) {
        viewModelScope.launch {
            _addCageState.value = UiState.Loading
            try {
                val cageApi = CageConfig.getApiService(token)
                val cageData = AddCage(
                    initial_population = initialPopulation,
                    cage_area = cageArea,
                    device_id = deviceId
                )
                val response = cageApi.addCage(token, "+07:00", cageData)
                _addCageState.value = UiState.Success(response)
            } catch (e: Exception) {
                _addCageState.value = UiState.Failure(e.message)
            }
        }
    }
}