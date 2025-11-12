package com.ljs.and.data.repository

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.InventoryOnHandResponse
import com.ljs.and.data.model.PurchaseOrder
import com.ljs.and.data.model.PurchaseOrderRequest
import com.ljs.and.data.model.PurchaseOrderResponse
import com.ljs.and.data.model.common.PaginatedData
import com.ljs.and.data.remote.InventoryApiService
import com.ljs.and.data.remote.PurchaseOrderApiService
import com.ljs.and.data.remote.RetrofitClient

class InventoryRepository {

    private val inventoryApiService: InventoryApiService by lazy {
        RetrofitClient.inventoryApiService
    }
    private val purchaseOrderApiService: PurchaseOrderApiService by lazy {
        RetrofitClient.purchaseOrderApiService
    }

    suspend fun fetchInventoryList(
        warehouseCode: String?,
        partKeyword: String?,
        supplierName: String?,
        minQty: Int?,
        maxQty: Int?,
        page: Int,
        size: Int
    ): Result<ApiResponse<InventoryOnHandResponse>> {
        return try {
            val response = inventoryApiService.getInventoryOnHand(
                warehouseCode = warehouseCode,
                partKeyword = partKeyword,
                supplierName = supplierName,
                minQty = minQty,
                maxQty = maxQty,
                page = page,
                size = size,
                sort = null
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitPurchaseOrder(
        userId: Long?,
        username: String?,
        rank: String?,
        region: String?,
        workType: String?,
        request: PurchaseOrderRequest
    ): Result<ApiResponse<PurchaseOrderResponse>> {
        return try {
            val response = purchaseOrderApiService.createPurchaseOrder(
                userId = userId,
                username = username,
                rank = rank,
                region = region,
                workType = workType,
                purchaseOrderRequest = request
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchBranchOrders(
        userId: Long?,
        username: String?,
        rank: String?,
        region: String?,
        workType: String?,
        startDate: String?,
        endDate: String?,
        page: Int?,
        size: Int?
    ): Result<ApiResponse<PaginatedData<PurchaseOrder>>> {
        return try {
            val response = purchaseOrderApiService.getBranchOrders(
                userId = userId,
                username = username,
                rank = rank,
                region = region,
                workType = workType,
                startDate = startDate,
                endDate = endDate,
                page = page,
                size = size
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
