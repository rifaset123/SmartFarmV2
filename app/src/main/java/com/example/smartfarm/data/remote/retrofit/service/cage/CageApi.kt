package com.example.smartfarm.data.remote.retrofit.service.cage

import com.example.smartfarm.data.model.AddCage
import com.example.smartfarm.data.remote.response.AddCageResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CageApi {
    @POST("api/add-cage")
    suspend fun addCage(
        @Header("Authorization") token: String,
        @Header("X-User-Offset") userOffset: String = "+07:00",
        @Body cage: AddCage
    ): AddCageResponse
}