package com.ljs.and.data.remote

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.InventoryOnHandResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface InventoryApiService {

    @GET("api/v1/inventory/onhand")
    suspend fun getInventoryOnHand(
        @Query("warehouseCode") warehouseCode: String?,
        @Query("partKeyword") partKeyword: String?,
        @Query("supplierName") supplierName: String?,
        @Query("minQty") minQty: Int?,
        @Query("maxQty") maxQty: Int?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: List<String>?
    ): ApiResponse<InventoryOnHandResponse>
}
