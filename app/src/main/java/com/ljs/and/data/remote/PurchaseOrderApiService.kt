package com.ljs.and.data.remote

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.PurchaseOrder
import com.ljs.and.data.model.PurchaseOrderRequest
import com.ljs.and.data.model.PurchaseOrderResponse
import com.ljs.and.data.model.PaginatedData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PurchaseOrderApiService {

    @POST("api/v1/purchase-orders")
    suspend fun createPurchaseOrder(
        @Query("userId") userId: Long?,
        @Query("username") username: String?,
        @Query("rank") rank: String?,
        @Query("region") region: String?,
        @Query("workType") workType: String?,
        @Body purchaseOrderRequest: PurchaseOrderRequest
    ): ApiResponse<PurchaseOrderResponse>

    @GET("api/v1/purchase-orders/branch")
    suspend fun getBranchOrders(
        @Query("userId") userId: Long?,
        @Query("username") username: String?,
        @Query("rank") rank: String?,
        @Query("region") region: String?,
        @Query("workType") workType: String?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("page") page: Int?,
        @Query("size") size: Int?
    ): ApiResponse<PaginatedData<PurchaseOrder>>
}
