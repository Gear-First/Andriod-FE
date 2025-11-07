package com.ljs.and.data.remote

import com.ljs.and.data.model.ReceivingResponse
import com.ljs.and.data.model.ShippingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApiService {

    @GET("api/v1/shipping/notes")
    suspend fun getShippingNotes(
        @Query("status") status: String = "all",
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("warehouseCode") warehouseCode: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100,
        @Query("sort") sort: String? = null
    ): ShippingResponse

    @GET("api/v1/receiving/notes")
    suspend fun getReceivingNotes(
        @Query("status") status: String = "all",
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("warehouseCode") warehouseCode: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100,
        @Query("sort") sort: String? = null
    ): ReceivingResponse
}
