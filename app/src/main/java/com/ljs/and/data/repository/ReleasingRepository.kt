package com.ljs.and.data.repository

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.CreateShippingRequest
import com.ljs.and.data.model.PagedShippingNotes
import com.ljs.and.data.model.ShippingNoteDetail
import com.ljs.and.data.model.UpdateShippingLineRequest
import com.ljs.and.data.remote.ReleasingApiService

class ReleasingRepository(private val apiService: ReleasingApiService) {

    suspend fun createShippingRequest(request: CreateShippingRequest): ApiResponse<ShippingNoteDetail> {
        return apiService.createShippingRequest(request)
    }

    suspend fun getNotDoneShippingNotes(
        date: String?,
        warehouseId: Long?,
        page: Int,
        size: Int,
        sort: String?
    ): ApiResponse<PagedShippingNotes> {
        return apiService.getNotDoneShippingNotes(date, warehouseId, page, size, sort)
    }

    suspend fun getDoneShippingNotes(
        date: String?,
        warehouseId: Long?,
        page: Int,
        size: Int,
        sort: String?
    ): ApiResponse<PagedShippingNotes> {
        return apiService.getDoneShippingNotes(date, warehouseId, page, size, sort)
    }

    suspend fun getShippingNoteDetail(noteId: Long): ApiResponse<ShippingNoteDetail> {
        return apiService.getShippingNoteDetail(noteId)
    }

    suspend fun updateShippingLine(noteId: Long, lineId: Long, request: UpdateShippingLineRequest): ApiResponse<ShippingNoteDetail> {
        return apiService.updateShippingLine(noteId, lineId, request)
    }
}
