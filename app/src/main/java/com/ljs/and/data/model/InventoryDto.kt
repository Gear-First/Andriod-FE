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
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("lowStock")
    val lowStock: Boolean,
    @SerializedName("safetyStockQty")
    val safetyStockQty: Int,
    @SerializedName("supplierName")
    val supplierName: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("priceTotal")
    val priceTotal: Long
)

data class Part(
    @SerializedName("id")
    val id: Long,
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String
)
