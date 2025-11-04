package com.ljs.and.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

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
    val defectiveItems: Int = 0,
    val isLoading: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

data class RequestState(
    val requestList: List<InventoryRequest> = emptyList(),
    val selectedFilter: String = "전체"
)


class InventoryViewModel : ViewModel() {

    private val _inventoryState = MutableStateFlow(InventoryState())
    val inventoryState: StateFlow<InventoryState> = _inventoryState.asStateFlow()

    private val _requestState = MutableStateFlow(RequestState())
    val requestState: StateFlow<RequestState> = _requestState.asStateFlow()

    private val pageSize = 10
    private var fullFilteredList: List<InventoryItem> = emptyList()


    init {
        updateInventoryFilter("전체") // Load initial data with default filter
        _requestState.value = RequestState(requestList = dummyRequestList)
    }

    private fun loadPageData() {
        viewModelScope.launch {
            _inventoryState.update { it.copy(isLoading = true) }
            delay(300) // Simulate short delay for UI feedback

            val page = _inventoryState.value.currentPage
            val startIndex = (page - 1) * pageSize
            val endIndex = (startIndex + pageSize).coerceAtMost(fullFilteredList.size)
            val pageItems = if (startIndex < fullFilteredList.size) fullFilteredList.subList(startIndex, endIndex) else emptyList()

            _inventoryState.update {
                it.copy(
                    isLoading = false,
                    inventoryList = pageItems
                )
            }
        }
    }

    fun goToPage(page: Int) {
        val totalPages = _inventoryState.value.totalPages
        if (page in 1..totalPages && page != _inventoryState.value.currentPage) {
            _inventoryState.update { it.copy(currentPage = page) }
            loadPageData()
        }
    }

    fun goToNextPage() {
        if (_inventoryState.value.currentPage < _inventoryState.value.totalPages) {
            val nextPage = _inventoryState.value.currentPage + 1
            goToPage(nextPage)
        }
    }

    fun goToPreviousPage() {
        if (_inventoryState.value.currentPage > 1) {
            val prevPage = _inventoryState.value.currentPage - 1
            goToPage(prevPage)
        }
    }

    fun updateInventoryFilter(filter: String) {
        fullFilteredList = if (filter == "전체") {
            fullDummyInventoryList
        } else {
            fullDummyInventoryList.filter { item ->
                when (filter) {
                    "정상" -> item.status == ItemStatus.NORMAL
                    "부족" -> item.status == ItemStatus.LOW_STOCK
                    else -> true
                }
            }
        }

        val totalPages = ceil(fullFilteredList.size.toDouble() / pageSize).toInt()

        _inventoryState.update {
            it.copy(
                selectedFilter = filter,
                currentPage = 1,
                totalPages = if (totalPages > 0) totalPages else 1,
                totalItems = fullDummyInventoryList.size,
                totalQuantity = fullDummyInventoryList.sumOf { item -> item.currentStock.toLong() },
                lackingItems = fullDummyInventoryList.count { item -> item.status == ItemStatus.LOW_STOCK }
            )
        }
        loadPageData()
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
private val fullDummyInventoryList = listOf(
    InventoryItem(1, "현대 모비스", "엔진 오일 필터", "EOF-001", "A-01-1", 5, 10, "2024-07-25", "https://picsum.photos/200", ItemStatus.LOW_STOCK, "김담당"),
    InventoryItem(2, "현대 오토에버", "타이어", "T-001", "B-01-1", 20, 15, "2024-07-24", "https://picsum.photos/201", ItemStatus.NORMAL, "김철수"),
    InventoryItem(3, "기아", "브레이크 패드", "BP-002", "C-02-3", 50, 20, "2024-07-23", "https://picsum.photos/202", ItemStatus.NORMAL, "박영희"),
    InventoryItem(4, "GM", "헤드라이트", "HL-003", "D-04-5", 2, 5, "2024-07-22", "https://picsum.photos/203", ItemStatus.LOW_STOCK, "이민준"),
    InventoryItem(5, "르노삼성", "배터리", "B-005", "F-03-2", 8, 5, "2024-07-20", "https://picsum.photos/205", ItemStatus.NORMAL, "정성호"),
    InventoryItem(6, "벤츠", "에어컨 필터", "AF-006", "G-02-4", 1, 2, "2024-07-19", "https://picsum.photos/206", ItemStatus.LOW_STOCK, "오지현"),
    InventoryItem(7, "BMW", "엔진", "E-007", "H-01-1", 15, 10, "2024-07-18", null, ItemStatus.NORMAL, "윤서준"),
    InventoryItem(8, "Audi", "스파크 플러그", "SP-008", "A-02-1", 30, 20, "2024-07-17", "https://picsum.photos/207", ItemStatus.NORMAL, "강지민"),
    InventoryItem(9, "Volvo", "라디에이터", "R-009", "B-03-2", 3, 5, "2024-07-16", "https://picsum.photos/208", ItemStatus.LOW_STOCK, "송예은"),
    InventoryItem(10, "Toyota", "알터네이터", "A-010", "C-04-3", 12, 10, "2024-07-15", "https://picsum.photos/209", ItemStatus.NORMAL, "임도현"),
    InventoryItem(11, "현대 모비스", "미션 오일", "MO-011", "D-01-4", 25, 20, "2024-07-14", "https://picsum.photos/210", ItemStatus.NORMAL, "김담당"),
    InventoryItem(12, "현대 오토에버", "점화 코일", "IC-012", "E-02-5", 8, 10, "2024-07-13", "https://picsum.photos/211", ItemStatus.LOW_STOCK, "김철수"),
    InventoryItem(13, "기아", "산소 센서", "OS-013", "F-03-1", 18, 15, "2024-07-12", "https://picsum.photos/212", ItemStatus.NORMAL, "박영희"),
    InventoryItem(14, "GM", "연료 펌프", "FP-014", "G-04-2", 4, 5, "2024-07-11", "https://picsum.photos/213", ItemStatus.LOW_STOCK, "이민준"),
    InventoryItem(15, "르노삼성", "서스펜션 스트럿", "SS-015", "H-01-3", 22, 10, "2024-07-10", "https://picsum.photos/214", ItemStatus.NORMAL, "정성호"),
    InventoryItem(16, "벤츠", "워터 펌프", "WP-016", "A-02-4", 9, 8, "2024-07-09", "https://picsum.photos/215", ItemStatus.NORMAL, "오지현"),
    InventoryItem(17, "BMW", "컨트롤 암", "CA-017", "B-03-5", 14, 10, "2024-07-08", null, ItemStatus.NORMAL, "윤서준"),
    InventoryItem(18, "Audi", "스로틀 바디", "TB-018", "C-04-1", 6, 5, "2024-07-07", "https://picsum.photos/216", ItemStatus.NORMAL, "강지민"),
    InventoryItem(19, "Volvo", "타이밍 벨트", "TB-019", "D-01-2", 2, 5, "2024-07-06", "https://picsum.photos/217", ItemStatus.LOW_STOCK, "송예은"),
    InventoryItem(20, "Toyota", "휠 베어링", "WB-020", "E-02-3", 35, 20, "2024-07-05", "https://picsum.photos/218", ItemStatus.NORMAL, "임도현")
)


private val dummyRequestList = listOf(
    InventoryRequest(1, "엔진 오일 필터", "EOF-001", 20, "2025.10.27", "김신청", "부족", RequestStatus.PENDING),
    InventoryRequest(2, "브레이크 패드", "BP-002", 10, "2025.10.26", "박담당", "부족", RequestStatus.APPROVED),
//    InventoryRequest(3, "타이어", "T-001", 4, "2025.10.25", "최팀장", "부족", RequestStatus.COMPLETED),
    InventoryRequest(4, "와이퍼 블레이드", "WB-004", 30, "2025.10.24", "김신청", "부족", RequestStatus.PENDING)
)
