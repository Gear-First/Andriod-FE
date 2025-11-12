package com.ljs.and.data.remote.fake

import com.ljs.and.data.model.InventoryItem
import com.ljs.and.data.model.Part
import com.ljs.and.data.remote.ApiResponse
import com.ljs.and.data.remote.DataWrapper
import com.ljs.and.data.remote.ReceivingItem
import com.ljs.and.data.remote.SearchApiService
import com.ljs.and.data.remote.ShippingItem
import kotlinx.coroutines.delay

class FakeSearchApiService : SearchApiService {

    private val dummyReceivingItems = (1..20).map {
        ReceivingItem(
            receivingNo = "RN-DUMMY-$it",
            supplierName = "Dummy Supplier $it",
            warehouseCode = "DUMMY-WC",
            status = if (it % 2 == 0) "DONE" else "PENDING",
            requestedAt = "2024-08-01"
        )
    }

    private val dummyShippingItems = (1..20).map {
        ShippingItem(
            shippingNo = "SN-DUMMY-$it",
            branchName = "Dummy Branch $it",
            warehouseCode = "DUMMY-WC",
            status = if (it % 2 == 0) "DONE" else "PENDING",
            requestedAt = "2024-08-01"
        )
    }

    private val dummyInventoryItems = (1..20).map {
        InventoryItem(
            warehouseCode = "DUMMY-WC",
            part = Part(id = it.toLong(), code = "P-DUMMY-$it", name = "Dummy Part $it"),
            onHandQty = it * 10,
            supplierName = "Dummy Supplier",
            safetyStockQty = 20
        )
    }

    override suspend fun getReceivingNotes(
        query: String?,
        warehouseCode: String?,
        status: String?,
        dateFrom: String?,
        dateTo: String?,
        page: Int,
        size: Int
    ): ApiResponse<ReceivingItem> {
        delay(300)
        val filtered = dummyReceivingItems.filter { (query == null || it.receivingNo.contains(query, ignoreCase = true) || it.supplierName?.contains(query, ignoreCase = true) == true) && (warehouseCode == null || it.warehouseCode == warehouseCode) && (status == null || it.status.equals(status, ignoreCase = true)) }
        val paged = filtered.drop(page * size).take(size)
        return ApiResponse(DataWrapper(paged))
    }

    override suspend fun getShippingNotes(
        query: String?,
        warehouseCode: String?,
        status: String?,
        dateFrom: String?,
        dateTo: String?,
        page: Int,
        size: Int
    ): ApiResponse<ShippingItem> {
        delay(300)
        val filtered = dummyShippingItems.filter { (query == null || it.shippingNo.contains(query, ignoreCase = true) || it.branchName?.contains(query, ignoreCase = true) == true) && (warehouseCode == null || it.warehouseCode == warehouseCode) && (status == null || it.status.equals(status, ignoreCase = true)) }
        val paged = filtered.drop(page * size).take(size)
        return ApiResponse(DataWrapper(paged))
    }

    override suspend fun getInventoryOnHand(
        query: String?,
        warehouseCode: String?,
        minQty: Int?,
        maxQty: Int?,
        page: Int,
        size: Int
    ): ApiResponse<InventoryItem> {
        delay(300)
        val filtered = dummyInventoryItems.filter { (query == null || it.part.name.contains(query, ignoreCase = true) || it.part.code.contains(query, ignoreCase = true)) && (warehouseCode == null || it.warehouseCode == warehouseCode) }
        val paged = filtered.drop(page * size).take(size)
        return ApiResponse(DataWrapper(paged))
    }
}
