package com.ljs.and.data.model

import com.google.gson.annotations.SerializedName

data class InventoryOnHandResponse(
    @SerializedName("items")
    val items: List<InventoryOnHandItem>,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("total")
    val total: Long
)

data class InventoryOnHandItem(
    @SerializedName("warehouseCode")
    val warehouseCode: String,
    @SerializedName("part")
    val part: Part,
    @SerializedName("onHandQty")
    val onHandQty: Int,
    @SerializedName("lastUpdatedAt")
    val lastUpdatedAt: String,
    @SerializedName("lowStock")
    val lowStock: Boolean,
    @SerializedName("safetyStockQty")
    val safetyStockQty: Int
)

data class Part(
    @SerializedName("id")
    val id: Long,
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String
)

data class PurchaseOrderRequest(
    @SerializedName("vehicleNumber")
    val vehicleNumber: String?,
    @SerializedName("vehicleModel")
    val vehicleModel: String?,
    @SerializedName("requesterId")
    val requesterId: Long,
    @SerializedName("requesterName")
    val requesterName: String,
    @SerializedName("requesterRole")
    val requesterRole: String,
    @SerializedName("requesterCode")
    val requesterCode: String,
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
    val price: Int?,
    @SerializedName("quantity")
    val quantity: Int
)

data class PurchaseOrderResponse(
    @SerializedName("orderId")
    val orderId: Long,
    @SerializedName("orderNumber")
    val orderNumber: String,
    @SerializedName("totalQuantity")
    val totalQuantity: Int,
    @SerializedName("orderStatus")
    val orderStatus: String,
    @SerializedName("items")
    val items: List<PurchaseOrderItemResponse>
)

data class PurchaseOrderItemResponse(
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
    val totalPrice: Int
)

data class BranchPurchaseOrderResponse(
    @SerializedName("content")
    val content: List<BranchPurchaseOrderItem>,
    @SerializedName("pageNumber")
    val pageNumber: Int,
    @SerializedName("pageSize")
    val pageSize: Int,
    @SerializedName("totalElements")
    val totalElements: Long?,
    @SerializedName("totalPages")
    val totalPages: Int?,
    @SerializedName("last")
    val last: Boolean,
    @SerializedName("sort")
    val sort: List<String>
)

data class BranchPurchaseOrderItem(
    @SerializedName("orderId")
    val orderId: Long,
    @SerializedName("orderNumber")
    val orderNumber: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("totalPrice")
    val totalPrice: Int,
    @SerializedName("requestDate")
    val requestDate: String,
    @SerializedName("processedDate")
    val processedDate: String?,
    @SerializedName("transferDate")
    val transferDate: String?,
    @SerializedName("completedDate")
    val completedDate: String?,
    @SerializedName("items")
    val items: List<PurchaseOrderItemResponse>
)
