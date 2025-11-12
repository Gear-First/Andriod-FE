package com.ljs.and.data.remote.fake

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.InventoryOnHandItem
import com.ljs.and.data.model.InventoryOnHandResponse
import com.ljs.and.data.model.Part
import com.ljs.and.data.remote.InventoryApiService
import kotlinx.coroutines.delay

class FakeInventoryApiService : InventoryApiService {

    override suspend fun getInventoryOnHand(
        warehouseCode: String?,
        partKeyword: String?,
        supplierName: String?,
        minQty: Int?,
        maxQty: Int?,
        page: Int,
        size: Int,
        sort: List<String>?
    ): ApiResponse<InventoryOnHandResponse> {
        delay(500) // Simulate network delay

        val dummyItems = (1..25).map { i ->
            InventoryOnHandItem(
                warehouseCode = warehouseCode ?: "DUMMY-WC",
                part = Part(
                    id = i.toLong(),
                    code = "DUMMY-PART-$i",
                    name = "더미 부품 $i"
                ),
                onHandQty = (i * 10),
                updatedAt = "2024-08-05",
                lowStock = (i % 5 == 0),
                safetyStockQty = 50,
                supplierName = "더미 공급업체",
                price = i * 1000,
                priceTotal = i * 1000L * (i * 10),
                lastReceivingDate = "2024-07-20"
            )
        }

        val paginatedItems = dummyItems.drop(page * size).take(size)

        val response = InventoryOnHandResponse(
            items = paginatedItems,
            page = page,
            size = size,
            total = dummyItems.size.toLong()
        )

        return ApiResponse(
            status = 200,
            success = true,
            message = "Success",
            data = response
        )
    }
}
