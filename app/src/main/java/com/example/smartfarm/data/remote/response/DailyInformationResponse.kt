package com.example.smartfarm.data.remote.response

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class DailyInformationResponse(

	@field:SerializedName("response")
	val response: DailyResponse? = null,

	@field:SerializedName("messages")
	val messages: String? = null
) : Parcelable

@Parcelize
data class DailyResponse(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("death")
	val death: Int? = null,

	@field:SerializedName("cage_id")
	val cageId: String? = null,

	@field:SerializedName("weight")
	val weight: Int? = null,

	@field:SerializedName("food")
	val food: Int? = null
) : Parcelable
