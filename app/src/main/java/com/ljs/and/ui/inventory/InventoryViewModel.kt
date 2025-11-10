package com.ljs.and.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ljs.and.data.model.InventoryOnHandItem
import com.ljs.and.data.model.PurchaseOrder
import com.ljs.and.data.model.PurchaseOrderItemRequest
import com.ljs.and.data.model.PurchaseOrderRequest
import com.ljs.and.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- State Data Classes ---
data class InventoryState(
    val inventoryList: List<InventoryOnHandItem> = emptyList(),
    val selectedFilter: String = "전체",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalItems: Long = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

data class RequestState(
    val requestList: List<PurchaseOrder> = emptyList(),
    val selectedFilter: String = "전체",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class PurchaseOrderCreationState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class InventoryViewModel : ViewModel() {

    private val repository = InventoryRepository()

    private val _inventoryState = MutableStateFlow(InventoryState())
    val inventoryState: StateFlow<InventoryState> = _inventoryState.asStateFlow()

    private val _requestState = MutableStateFlow(RequestState())
    val requestState: StateFlow<RequestState> = _requestState.asStateFlow()

    private val _purchaseOrderCreationState = MutableStateFlow(PurchaseOrderCreationState())
    val purchaseOrderCreationState: StateFlow<PurchaseOrderCreationState> = _purchaseOrderCreationState.asStateFlow()

    private var originalInventoryList: List<InventoryOnHandItem> = emptyList()
    private var originalRequestList: List<PurchaseOrder> = emptyList()

    init {
        loadInventory()
        loadPurchaseOrders()
    }

    fun updateInventoryFilter(filter: String) {
        val filteredList = when (filter) {
            "정상" -> originalInventoryList.filter { !it.lowStock }
            "부족" -> originalInventoryList.filter { it.lowStock }
            else -> originalInventoryList
        }
        _inventoryState.update { it.copy(selectedFilter = filter, inventoryList = filteredList) }
    }

    fun updateRequestFilter(filter: String) {
        val filteredList = when (filter) {
            "대기" -> originalRequestList.filter { it.status == "PENDING" }
            "승인" -> originalRequestList.filter { it.status == "APPROVED" } // 예시 상태
            "반려" -> originalRequestList.filter { it.status == "REJECTED" } // 예시 상태
            else -> originalRequestList
        }
        _requestState.update { it.copy(selectedFilter = filter, requestList = filteredList) }
    }

    fun loadInventory(page: Int = 0) {
        viewModelScope.launch {
            _inventoryState.update { it.copy(isLoading = true) }
            repository.fetchInventoryList(
                warehouseCode = "서울",
                partKeyword = null, supplierName = null, minQty = null, maxQty = null,
                page = page, size = 20
            ).onSuccess { apiResponse ->
                if (apiResponse.success && apiResponse.data != null) {
                    originalInventoryList = apiResponse.data.items
                    _inventoryState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            totalItems = apiResponse.data.total,
                            currentPage = apiResponse.data.page + 1,
                            totalPages = if (apiResponse.data.size == 0) 1 else kotlin.math.ceil(apiResponse.data.total.toDouble() / apiResponse.data.size).toInt(),
                            errorMessage = null
                        )
                    }
                    updateInventoryFilter(_inventoryState.value.selectedFilter)
                } else {
                    _inventoryState.update { it.copy(isLoading = false, errorMessage = apiResponse.message) }
                }
            }.onFailure { throwable ->
                _inventoryState.update { it.copy(isLoading = false, errorMessage = throwable.message) }
            }
        }
    }

    fun loadPurchaseOrders() {
        viewModelScope.launch {
            _requestState.update { it.copy(isLoading = true) }
            repository.fetchBranchOrders(
                userId = 4, username = "이지수", rank = "팀장", region = "수원", workType = "창고",
                startDate = null, endDate = null, page = 0, size = 100 // 모든 내역을 가져오기 위해 큰 size 사용
            ).onSuccess { apiResponse ->
                if (apiResponse.success && apiResponse.data != null) {
                    originalRequestList = apiResponse.data.content
                    _requestState.update { it.copy(isLoading = false, errorMessage = null) }
                    updateRequestFilter(_requestState.value.selectedFilter)
                } else {
                    _requestState.update { it.copy(isLoading = false, errorMessage = apiResponse.message) }
                }
            }.onFailure { throwable ->
                _requestState.update { it.copy(isLoading = false, errorMessage = throwable.message) }
            }
        }
    }

    fun createPurchaseOrder(
        partId: Long,
        partName: String,
        partCode: String,
        price: Int,
        quantity: Int
    ) {
        viewModelScope.launch {
            _purchaseOrderCreationState.update { it.copy(isLoading = true) }

            val request = PurchaseOrderRequest(
                vehicleNumber = "",
                vehicleModel = "",
//                receiptNum = "R-" + System.currentTimeMillis(),
                receiptNum = "",
                items = listOf(
                    PurchaseOrderItemRequest(partId, partName, partCode, price, quantity)
                )
            )

            repository.submitPurchaseOrder(
                userId = 4, username = "이지수", rank = "팀장", region = "수원", workType = "창고",
                request = request
            ).onSuccess { apiResponse ->
                if (apiResponse.success) {
                    _purchaseOrderCreationState.update { it.copy(isLoading = false, isSuccess = true) }
                    loadPurchaseOrders() // 성공 시 목록 새로고침
                } else {
                    _purchaseOrderCreationState.update { it.copy(isLoading = false, error = apiResponse.message) }
                }
            }.onFailure { throwable ->
                _purchaseOrderCreationState.update { it.copy(isLoading = false, error = throwable.message) }
            }
        }
    }

    fun resetPurchaseOrderCreationState() {
        _purchaseOrderCreationState.value = PurchaseOrderCreationState()
    }
}
