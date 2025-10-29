package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

// GET /api/v1/receiving/not-done, /api/v1/receiving/done 의 응답 페이지 구조
data class ReceivingNotePage(
    @SerializedName("items")
    val items: List<ReceivingNote>,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("total")
    val total: Long
)

// 입고 목록의 개별 아이템
data class ReceivingNote(
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
    val completedAt: String?
)

// GET /api/v1/receiving/{noteId} 의 응답 상세 구조
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
    val receivingNo: String?,
    @SerializedName("warehouseId")
    val warehouseId: Long?,
    @SerializedName("requestedAt")
    val requestedAt: String?,
    @SerializedName("expectedReceiveDate")
    val expectedReceiveDate: String?,
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

// 입고 상세의 개별 품목 라인
data class ReceivingLine(
    @SerializedName("lineId")
    val lineId: Long,
    @SerializedName("product")
    val product: Product,
    @SerializedName("orderedQty")
    val orderedQty: Int,
    @SerializedName("inspectedQty")
    val inspectedQty: Int,
    @SerializedName("issueQty")
    val issueQty: Int,
    @SerializedName("status")
    val status: String
)

// PATCH /api/v1/receiving/{noteId}/lines/{lineId} 의 요청 본문
data class UpdateInspectionRequest(
    @SerializedName("inspectedQty")
    val inspectedQty: Int,
    @SerializedName("hasIssue")
    val hasIssue: Boolean
)
