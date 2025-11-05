package com.ljs.and.data.repository

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.AssigneeInfo
import com.ljs.and.data.model.CreateShippingRequest
import com.ljs.and.data.model.PagedShippingNotes
import com.ljs.and.data.model.ShippingCompletion
import com.ljs.and.data.model.ShippingNoteDetail
import com.ljs.and.data.model.UpdateShippingLineRequest
import com.ljs.and.data.remote.ReleasingApiService

class ReleasingRepository(private val apiService: ReleasingApiService) {

    suspend fun createShippingRequest(request: CreateShippingRequest): ApiResponse<ShippingNoteDetail> {
        return apiService.createShippingRequest(request)
    }

    suspend fun completeShipping(noteId: Long, assigneeInfo: AssigneeInfo): ApiResponse<ShippingCompletion> {
        return apiService.completeShipping(noteId, assigneeInfo)
    }

    suspend fun updateShippingLine(noteId: Long, lineId: Long, request: UpdateShippingLineRequest): ApiResponse<ShippingNoteDetail> {
        return apiService.updateShippingLine(noteId, lineId, request)
    }

    suspend fun getShippingNoteDetail(noteId: Long): ApiResponse<ShippingNoteDetail> {
        return apiService.getShippingNoteDetail(noteId)
    }

    suspend fun getShippingNotes(
        status: String,
        date: String?,
        dateFrom: String?,
        dateTo: String?,
        warehouseCode: String?,
        page: Int,
        size: Int,
        sort: List<String>?
    ): ApiResponse<PagedShippingNotes> {
        return apiService.getShippingNotes(status, date, dateFrom, dateTo, warehouseCode, page, size, sort)
    }

    suspend fun getNotDoneShippingNotes(
        date: String?,
        dateFrom: String?,
        dateTo: String?,
        warehouseCode: String?,
        page: Int,
        size: Int,
        sort: List<String>?
    ): ApiResponse<PagedShippingNotes> {
        return apiService.getNotDoneShippingNotes(date, dateFrom, dateTo, warehouseCode, page, size, sort)
    }

    suspend fun getDoneShippingNotes(
        date: String?,
        dateFrom: String?,
        dateTo: String?,
        warehouseCode: String?,
        page: Int,
        size: Int,
        sort: List<String>?
    ): ApiResponse<PagedShippingNotes> {
        return apiService.getDoneShippingNotes(date, dateFrom, dateTo, warehouseCode, page, size, sort)
    }
}
