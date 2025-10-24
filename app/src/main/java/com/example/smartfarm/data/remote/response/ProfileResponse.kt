package com.example.smartfarm.data.remote.response

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class ProfileResponse(

	@field:SerializedName("response")
	val response: List<ProfileResponseItem?>? = null,

	@field:SerializedName("messages")
	val messages: String? = null
) : Parcelable

@Parcelize
data class ProfileResponseItem(

	@field:SerializedName("province")
	val province: String? = null,

	@field:SerializedName("city")
	val city: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("email")
	val email: String? = null
) : Parcelable
