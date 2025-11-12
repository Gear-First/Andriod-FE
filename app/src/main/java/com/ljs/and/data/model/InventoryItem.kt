package com.ljs.and.data.model

data class InventoryItem(
    val warehouseCode: String,
    val part: Part,
    val onHandQty: Int,
    val supplierName: String?,
    val safetyStockQty: Int
)
