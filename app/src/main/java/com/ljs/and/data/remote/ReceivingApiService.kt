package com.ljs.and.data.remote

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.ReceivingNoteDetail
import com.ljs.and.data.model.ReceivingNotePage
import com.ljs.and.data.model.UpdateInspectionRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface ReceivingApiService {

    @GET("api/v1/receiving/not-done")
    suspend fun getNotDoneReceivingNotes(
        @Query("date") date: String?,
        @Query("warehouseId") warehouseId: Long?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<ReceivingNotePage>

    @GET("api/v1/receiving/done")
    suspend fun getDoneReceivingNotes(
        @Query("date") date: String?,
        @Query("warehouseId") warehouseId: Long?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<ReceivingNotePage>

    @GET("api/v1/receiving/{noteId}")
    suspend fun getReceivingNoteDetail(@Path("noteId") noteId: Long): ApiResponse<ReceivingNoteDetail>

    @PATCH("api/v1/receiving/{noteId}/lines/{lineId}")
    suspend fun updateReceivingLine(
        @Path("noteId") noteId: Long,
        @Path("lineId") lineId: Long,
        @Body request: UpdateInspectionRequest
    ): ApiResponse<ReceivingNoteDetail>
}
