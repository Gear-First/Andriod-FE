package com.ljs.and.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ljs.and.data.model.BranchPurchaseOrderItem
import com.ljs.and.data.model.InventoryOnHandItem
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
    val totalItems: Int = 0,
    val totalQuantity: Long = 0,
    val lackingItems: Int = 0,
    val isLoading: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

data class RequestState(
    val requestList: List<BranchPurchaseOrderItem> = emptyList(),
    val selectedFilter: String = "전체"
)

data class PurchaseOrderCreationState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class InventoryViewModel : ViewModel() {

    // Use the singleton repository instance
    private val repository = InventoryRepository

    private val _inventoryState = MutableStateFlow(InventoryState())
    val inventoryState: StateFlow<InventoryState> = _inventoryState.asStateFlow()

    private val _requestState = MutableStateFlow(RequestState())
    val requestState: StateFlow<RequestState> = _requestState.asStateFlow()

    private val _purchaseOrderCreationState = MutableStateFlow(PurchaseOrderCreationState())
    val purchaseOrderCreationState: StateFlow<PurchaseOrderCreationState> = _purchaseOrderCreationState.asStateFlow()

    private var originalInventoryList: List<InventoryOnHandItem> = emptyList()
    private var originalRequestList: List<BranchPurchaseOrderItem> = emptyList()

    init {
        loadInventory()
        loadPurchaseOrders()
    }

    fun resetPurchaseOrderCreationState() {
        _purchaseOrderCreationState.value = PurchaseOrderCreationState()
    }

    fun updateInventoryFilter(filter: String) {
        _inventoryState.update { currentState ->
            val filteredList = when (filter) {
                "정상" -> originalInventoryList.filter { !it.lowStock }
                "부족" -> originalInventoryList.filter { it.lowStock }
                else -> originalInventoryList
            }
            currentState.copy(
                selectedFilter = filter,
                inventoryList = filteredList,
                lackingItems = originalInventoryList.count { it.lowStock } // Recalculate based on original list
            )
        }
    }

    fun updateRequestFilter(filter: String) {
        _requestState.update { currentState ->
            val filteredList = when (filter) {
                "대기" -> originalRequestList.filter { it.status == "PENDING" }
                "승인" -> originalRequestList.filter { it.status == "APPROVED" }
                else -> originalRequestList
            }
            currentState.copy(
                selectedFilter = filter,
                requestList = filteredList
            )
        }
    }

    fun loadInventory(page: Int = 0, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _inventoryState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getInventoryOnHand(
                    warehouseCode = "서울",
                    partKeyword = null,
                    supplierName = null,
                    minQty = null,
                    maxQty = null,
                    page = page,
                    size = 20, // Keep pagination size for this screen
                    sort = null,
                    forceRefresh = forceRefresh
                )
                if (response.success) {
                    originalInventoryList = response.data?.items ?: emptyList()
                    val currentFilter = _inventoryState.value.selectedFilter
                    val filteredList = when (currentFilter) {
                        "정상" -> originalInventoryList.filter { !it.lowStock }
                        "부족" -> originalInventoryList.filter { it.lowStock }
                        else -> originalInventoryList
                    }

                    _inventoryState.update {
                        it.copy(
                            inventoryList = filteredList,
                            totalItems = response.data?.total?.toInt() ?: 0,
                            currentPage = response.data?.page?.plus(1) ?: 1,
                            totalPages = response.data?.let {
                                if (it.size == 0) 1 else kotlin.math.ceil(it.total.toDouble() / it.size).toInt()
                            } ?: 1,
                            totalQuantity = originalInventoryList.sumOf { item -> item.onHandQty.toLong() },
                            lackingItems = originalInventoryList.count { it.lowStock },
                            isLoading = false
                        )
                    }
                } else {
                    _inventoryState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _inventoryState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadPurchaseOrders(page: Int = 0, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val allItems = mutableListOf<BranchPurchaseOrderItem>()
                var currentPage = 0
                var totalPages = 1
                while (currentPage < totalPages) {
                    val response = repository.getBranchPurchaseOrders(
                        branchCode = "seoul",
                        engineerId = 1111, // Get from logged in user
                        startDate = null,
                        endDate = null,
                        page = currentPage,
                        size = 100, // Adjust size as needed
                        forceRefresh = forceRefresh
                    )
                    if (response.success) {
                        response.data?.content?.let { allItems.addAll(it) }
                        totalPages = response.data?.totalPages ?: 1
                        currentPage++
                    } else {
                        break // Exit on failure
                    }
                }

                originalRequestList = allItems
                val currentFilter = _requestState.value.selectedFilter
                val filteredList = when (currentFilter) {
                    "대기" -> originalRequestList.filter { it.status == "PENDING" }
                    "승인" -> originalRequestList.filter { it.status == "APPROVED" }
                    else -> originalRequestList
                }
                _requestState.update {
                    it.copy(requestList = filteredList, selectedFilter = currentFilter)
                }

            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    fun createPurchaseOrder(partName: String, partCode: String, quantity: Int) {
        viewModelScope.launch {
            _purchaseOrderCreationState.update { it.copy(isLoading = true, isSuccess = false, error = null) }
            val request = PurchaseOrderRequest(
                vehicleNumber = "",
                vehicleModel = "",
                requesterId = 1111L,
                requesterName = "2222",
                requesterRole = "3333",
                requesterCode = "seoul",
                receiptNum = "",
                items = listOf(
                    PurchaseOrderItemRequest(
                        partId = 0L,
                        partName = partName,
                        partCode = partCode,
                        price = 0,
                        quantity = quantity
                    )
                )
            )
            try {
                val response = repository.createPurchaseOrder(request)
                if (response.success) {
                    _purchaseOrderCreationState.update { it.copy(isLoading = false, isSuccess = true) }
                    loadPurchaseOrders(forceRefresh = true) // Refresh the list
                } else {
                    _purchaseOrderCreationState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _purchaseOrderCreationState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
