package com.ljs.and.data.remote

import com.ljs.and.data.model.InventoryItem
import retrofit2.http.GET
import retrofit2.http.Query

// Generic response wrappers
data class ApiResponse<T>(
    val data: DataWrapper<T>
)

data class DataWrapper<T>(
    val items: List<T>
)

// DTO for Receiving API response
data class ReceivingItem(
    val receivingNo: String,
    val supplierName: String?,
    val warehouseCode: String,
    val status: String,
    val requestedAt: String
)

// DTO for Shipping API response
data class ShippingItem(
    val shippingNo: String,
    val branchName: String?,
    val warehouseCode: String,
    val status: String,
    val requestedAt: String
)

interface SearchApiService {

    @GET("/warehouse/api/v1/receiving/notes")
    suspend fun getReceivingNotes(
        @Query("q") query: String?,
        @Query("warehouseCode") warehouseCode: String?,
        @Query("status") status: String?,
        @Query("dateFrom") dateFrom: String?,
        @Query("dateTo") dateTo: String?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<ReceivingItem>

    @GET("/warehouse/api/v1/shipping/notes")
    suspend fun getShippingNotes(
        @Query("q") query: String?,
        @Query("warehouseCode") warehouseCode: String?,
        @Query("status") status: String?,
        @Query("dateFrom") dateFrom: String?,
        @Query("dateTo") dateTo: String?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<ShippingItem>

    @GET("/warehouse/api/v1/inventory/on-hand")
    suspend fun getInventoryOnHand(
        @Query("q") query: String?,
        @Query("warehouseCode") warehouseCode: String?,
        @Query("minQty") minQty: Int?,
        @Query("maxQty") maxQty: Int?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<InventoryItem>
}
