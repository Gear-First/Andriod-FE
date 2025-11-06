package com.ljs.and.data.remote

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.BranchPurchaseOrderResponse
import com.ljs.and.data.model.PurchaseOrderRequest
import com.ljs.and.data.model.PurchaseOrderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PurchaseOrderApiService {

    @POST("api/v1/purchase-orders")
    suspend fun createPurchaseOrder(
        @Body purchaseOrderRequest: PurchaseOrderRequest
    ): ApiResponse<PurchaseOrderResponse>

    @GET("api/v1/purchase-orders/branch")
    suspend fun getBranchPurchaseOrders(
        @Query("branchCode") branchCode: String,
        @Query("engineerId") engineerId: Long,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<BranchPurchaseOrderResponse>
}
