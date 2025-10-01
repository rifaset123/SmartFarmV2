package com.example.smartfarm.data.remote.retrofit.service

import com.example.smartfarm.data.remote.model.LoginRequest
import com.example.smartfarm.data.remote.response.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginApi {
    @Headers("Content-Type: application/json")
    @POST("v1/accounts:signInWithPassword")
    fun signInWithPassword(
        @Query("key") apiKey: String,
        @Body request: LoginRequest
    ): Call<LoginResponse>
}