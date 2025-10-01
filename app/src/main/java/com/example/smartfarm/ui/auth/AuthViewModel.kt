package com.example.smartfarm.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartfarm.data.model.User
import com.example.smartfarm.data.repository.AuthRepository
import com.example.smartfarm.util.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    val repository: AuthRepository
) : ViewModel() {

    private val _register = MutableLiveData<UiState<String>>()
    val register: LiveData<UiState<String>>
        get() = _register

    private val _login = MutableLiveData<UiState<String>>()
    val login: LiveData<UiState<String>>
        get() = _login

    private val _forgotPassword = MutableLiveData<UiState<String>>()
    val forgotPassword: LiveData<UiState<String>>
        get() = _forgotPassword


    private val _apiRegistration = MutableLiveData<UiState<String>>()
    val apiRegistration: LiveData<UiState<String>>
        get() = _apiRegistration

    fun register(email: String, password: String, user: User) {
        _register.value = UiState.Loading
        repository.registerUser(
            email = email,
            password = password,
            user = user
        ) { _register.value = it }
    }

    fun getCurrentUser(callback: (FirebaseUser?) -> Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        callback(firebaseUser)
    }

    fun registerToCustomApi(token: String, firebaseId: String, user: User) {
        _apiRegistration.value = UiState.Loading
        repository.registerToCustomApi(
            token = token,
            firebaseId = firebaseId,
            user = user
        ) { _apiRegistration.value = it }
    }

    fun login(
        email: String,
        password: String
    ) {
        _login.value = UiState.Loading
        repository.loginUser(
            email,
            password
        ){
            _login.value = it
        }
    }

    fun forgotPassword(email: String) {
        _forgotPassword.value = UiState.Loading
        repository.forgotPassword(email){
            _forgotPassword.value = it
        }
    }

    fun logout(result: () -> Unit){
        repository.logout(result)
    }

    fun getSession(result: (User?) -> Unit){
        repository.getSession(result)
    }
}