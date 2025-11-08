package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

// 입고 요청 생성 Request Body
data class CreateReceivingRequest(
    @SerializedName("supplierName")
    val supplierName: String,
    @SerializedName("warehouseCode")
    val warehouseCode: String,
    @SerializedName("receivingNo")
    val receivingNo: String?,
    @SerializedName("requestedAt")
    val requestedAt: String,
    @SerializedName("expectedReceiveDate")
    val expectedReceiveDate: String?,
    @SerializedName("remark")
    val remark: String?,
    @SerializedName("lines")
    val lines: List<CreateReceivingLine>
)

data class CreateReceivingLine(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("orderedQty")
    val orderedQty: Int,
    @SerializedName("lineRemark")
    val lineRemark: String?
)

// 입고 항목 업데이트 Request Body
data class UpdateReceivingLineRequest(
    @SerializedName("inspectedQty")
    val inspectedQty: Int,
    @SerializedName("rejected")
    val rejected: Boolean
)

// 입고 완료 응답
data class ReceivingCompletion(
    @SerializedName("completedAt")
    val completedAt: String,
    @SerializedName("appliedQtyTotal")
    val appliedQtyTotal: Int
)

// 입고 통합 리스트 조회 응답
data class PagedReceivingNotes(
    @SerializedName("items")
    val items: List<ReceivingNote>,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("total")
    val total: Long
)

// 입고 리스트 아이템
data class ReceivingNote(
    @SerializedName("noteId")
    val noteId: Long,
    @SerializedName("receivingNo")
    val receivingNo: String,
    @SerializedName("supplierName")
    val supplierName: String,
    @SerializedName("itemKindsNumber")
    val itemKindsNumber: Int,
    @SerializedName("totalQty")
    val totalQty: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("warehouseCode")
    val warehouseCode: String,
    @SerializedName("requestedAt")
    val requestedAt: String,
    @SerializedName("expectedReceiveDate")
    val expectedReceiveDate: String,
    @SerializedName("completedAt")
    val completedAt: String?
)

// 입고 내역서 상세
data class ReceivingNoteDetail(
    @SerializedName("noteId")
    val noteId: Long,
    @SerializedName("supplierName")
    val supplierName: String,
    @SerializedName("itemKindsNumber")
    val itemKindsNumber: Int,
    @SerializedName("totalQty")
    val totalQty: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("completedAt")
    val completedAt: String?,
    @SerializedName("receivingNo")
    val receivingNo: String,
    @SerializedName("warehouseCode")
    val warehouseCode: String,
    @SerializedName("requestedAt")
    val requestedAt: String,
    @SerializedName("expectedReceiveDate")
    val expectedReceiveDate: String,
    @SerializedName("receivedAt")
    val receivedAt: String?,
    @SerializedName("inspectorName")
    val inspectorName: String?,
    @SerializedName("inspectorDept")
    val inspectorDept: String?,
    @SerializedName("inspectorPhone")
    val inspectorPhone: String?,
    @SerializedName("remark")
    val remark: String?,
    @SerializedName("lines")
    val lines: List<ReceivingLine>
)

// 입고 상세 항목
data class ReceivingLine(
    @SerializedName("lineId")
    val lineId: Long,
    @SerializedName("product")
    val product: Product,
    @SerializedName("orderedQty")
    val orderedQty: Int,
    @SerializedName("inspectedQty")
    val inspectedQty: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("lineRemark")
    val lineRemark: String?
)
