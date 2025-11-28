package com.example.smartfarm.data.remote.retrofit.config.cage

import com.example.smartfarm.data.remote.retrofit.service.cage.CageApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CageConfig {
    private const val BASE_URL = "http://10.94.215.155:5000/"
    val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
        level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
    }

    fun getApiService(bearerToken: String): CageApi {
//        val authInterceptor = okhttp3.Interceptor { chain ->
//            val request = chain.request().newBuilder()
//                .addHeader("Authorization", "Bearer $bearerToken")
//                .build()
//            chain.proceed(request)
//        }

        val client = okhttp3.OkHttpClient.Builder()
//            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(CageApi::class.java)
    }
}