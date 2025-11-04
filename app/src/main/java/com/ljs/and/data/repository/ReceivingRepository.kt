package com.ljs.and.data.repository

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.CreateReceivingRequest
import com.ljs.and.data.model.InspectorInfo
import com.ljs.and.data.model.PagedReceivingNotes
import com.ljs.and.data.model.ReceivingCompletion
import com.ljs.and.data.model.ReceivingNoteDetail
import com.ljs.and.data.model.UpdateReceivingLineRequest
import com.ljs.and.data.remote.ReceivingApiService

class ReceivingRepository(private val apiService: ReceivingApiService) {

    suspend fun createReceivingRequest(request: CreateReceivingRequest): ApiResponse<ReceivingNoteDetail> {
        return apiService.createReceivingRequest(request)
    }

    suspend fun completeReceiving(noteId: Long, inspectorInfo: InspectorInfo): ApiResponse<ReceivingCompletion> {
        return apiService.completeReceiving(noteId, inspectorInfo)
    }

    suspend fun updateReceivingLine(noteId: Long, lineId: Long, request: UpdateReceivingLineRequest): ApiResponse<ReceivingNoteDetail> {
        return apiService.updateReceivingLine(noteId, lineId, request)
    }

    suspend fun getReceivingNoteDetail(noteId: Long): ApiResponse<ReceivingNoteDetail> {
        return apiService.getReceivingNoteDetail(noteId)
    }

    suspend fun getReceivingNotes(
        status: String,
        date: String?,
        dateFrom: String?,
        dateTo: String?,
        warehouseCode: String?,
        page: Int,
        size: Int,
        sort: List<String>?
    ): ApiResponse<PagedReceivingNotes> {
        return apiService.getReceivingNotes(status, date, dateFrom, dateTo, warehouseCode, page, size, sort)
    }

    suspend fun getNotDoneReceivingNotes(
        date: String?,
        dateFrom: String?,
        dateTo: String?,
        warehouseCode: String?,
        page: Int,
        size: Int,
        sort: List<String>?
    ): ApiResponse<PagedReceivingNotes> {
        return apiService.getNotDoneReceivingNotes(date, dateFrom, dateTo, warehouseCode, page, size, sort)
    }

    suspend fun getDoneReceivingNotes(
        date: String?,
        dateFrom: String?,
        dateTo: String?,
        warehouseCode: String?,
        page: Int,
        size: Int,
        sort: List<String>?
    ): ApiResponse<PagedReceivingNotes> {
        return apiService.getDoneReceivingNotes(date, dateFrom, dateTo, warehouseCode, page, size, sort)
    }
}
