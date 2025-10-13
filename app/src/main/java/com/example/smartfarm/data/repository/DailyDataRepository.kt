package com.example.smartfarm.data.repository

import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.data.remote.response.GetDailyInformationResponse
import com.example.smartfarm.data.remote.response.GetDailyResponseItem
import com.example.smartfarm.data.remote.retrofit.service.daily.DailyActivityApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyDataRepository @Inject constructor(
    private val dailyApi: DailyActivityApi
) {
    private val dataList = mutableListOf<DailyData>()
    private val _allData = MutableStateFlow<List<DailyData>>(emptyList())
    val allData = _allData.asStateFlow() // observe everything if needed

    suspend fun addDailyActivity(
        bearer: String?,
        req: DailyData
    ): Result<Unit> = runCatching {
        val res = dailyApi.addDailyData(req, bearer)
    }

    suspend fun getDailyActivities(
        bearer: String?,
        cageId: String
    ): Result<List<GetDailyResponseItem>> = runCatching {
        val res = dailyApi.getDailyActivities(cageId, bearer)
        res.response?.filterNotNull() ?: emptyList()
    }

    fun getAllData(): List<DailyData> = dataList


    // helpers
    fun getDataByCageId(cageId: String): List<DailyData> =
        dataList.filter { it.cage_id == cageId }

    fun observeDataByCageId(cageId: String): Flow<List<DailyData>> =
        allData.map { list -> list.filter { it.cage_id == cageId } }

    fun clearForCage(cageId: String) {
        dataList.removeAll { it.cage_id == cageId }
        _allData.value = dataList.toList()
    }
}
