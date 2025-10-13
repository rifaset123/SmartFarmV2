package com.example.smartfarm.data.repository

import com.example.smartfarm.data.model.ActivateCage
import com.example.smartfarm.data.model.AddCage
import com.example.smartfarm.data.remote.response.AddCageResponse
import com.example.smartfarm.data.remote.response.GetCageResponse
import com.example.smartfarm.data.remote.retrofit.service.cage.CageApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivateCageRepository @Inject constructor(
    private val cageApi: CageApi
) {
    suspend fun getCages(token: String): Result<GetCageResponse> {
        return try {
            val response = cageApi.getCage("Bearer $token")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateCage(bearer: String?, body: ActivateCage): Result<AddCageResponse> = runCatching {
        cageApi.activateCage(body, bearer)
    }
}