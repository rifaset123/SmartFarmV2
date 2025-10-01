package com.example.smartfarm.data.remote.model

data class LoginRequest (
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true,
)