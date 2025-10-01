package com.example.smartfarm.di

import com.example.smartfarm.data.remote.retrofit.service.LoginApi
import com.example.smartfarm.data.remote.retrofit.service.RegisterApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoginApi(retrofit: Retrofit): LoginApi {
        return retrofit.create(LoginApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRegisterApi(retrofit: Retrofit): RegisterApi {
        return retrofit.create(RegisterApi::class.java)
    }
}