package com.example.smartfarm.data.model

data class Notification (
    val id: String,
    val title: String,
    val createdAtIso: String,
    val isRead: Boolean
)