package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

// 출고 요청 생성 Request Body
data class CreateShippingRequest(
    @SerializedName("branchName")
    val branchName: String,
    @SerializedName("warehouseCode")
    val warehouseCode: String,
    @SerializedName("shippingNo")
    val shippingNo: String?,
    @SerializedName("requestedAt")
    val requestedAt: String,
    @SerializedName("expectedShipDate")
    val expectedShipDate: String?,
    @SerializedName("remark")
    val remark: String?,
    @SerializedName("lines")
    val lines: List<CreateShippingLine>
)

data class CreateShippingLine(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("orderedQty")
    val orderedQty: Int,
    @SerializedName("lineRemark")
    val lineRemark: String?
)

// 출고 완료 요청 Request Body
data class CompleteShippingRequest(
    @SerializedName("assigneeName")
    val assigneeName: String,
    @SerializedName("assigneeDept")
    val assigneeDept: String,
    @SerializedName("assigneePhone")
    val assigneePhone: String
)

// 출고 항목 업데이트 Request Body
data class UpdateShippingLineRequest(
    @SerializedName("pickedQty")
    val pickedQty: Int,
    @SerializedName("lineRemark")
    val lineRemark: String?
)

// 출고 완료 응답
data class ShippingCompletion(
    @SerializedName("completedAt")
    val completedAt: String,
    @SerializedName("totalShippedQty")
    val totalShippedQty: Int
)

// 출고 통합 리스트 조회 응답
data class PagedShippingNotes(
    @SerializedName("items")
    val items: List<ShippingNote>,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("total")
    val total: Long
)

// 출고 리스트 아이템
data class ShippingNote(
    @SerializedName("noteId")
    val noteId: Long,
    @SerializedName("shippingNo")
    val shippingNo: String,
    @SerializedName("branchName")
    val branchName: String,
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
    @SerializedName("expectedShipDate")
    val expectedShipDate: String?,
    @SerializedName("completedAt")
    val completedAt: String?
)

// 출고 내역서 상세
data class ShippingNoteDetail(
    @SerializedName("noteId")
    val noteId: Long,
    @SerializedName("branchName")
    val branchName: String,
    @SerializedName("itemKindsNumber")
    val itemKindsNumber: Int,
    @SerializedName("totalQty")
    val totalQty: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("completedAt")
    val completedAt: String?,
    @SerializedName("shippingNo")
    val shippingNo: String,
    @SerializedName("warehouseCode")
    val warehouseCode: String,
    @SerializedName("requestedAt")
    val requestedAt: String,
    @SerializedName("expectedShipDate")
    val expectedShipDate: String?,
    @SerializedName("shippedAt")
    val shippedAt: String?,
    @SerializedName("assigneeName")
    val assigneeName: String?,
    @SerializedName("assigneeDept")
    val assigneeDept: String?,
    @SerializedName("assigneePhone")
    val assigneePhone: String?,
    @SerializedName("remark")
    val remark: String?,
    @SerializedName("lines")
    val lines: List<ShippingLine>
)

// 출고 상세 항목
data class ShippingLine(
    @SerializedName("lineId")
    val lineId: Long,
    @SerializedName("product")
    val product: Product,
    @SerializedName("orderedQty")
    val orderedQty: Int,
    @SerializedName("pickedQty")
    val pickedQty: Int,
    @SerializedName("status")
    val status: String
)
