package com.example.smartfarm.data.repository

import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.data.remote.retrofit.service.cage.CageApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KandangRepository @Inject constructor(
) {
    private val dataList = mutableListOf<DailyData>()

    fun insertDailyData(data: DailyData) {
        dataList.add(data)
    }

    fun getAllData(): List<DailyData> = dataList
}
