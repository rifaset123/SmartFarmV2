package com.example.smartfarm.data.remote.retrofit.service

import com.example.smartfarm.data.model.User
import com.example.smartfarm.data.remote.response.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface FirebaseAuthApi {
    @POST("v1/accounts:signInWithPassword")
    fun signInWithPassword(
        @Query("key") apiKey: String,
        @Body request: User
    ): Call<RegisterResponse>
}