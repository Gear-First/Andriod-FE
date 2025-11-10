package com.ljs.and.data.repository

import com.ljs.and.data.model.NoteCountsData
import com.ljs.and.data.remote.HomeApiService
import java.text.SimpleDateFormat
import java.util.Calendar
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

    suspend fun getWeeklyInOutData(baseDate: Date, warehouseCode: String?): Map<String, Pair<Int, Int>> {
        val calendar = Calendar.getInstance()
        calendar.time = baseDate
        val dateToString = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val dateFromString = dateFormat.format(calendar.time)

        val shippingResponse = homeApiService.getShippingNotes(
            dateFrom = null,
            dateTo = null,
            warehouseCode = warehouseCode
        )

        val receivingResponse = homeApiService.getReceivingNotes(
            dateFrom = null,
            dateTo = null,
            warehouseCode = warehouseCode
        )

        val dailyData = mutableMapOf<String, Pair<Int, Int>>()
        val dates = getDatesBetween(dateFromString, dateToString)
        dates.forEach { date ->
            dailyData[date] = Pair(0, 0)
        }

        val dateStringsInPeriod = getDatesBetween(dateFromString, dateToString)

        receivingResponse.data?.items?.forEach { item ->
            item.expectedReceiveDate?.let {
                val date = it.substring(0, 10)
                if (dateStringsInPeriod.contains(date)) {
                    val current = dailyData[date]!!
                    dailyData[date] = Pair(current.first + item.totalQty, current.second)
                }
            }
        }

        shippingResponse.data?.items?.forEach { item ->
            item.expectedShipDate?.let {
                val date = it.substring(0, 10)
                if (dateStringsInPeriod.contains(date)) {
                    val current = dailyData[date]!!
                    dailyData[date] = Pair(current.first, current.second + item.totalQty)
                }
            }
        }
        return dailyData
    }

    private fun getDatesBetween(startDate: String, endDate: String): List<String> {
        val dates = mutableListOf<String>()
        val start = dateFormat.parse(startDate)
        val end = dateFormat.parse(endDate)
        val cal = Calendar.getInstance()
        if (start != null) {
            cal.time = start
        }
        while (end != null && (cal.time.before(end) || cal.time == end)) {
            dates.add(dateFormat.format(cal.time))
            cal.add(Calendar.DATE, 1)
        }
        return dates
    }
}
