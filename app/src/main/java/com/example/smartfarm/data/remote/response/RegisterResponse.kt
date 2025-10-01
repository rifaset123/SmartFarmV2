package com.example.smartfarm.data.remote.response

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class RegisterResponse(

	@field:SerializedName("response")
	val response: Boolean? = null,

	@field:SerializedName("messages")
	val messages: String? = null
) : Parcelable
