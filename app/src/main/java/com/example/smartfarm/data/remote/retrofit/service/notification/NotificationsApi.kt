package com.example.smartfarm.data.remote.retrofit.service.notification

import com.example.smartfarm.data.remote.response.MarkNotificationsReadRequest
import com.example.smartfarm.data.remote.response.MarkNotificationsReadResponse
import com.example.smartfarm.data.remote.response.NotificationsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface NotificationsApi {
    @GET("api/get-notif-history")
    suspend fun getNotifications(
        @Header("Authorization") bearer: String? = null,
        @Header("X-User-Offset") userOffset: String = "+07:00",
    ): NotificationsResponse

    @POST("api/update-read-status-notifications")
    suspend fun markNotificationsRead(
        @Header("Authorization") bearer: String? = null,
        @Header("X-User-Offset") userOffset: String = "+07:00",
        @Body body: MarkNotificationsReadRequest
    ): MarkNotificationsReadResponse
}