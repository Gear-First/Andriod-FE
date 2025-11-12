package com.ljs.and.data.repository

import com.ljs.and.data.model.InOutData
import com.ljs.and.data.model.NoteCountsData
import com.ljs.and.data.remote.HomeApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    suspend fun getWeeklyInOutData(baseDate: Date, warehouseCode: String): Result<List<InOutData>> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = baseDate

            // Find Sunday of the week of baseDate
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            val dateFrom = dateFormat.format(calendar.time)

            // Find Saturday of the week of baseDate
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            val dateTo = dateFormat.format(calendar.time)

            coroutineScope {
                val receivingDeferred = async {
                    homeApiService.getReceivingNotes(
                        status = "all",
                        dateFrom = dateFrom,
                        dateTo = dateTo,
                        warehouseCode = warehouseCode
                    )
                }

                val shippingDeferred = async {
                    homeApiService.getShippingNotes(
                        status = "all",
                        dateFrom = dateFrom,
                        dateTo = dateTo,
                        warehouseCode = warehouseCode
                    )
                }

                val receivingResponse = receivingDeferred.await()
                val shippingResponse = shippingDeferred.await()

                val dailyTotals = mutableMapOf<String, Pair<Float, Float>>()
                val tempCal = Calendar.getInstance()
                tempCal.time = dateFormat.parse(dateFrom)!!
                for (i in 0..6) {
                    dailyTotals[dateFormat.format(tempCal.time)] = Pair(0f, 0f)
                    tempCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                receivingResponse.data?.items?.forEach { item ->
                    val date = item.requestedAt.substring(0, 10)
                    if (dailyTotals.containsKey(date)) {
                        val current = dailyTotals[date]!!
                        dailyTotals[date] = Pair(current.first + item.totalQty, current.second)
                    }
                }

                shippingResponse.data?.items?.forEach { item ->
                    val date = item.requestedAt.substring(0, 10)
                    if (dailyTotals.containsKey(date)) {
                        val current = dailyTotals[date]!!
                        dailyTotals[date] = Pair(current.first, current.second + item.totalQty)
                    }
                }
                
                val inOutDataList = dailyTotals.map { (date, totals) ->
                    InOutData(
                        day = date,
                        inbound = totals.first,
                        outbound = totals.second
                    )
                }
                Result.success(inOutDataList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
