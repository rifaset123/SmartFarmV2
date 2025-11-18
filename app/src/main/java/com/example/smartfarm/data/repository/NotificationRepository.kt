package com.example.smartfarm.data.repository

import com.example.smartfarm.data.remote.response.NotifResponseItem

interface NotificationRepository {
    suspend fun list(bearerToken: String?): Result<List<NotifResponseItem?>>
    suspend fun markRead(bearerToken: String?, id: List<String>): Result<Unit>
}