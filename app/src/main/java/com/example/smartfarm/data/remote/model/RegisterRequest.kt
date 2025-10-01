package com.example.smartfarm.data.remote.model

data class RegisterRequest (
    val firebase_id: String,
    val name: String,
    val province: String,
    val city: String,
    val phone: String,
    val email: String,
)