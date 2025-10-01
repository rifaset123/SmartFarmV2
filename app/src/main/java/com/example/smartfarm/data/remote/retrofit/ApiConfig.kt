package com.example.smartfarm.data.remote.retrofit

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig {
//    companion object{
//
//        object CageClient {
//            private const val BASE_URL = "http://10.0.2.2:5000/"  // backend
//
//            val instance: AuthApi by lazy {
//                Retrofit.Builder()
//                    .baseUrl(BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build()
//                    .create(AuthApi::class.java)
//            }
//        }
//
//        object FirebaseCLient {
//            private const val BASE_URL = "https://identitytoolkit.googleapis.com/"
//
//            val instance: FirebaseAuthApi by lazy {
//                Retrofit.Builder()
//                    .baseUrl(BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build()
//                    .create(FirebaseAuthApi::class.java)
//            }
//        }
//    }
}