package com.ljs.and.ui.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ljs.and.data.model.BranchPurchaseOrderItem
import com.ljs.and.data.model.InventoryOnHandItem
import com.ljs.and.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// --- Data Classes (DTOs) ---

data class InventoryItemData(val name: String, val quantity: Int, val color: Color)
data class InOutData(val day: String, val inbound: Float, val outbound: Float)
data class NotificationItem(val title: String, val content: String, val time: String, val type: NotificationType)
enum class NotificationType {
    NOTICE,
    INBOUND,
    STOCK_ALERT,
    OUTBOUND
}

// --- UI State ---

data class HomeUiState(
    val selectedDate: String = "",
    val isTodaySelected: Boolean = true,
    val isDatePickerVisible: Boolean = false,
    val isNotificationDialogVisible: Boolean = false,
    val visibleChart: ChartType? = null,
    val status: StatusData = StatusData(),
    val inventoryItems: List<InventoryItemData> = emptyList(),
    val weeklyInOutData: List<InOutData> = emptyList(),
    val notifications: List<NotificationItem> = emptyList()
)

data class StatusData(
    val inboundCount: Int = 0,
    val outboundCount: Int = 0,
    val lowStockCount: Int = 0,
    val requestCount: Int = 0
)

enum class ChartType {
    INVENTORY,
    WEEKLY
}


// --- ViewModel ---

class HomeViewModel : ViewModel() {

    private val repository = InventoryRepository

    private val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    private val _uiState = MutableStateFlow(HomeUiState(selectedDate = sdf.format(Date())))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun refreshData() {
        loadStatusData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    inventoryItems = getSampleInventoryData(),
                    weeklyInOutData = getSampleWeeklyData(),
                    notifications = getSampleNotifications()
                )
            }
            loadStatusData()
        }
    }

    private fun loadStatusData() {
        viewModelScope.launch {
            try {
                // Fetch all inventory items for low stock count
                val allInventoryItems = mutableListOf<InventoryOnHandItem>()
                var inventoryPage = 0
                var inventoryTotalPages = 1
                while (inventoryPage < inventoryTotalPages) {
                    val inventoryResponse = repository.getInventoryOnHand(
                        warehouseCode = "서울",
                        partKeyword = null,
                        supplierName = null,
                        minQty = null,
                        maxQty = null,
                        page = inventoryPage,
                        size = 100, // Adjust size as needed
                        sort = null
                    )
                    if (inventoryResponse.success) {
                        inventoryResponse.data?.items?.let { allInventoryItems.addAll(it) }
                        inventoryTotalPages = inventoryResponse.data?.let {
                            if (it.size == 0) 1 else kotlin.math.ceil(it.total.toDouble() / it.size).toInt()
                        } ?: 1
                        inventoryPage++
                    } else {
                        break // Exit loop on failure
                    }
                }

                // Fetch all purchase orders for request count
                val allPurchaseOrderItems = mutableListOf<BranchPurchaseOrderItem>()
                var requestPage = 0
                var requestTotalPages = 1
                while (requestPage < requestTotalPages) {
                    val requestResponse = repository.getBranchPurchaseOrders(
                        branchCode = "seoul",
                        engineerId = 1111,
                        startDate = null,
                        endDate = null,
                        page = requestPage,
                        size = 100
                    )
                    if (requestResponse.success) {
                        requestResponse.data?.content?.let { allPurchaseOrderItems.addAll(it) }
                        requestTotalPages = requestResponse.data?.totalPages ?: 1
                        requestPage++
                    } else {
                        break // Exit loop on failure
                    }
                }

                val lowStockCount = allInventoryItems.count { it.lowStock }
                val requestCount = allPurchaseOrderItems.size

                _uiState.update {
                    it.copy(status = it.status.copy(lowStockCount = lowStockCount, requestCount = requestCount))
                }

            } catch (e: Exception) {
                // Handle error
            }
        }
    }


    private fun updateStatusForDate(dateMillis: Long) {
        val selectedDateStr = sdf.format(Date(dateMillis))
        val todayStr = sdf.format(Date())

        val newStatus = if (selectedDateStr == todayStr) {
            // Restore original counts for today
            StatusData(inboundCount = 12, outboundCount = 8, lowStockCount = _uiState.value.status.lowStockCount, requestCount = _uiState.value.status.requestCount)
        } else {
            // Generate random data for other dates to simulate fetching new data
            StatusData(
                inboundCount = Random.nextInt(0, 21),
                outboundCount = Random.nextInt(0, 16),
                lowStockCount = _uiState.value.status.lowStockCount, // Keep other statuses same
                requestCount = _uiState.value.status.requestCount
            )
        }
        _uiState.update { it.copy(status = newStatus) }
    }


    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ShowDatePicker -> _uiState.update { it.copy(isDatePickerVisible = true) }
            is HomeEvent.HideDatePicker -> _uiState.update { it.copy(isDatePickerVisible = false) }
            is HomeEvent.DateSelected -> {
                val newDate = sdf.format(Date(event.dateMillis))
                val todayDate = sdf.format(Date())
                _uiState.update {
                    it.copy(
                        selectedDate = newDate,
                        isDatePickerVisible = false,
                        isTodaySelected = newDate == todayDate
                    )
                }
                // Update status counts for the selected date
                updateStatusForDate(event.dateMillis)
            }
            is HomeEvent.ShowNotificationDialog -> _uiState.update { it.copy(isNotificationDialogVisible = true) }
            is HomeEvent.HideNotificationDialog -> _uiState.update { it.copy(isNotificationDialogVisible = false) }
            is HomeEvent.ShowChart -> _uiState.update { it.copy(visibleChart = event.chartType) }
            is HomeEvent.HideChart -> _uiState.update { it.copy(visibleChart = null) }
        }
    }

    // --- Sample Data Providers ---

    private fun getSampleInventoryData(): List<InventoryItemData> {
        return listOf(
            InventoryItemData("엔진오일", 17439, Color(0xFF0D47A1)),
            InventoryItemData("브레이크패드", 9478, Color(0xFF1976D2)),
            InventoryItemData("부동액", 18197, Color(0xFF2196F3)),
            InventoryItemData("타이어", 12510, Color(0xFF64B5F6)),
            InventoryItemData("필터", 14406, Color(0xFFBBDEFB))
        )
    }

    private fun getSampleWeeklyData(): List<InOutData> {
        return listOf(
            InOutData("S", 30f, 25f),
            InOutData("M", 40f, 35f),
            InOutData("T", 60f, 45f),
            InOutData("W", 90f, 80f),
            InOutData("T", 50f, 40f),
            InOutData("F", 35f, 30f),
            InOutData("S", 25f, 20f),
        )
    }

    private fun getSampleNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem("새로운 공지", "시스템 점검이 2024-09-15에 예정되어 있습니다.", "1시간 전", NotificationType.NOTICE),
            NotificationItem("입고 완료", "SKU-12345 상품이 입고되었습니다.", "3시간 전", NotificationType.INBOUND),
            NotificationItem("재고 부족 알림", "상품 '샘플-A'의 재고가 10개 미만입니다.", "1일 전", NotificationType.STOCK_ALERT),
            NotificationItem("출고 예정", "주문 #98765에 대한 상품 출고가 예정되어 있습니다.", "2일 전", NotificationType.OUTBOUND)
        )
    }
}

// --- Events ---

sealed class HomeEvent {
    data object ShowDatePicker : HomeEvent()
    data object HideDatePicker : HomeEvent()
    data class DateSelected(val dateMillis: Long) : HomeEvent()
    data object ShowNotificationDialog : HomeEvent()
    data object HideNotificationDialog : HomeEvent()
    data class ShowChart(val chartType: ChartType) : HomeEvent()
    data object HideChart : HomeEvent()
}
