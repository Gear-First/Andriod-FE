package com.ljs.and.data

enum class RequestStatus(val displayName: String) {
    PENDING("대기"),
    APPROVED("승인")
}

data class InventoryRequest(
    val id: Int,
    val itemName: String,
    val quantity: Int,
    val requestDate: String,
    val reason: String,
    val status: RequestStatus
)
