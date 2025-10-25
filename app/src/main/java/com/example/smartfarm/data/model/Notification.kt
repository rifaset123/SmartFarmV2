package com.example.smartfarm.data.model

data class Notification (
    val id: String,
    val title: String,         // "Kandang {name} diprediksi {normal|abnormal}."
    val createdAtIso: String,
    val isRead: Boolean
)