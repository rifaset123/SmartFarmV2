package com.example.smartfarm.data.model

data class User(
    var firebase_id: String = "",
    val name: String = "",
    val province: String = "",
    val city: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirm_password: String = "",
)