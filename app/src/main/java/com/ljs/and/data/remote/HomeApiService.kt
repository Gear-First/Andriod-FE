package com.ljs.and.data.remote

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.InOutData
import com.ljs.and.data.model.TopInventoryItemDto
import com.ljs.and.data.model.NoteCountsResponse
import com.ljs.and.data.model.ReceivingNotesResponse
import com.ljs.and.data.model.ReceivingResponse
import com.ljs.and.data.model.ShippingNotesResponse
import com.ljs.and.data.model.ShippingResponse
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Date

interface HomeApiService {

    @GET("api/v1/shipping/not-done")
    suspend fun getShippingNotes(
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("warehouseCode") warehouseCode: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100,
        @Query("sort") sort: String? = null
    ): ShippingResponse

    @GET("api/v1/receiving/not-done")
    suspend fun getReceivingNotes(
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("warehouseCode") warehouseCode: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100,
        @Query("sort") sort: String? = null
    ): ReceivingResponse

    @GET("api/v1/summary/note-counts")
    suspend fun getNoteCounts(
        @Query("requestDate") requestDate: String
    ): NoteCountsResponse

    @GET("api/v1/receiving/notes")
    suspend fun getReceivingNotes(
        @Query("status") status: String,
        @Query("dateFrom") dateFrom: String?,
        @Query("dateTo") dateTo: String?,
        @Query("warehouseCode") warehouseCode: String?
    ): ReceivingNotesResponse

    @GET("api/v1/shipping/notes")
    suspend fun getShippingNotes(
        @Query("status") status: String,
        @Query("dateFrom") dateFrom: String?,
        @Query("dateTo") dateTo: String?,
        @Query("warehouseCode") warehouseCode: String?
    ): ShippingNotesResponse

    @GET("api/v1/summary/top-inventory")
    suspend fun getTopInventoryItems(): ApiResponse<List<TopInventoryItemDto>>

    @GET("api/v1/summary/weekly-in-out")
    suspend fun getWeeklyInOutData(
        @Query("baseDate") baseDate: String,
        @Query("warehouseCode") warehouseCode: String
    ): ApiResponse<List<InOutData>>
}
