package com.example.smartfarm.data.model

data class DeviceData(
    val deviceId: String,
    val temperature: Double,
    val humidity: Double,
    val ammonia: Double,
    val timestamp: Long,
    val offset: Int
)