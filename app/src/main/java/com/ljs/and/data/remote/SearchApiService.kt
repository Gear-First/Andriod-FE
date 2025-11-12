package com.ljs.and.data.remote

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.InventoryItem
import com.ljs.and.data.model.ItemsResponse
import com.ljs.and.data.model.ReceivingItem
import com.ljs.and.data.model.ShippingItem
import retrofit2.http.GET
import retrofit2.http.Query

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
    ): ApiResponse<ItemsResponse<ReceivingItem>>

    @GET("/warehouse/api/v1/shipping/notes")
    suspend fun getShippingNotes(
        @Query("q") query: String?,
        @Query("warehouseCode") warehouseCode: String?,
        @Query("status") status: String?,
        @Query("dateFrom") dateFrom: String?,
        @Query("dateTo") dateTo: String?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<ItemsResponse<ShippingItem>>

    @GET("/warehouse/api/v1/inventory/on-hand")
    suspend fun getInventoryOnHand(
        @Query("q") query: String?,
        @Query("warehouseCode") warehouseCode: String?,
        @Query("minQty") minQty: Int?,
        @Query("maxQty") maxQty: Int?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<ItemsResponse<InventoryItem>>
}
