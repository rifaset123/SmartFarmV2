package com.example.smartfarm.data.remote.retrofit.service.daily

import com.example.smartfarm.data.model.AddCage
import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.data.remote.response.AddCageResponse
import com.example.smartfarm.data.remote.response.GetDailyInformationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface DailyActivityApi {
    @POST("api/add-daily-activity")
    suspend fun addDailyData(
        @Body cage: DailyData,
        @Header("Authorization") token: String? = null,
        @Header("X-User-Offset") userOffset: String = "+07:00",
    ): AddCageResponse

    @GET("api/get-daily-activities/{cageId}")
    suspend fun getDailyActivities(
        @Path("cageId") cageId: String,
        @Header("Authorization") bearer: String? = null,
        @Header("X-User-Offset") userOffset: String = "+07:00",
        ): GetDailyInformationResponse
}