package com.example.smartfarm.data.model

data class DailyData(
    val coopName: String,
    val date: String,
    val ayamMati: Int,
    val pakan: String,
    val minum: String,
    val catatan: String
)