package com.example.smartfarm.data.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetCageResponse(
	@field:SerializedName("response")
	val response: List<ResponseItem?>? = null,
	@field:SerializedName("messages")
	val messages: String? = null
) : Parcelable

@Parcelize
data class ResponseItem(
	@field:SerializedName("date_activated")
	val dateActivated: String? = null,
	@field:SerializedName("prediction_result_data")
	val predictionResultData: List<PredictionResultItem?>? = null,
	@field:SerializedName("device_id")
	val deviceId: String? = null,
	@field:SerializedName("cage_status")
	val status: String? = null,            // mapped to cage_status
	@field:SerializedName("current_population")
	val currentPopulation: Int? = null,
	@field:SerializedName("created_at")
	val createdAt: String? = null,
	@field:SerializedName("id")
	val id: String? = null,
	@field:SerializedName("initial_population")
	val initialPopulation: Int? = null,
	@field:SerializedName("cage_area")
	val cageArea: Double? = null,
	@field:SerializedName("cage_name")
	val cageName: String? = null
) : Parcelable

@Parcelize
data class PredictionResultItem(
	@field:SerializedName("predicted_at")
	val predictedAt: String? = null,
	@field:SerializedName("prediction_details")
	val predictionDetails: PredictionDetailsDto? = null,  // <-- renamed type
	@field:SerializedName("prediction_status")
	val predictionStatus: String? = null,
	@field:SerializedName("error")
	val error: Int? = null
) : Parcelable

@Parcelize
data class PredictionDetailsDto(         // <-- renamed to avoid clash
	@field:SerializedName("ammo")
	val ammo: Double? = null,            // 31.05
	@field:SerializedName("device_id")
	val deviceId: String? = null,
	@field:SerializedName("prediction_result")
	val predictionResult: String? = null, // "normal"/"abnormal"
	@field:SerializedName("temperature")
	val temperature: Double? = null,      // 27.45
	@field:SerializedName("humidity")
	val humidity: Double? = null          // 63.6
) : Parcelable
