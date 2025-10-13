package com.example.smartfarm.data.remote.response

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

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

	@field:SerializedName("device_status")
	val deviceStatus: String? = null,

	@field:SerializedName("device_id")
	val deviceId: String? = null,

	@field:SerializedName("current_population")
	val currentPopulation: Int? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("initial_population")
	val initialPopulation: Int? = null,

	@field:SerializedName("cage_area")
	val cageArea: String? = null,

	@field:SerializedName("cage_name")
	val cageName: String? = null,

	@field:SerializedName("status")
	val status: String? = null
) : Parcelable
