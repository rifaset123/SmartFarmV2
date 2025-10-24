package com.example.smartfarm.data.remote.retrofit.service

import com.example.smartfarm.data.remote.response.ProfileResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface ProfileApi {
    @GET("api/get-profile")
    suspend fun getProfile(
        @Header("Authorization") bearer: String? = null
    ): ProfileResponse
}