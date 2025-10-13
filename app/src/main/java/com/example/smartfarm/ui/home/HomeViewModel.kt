// ui/home/HomeViewModel.kt
package com.example.smartfarm.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.smartfarm.data.model.ActivateCage
import com.example.smartfarm.data.remote.response.GetCageResponse
import com.example.smartfarm.data.remote.response.ResponseItem
import com.example.smartfarm.data.repository.ActivateCageRepository
import com.example.smartfarm.data.repository.DailyDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class PrimaryButtonMode { ACTIVATE, ENTER, HIDDEN }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cageRepository: ActivateCageRepository,
    private val dailyRepository: DailyDataRepository
) : ViewModel() {

    private val _cageNames = MutableLiveData<List<String>>()
    val cageNames: LiveData<List<String>> = _cageNames

    private val _cages = MutableLiveData<List<ResponseItem>>()
    val cages: LiveData<List<ResponseItem>> = _cages

    private val _selectedCoop = MutableLiveData(0)
    val selectedCoop: LiveData<Int> = _selectedCoop

    private val _cagesData = MutableLiveData<GetCageResponse>()
    val cagesData: LiveData<GetCageResponse> = _cagesData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _uiMessage = MutableLiveData<String>()
    val uiMessage: LiveData<String> = _uiMessage

    private val _todayFood = MutableLiveData<Int?>(null)
    val todayFood: LiveData<Int?> = _todayFood

    private val _todayDrink = MutableLiveData<Int?>(null)
    val todayDrink: LiveData<Int?> = _todayDrink

    private val _todayDeath = MutableLiveData<Int?>(null)
    val todayDeath: LiveData<Int?> = _todayDeath

    // expose a mode for the primary button
    private val _primaryButtonMode = MediatorLiveData<PrimaryButtonMode>().apply {
        value = PrimaryButtonMode.HIDDEN
    }
    val primaryButtonMode: LiveData<PrimaryButtonMode> = _primaryButtonMode

    private var authToken: String? = null

    fun setSelectedCoop(position: Int) {
        _selectedCoop.value = position
        recomputeButtonMode()
    }

    fun getCages(token: String) {
        authToken = token
        viewModelScope.launch {
            _isLoading.value = true
            cageRepository.getCages(token)
                .onSuccess { response ->
                    _cagesData.value = response
                    val list = response.response?.filterNotNull().orEmpty()
                    _cages.value = list
                    _cageNames.value = if (list.isEmpty())
                        listOf("Tidak ada kandang") else list.map { it.cageName ?: "(tanpa nama)" }
                    _isLoading.value = false
                    recomputeButtonMode()
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Unknown error"
                    _isLoading.value = false
                    recomputeButtonMode()
                }
        }
    }

    private fun currentSelectedItem(): ResponseItem? {
        val idx = _selectedCoop.value ?: 0
        return _cages.value?.getOrNull(idx)
    }

    fun selectedCageStatus(): String? = currentSelectedItem()?.status

    fun isSelectedCageActive(): Boolean {
        return selectedCageStatus()?.equals("active", ignoreCase = true) == true
    }

    fun selectedCageName(): String = currentSelectedItem()?.cageName ?: "(tanpa nama)"

    fun getSelectedCageId(): String? {
        val idx = _selectedCoop.value ?: 0
        return _cages.value?.getOrNull(idx)?.id
    }

    private fun recomputeButtonMode() {
        val list = _cages.value.orEmpty()
        val idx = _selectedCoop.value ?: 0
        val item = list.getOrNull(idx)
        _primaryButtonMode.value = when {
            item == null -> PrimaryButtonMode.HIDDEN
            item.status.equals("active", ignoreCase = true) -> PrimaryButtonMode.ENTER
            else -> PrimaryButtonMode.ACTIVATE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun activateSelectedCage(date: LocalDate = LocalDate.now()) {
        val cageId = getSelectedCageId()
        if (cageId.isNullOrBlank()) {
            _errorMessage.value = "Cage ID tidak ditemukan"
            return
        }

        val body = ActivateCage(
            cage_id = cageId,
            date_activated = date.format(DateTimeFormatter.ISO_DATE)
        )

        viewModelScope.launch {
            _isLoading.value = true
            // IMPORTANT pass Authorization header as Bearer token if your API expects it
            val tokenHeader = authToken?.takeIf { it.isNotBlank() }?.let { "Bearer $it" }
            cageRepository.activateCage(tokenHeader, body)
                .onSuccess {
                    _uiMessage.value = "Kandang ${selectedCageName()} diaktifkan"
                    // refresh list to get the updated status
                    authToken?.let { getCages(it) } ?: run { _isLoading.value = false }
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Gagal mengaktifkan kandang"
                    _isLoading.value = false
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshToday(cageId: String, bearerToken: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            dailyRepository.getDailyActivities(
                bearer = bearerToken?.let { "Bearer $it" },
                cageId = cageId
            ).onSuccess { list ->
                // find entry for "today" (Asia/Jakarta)
                val zone = java.time.ZoneId.of("Asia/Jakarta")
                val todayStr = java.time.LocalDate.now(zone).toString() // yyyy-MM-dd
                val todayItem = list.firstOrNull { it.date?.startsWith(todayStr) == true }

                _todayFood.value = todayItem?.food
                _todayDrink.value = todayItem?.drink
                _todayDeath.value = todayItem?.death
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Gagal memuat data hari ini"
            }
            _isLoading.value = false
        }
    }
}
