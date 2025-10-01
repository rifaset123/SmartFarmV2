package com.example.smartfarm.data.remote.retrofit.service

import com.example.smartfarm.data.model.User
import com.example.smartfarm.data.remote.model.RegisterRequest
import com.example.smartfarm.data.remote.response.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface RegisterApi {
    @POST("api/register")
    suspend fun registerUser(
        @Header("Authorization") token: String,
        @Body user: User
    ): RegisterResponse
//    @POST("login")
//    fun login(
//        @Body request: LoginRequest
//    ): Call<LoginResponse>
}