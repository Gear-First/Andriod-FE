package com.ljs.and.ui.inventory

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

// --- Data Classes (moved from data package) ---
enum class ItemStatus(val displayName: String) {
    COMPLETED("완료"),
//    DEFECTIVE("불량"),
    NORMAL("정상"),
    LOW_STOCK("부족"),
    MISSING("누락"),
//    SOLD_OUT("소진")
}

data class InventoryItem(
    val id: Int,
    val supplier: String,           // 공급 업체
    val name: String,               // 부품명
    val code: String,               // 부품 코드
    val location: String,           // 위치
    val currentStock: Int,          // 현재고
    val minimumStock: Int,          // 최소재고
    val lastTransactionDate: String,// 최근 입출고일
    val imageUrl: String? = null,   // 품목 이미지 URL
    val status: ItemStatus,         // 상태
    val manager: String? = null     // 담당자
)

enum class RequestStatus(val displayName: String) {
    PENDING("대기"),
    APPROVED("승인"),
//    COMPLETED("완료")
}

data class InventoryRequest(
    val id: Int,
    val itemName: String,
    val itemCode: String,
    val quantity: Int,
    val requestDate: String,
    val requester: String,
    val reason: String,
    val status: RequestStatus,
    val isCanceled: Boolean = false
)

// --- State Data Classes ---
data class InventoryState(
    val inventoryList: List<InventoryItem> = emptyList(),
    val selectedFilter: String = "전체",
    val totalItems: Int = 0,
    val totalQuantity: Long = 0,
    val lackingItems: Int = 0,
    val defectiveItems: Int = 0
)

data class RequestState(
    val requestList: List<InventoryRequest> = emptyList(),
    val selectedFilter: String = "전체"
)


class InventoryViewModel : ViewModel() {

    // --- Inventory State Management ---
    private val _inventoryState = MutableStateFlow(InventoryState())
    val inventoryState: StateFlow<InventoryState> = _inventoryState.asStateFlow()

    // --- Request State Management ---
    private val _requestState = MutableStateFlow(RequestState())
    val requestState: StateFlow<RequestState> = _requestState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _inventoryState.value = InventoryState(
            inventoryList = dummyInventoryList,
            totalItems = dummyInventoryList.size,
            totalQuantity = dummyInventoryList.sumOf { it.currentStock.toLong() },
            lackingItems = dummyInventoryList.count { it.status == ItemStatus.LOW_STOCK },
//                    || it.status == ItemStatus.SOLD_OUT },
//            defectiveItems = dummyInventoryList.count { it.status == ItemStatus.DEFECTIVE }
        )

        _requestState.value = RequestState(
            requestList = dummyRequestList
        )
    }

    // --- Inventory Functions ---
    fun updateInventoryFilter(filter: String) {
        _inventoryState.update { it.copy(selectedFilter = filter) }
    }

    // --- Request Functions ---
    fun updateRequestFilter(filter: String) {
        _requestState.update { it.copy(selectedFilter = filter) }
    }

    fun addInventoryRequest(
        partName: String, partCode: String, quantity: Int, reason: String, requester: String
    ) {
        val newId = (_requestState.value.requestList.maxOfOrNull { it.id } ?: 0) + 1
        val newRequest = InventoryRequest(
            id = newId,
            itemName = partName,
            itemCode = partCode,
            quantity = quantity,
            requestDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date()),
            requester = requester,
            reason = reason,
            status = RequestStatus.PENDING
        )

        _requestState.update {
            it.copy(requestList = it.requestList + newRequest)
        }
    }

    fun cancelRequest(requestId: Int) {
        _requestState.update { state ->
            val updatedList = state.requestList.map {
                if (it.id == requestId) it.copy(isCanceled = true) else it
            }
            state.copy(requestList = updatedList)
        }
    }
}

// --- Dummy Data ---
private val dummyInventoryList = listOf(
    InventoryItem(1, "현대 모비스", "엔진 오일 필터", "EOF-001", "A-01-1", 5, 10, "2024-07-25", "https://picsum.photos/200", ItemStatus.LOW_STOCK, "김담당"),
    InventoryItem(2, "현대 오토에버", "타이어", "T-001", "B-01-1", 20, 15, "2024-07-24", "https://picsum.photos/201", ItemStatus.NORMAL, "김철수"),
    InventoryItem(3, "기아", "브레이크 패드", "BP-002", "C-02-3", 50, 20, "2024-07-23", "https://picsum.photos/202", ItemStatus.NORMAL, "박영희"),
    InventoryItem(4, "GM", "헤드라이트", "HL-003", "D-04-5", 2, 5, "2024-07-22", "https://picsum.photos/203", ItemStatus.LOW_STOCK, "이민준"),
//    InventoryItem(5, "쌍용", "와이퍼 블레이드", "WB-004", "E-01-1", 0, 10, "2024-07-21", "https://picsum.photos/204", ItemStatus.SOLD_OUT, "최지아"),
    InventoryItem(6, "르노삼성", "배터리", "B-005", "F-03-2", 8, 5, "2024-07-20", "https://picsum.photos/205", ItemStatus.NORMAL, "정성호"),
    InventoryItem(7, "벤츠", "에어컨 필터", "AF-006", "G-02-4", 1, 2, "2024-07-19", "https://picsum.photos/206", ItemStatus.LOW_STOCK, "오지현"),
//    InventoryItem(8, "BMW", "엔진", "E-007", "H-01-1", 1, 1, "2024-07-18", null, ItemStatus.DEFECTIVE, "윤서준")
)

private val dummyRequestList = listOf(
    InventoryRequest(1, "엔진 오일 필터", "EOF-001", 20, "2025.10.27", "김신청", "부족", RequestStatus.PENDING),
    InventoryRequest(2, "브레이크 패드", "BP-002", 10, "2025.10.26", "박담당", "부족", RequestStatus.APPROVED),
//    InventoryRequest(3, "타이어", "T-001", 4, "2025.10.25", "최팀장", "부족", RequestStatus.COMPLETED),
    InventoryRequest(4, "와이퍼 블레이드", "WB-004", 30, "2025.10.24", "김신청", "부족", RequestStatus.PENDING)
)
