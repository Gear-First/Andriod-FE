package com.ljs.and.ui.home

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ljs.and.data.model.InventoryOnHandItem
import com.ljs.and.data.model.PurchaseOrder
import com.ljs.and.data.model.UserManager
import com.ljs.and.data.remote.HomeApiService
import com.ljs.and.data.repository.HomeRepository
import com.ljs.and.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val userName: String = "", // userName 추가
    val userEmail: String = "",
    val selectedDate: String = "",
    val isTodaySelected: Boolean = true,
    val isDatePickerVisible: Boolean = false,
    val isNotificationDialogVisible: Boolean = false,
    val visibleChart: ChartType? = null,
    val status: StatusData = StatusData(),
    val inventoryItems: List<InventoryItemData> = emptyList(),
    val weeklyInOutData: List<InOutData> = emptyList(),
    val notifications: List<NotificationItem> = emptyList(),
    val weeklyChartDateRange: String = ""
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

    private val inventoryRepository = InventoryRepository()

    private val homeApiService: HomeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://34.120.215.23/warehouse/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HomeApiService::class.java)
    }
    private val homeRepository = HomeRepository(homeApiService)

    private val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    private val apiSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _uiState = MutableStateFlow(HomeUiState(selectedDate = sdf.format(Date())))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val chartColors = listOf(
        Color(0xFF0D47A1),
        Color(0xFF1976D2),
        Color(0xFF2196F3),
        Color(0xFF64B5F6),
        Color(0xFFBBDEFB)
    )

    init {
        loadUserInfo()
        loadInitialData()
    }

    fun loadUserInfo() {
        _uiState.update { 
            it.copy(
                userName = UserManager.userName ?: "사용자",
                userEmail = UserManager.email ?: "정보 없음"
            ) 
        }
    }

    fun refreshData() {
        loadUserInfo()
        val selectedDate = sdf.parse(_uiState.value.selectedDate) ?: Date()
        loadStatusDataForDate(selectedDate)
        loadWeeklyInOutData(selectedDate)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    notifications = getSampleNotifications()
                )
            }
            val selectedDate = sdf.parse(_uiState.value.selectedDate) ?: Date()
            loadStatusDataForDate(selectedDate)
            loadWeeklyInOutData(selectedDate)
        }
    }

    private fun loadWeeklyInOutData(baseDate: Date) {
        viewModelScope.launch {
            try {
                val dailyData = homeRepository.getWeeklyInOutData(baseDate = baseDate, warehouseCode = "수원")
                val daySdf = SimpleDateFormat("EEE", Locale.KOREAN)
                val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val weeklyData = dailyData.entries
                    .map { entry ->
                        val date = dateSdf.parse(entry.key)
                        val dayName = daySdf.format(date!!)
                        val calendar = Calendar.getInstance().apply { time = date }
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        Triple(dayOfWeek, dayName, InOutData(
                            day = dayName,
                            inbound = entry.value.first.toFloat(),
                            outbound = entry.value.second.toFloat()
                        ))
                    }
                    .sortedBy { it.first } // Sort by day of the week
                    .map { it.third } // Extract the InOutData

                val calendar = Calendar.getInstance()
                calendar.time = baseDate
                val toDate = sdf.format(calendar.time)
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                val fromDate = sdf.format(calendar.time)

                _uiState.update { it.copy(
                    weeklyInOutData = weeklyData,
                    weeklyChartDateRange = "$fromDate - $toDate"
                ) }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading weekly in/out data", e)
                _uiState.update { it.copy(weeklyInOutData = emptyList()) }
            }
        }
    }


    private fun loadStatusDataForDate(date: Date) {
        viewModelScope.launch {
            val dateString = apiSdf.format(date)
            homeRepository.getNoteCounts(dateString)
                .onSuccess { data ->
                    _uiState.update { currentState ->
                        currentState.copy(status = currentState.status.copy(
                            inboundCount = data.receivingCount,
                            outboundCount = data.shippingCount
                        ))
                    }
                }.onFailure {
                    Log.e("HomeViewModel", "Error loading note counts", it)
                     _uiState.update { currentState ->
                        currentState.copy(status = currentState.status.copy(
                            inboundCount = 0,
                            outboundCount = 0
                        ))
                    }
                }

            val allInventoryItems = mutableListOf<InventoryOnHandItem>()
            inventoryRepository.fetchInventoryList("수원", null, null, null, null, 0, 100)
                .onSuccess {
                    if (it.success) {
                        allInventoryItems.addAll(it.data?.items ?: emptyList())
                    }
                }

            val allPurchaseOrderItems = mutableListOf<PurchaseOrder>()
            inventoryRepository.fetchBranchOrders(4, "이지수", "팀장", "수원", "창고", null, null, 0, 100)
                .onSuccess {
                    if (it.success) {
                        allPurchaseOrderItems.addAll(it.data?.content ?: emptyList())
                    }
                }

            val lowStockCount = allInventoryItems.count { it.lowStock }
            val requestCount = allPurchaseOrderItems.size

            val top5Inventory = allInventoryItems
                .sortedByDescending { it.onHandQty }
                .take(5)
                .mapIndexed { index, item ->
                    InventoryItemData(
                        name = item.part.name,
                        quantity = item.onHandQty,
                        color = chartColors.getOrElse(index) { Color.Gray }
                    )
                }

            _uiState.update {
                it.copy(
                    status = it.status.copy(lowStockCount = lowStockCount, requestCount = requestCount),
                    inventoryItems = top5Inventory
                )
            }
        }
    }


    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ShowDatePicker -> _uiState.update { it.copy(isDatePickerVisible = true) }
            is HomeEvent.HideDatePicker -> _uiState.update { it.copy(isDatePickerVisible = false) }
            is HomeEvent.DateSelected -> {
                val selectedDate = Date(event.dateMillis)
                val newDate = sdf.format(selectedDate)
                val todayDate = sdf.format(Date())
                _uiState.update {
                    it.copy(
                        selectedDate = newDate,
                        isDatePickerVisible = false,
                        isTodaySelected = newDate == todayDate
                    )
                }
                loadStatusDataForDate(selectedDate)
                loadWeeklyInOutData(selectedDate)
            }
            is HomeEvent.ShowNotificationDialog -> _uiState.update { it.copy(isNotificationDialogVisible = true) }
            is HomeEvent.HideNotificationDialog -> _uiState.update { it.copy(isNotificationDialogVisible = false) }
            is HomeEvent.ShowChart -> {
                val selectedDate = sdf.parse(_uiState.value.selectedDate) ?: Date()
                if (event.chartType == ChartType.WEEKLY) {
                    loadWeeklyInOutData(selectedDate)
                }
                _uiState.update { it.copy(visibleChart = event.chartType) }
            }
            is HomeEvent.HideChart -> _uiState.update { it.copy(visibleChart = null) }
        }
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
