// ui/profile/ProfileViewModel.kt
package com.example.smartfarm.ui.profile

import androidx.lifecycle.*
import com.example.smartfarm.data.remote.response.ProfileResponseItem
import com.example.smartfarm.data.repository.ProfileRepository
import com.example.smartfarm.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState<ProfileResponseItem?>>()
    val state: LiveData<UiState<ProfileResponseItem?>> = _state

    fun loadProfile(bearer: String?) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            repo.getProfile(bearer)
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Failure(it.message) }
        }
    }
}
