package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

// 재고 현황 차트용 데이터 클래스 (UI 독립적)
data class TopInventoryItemDto(
    @SerializedName("name") val name: String,
    @SerializedName("quantity") val quantity: Int
)

data class ShippingResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ShippingData?
)

data class ShippingData(
    @SerializedName("items") val items: List<ShippingItem>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total") val total: Long
)

data class ShippingItem(
    @SerializedName("noteId") val noteId: Long,
    @SerializedName("shippingNo") val shippingNo: String,
    @SerializedName("branchName") val branchName: String,
    @SerializedName("itemKindsNumber") val itemKindsNumber: Int,
    @SerializedName("totalQty") val totalQty: Int,
    @SerializedName("status") val status: String,
    @SerializedName("warehouseCode") val warehouseCode: String,
    @SerializedName("requestedAt") val requestedAt: String,
    @SerializedName("expectedShipDate") val expectedShipDate: String?,
    @SerializedName("completedAt") val completedAt: String?
)

data class ReceivingResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ReceivingData?
)

data class ReceivingData(
    @SerializedName("items") val items: List<ReceivingItem>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total") val total: Long
)

data class ReceivingItem(
    @SerializedName("noteId") val noteId: Long,
    @SerializedName("receivingNo") val receivingNo: String,
    @SerializedName("supplierName") val supplierName: String?,
    @SerializedName("itemKindsNumber") val itemKindsNumber: Int,
    @SerializedName("totalQty") val totalQty: Int,
    @SerializedName("status") val status: String,
    @SerializedName("warehouseCode") val warehouseCode: String,
    @SerializedName("requestedAt") val requestedAt: String,
    @SerializedName("expectedReceiveDate") val expectedReceiveDate: String?,
    @SerializedName("completedAt") val completedAt: String?
)

data class NoteCountsResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: NoteCountsData?
)

data class NoteCountsData(
    @SerializedName("date") val date: String,
    @SerializedName("receivingCount") val receivingCount: Int,
    @SerializedName("shippingCount") val shippingCount: Int
)

data class ReceivingNotesResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ReceivingNotesData?
)

data class ReceivingNotesData(
    @SerializedName("items") val items: List<ReceivingItem>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total") val total: Long
)

data class ShippingNotesResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ShippingNotesData?
)

data class ShippingNotesData(
    @SerializedName("items") val items: List<ShippingItem>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total") val total: Long
)

data class InOutData(
    val day: String,
    val inbound: Float,
    val outbound: Float
)
