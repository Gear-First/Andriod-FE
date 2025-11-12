package com.ljs.and.data.model

data class SearchItem(
    val type: String, // "입고" or "출고"
    val no: String,
    val partnerName: String?,
    val warehouseCode: String,
    val status: String,
    val requestedAt: String
)
