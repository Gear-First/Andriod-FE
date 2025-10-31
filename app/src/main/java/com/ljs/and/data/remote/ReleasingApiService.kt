package com.ljs.and.data.remote

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.CreateShippingRequest
import com.ljs.and.data.model.PagedShippingNotes
import com.ljs.and.data.model.ShippingNoteDetail
import com.ljs.and.data.model.UpdateShippingLineRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface ReleasingApiService {

    @POST("api/v1/shipping")
    suspend fun createShippingRequest(@Body request: CreateShippingRequest): ApiResponse<ShippingNoteDetail>

    @GET("api/v1/shipping/not-done")
    suspend fun getNotDoneShippingNotes(
        @Query("date") date: String?,
        @Query("warehouseId") warehouseId: Long?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String?
    ): ApiResponse<PagedShippingNotes>

    @GET("api/v1/shipping/done")
    suspend fun getDoneShippingNotes(
        @Query("date") date: String?,
        @Query("warehouseId") warehouseId: Long?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String?
    ): ApiResponse<PagedShippingNotes>

    @GET("api/v1/shipping/{noteId}")
    suspend fun getShippingNoteDetail(@Path("noteId") noteId: Long): ApiResponse<ShippingNoteDetail>

    @PATCH("api/v1/shipping/{noteId}/lines/{lineId}")
    suspend fun updateShippingLine(
        @Path("noteId") noteId: Long,
        @Path("lineId") lineId: Long,
        @Body request: UpdateShippingLineRequest
    ): ApiResponse<ShippingNoteDetail>
}
