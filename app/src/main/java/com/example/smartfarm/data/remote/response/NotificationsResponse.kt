package com.example.smartfarm.data.remote.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class NotificationsResponse(

	@SerialName("response")
	val response: List<NotifResponseItem?>? = null,

	@SerialName("messages")
	val messages: String? = null
)

@Serializable
data class NotifResponseItem(

	@SerialName("prediction_detail")
	val predictionDetail: List<PredictionDetailItem?>? = null,

	@SerialName("broiler_prediction_id")
	val broilerPredictionId: String? = null,

	@SerialName("cage_id")
	val cageId: String? = null,

	@SerialName("created_at")
	val createdAt: String? = null,

	@SerialName("read_status")
	val readStatus: Boolean? = null,

	@SerialName("id")
	val id: String? = null,

	@SerialName("cage_name")
	val cageName: String? = null
)

@Serializable
data class PredictionDetailItem(

	@SerialName("ammo")
	val ammo: Any? = null,

	@SerialName("device_id")
	val deviceId: String? = null,

	@SerialName("prediction_result")
	val predictionResult: String? = null,

	@SerialName("temperature")
	val temperature: Any? = null,

	@SerialName("humidity")
	val humidity: Any? = null
)
