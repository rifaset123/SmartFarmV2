package com.example.smartfarm.data.repository

import com.example.smartfarm.data.model.User
import com.example.smartfarm.util.UiState

interface AuthRepository {
    fun registerUser(email: String, password: String, user: User, result: (UiState<String>) -> Unit)
    fun registerToCustomApi(token: String, firebaseId: String, user: User, result: (UiState<String>) -> Unit)
    fun updateUserInfo(user: User, result: (UiState<String>) -> Unit)
    fun loginUser(email: String, password: String, result: (UiState<String>) -> Unit)
    fun forgotPassword(email: String, result: (UiState<String>) -> Unit)
    fun logout(result: () -> Unit)
    fun storeSession(id: String, result: (User?) -> Unit)
    fun getSession(result: (User?) -> Unit)
}