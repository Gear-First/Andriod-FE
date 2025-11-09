package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

// 발주 요청 Body
data class PurchaseOrderRequest(
    @SerializedName("vehicleNumber")
    val vehicleNumber: String?,
    @SerializedName("vehicleModel")
    val vehicleModel: String?,
    @SerializedName("receiptNum")
    val receiptNum: String,
    @SerializedName("items")
    val items: List<PurchaseOrderItemRequest>
)

data class PurchaseOrderItemRequest(
    @SerializedName("partId")
    val partId: Long,
    @SerializedName("partName")
    val partName: String,
    @SerializedName("partCode")
    val partCode: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("quantity")
    val quantity: Int
)

// 발주 요청 응답
data class PurchaseOrderResponse(
    @SerializedName("orderId")
    val orderId: Long,
    @SerializedName("orderNumber")
    val orderNumber: String,
    @SerializedName("totalQuantity")
    val totalQuantity: Int,
    @SerializedName("orderStatus")
    val orderStatus: String
)

// 발주 내역 조회 응답
data class PurchaseOrder(
    @SerializedName("orderId")
    val orderId: Long,
    @SerializedName("orderNumber")
    val orderNumber: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("totalPrice")
    val totalPrice: Long,
    @SerializedName("requestDate")
    val requestDate: String,
    @SerializedName("items")
    val items: List<PurchaseOrderItem>
)

data class PurchaseOrderItem(
    @SerializedName("id")
    val id: Long,
    @SerializedName("partName")
    val partName: String,
    @SerializedName("partCode")
    val partCode: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("totalPrice")
    val totalPrice: Long
)
