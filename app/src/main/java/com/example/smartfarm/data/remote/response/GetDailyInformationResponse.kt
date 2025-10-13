package com.example.smartfarm.data.remote.response

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class GetDailyInformationResponse(

	@field:SerializedName("response")
	val response: List<GetDailyResponseItem?>? = null,

	@field:SerializedName("messages")
	val messages: String? = null
) : Parcelable

@Parcelize
data class GetDailyResponseItem(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("death")
	val death: Int? = null,

	@field:SerializedName("cage_id")
	val cageId: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("weight")
	val weight: Int? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("drink")
	val drink: Int? = null,

	@field:SerializedName("food")
	val food: Int? = null
) : Parcelable
