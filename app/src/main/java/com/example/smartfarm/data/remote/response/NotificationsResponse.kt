package com.example.smartfarm.data.remote.response

import com.google.gson.annotations.SerializedName

data class NotificationsResponse(

	@field:SerializedName("response")
	val response: List<NotifResponseItem?>? = null,

	@field:SerializedName("messages")
	val messages: String? = null
)

data class NotifResponseItem(

	// PERHATIKAN: plural 'prediction_details'
	@field:SerializedName("prediction_details")
	val predictionDetail: List<PredictionDetailItem?>? = null,

	@field:SerializedName("broiler_prediction_id")
	val broilerPredictionId: String? = null,

	@field:SerializedName("cage_id")
	val cageId: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("read_status")
	val readStatus: Boolean? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("cage_name")
	val cageName: String? = null
)

data class PredictionDetailItem(

	@field:SerializedName("ammo")
	val ammo: Double? = null,

	@field:SerializedName("device_id")
	val deviceId: String? = null,

	@field:SerializedName("prediction_result")
	val predictionResult: String? = null,

	@field:SerializedName("temperature")
	val temperature: Double? = null,

	@field:SerializedName("humidity")
	val humidity: Double? = null
)
