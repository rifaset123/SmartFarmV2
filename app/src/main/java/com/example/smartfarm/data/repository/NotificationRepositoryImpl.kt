package com.example.smartfarm.data.repository

import com.example.smartfarm.data.remote.response.MarkNotificationsReadRequest
import com.example.smartfarm.data.remote.response.NotifResponseItem
import com.example.smartfarm.data.remote.retrofit.service.notification.NotificationsApi
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationsApi
) : NotificationRepository {

    override suspend fun list(bearerToken: String?): Result<List<NotifResponseItem>> = runCatching {
        val bearerHeader = bearerToken?.let { "Bearer $it" }
        val res = api.getNotifications(bearerHeader)
        res.response
            ?.filterNotNull()
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()
    }

    override suspend fun markRead(
        bearerToken: String?,
        id: List<String>
    ): Result<Unit>  = runCatching {
        if (id.isEmpty()) return@runCatching
        if (bearerToken.isNullOrBlank()) error("Bearer token is required for markRead")

        val body = MarkNotificationsReadRequest(id)
        api.markNotificationsRead(
            bearer = "Bearer $bearerToken",
            body = body
        )
    }

}