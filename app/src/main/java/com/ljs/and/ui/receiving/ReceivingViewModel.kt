package com.ljs.and.ui.receiving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ljs.and.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReceivingItem(
    val id: String,
    val supplier: String,
    val expectedDate: String,
    val completionDate: String?,
    val totalQuantity: Int,
    val manager: String,
    var status: String
)

data class InspectionItem(
    val id: String,
    val receivingId: String,
    val partName: String,
    val partCode: String,
    val quantity: Int,
    val location: String,
    val imageUrl: Int? = null,
    var isInspected: Boolean = false
)

data class ReceivingUiState(
    val receivingList: List<ReceivingItem> = emptyList(),
    val inspectionList: List<InspectionItem> = emptyList(),
    val selectedReceivingItem: ReceivingItem? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentInspectionItemId: String? = null // 현재 검수중인 아이템 ID
)

class ReceivingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReceivingUiState())
    val uiState: StateFlow<ReceivingUiState> = _uiState.asStateFlow()

    init {
        loadReceivingList()
    }

    private fun loadReceivingList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val dummyList = getDummyReceivingItems()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    receivingList = dummyList
                )
            }
        }
    }

    fun selectReceivingItem(item: ReceivingItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedReceivingItem = item) }
            val inspectionItems = getDummyInspectionItems(item.id)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    inspectionList = inspectionItems
                )
            }
        }
    }

    fun setCurrentInspectionItem(itemId: String) {
        _uiState.update { it.copy(currentInspectionItemId = itemId) }
    }

    fun completeCurrentInspection() {
        _uiState.value.currentInspectionItemId?.let { itemId ->
            completeInspection(itemId)
            _uiState.update { it.copy(currentInspectionItemId = null) } // 처리 후 ID 초기화
        }
    }

    fun completeInspection(itemId: String) {
        _uiState.update { currentState ->
            val updatedList = currentState.inspectionList.map {
                if (it.id == itemId) it.copy(isInspected = true) else it
            }
            currentState.copy(inspectionList = updatedList)
        }
    }

    fun completeAllInspections() {
        val receivingIdToUpdate = _uiState.value.selectedReceivingItem?.id ?: return
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        val completionDate = sdf.format(Date())

        _uiState.update { currentState ->
            val updatedReceivingList = currentState.receivingList.map {
                if (it.id == receivingIdToUpdate) {
                    it.copy(status = "완료", completionDate = completionDate)
                } else {
                    it
                }
            }
            currentState.copy(receivingList = updatedReceivingList, inspectionList = emptyList(), selectedReceivingItem = null)
        }
    }

    private fun getDummyReceivingItems(): List<ReceivingItem> {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        return listOf(
            ReceivingItem(id = "R-001", supplier = "거래처 A", expectedDate = currentDate, completionDate = null, totalQuantity = 120, manager = "김담당", status = "대기"),
            ReceivingItem(id = "R-002", supplier = "거래처 B", expectedDate = currentDate, completionDate = null, totalQuantity = 85, manager = "박담당", status = "대기"),
            ReceivingItem(id = "R-003", supplier = "거래처 C", expectedDate = "2024.09.20", completionDate = "2024.09.20 14:30", totalQuantity = 200, manager = "최담당", status = "완료"),
            ReceivingItem(id = "R-004", supplier = "거래처 A", expectedDate = "2024.09.21", completionDate = null, totalQuantity = 150, manager = "김담당", status = "대기")
        ).sortedByDescending { it.expectedDate }
    }

    private fun getDummyInspectionItems(receivingId: String): List<InspectionItem> {
        return when (receivingId) {
            "R-001" -> listOf(
                InspectionItem("P-01", receivingId, "엔진 오일 필터", "EOF-001", 50, "A-01-1", R.drawable.ic_launcher_background),
                InspectionItem("P-02", receivingId, "브레이크 패드", "BP-002", 70, "A-01-2", R.drawable.ic_launcher_background)
            )
            "R-002" -> listOf(
                InspectionItem("P-03", receivingId, "에어컨 필터", "ACF-003", 85, "B-02-1", R.drawable.ic_launcher_background)
            )
            "R-003" -> listOf(
                InspectionItem("P-05", receivingId, "와이퍼 블레이드", "WB-005", 200, "D-04-1", R.drawable.ic_launcher_background, isInspected = true)
            )
            "R-004" -> listOf(
                InspectionItem("P-01", receivingId, "엔진 오일 필터", "EOF-001", 60, "A-01-1", R.drawable.ic_launcher_background),
                InspectionItem("P-04", receivingId, "타이밍 벨트", "TB-004", 90, "C-03-3", R.drawable.ic_launcher_background)
            )
            else -> emptyList()
        }
    }
}