package com.ljs.and.data.remote.fake

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.AssigneeInfo
import com.ljs.and.data.model.CreateShippingRequest
import com.ljs.and.data.model.PagedShippingNotes
import com.ljs.and.data.model.Product
import com.ljs.and.data.model.ShippingCompletion
import com.ljs.and.data.model.ShippingLine
import com.ljs.and.data.model.ShippingNote
import com.ljs.and.data.model.ShippingNoteDetail
import com.ljs.and.data.model.UpdateShippingLineRequest
import com.ljs.and.data.remote.ReleasingApiService
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class FakeReleasingApiService : ReleasingApiService {

    private var dummyShippingNoteDetails: MutableList<ShippingNoteDetail>
    private var dummyShippingNotes: MutableList<ShippingNote>

    init {
        val initialDetails = (1..10).map {
            val lines = (1..5).map { lineIndex ->
                ShippingLine(
                    lineId = (it * 10 + lineIndex).toLong(),
                    product = Product(id = lineIndex.toLong(), code = "P00$lineIndex", name = "더미 상품 $lineIndex"),
                    orderedQty = lineIndex * 10,
                    pickedQty = 0,
                    status = "PENDING"
                )
            }
            ShippingNoteDetail(
                noteId = it.toLong(),
                branchName = "더미 지점 $it",
                itemKindsNumber = lines.size,
                totalQty = lines.sumOf { sl -> sl.orderedQty },
                status = if (it % 3 == 0) "COMPLETED" else "IN_PROGRESS",
                completedAt = if (it % 3 == 0) "2024-08-05" else null,
                shippingNo = "SN-DUMMY-00$it",
                warehouseCode = "WC-DUMMY",
                requestedAt = "2024-08-01",
                expectedShipDate = "2024-08-10",
                shippedAt = if (it % 3 == 0) "2024-08-06" else null,
                assigneeName = if (it % 3 == 0) "김출고" else null,
                assigneeDept = if (it % 3 == 0) "출고팀" else null,
                assigneePhone = if (it % 3 == 0) "010-1234-5678" else null,
                remark = "더미 출고 요청 $it",
                lines = lines
            )
        }
        dummyShippingNoteDetails = initialDetails.toMutableList()

        dummyShippingNotes = initialDetails.map { 
            ShippingNote(
                noteId = it.noteId,
                shippingNo = it.shippingNo,
                branchName = it.branchName,
                itemKindsNumber = it.itemKindsNumber,
                totalQty = it.totalQty,
                status = it.status,
                warehouseCode = it.warehouseCode,
                requestedAt = it.requestedAt,
                expectedShipDate = it.expectedShipDate,
                completedAt = it.completedAt
            )
        }.toMutableList()
    }

    override suspend fun createShippingRequest(request: CreateShippingRequest): ApiResponse<ShippingNoteDetail> {
        delay(500)
        val newNoteId = (dummyShippingNoteDetails.maxOfOrNull { it.noteId } ?: 0) + 1
        val newLines = request.lines.mapIndexed { index, line ->
            ShippingLine(
                lineId = newNoteId * 10 + index,
                product = Product(id = line.productId, code = "P-New", name = "새 상품"),
                orderedQty = line.orderedQty,
                pickedQty = 0,
                status = "PENDING"
            )
        }
        val newDetail = ShippingNoteDetail(
            noteId = newNoteId,
            branchName = request.branchName,
            itemKindsNumber = request.lines.size,
            totalQty = request.lines.sumOf { it.orderedQty },
            status = "IN_PROGRESS",
            completedAt = null,
            shippingNo = request.shippingNo ?: "SN-DUMMY-$newNoteId",
            warehouseCode = request.warehouseCode,
            requestedAt = request.requestedAt,
            expectedShipDate = request.expectedShipDate,
            shippedAt = null,
            assigneeName = null,
            assigneeDept = null,
            assigneePhone = null,
            remark = request.remark,
            lines = newLines
        )
        
        dummyShippingNoteDetails.add(0, newDetail)

        val newNote = ShippingNote(
            noteId = newDetail.noteId,
            shippingNo = newDetail.shippingNo,
            branchName = newDetail.branchName,
            itemKindsNumber = newDetail.itemKindsNumber,
            totalQty = newDetail.totalQty,
            status = newDetail.status,
            warehouseCode = newDetail.warehouseCode,
            requestedAt = newDetail.requestedAt,
            expectedShipDate = newDetail.expectedShipDate,
            completedAt = newDetail.completedAt
        )
        dummyShippingNotes.add(0, newNote)

        return ApiResponse(200, true, "Success", newDetail)
    }

    override suspend fun completeShipping(noteId: Long, assigneeInfo: AssigneeInfo): ApiResponse<ShippingCompletion> {
        delay(500)
        val noteIndex = dummyShippingNoteDetails.indexOfFirst { it.noteId == noteId }
        
        if (noteIndex != -1) {
            val oldNote = dummyShippingNoteDetails[noteIndex]
            val completedNote = oldNote.copy(
                status = "COMPLETED",
                completedAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                assigneeName = assigneeInfo.assigneeName,
                assigneeDept = assigneeInfo.assigneeDept,
                assigneePhone = assigneeInfo.assigneePhone
            )
            dummyShippingNoteDetails[noteIndex] = completedNote

            val summaryNoteIndex = dummyShippingNotes.indexOfFirst { it.noteId == noteId }
            if (summaryNoteIndex != -1) {
                val oldSummary = dummyShippingNotes[summaryNoteIndex]
                dummyShippingNotes[summaryNoteIndex] = oldSummary.copy(
                    status = "COMPLETED",
                    completedAt = completedNote.completedAt
                )
            }
            
            return ApiResponse(
                status = 200, 
                success = true, 
                message = "Success", 
                data = ShippingCompletion(
                    completedAt = completedNote.completedAt!!,
                    totalShippedQty = completedNote.lines.sumOf { it.pickedQty }
                )
            )
        } else {
            return ApiResponse(404, false, "Note not found", null)
        }
    }

    override suspend fun updateShippingLine(noteId: Long, lineId: Long, request: UpdateShippingLineRequest): ApiResponse<ShippingNoteDetail> {
        delay(300)
        val noteIndex = dummyShippingNoteDetails.indexOfFirst { it.noteId == noteId }

        if (noteIndex != -1) {
            val oldNote = dummyShippingNoteDetails[noteIndex]
            
            val updatedLines = oldNote.lines.map { line ->
                if (line.lineId == lineId) {
                    val newStatus = when {
                        request.pickedQty >= line.orderedQty -> "READY"
                        request.pickedQty < line.orderedQty && request.pickedQty > 0 -> "SHORTAGE"
                        else -> "PENDING"
                    }
                    line.copy(pickedQty = request.pickedQty, status = newStatus)
                } else {
                    line
                }
            }
            
            val updatedNote = oldNote.copy(lines = updatedLines)
            dummyShippingNoteDetails[noteIndex] = updatedNote
            
            return ApiResponse(200, true, "Success", updatedNote)
        } else {
            return ApiResponse(404, false, "Note not found", null)
        }
    }

    override suspend fun getShippingNoteDetail(noteId: Long): ApiResponse<ShippingNoteDetail> {
        delay(200)
        val noteDetail = dummyShippingNoteDetails.find { it.noteId == noteId }
        return if (noteDetail != null) {
            ApiResponse(200, true, "Success", noteDetail)
        } else {
            ApiResponse(404, false, "Not Found", null)
        }
    }

    private suspend fun getPaginatedNotes(
        notes: List<ShippingNote>,
        page: Int,
        size: Int
    ): ApiResponse<PagedShippingNotes> {
        delay(500)
        val paginated = notes.drop(page * size).take(size)
        return ApiResponse(
            status = 200,
            success = true,
            message = "Success",
            data = PagedShippingNotes(
                items = paginated,
                page = page,
                size = size,
                total = notes.size.toLong()
            )
        )
    }

    override suspend fun getShippingNotes(status: String, date: String?, dateFrom: String?, dateTo: String?, warehouseCode: String?, page: Int, size: Int, sort: List<String>?): ApiResponse<PagedShippingNotes> {
        val filtered = dummyShippingNotes.filter { it.status.equals(status, ignoreCase = true) }
        return getPaginatedNotes(filtered, page, size)
    }

    override suspend fun getNotDoneShippingNotes(date: String?, dateFrom: String?, dateTo: String?, warehouseCode: String?, page: Int, size: Int, sort: List<String>?): ApiResponse<PagedShippingNotes> {
        val filtered = dummyShippingNotes.filter { it.status != "COMPLETED" }
        return getPaginatedNotes(filtered, page, size)
    }

    override suspend fun getDoneShippingNotes(date: String?, dateFrom: String?, dateTo: String?, warehouseCode: String?, page: Int, size: Int, sort: List<String>?): ApiResponse<PagedShippingNotes> {
        val filtered = dummyShippingNotes.filter { it.status == "COMPLETED" }
        return getPaginatedNotes(filtered, page, size)
    }
}
