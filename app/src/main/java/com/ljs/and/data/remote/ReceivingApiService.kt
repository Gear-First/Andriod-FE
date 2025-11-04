package com.ljs.and.data.remote

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.CreateReceivingRequest
import com.ljs.and.data.model.InspectorInfo
import com.ljs.and.data.model.PagedReceivingNotes
import com.ljs.and.data.model.ReceivingCompletion
import com.ljs.and.data.model.ReceivingNoteDetail
import com.ljs.and.data.model.UpdateReceivingLineRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReceivingApiService {

    @POST("api/v1/receiving")
    suspend fun createReceivingRequest(@Body request: CreateReceivingRequest): ApiResponse<ReceivingNoteDetail>

    @POST("api/v1/receiving/{noteId}:complete")
    suspend fun completeReceiving(
        @Path("noteId") noteId: Long,
        @Body inspectorInfo: InspectorInfo
    ): ApiResponse<ReceivingCompletion>

    @PATCH("api/v1/receiving/{noteId}/lines/{lineId}")
    suspend fun updateReceivingLine(
        @Path("noteId") noteId: Long,
        @Path("lineId") lineId: Long,
        @Body request: UpdateReceivingLineRequest
    ): ApiResponse<ReceivingNoteDetail>

    @GET("api/v1/receiving/{noteId}")
    suspend fun getReceivingNoteDetail(@Path("noteId") noteId: Long): ApiResponse<ReceivingNoteDetail>

    @GET("api/v1/receiving/notes")
    suspend fun getReceivingNotes(
        @Query("status") status: String,
        @Query("date") date: String?,
        @Query("dateFrom") dateFrom: String?,
        @Query("dateTo") dateTo: String?,
        @Query("warehouseCode") warehouseCode: String?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: List<String>?
    ): ApiResponse<PagedReceivingNotes>

    @GET("api/v1/receiving/not-done")
    suspend fun getNotDoneReceivingNotes(
        @Query("date") date: String?,
        @Query("dateFrom") dateFrom: String?,
        @Query("dateTo") dateTo: String?,
        @Query("warehouseCode") warehouseCode: String?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: List<String>?
    ): ApiResponse<PagedReceivingNotes>

    @GET("api/v1/receiving/done")
    suspend fun getDoneReceivingNotes(
        @Query("date") date: String?,
        @Query("dateFrom") dateFrom: String?,
        @Query("dateTo") dateTo: String?,
        @Query("warehouseCode") warehouseCode: String?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: List<String>?
    ): ApiResponse<PagedReceivingNotes>
}
