package com.example.smartfarm.data.model

data class DailyData(
    val cage_id: String,
    val date: String,
    val food: Int,
    val drink: Int,
    val weight: Int,
    val death: Int,
    val notes: String
)