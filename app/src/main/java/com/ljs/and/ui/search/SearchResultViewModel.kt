package com.ljs.and.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data classes moved from SearchResultScreen.kt
sealed class SearchResult {
    data class Receiving(val item: ReceivingSearchResultItem) : SearchResult()
    data class Releasing(val item: ReleasingSearchResultItem) : SearchResult()
    data class Inventory(val item: InventorySearchResultItem) : SearchResult()
    data class Pending(val item: PendingItem) : SearchResult()
}

enum class ItemStatus(val displayName: String) {
    COMPLETED("완료"),
    NORMAL("정상"),
    LOW_STOCK("부족"),
    DEFECTIVE("불량")
}

// Updated to match InspectionItemCard structure from ReceivingInspectionScreen.kt
data class ReceivingSearchResultItem(
    val lineId: Long,
    val supplierName: String,
    val productName: String,
    val productLot: String,
    val receivingNo: String,
    val orderedQty: Int,
    val inspectedQty: Int,
    val imgUrl: String?,
    val status: String
)

// Updated to match PickingItemCard structure from ReleasingPickingScreen.kt
data class ReleasingSearchResultItem(
    val lineId: Long,
    val customerName: String,
    val productName: String,
    val productLot: String,
    val shippingNo: String,
    val allocatedQty: Int,
    val pickedQty: Int,
    val imgUrl: String?,
    val status: String
)

data class InventorySearchResultItem(
    val id: Int,
    val supplier: String,
    val name: String,
    val code: String,
    val location: String,
    val currentStock: Int,
    val minimumStock: Int,
    val lastTransactionDate: String,
    val imageUrl: String? = null,
    val status: ItemStatus,
    val manager: String? = null
)

data class PendingItem(
    val type: String, // "입고" or "출고"
    val number: String,
    val company: String,
    val partName: String,
    val location: String,
    val quantity: Int,
    val manager: String,
    val imageUrl: String,
    val status: String // "검수 중" or "피킹 중"
)


class SearchResultViewModel : ViewModel() {

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    // Dummy Data
    private val dummyReceivingResults = listOf(
        ReceivingSearchResultItem(1L, "현대 글로비스", "엔진오일", "LOT123", "IN-20240728-001", 10, 5, "https://picsum.photos/200", "INSPECTING"),
        ReceivingSearchResultItem(2L, "보쉬", "브레이크 패드", "LOT456", "IN-20240728-002", 20, 20, "https://picsum.photos/201", "ACCEPTED")
    )

    private val dummyReleasingResults = listOf(
        ReleasingSearchResultItem(1L, "현대 모비스", "엔진오일", "LOT123", "OUT-20240728-001", 15, 5, "https://picsum.photos/202", "PICKING"),
        ReleasingSearchResultItem(2L, "현대 오토에버", "브레이크 패드", "LOT456", "OUT-20240728-002", 10, 10, "https://picsum.photos/203", "COMPLETED")
    )

    private val dummyInventoryResults = listOf(
        InventorySearchResultItem(1, "현대 모비스", "엔진 오일 필터", "EOF-001", "A-01-1", 5, 10, "2024-07-25", "https://picsum.photos/200", ItemStatus.LOW_STOCK, "김담당"),
        InventorySearchResultItem(2, "보쉬", "브레이크 패드", "BP-002", "B-01-1", 50, 20, "2024-07-23", "https://picsum.photos/202", ItemStatus.NORMAL, "박영희"),
        InventorySearchResultItem(4, "한국타이어", "타이어", "T-001", "C-07-5", 2, 5, "2024-07-22", "https://picsum.photos/203", ItemStatus.LOW_STOCK, "이민준"),
    )

    private val dummyPendingResults = listOf(
        PendingItem("입고", "IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "https://i.namu.wiki/i/22z_qpCg1pxtx5-2a2p3rf_YnS9vyN03pt580e0Jc5n3rSg2g2TfNT9c2mfp3aLp4z-mAs2T9oPMwT3QYDYi6A.webp", "검수 중"),
        PendingItem("출고", "OUT - EFGH", "현대 오토에버", "엔진 필터", "B-01-1", 1, "김유신", "https://i.namu.wiki/i/22z_qpCg1pxtx5-2a2p3rf_YnS9vyN03pt580e0Jc5n3rSg2g2TfNT9c2mfp3aLp4z-mAs2T9oPMwT3QYDYi6A.webp", "피킹 중")
    )

    fun search(flowType: String, query: String) {
        viewModelScope.launch {
            val results = when (flowType) {
                "receiving" -> dummyReceivingResults
                    .filter { query.isBlank() || it.supplierName.contains(query, ignoreCase = true) || it.productName.contains(query, ignoreCase = true) || it.receivingNo.contains(query, ignoreCase = true) }
                    .map { SearchResult.Receiving(it) }
                "releasing" -> dummyReleasingResults
                    .filter { query.isBlank() || it.customerName.contains(query, ignoreCase = true) || it.productName.contains(query, ignoreCase = true) || it.shippingNo.contains(query, ignoreCase = true) }
                    .map { SearchResult.Releasing(it) }
                "inventory" -> dummyInventoryResults
                    .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.supplier.contains(query, ignoreCase = true) || it.code.contains(query, ignoreCase = true) }
                    .map { SearchResult.Inventory(it) }
                "pending" -> dummyPendingResults
                    .filter { query.isBlank() || it.partName.contains(query, ignoreCase = true) || it.company.contains(query, ignoreCase = true) }
                    .map { SearchResult.Pending(it) }
                else -> emptyList()
            }
            _searchResults.value = results
        }
    }
}