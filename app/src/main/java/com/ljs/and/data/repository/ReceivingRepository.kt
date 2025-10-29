package com.ljs.and.data.repository

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.ReceivingNoteDetail
import com.ljs.and.data.model.ReceivingNotePage
import com.ljs.and.data.model.UpdateInspectionRequest
import com.ljs.and.data.remote.ReceivingApiService

class ReceivingRepository(private val apiService: ReceivingApiService) {

    suspend fun getNotDoneReceivingNotes(date: String?, warehouseId: Long?, page: Int, size: Int): ApiResponse<ReceivingNotePage> {
        return apiService.getNotDoneReceivingNotes(date, warehouseId, page, size)
    }

    suspend fun getDoneReceivingNotes(date: String?, warehouseId: Long?, page: Int, size: Int): ApiResponse<ReceivingNotePage> {
        return apiService.getDoneReceivingNotes(date, warehouseId, page, size)
    }

    suspend fun getReceivingNoteDetail(noteId: Long): ApiResponse<ReceivingNoteDetail> {
        return apiService.getReceivingNoteDetail(noteId)
    }

    suspend fun updateReceivingLine(noteId: Long, lineId: Long, request: UpdateInspectionRequest): ApiResponse<ReceivingNoteDetail> {
        return apiService.updateReceivingLine(noteId, lineId, request)
    }
}
