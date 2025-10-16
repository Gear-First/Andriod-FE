package com.ljs.and.data

enum class ItemStatus(val displayName: String) {
    COMPLETED("완료"),
    DEFECTIVE("불량"),
    NORMAL("정상"),
    LOW_STOCK("부족"),
    MISSING("누락"),
    SOLD_OUT("소진")
}

data class InventoryItem(
    val id: Int,
    val receiptNumber: String,      // 입고 번호
    val supplier: String,           // 공급 업체
    val name: String,               // 부품명
    val location: String,           // 위치
    val currentStock: Int,          // 현재고
    val minimumStock: Int,          // 최소재고
    val imageUrl: String? = null,   // 품목 이미지 URL
    val status: ItemStatus,         // 상태
    val manager: String? = null     // 담당자
)
