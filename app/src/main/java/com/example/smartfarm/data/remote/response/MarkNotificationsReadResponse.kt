package com.example.smartfarm.data.remote.response

import com.google.gson.annotations.SerializedName

data class MarkNotificationsReadRequest(
    @field:SerializedName("notification_ids")
    val notificationIds: List<String>
)

data class MarkNotificationsReadInnerResponse(
    @field:SerializedName("message")
    val message: String? = null
)

data class MarkNotificationsReadResponse(
    @field:SerializedName("response")
    val response: MarkNotificationsReadInnerResponse? = null,

    @field:SerializedName("messages")
    val messages: String? = null
)