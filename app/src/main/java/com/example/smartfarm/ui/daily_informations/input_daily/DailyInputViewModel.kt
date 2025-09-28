package com.example.smartfarm.ui.daily_informations.input_daily

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.data.repository.KandangRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DailyInputViewModel @Inject constructor(
    private val repository: KandangRepository
) : ViewModel() {

    private val _dailyData = MutableLiveData<List<DailyData>>(emptyList())
    val dailyData: LiveData<List<DailyData>> get() = _dailyData

    fun insertData(data: DailyData) {
        repository.insertDailyData(data)
        _dailyData.value = repository.getAllData()
    }
}