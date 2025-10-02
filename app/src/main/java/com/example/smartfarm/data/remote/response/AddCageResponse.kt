package com.example.smartfarm.data.remote.response

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class AddCageResponse(

	@field:SerializedName("response")
	val response: Response? = null,

	@field:SerializedName("messages")
	val messages: String? = null
) : Parcelable

@Parcelize
data class Response(

	@field:SerializedName("device_id")
	val deviceId: String? = null,

	@field:SerializedName("cage_id")
	val cageId: String? = null,

	@field:SerializedName("current_population")
	val currentPopulation: Int? = null,

	@field:SerializedName("initial_population")
	val initialPopulation: Int? = null,

	@field:SerializedName("cage_area")
	val cageArea: Double? = null,

	@field:SerializedName("status")
	val status: String? = null
) : Parcelable
