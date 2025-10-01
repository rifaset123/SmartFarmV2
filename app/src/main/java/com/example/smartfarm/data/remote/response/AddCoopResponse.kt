package com.example.smartfarm.data.remote.response

import com.google.gson.annotations.SerializedName

data class AddCoopResponse(

	@field:SerializedName("response")
	val response: Response? = null,

	@field:SerializedName("messages")
	val messages: String? = null
)

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
	val cageArea: Any? = null,

	@field:SerializedName("status")
	val status: String? = null
)
