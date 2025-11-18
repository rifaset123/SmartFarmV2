// ui/home/HomeViewModel.kt
package com.example.smartfarm.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.smartfarm.data.model.ActivateCage
import com.example.smartfarm.data.model.DeviceData
import com.example.smartfarm.data.remote.response.GetCageResponse
import com.example.smartfarm.data.remote.response.ResponseItem
import com.example.smartfarm.data.repository.ActivateCageRepository
import com.example.smartfarm.data.repository.DailyDataRepository
import com.example.smartfarm.mqtt.MqttClientManager
import com.example.smartfarm.mqtt.MqttSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class PrimaryButtonMode { ACTIVATE, ENTER, HIDDEN }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cageRepository: ActivateCageRepository,
    private val dailyRepository: DailyDataRepository,
    private val mqtt: MqttSyncManager
) : ViewModel() {

    private val _cageNames = MutableLiveData<List<String>>()
    val cageNames: LiveData<List<String>> = _cageNames

    private val _cages = MutableLiveData<List<ResponseItem>>()
    val cages: LiveData<List<ResponseItem>> = _cages

    private val _selectedCoop = MutableLiveData(0)
    val selectedCoop: LiveData<Int> = _selectedCoop

    private val _cagesData = MutableLiveData<GetCageResponse>()
    val cagesData: LiveData<GetCageResponse> = _cagesData

    private val _isCageActive = MutableLiveData<Boolean>(false)
    var isCageActive: LiveData<Boolean> = _isCageActive

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

    // mqtt live data
    private val _deviceData = MutableLiveData<DeviceData?>()
    val deviceData: LiveData<DeviceData?> = _deviceData

    // optional separate fields for UI
    private val _deviceTemperature = MutableLiveData<Double>()
    val deviceTemperature: LiveData<Double> = _deviceTemperature

    private val _deviceHumidity = MutableLiveData<Double>()
    val deviceHumidity: LiveData<Double> = _deviceHumidity

    private val _deviceAmmonia = MutableLiveData<Double>()
    val deviceAmmonia: LiveData<Double> = _deviceAmmonia

    private val _sensorStatusText = MutableLiveData<String?>()
    val sensorStatusText: LiveData<String?> = _sensorStatusText

    private val _predictionStatus = MutableLiveData<String?>()
    val predictionStatus: LiveData<String?> = _predictionStatus

    private val _predictionTime = MutableLiveData<String?>()
    val predictionTime: LiveData<String?> = _predictionTime


    private var lastSubscribedTopic: String? = null
    private var lastDataTopic: String? = null
    private var lastStatusTopic: String? = null


    init {
        mqtt.connect()
        viewModelScope.launch {
            mqtt.messages.collect { msg ->
                when {
                    msg.topic.startsWith("iot/broiler/data/") -> {
                        val obj = JSONObject(msg.payload)
                        obj.optDouble("temperature").takeIf { !it.isNaN() }?.let { _deviceTemperature.postValue(it) }
                        obj.optDouble("humidity").takeIf { !it.isNaN() }?.let { _deviceHumidity.postValue(it) }
                        obj.optDouble("ammonia").takeIf { !it.isNaN() }?.let { _deviceAmmonia.postValue(it) }

                        _sensorStatusText.postValue("Online")
                    }
                    msg.topic.startsWith("iot/broiler/status/") -> {
                        when (msg.payload.trim().lowercase()) {
                            "online"  -> _sensorStatusText.postValue("Online")
                            "offline" -> {
                                _sensorStatusText.postValue("Offline")
                                clearRealtimeValues()
                            }
                        }
                    }
                }
            }
        }
    }


    private fun parseDevicePayload(payload: String): DeviceData? {
        return try {
            val obj = JSONObject(payload)
            DeviceData(
                deviceId = obj.optString("device_id"),
                temperature = obj.optDouble("temperature"),
                humidity = obj.optDouble("humidity"),
                ammonia = obj.optDouble("ammonia"),
                timestamp = obj.optLong("timestamp"),
                offset = obj.optInt("offset")
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        lastDataTopic?.let { mqtt.unsubscribe(it) }
        lastStatusTopic?.let { mqtt.unsubscribe(it) }
        lastDataTopic = null
        lastStatusTopic = null
        super.onCleared()
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

                    // after you set the list...
                    val first = list.firstOrNull()
                    val active = first?.status?.equals("active", ignoreCase = true) == true
                    _isCageActive.value = active
                    val deviceId = first?.deviceId

                    updatePredictionForSelectedCage()

                    when {
                        active && deviceId.isNullOrBlank() -> {
                            showDeviceNotFound()
                        }
                        active && deviceId != null -> {
                            clearRealtimeValues()
                            subscribeToDevice(deviceId)      // sets default "Offline" for active+device
                        }
                        else -> {
                            clearRealtimeUiAndUnsubscribe()  // inactive -> blank/null status
                        }
                    }

                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Unknown error"
                    _isLoading.value = false
                    recomputeButtonMode()
                }

        }
    }

    fun setSelectedCoop(position: Int) {
        _selectedCoop.value = position
        recomputeButtonMode()

        // clear old readings immediately
        clearRealtimeValues()

        val item = currentSelectedItem()
        val active = item?.status?.equals("active", ignoreCase = true) == true
        val deviceId = item?.deviceId

        updatePredictionForSelectedCage()

        when {
            active && deviceId.isNullOrBlank() -> {
                showDeviceNotFound()
            }
            active && !deviceId.isNullOrBlank() -> {
                subscribeToDevice(deviceId)   // default "Offline" until data/status says Online
            }
            else -> {
                clearRealtimeUiAndUnsubscribe() // inactive -> blank
            }
        }
    }

    private fun subscribeToDevice(deviceId: String) {
        // ensure stale values are gone even for “offline” kandang
        clearRealtimeValues()

        val dataTopic = "iot/broiler/data/$deviceId"
        val statusTopic = "iot/broiler/status/$deviceId"

        // unsubscribe previous topics if changed (as you already do)
        if (lastDataTopic != null && lastDataTopic != dataTopic) mqtt.unsubscribe(lastDataTopic!!)
        if (lastStatusTopic != null && lastStatusTopic != statusTopic) mqtt.unsubscribe(lastStatusTopic!!)

        // subscribe new
        if (lastDataTopic != dataTopic) { mqtt.subscribe(dataTopic); lastDataTopic = dataTopic }
        if (lastStatusTopic != statusTopic) { mqtt.subscribe(statusTopic); lastStatusTopic = statusTopic }

        // default label for active+device is Offline until proven Online
        _sensorStatusText.value = "Offline"
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
        // only allow if selected cage is active
        if (!isSelectedCageActive()) {
            _errorMessage.value = "Kandang belum aktif"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            dailyRepository.getDailyActivities(
                bearer = bearerToken?.let { "Bearer $it" },
                cageId = cageId
            ).onSuccess { list ->
                val zone = java.time.ZoneId.of("Asia/Jakarta")
                val todayStr = java.time.LocalDate.now(zone).toString()
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

    private fun updatePredictionForSelectedCage() {
        val item = currentSelectedItem() ?: run {
            _predictionStatus.value = null
            _predictionTime.value = null
            return
        }

        // Ambil hanya data yang success dan punya details
        val list = item.predictionResultData
            .orEmpty()
            .mapNotNull { it }                              // buang null
            .filter { it.predictionStatus == "success" && it.predictionDetails != null }

        if (list.isEmpty()) {
            _predictionStatus.value = null
            _predictionTime.value = null
            return
        }

        // Ambil prediksi paling baru (berdasarkan predictedAt ISO string)
        val latest = list.maxByOrNull { it.predictedAt ?: "" } ?: run {
            _predictionStatus.value = null
            _predictionTime.value = null
            return
        }

        // normal / abnormal (dari PredictionDetailsDto.predictionResult)
        val result = latest.predictionDetails?.predictionResult
        _predictionStatus.value = result   // simpan raw, nanti di UI kita format

        // Format jam dari predictedAt → HH:mm
        val timeText = latest.predictedAt?.let { raw ->
            try {
                val odt = java.time.OffsetDateTime.parse(raw)
                val t = odt.toLocalTime()
                String.format("%02d:%02d", t.hour, t.minute)
            } catch (e: Exception) {
                null
            }
        }

        _predictionTime.value = timeText
    }



    private fun clearRealtimeUiAndUnsubscribe() {
        _deviceTemperature.value = 0.0
        _deviceHumidity.value = 0.0
        _deviceAmmonia.value = 0.0
        _deviceData.value = null
        _sensorStatusText.value = null

        lastDataTopic?.let { mqtt.unsubscribe(it) }
        lastStatusTopic?.let { mqtt.unsubscribe(it) }
        lastDataTopic = null
        lastStatusTopic = null
    }

    private fun showDeviceNotFound() {
        clearRealtimeValues()
        // stop any previous subscriptions
        lastDataTopic?.let { mqtt.unsubscribe(it) }
        lastStatusTopic?.let { mqtt.unsubscribe(it) }
        lastDataTopic = null
        lastStatusTopic = null

        _sensorStatusText.value = "Device not found"
    }


    private fun clearRealtimeValues() {
        _deviceTemperature.value = 0.0
        _deviceHumidity.value = 0.0
        _deviceAmmonia.value = 0.0
        _deviceData.value = null
    }

}
