package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

// POST /api/v1/shipping 요청 본문
data class CreateShippingRequest(
    @SerializedName("customerName")
    val customerName: String = "",
    @SerializedName("warehouseId")
    val warehouseId: Long = 0,
    @SerializedName("shippingNo")
    val shippingNo: String = "",
    @SerializedName("requestedAt")
    val requestedAt: String = "",
    @SerializedName("expectedShipDate")
    val expectedShipDate: String = "",
    @SerializedName("remark")
    val remark: String = "",
    @SerializedName("lines")
    val lines: List<CreateShippingRequestLine> = emptyList()
)

data class CreateShippingRequestLine(
    @SerializedName("productId")
    val productId: Long = 0,
    @SerializedName("orderedQty")
    val orderedQty: Int = 0,
    @SerializedName("lineRemark")
    val lineRemark: String = ""
)

// GET /api/v1/shipping/not-done, /api/v1/shipping/done 응답 본문 (data 필드)
data class PagedShippingNotes(
    @SerializedName("items")
    val items: List<ShippingNote> = emptyList(),
    @SerializedName("page")
    val page: Int = 0,
    @SerializedName("size")
    val size: Int = 0,
    @SerializedName("total")
    val total: Long = 0
)

// GET /api/v1/shipping/not-done, /api/v1/shipping/done 응답 본문 (items 필드)
data class ShippingNote(
    @SerializedName("noteId")
    val noteId: Long = 0,
    @SerializedName("branchName")
    val branchName: String = "",
    @SerializedName("itemKindsNumber")
    val itemKindsNumber: Int = 0,
    @SerializedName("totalQty")
    val totalQty: Int = 0,
    @SerializedName("status")
    val status: String = "",
    @SerializedName("completedAt")
    val completedAt: String? = null
)

// GET /api/v1/shipping/{noteId}, POST /api/v1/shipping, PATCH /api/v1/shipping/{noteId}/lines/{lineId} 응답 본문 (data 필드)
data class ShippingNoteDetail(
    @SerializedName("noteId")
    val noteId: Long = 0,
    @SerializedName("branchName")
    val branchName: String = "",
    @SerializedName("itemKindsNumber")
    val itemKindsNumber: Int = 0,
    @SerializedName("totalQty")
    val totalQty: Int = 0,
    @SerializedName("status")
    val status: String = "",
    @SerializedName("completedAt")
    val completedAt: String? = null,
    @SerializedName("shippingNo")
    val shippingNo: String? = null,
    @SerializedName("warehouseId")
    val warehouseId: Long? = null,
    @SerializedName("requestedAt")
    val requestedAt: String? = null,
    @SerializedName("expectedShipDate")
    val expectedShipDate: String? = null,
    @SerializedName("shippedAt")
    val shippedAt: String? = null,
    @SerializedName("assigneeName")
    val assigneeName: String? = null,
    @SerializedName("assigneeDept")
    val assigneeDept: String? = null,
    @SerializedName("assigneePhone")
    val assigneePhone: String? = null,
    @SerializedName("remark")
    val remark: String? = null,
    @SerializedName("lines")
    val lines: List<ShippingLine> = emptyList()
)

data class ShippingLine(
    @SerializedName("lineId")
    val lineId: Long = 0,
    @SerializedName("product")
    val product: Product = Product(),
    @SerializedName("orderedQty")
    val orderedQty: Int = 0,
    @SerializedName("allocatedQty")
    val allocatedQty: Int = 0,
    @SerializedName("pickedQty")
    val pickedQty: Int = 0,
    @SerializedName("status")
    val status: String = ""
)

// PATCH /api/v1/shipping/{noteId}/lines/{lineId} 요청 본문
data class UpdateShippingLineRequest(
    @SerializedName("allocatedQty")
    val allocatedQty: Int,
    @SerializedName("pickedQty")
    val pickedQty: Int
)
