package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

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
