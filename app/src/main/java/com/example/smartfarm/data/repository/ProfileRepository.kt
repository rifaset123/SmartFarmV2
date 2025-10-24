package com.example.smartfarm.data.repository

import com.example.smartfarm.data.remote.response.ProfileResponseItem
import com.example.smartfarm.data.remote.retrofit.service.ProfileApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api: ProfileApi
) {
    suspend fun getProfile(bearer: String?): Result<ProfileResponseItem?> = runCatching {
        val res = api.getProfile(bearer)
        res.response?.firstOrNull()
    }
}
