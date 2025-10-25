package com.example.smartfarm.data.repository

import com.example.smartfarm.data.remote.retrofit.service.notification.NotificationsApi
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationsApi
) : NotificationRepository {

    override suspend fun list(bearerToken: String?) = runCatching {
        val auth = bearerToken?.let { "Bearer $it" }
        api.getNotifications(auth).response.orEmpty()
    }

    override suspend fun markRead(bearerToken: String?, id: String?) = runCatching {
        val auth = bearerToken?.let { "Bearer $it" }
        val body = mapOf("ids" to listOf(id)) // or mapOf("id" to id) if your API wants single
        val res = api.markAsRead(auth,"+07:00", body as Map<String, List<String>>)
        if (!res.isSuccessful) error("Mark read failed: ${res.code()}")
        Unit
    }
}