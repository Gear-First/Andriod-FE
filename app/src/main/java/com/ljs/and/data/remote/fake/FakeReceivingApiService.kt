package com.ljs.and.data.remote.fake

import com.ljs.and.data.model.CreateReceivingRequest
import com.ljs.and.data.model.InspectorInfo
import com.ljs.and.data.model.PagedReceivingNotes
import com.ljs.and.data.model.Product
import com.ljs.and.data.model.ReceivingCompletion
import com.ljs.and.data.model.ReceivingLine
import com.ljs.and.data.model.ReceivingNote
import com.ljs.and.data.model.ReceivingNoteDetail
import com.ljs.and.data.model.UpdateReceivingLineRequest
import com.ljs.and.data.remote.ReceivingApiService
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class FakeReceivingApiService : ReceivingApiService {

    // 변경된 상태를 유지하기 위해 MutableList로 수정합니다.
    private var dummyReceivingNoteDetails: MutableList<ReceivingNoteDetail>
    private var dummyReceivingNotes: MutableList<ReceivingNote>
    private val dummyProducts: List<Product>

    init {
        dummyProducts = (1..20).map {
            Product(id = it.toLong(), lot = "LOT-NO-$it", code = "PART-NO-$it", name = "부품 $it")
        }

        val initialDetails = (1..10).map {
            val lines = (1..5).map { lineIndex ->
                val product = dummyProducts.random()
                ReceivingLine(
                    lineId = (it * 10 + lineIndex).toLong(),
                    product = product,
                    orderedQty = lineIndex * 10,
                    inspectedQty = 0,
                    status = "PENDING", // UI 코드와 상태 문자열 일치
                    lineRemark = "라인 비고 $lineIndex"
                )
            }
            ReceivingNoteDetail(
                noteId = it.toLong(),
                supplierName = "더미 공급업체 $it",
                itemKindsNumber = lines.size,
                totalQty = lines.sumOf { rl -> rl.orderedQty },
                status = if (it % 3 == 0) "COMPLETED" else "IN_PROGRESS", // UI 코드와 상태 문자열 일치
                completedAt = if (it % 3 == 0) "2024-08-05" else null,
                receivingNo = "RN-DUMMY-00$it",
                warehouseCode = "WC-DUMMY",
                requestedAt = "2024-08-01",
                expectedReceiveDate = "2024-08-10",
                receivedAt = if (it % 3 == 0) "2024-08-06" else null,
                inspectorName = if (it % 3 == 0) "박검수" else null,
                inspectorDept = if (it % 3 == 0) "검수팀" else null,
                inspectorPhone = if (it % 3 == 0) "010-8765-4321" else null,
                remark = "더미 입고 요청 $it",
                lines = lines
            )
        }
        dummyReceivingNoteDetails = initialDetails.toMutableList()

        dummyReceivingNotes = initialDetails.map { 
            ReceivingNote(
                noteId = it.noteId,
                receivingNo = it.receivingNo,
                supplierName = it.supplierName,
                itemKindsNumber = it.itemKindsNumber,
                totalQty = it.totalQty,
                status = it.status,
                warehouseCode = it.warehouseCode,
                requestedAt = it.requestedAt,
                expectedReceiveDate = it.expectedReceiveDate,
                completedAt = it.completedAt
            )
        }.toMutableList()
    }

    override suspend fun createReceivingRequest(request: CreateReceivingRequest): com.ljs.and.data.model.ApiResponse<ReceivingNoteDetail> {
        delay(500)
        val newNoteId = (dummyReceivingNoteDetails.maxOfOrNull { it.noteId } ?: 0) + 1
        val newLines = request.lines.mapIndexed { index, line ->
            val product = dummyProducts.find { it.id == line.productId } ?: Product(id = line.productId, lot = "LOT-New", code = "P-New", name = "새 상품")
            ReceivingLine(
                lineId = newNoteId * 10 + index,
                product = product,
                orderedQty = line.orderedQty,
                inspectedQty = 0,
                status = "PENDING",
                lineRemark = line.lineRemark
            )
        }
        val newDetail = ReceivingNoteDetail(
            noteId = newNoteId,
            supplierName = request.supplierName,
            itemKindsNumber = request.lines.size,
            totalQty = request.lines.sumOf { it.orderedQty },
            status = "IN_PROGRESS",
            completedAt = null,
            receivingNo = request.receivingNo ?: "RN-DUMMY-$newNoteId",
            warehouseCode = request.warehouseCode,
            requestedAt = request.requestedAt,
            expectedReceiveDate = request.expectedReceiveDate ?: "",
            receivedAt = null,
            inspectorName = null,
            inspectorDept = null,
            inspectorPhone = null,
            remark = request.remark,
            lines = newLines
        )
        
        dummyReceivingNoteDetails.add(0, newDetail)
        
        val newNote = ReceivingNote(
            noteId = newDetail.noteId,
            receivingNo = newDetail.receivingNo,
            supplierName = newDetail.supplierName,
            itemKindsNumber = newDetail.itemKindsNumber,
            totalQty = newDetail.totalQty,
            status = newDetail.status,
            warehouseCode = newDetail.warehouseCode,
            requestedAt = newDetail.requestedAt,
            expectedReceiveDate = newDetail.expectedReceiveDate,
            completedAt = newDetail.completedAt
        )
        dummyReceivingNotes.add(0, newNote)

        return com.ljs.and.data.model.ApiResponse(200, true, "Success", newDetail)
    }

    override suspend fun completeReceiving(noteId: Long, inspectorInfo: InspectorInfo): com.ljs.and.data.model.ApiResponse<ReceivingCompletion> {
        delay(500)
        val noteIndex = dummyReceivingNoteDetails.indexOfFirst { it.noteId == noteId }
        
        if (noteIndex != -1) {
            val oldNote = dummyReceivingNoteDetails[noteIndex]
            val completedNote = oldNote.copy(
                status = "COMPLETED", // UI 코드와 상태 문자열 일치
                completedAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                inspectorName = inspectorInfo.inspectorName,
                inspectorDept = inspectorInfo.inspectorDept,
                inspectorPhone = inspectorInfo.inspectorPhone
            )
            dummyReceivingNoteDetails[noteIndex] = completedNote

            val summaryNoteIndex = dummyReceivingNotes.indexOfFirst { it.noteId == noteId }
            if (summaryNoteIndex != -1) {
                val oldSummary = dummyReceivingNotes[summaryNoteIndex]
                dummyReceivingNotes[summaryNoteIndex] = oldSummary.copy(
                    status = "COMPLETED",
                    completedAt = completedNote.completedAt
                )
            }
            
            return com.ljs.and.data.model.ApiResponse(
                status = 200, 
                success = true, 
                message = "Success", 
                data = ReceivingCompletion(
                    completedAt = completedNote.completedAt!!,
                    appliedQtyTotal = completedNote.lines.sumOf { it.inspectedQty }
                )
            )
        } else {
            return com.ljs.and.data.model.ApiResponse(404, false, "Note not found", null)
        }
    }

    override suspend fun updateReceivingLine(noteId: Long, lineId: Long, request: UpdateReceivingLineRequest): com.ljs.and.data.model.ApiResponse<ReceivingNoteDetail> {
        delay(300)
        val noteIndex = dummyReceivingNoteDetails.indexOfFirst { it.noteId == noteId }

        if (noteIndex != -1) {
            val oldNote = dummyReceivingNoteDetails[noteIndex]
            
            val updatedLines = oldNote.lines.map { line ->
                if (line.lineId == lineId) {
                    // UI 코드와 상태 문자열 일치
                    line.copy(inspectedQty = request.inspectedQty, status = if (request.rejected) "REJECTED" else "ACCEPTED")
                } else {
                    line
                }
            }
            
            val updatedNote = oldNote.copy(lines = updatedLines)
            dummyReceivingNoteDetails[noteIndex] = updatedNote
            
            return com.ljs.and.data.model.ApiResponse(200, true, "Success", updatedNote)
        } else {
            return com.ljs.and.data.model.ApiResponse(404, false, "Note not found", null)
        }
    }

    override suspend fun getReceivingNoteDetail(noteId: Long): com.ljs.and.data.model.ApiResponse<ReceivingNoteDetail> {
        delay(200)
        val noteDetail = dummyReceivingNoteDetails.find { it.noteId == noteId }
        return if (noteDetail != null) {
            com.ljs.and.data.model.ApiResponse(200, true, "Success", noteDetail)
        } else {
            com.ljs.and.data.model.ApiResponse(404, false, "Not Found", null)
        }
    }

    private suspend fun getPaginatedNotes(
        notes: List<ReceivingNote>,
        page: Int,
        size: Int
    ): com.ljs.and.data.model.ApiResponse<PagedReceivingNotes> {
        delay(500)
        val paginated = notes.drop(page * size).take(size)
        return com.ljs.and.data.model.ApiResponse(
            status = 200,
            success = true,
            message = "Success",
            data = PagedReceivingNotes(
                items = paginated,
                page = page,
                size = size,
                total = notes.size.toLong()
            )
        )
    }

    override suspend fun getReceivingNotes(q: String?, status: String, date: String?, dateFrom: String?, dateTo: String?, warehouseCode: String?, page: Int, size: Int): com.ljs.and.data.model.ApiResponse<PagedReceivingNotes> {
        val filteredByStatus = dummyReceivingNotes.filter { it.status.equals(status, ignoreCase = true) }
        val filteredByQuery = if (q.isNullOrBlank()) {
            filteredByStatus
        } else {
            filteredByStatus.filter { note ->
                dummyReceivingNoteDetails.find { it.noteId == note.noteId }?.lines?.any { line ->
                    line.product.lot.contains(q, ignoreCase = true)
                } ?: false
            }
        }
        return getPaginatedNotes(filteredByQuery, page, size)
    }

    override suspend fun getNotDoneReceivingNotes(date: String?, dateFrom: String?, dateTo: String?, warehouseCode: String?, page: Int, size: Int): com.ljs.and.data.model.ApiResponse<PagedReceivingNotes> {
        val filtered = dummyReceivingNotes.filter { it.status != "COMPLETED" }
        return getPaginatedNotes(filtered, page, size)
    }

    override suspend fun getDoneReceivingNotes(date: String?, dateFrom: String?, dateTo: String?, warehouseCode: String?, page: Int, size: Int): com.ljs.and.data.model.ApiResponse<PagedReceivingNotes> {
        val filtered = dummyReceivingNotes.filter { it.status == "COMPLETED" }
        return getPaginatedNotes(filtered, page, size)
    }
}
