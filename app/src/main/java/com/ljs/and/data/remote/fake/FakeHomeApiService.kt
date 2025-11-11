package com.ljs.and.data.remote.fake

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.InOutData
import com.ljs.and.data.model.TopInventoryItemDto
import com.ljs.and.data.model.NoteCountsResponse
import com.ljs.and.data.model.ReceivingNotesResponse
import com.ljs.and.data.model.ReceivingResponse
import com.ljs.and.data.model.ShippingNotesResponse
import com.ljs.and.data.model.ShippingResponse
import com.ljs.and.data.model.NoteCountsData
import com.ljs.and.data.model.ReceivingData
import com.ljs.and.data.model.ReceivingItem
import com.ljs.and.data.model.ReceivingNotesData
import com.ljs.and.data.model.ShippingData
import com.ljs.and.data.model.ShippingItem
import com.ljs.and.data.model.ShippingNotesData
import com.ljs.and.data.remote.HomeApiService
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class FakeHomeApiService : HomeApiService {

    private val dummyShippingItems = (1..5).map {
        ShippingItem(
            noteId = 100L + it,
            shippingNo = "DUMMY-SHIP-00$it",
            branchName = "더미 지점 $it",
            itemKindsNumber = 3,
            totalQty = 15,
            status = "PENDING",
            warehouseCode = "DUMMY-WC",
            requestedAt = "2024-08-05",
            expectedShipDate = "2024-08-10",
            completedAt = null
        )
    }

    private val dummyReceivingItems = (1..5).map {
        ReceivingItem(
            noteId = 200L + it,
            receivingNo = "DUMMY-RCV-00$it",
            supplierName = "더미 공급사 $it",
            itemKindsNumber = 5,
            totalQty = 50,
            status = "PENDING",
            warehouseCode = "DUMMY-WC",
            requestedAt = "2024-08-04",
            expectedReceiveDate = "2024-08-09",
            completedAt = null
        )
    }

    override suspend fun getShippingNotes(
        dateFrom: String?,
        dateTo: String?,
        warehouseCode: String?,
        page: Int,
        size: Int,
        sort: String?
    ): ShippingResponse {
        delay(400)
        return ShippingResponse(
            status = 200,
            success = true,
            message = "Success (Fake)",
            data = ShippingData(
                items = dummyShippingItems,
                page = page,
                size = size,
                total = dummyShippingItems.size.toLong()
            )
        )
    }

    override suspend fun getReceivingNotes(
        dateFrom: String?,
        dateTo: String?,
        warehouseCode: String?,
        page: Int,
        size: Int,
        sort: String?
    ): ReceivingResponse {
        delay(400)
        return ReceivingResponse(
            status = 200,
            success = true,
            message = "Success (Fake)",
            data = ReceivingData(
                items = dummyReceivingItems,
                page = page,
                size = size,
                total = dummyReceivingItems.size.toLong()
            )
        )
    }

    override suspend fun getNoteCounts(requestDate: String): NoteCountsResponse {
        delay(600)
        return NoteCountsResponse(
            status = 200,
            success = true,
            message = "Success (Fake)",
            data = NoteCountsData(
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                receivingCount = Random.nextInt(5, 15),
                shippingCount = Random.nextInt(3, 10)
            )
        )
    }

    override suspend fun getReceivingNotes(
        status: String,
        dateFrom: String?,
        dateTo: String?,
        warehouseCode: String?
    ): ReceivingNotesResponse {
        delay(450)
        return ReceivingNotesResponse(
            status = 200,
            success = true,
            message = "Success (Fake)",
            data = ReceivingNotesData(
                items = dummyReceivingItems.filter { it.status.equals(status, ignoreCase = true) },
                page = 0,
                size = 100,
                total = dummyReceivingItems.size.toLong()
            )
        )
    }

    override suspend fun getShippingNotes(
        status: String,
        dateFrom: String?,
        dateTo: String?,
        warehouseCode: String?
    ): ShippingNotesResponse {
        delay(450)
        return ShippingNotesResponse(
            status = 200,
            success = true,
            message = "Success (Fake)",
            data = ShippingNotesData(
                items = dummyShippingItems.filter { it.status.equals(status, ignoreCase = true) },
                page = 0,
                size = 100,
                total = dummyShippingItems.size.toLong()
            )
        )
    }

    override suspend fun getTopInventoryItems(): ApiResponse<List<TopInventoryItemDto>> {
        delay(700)
        val topItems = listOf(
            TopInventoryItemDto("엔진 오일 필터", 150),
            TopInventoryItemDto("브레이크 패드 세트", 120),
            TopInventoryItemDto("에어컨 필터", 95),
            TopInventoryItemDto("와이퍼 블레이드", 80),
            TopInventoryItemDto("점화 플러그", 50)
        )
        return ApiResponse(200, true, "Success", topItems)
    }

    override suspend fun getWeeklyInOutData(baseDate: String, warehouseCode: String): ApiResponse<List<InOutData>> {
        delay(500)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = sdf.parse(baseDate) ?: Date()

        val calendar = Calendar.getInstance()
        calendar.time = parsedDate
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        val weeklyData = (0..6).map {
            val date = calendar.time
            val dateString = sdf.format(date)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            InOutData(
                day = dateString,
                inbound = Random.nextFloat() * 200,
                outbound = Random.nextFloat() * 150
            )
        }
        return ApiResponse(200, true, "Success (Fake)", weeklyData)
    }
}
