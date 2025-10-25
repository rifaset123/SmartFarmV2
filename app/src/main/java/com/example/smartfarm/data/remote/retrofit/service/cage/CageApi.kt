package com.example.smartfarm.data.remote.retrofit.service.cage

import com.example.smartfarm.data.model.ActivateCage
import com.example.smartfarm.data.model.AddCage
import com.example.smartfarm.data.remote.response.AddCageResponse
import com.example.smartfarm.data.remote.response.GetCageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface CageApi {
    @POST("api/add-cage")
    suspend fun addCage(
        @Header("Authorization") token: String,
        @Header("X-User-Offset") userOffset: String = "+07:00",
        @Body cage: AddCage
    ): AddCageResponse

    @GET("api/get-cages-v2")
    suspend fun getCage(
        @Header("Authorization") token: String,
        @Header("X-User-Offset") userOffset: String = "+07:00"
    ): GetCageResponse

    @POST("api/activate-cage")
    suspend fun activateCage(
        @Body body: ActivateCage,
        @Header("Authorization") bearer: String? = null
,        @Header("X-User-Offset") userOffset: String = "+07:00"
    ): AddCageResponse
}