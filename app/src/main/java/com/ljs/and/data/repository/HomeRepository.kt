package com.ljs.and.data.repository

import com.ljs.and.data.model.InOutData
import com.ljs.and.data.model.NoteCountsData
import com.ljs.and.data.remote.HomeApiService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeRepository(private val homeApiService: HomeApiService) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getNoteCounts(date: String): Result<NoteCountsData> {
        return try {
            val response = homeApiService.getNoteCounts(date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeeklyInOutData(baseDate: Date, warehouseCode: String): Result<List<InOutData>> {
        return try {
            val baseDateString = dateFormat.format(baseDate)
            val response = homeApiService.getWeeklyInOutData(baseDateString, warehouseCode)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
