package com.ljs.and.ui.releasing

data class ReleasingItem(
    val id: String,
    val customer: String,
    val expectedDate: String,
    val completionDate: String? = null,
    val totalQuantity: Int,
    val manager: String,
    val status: String
)

data class PickingItem(
    val id: String,
    val releasingId: String,
    val partName: String,
    val partCode: String,
    val quantity: Int,
    val location: String,
    val isPicked: Boolean = false,
    val imageUrl: Int? = null
)
