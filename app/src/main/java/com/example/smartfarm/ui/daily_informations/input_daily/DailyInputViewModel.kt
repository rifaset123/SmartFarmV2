// DailyInputViewModel.kt
package com.example.smartfarm.ui.daily_informations.input_daily

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.data.repository.DailyDataRepository
import com.example.smartfarm.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class DailyInputViewModel @Inject constructor(
    private val repository: DailyDataRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _submitState = MutableLiveData<UiState<Unit>>()
    val submitState: LiveData<UiState<Unit>> = _submitState

    fun submitDailyActivity(bearer: String?, req: DailyData) {
        viewModelScope.launch {
            _isLoading.value = true
            _submitState.value = UiState.Loading

            repository.addDailyActivity(bearer, req)
                .onSuccess {
                    _message.value = "Aktivitas harian berhasil disimpan"
                    _submitState.value = UiState.Success(Unit)
                }
                .onFailure { e ->
                    val msg = when (e) {
                        is HttpException -> {
                            // khusus error 400 baca body
                            if (e.code() == 400) {
                                val raw = e.response()?.errorBody()?.string()
                                val parsed = try {
                                    val json = JSONObject(raw ?: "")
                                    // sesuai contoh
                                    json.optString(
                                        "response",
                                        "Penambahan informasi harian hanya dapat dilakukan setelah tanggal aktivasi."
                                    )
                                } catch (ex: Exception) {
                                    "Penambahan informasi harian hanya dapat dilakukan setelah tanggal aktivasi."
                                }
                                parsed
                            } else {
                                "Gagal menyimpan aktivitas harian (${e.code()})"
                            }
                        }
                        else -> e.message ?: "Gagal menyimpan aktivitas harian"
                    }

                    _message.value = msg
                    _submitState.value = UiState.Failure(msg)
                }

            _isLoading.value = false
        }
    }
}
