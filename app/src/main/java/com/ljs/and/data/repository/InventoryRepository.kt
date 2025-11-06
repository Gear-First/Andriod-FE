package com.ljs.and.data.repository

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.BranchPurchaseOrderResponse
import com.ljs.and.data.model.InventoryOnHandResponse
import com.ljs.and.data.model.PurchaseOrderRequest
import com.ljs.and.data.model.PurchaseOrderResponse
import com.ljs.and.data.remote.InventoryApiService
import com.ljs.and.data.remote.PurchaseOrderApiService
import com.ljs.and.data.remote.RetrofitClient

object InventoryRepository {

    private val inventoryApiService: InventoryApiService = RetrofitClient.inventoryApiService
    private val purchaseOrderApiService: PurchaseOrderApiService = RetrofitClient.purchaseOrderApiService

    // Caching
    private var inventoryCache: ApiResponse<InventoryOnHandResponse>? = null
    private var purchaseOrdersCache: ApiResponse<BranchPurchaseOrderResponse>? = null

    suspend fun getInventoryOnHand(
        warehouseCode: String?,
        partKeyword: String?,
        supplierName: String?,
        minQty: Int?,
        maxQty: Int?,
        page: Int,
        size: Int,
        sort: List<String>?,
        forceRefresh: Boolean = false
    ): ApiResponse<InventoryOnHandResponse> {
        if (!forceRefresh && inventoryCache != null) {
            return inventoryCache!!
        }

        val response = inventoryApiService.getInventoryOnHand(
            warehouseCode, partKeyword, supplierName, minQty, maxQty, page, size, sort
        )
        inventoryCache = response
        return response
    }

    suspend fun createPurchaseOrder(
        purchaseOrderRequest: PurchaseOrderRequest
    ): ApiResponse<PurchaseOrderResponse> {
        val response = purchaseOrderApiService.createPurchaseOrder(purchaseOrderRequest)
        if (response.success) {
            // Invalidate cache on successful creation
            purchaseOrdersCache = null
        }
        return response
    }

    suspend fun getBranchPurchaseOrders(
        branchCode: String,
        engineerId: Long,
        startDate: String?,
        endDate: String?,
        page: Int,
        size: Int,
        forceRefresh: Boolean = false
    ): ApiResponse<BranchPurchaseOrderResponse> {
        if (!forceRefresh && purchaseOrdersCache != null) {
            return purchaseOrdersCache!!
        }

        val response = purchaseOrderApiService.getBranchPurchaseOrders(
            branchCode, engineerId, startDate, endDate, page, size
        )
        purchaseOrdersCache = response
        return response
    }

    // Public method to invalidate caches if needed from outside
    fun invalidateCaches() {
        inventoryCache = null
        purchaseOrdersCache = null
    }
}
