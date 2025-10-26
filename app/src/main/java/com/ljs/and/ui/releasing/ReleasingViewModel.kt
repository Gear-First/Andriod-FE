package com.ljs.and.ui.releasing

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

data class ReleasingUiState(
    val releasingList: List<ReleasingItem> = emptyList(),
    val pickingList: List<PickingItem> = emptyList(),
    val selectedReleasingItem: ReleasingItem? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentPickingItemId: String? = null
)

class ReleasingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReleasingUiState())
    val uiState: StateFlow<ReleasingUiState> = _uiState.asStateFlow()

    init {
        loadReleasingList()
    }

    private fun loadReleasingList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val dummyList = getDummyReleasingItems()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    releasingList = dummyList
                )
            }
        }
    }

    fun selectReleasingItem(item: ReleasingItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedReleasingItem = item) }
            val pickingItems = getDummyPickingItems(item.id)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    pickingList = pickingItems
                )
            }
        }
    }

    fun setCurrentPickingItem(itemId: String) {
        _uiState.update { it.copy(currentPickingItemId = itemId) }
    }

    fun completeCurrentPicking() {
        _uiState.value.currentPickingItemId?.let { itemId ->
            completePicking(itemId)
            _uiState.update { it.copy(currentPickingItemId = null) }
        }
    }

    fun completePicking(itemId: String) {
        _uiState.update { currentState ->
            val updatedList = currentState.pickingList.map {
                if (it.id == itemId) it.copy(isPicked = true) else it
            }
            currentState.copy(pickingList = updatedList)
        }
    }

    fun completeAllPicking() {
        val releasingIdToUpdate = _uiState.value.selectedReleasingItem?.id ?: return
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        val completionDate = sdf.format(Date())

        _uiState.update { currentState ->
            val updatedReleasingList = currentState.releasingList.map {
                if (it.id == releasingIdToUpdate) {
                    it.copy(status = "완료", completionDate = completionDate)
                } else {
                    it
                }
            }
            currentState.copy(releasingList = updatedReleasingList, pickingList = emptyList(), selectedReleasingItem = null)
        }
    }

    private fun getDummyReleasingItems(): List<ReleasingItem> {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        return listOf(
            ReleasingItem(id = "O-001", customer = "거래처 A", expectedDate = currentDate, completionDate = null, totalQuantity = 120, manager = "김담당", status = "대기"),
            ReleasingItem(id = "O-002", customer = "거래처 B", expectedDate = currentDate, completionDate = null, totalQuantity = 85, manager = "박담당", status = "대기"),
            ReleasingItem(id = "O-003", customer = "거래처 C", expectedDate = "2024.09.20", completionDate = "2024.09.20 14:30", totalQuantity = 200, manager = "최담당", status = "완료"),
            ReleasingItem(id = "O-004", customer = "거래처 A", expectedDate = "2024.09.21", completionDate = null, totalQuantity = 150, manager = "김담당", status = "대기")
        ).sortedByDescending { it.expectedDate }
    }

    private fun getDummyPickingItems(releasingId: String): List<PickingItem> {
        return when (releasingId) {
            "O-001" -> listOf(
                PickingItem("P-01", releasingId, "엔진 오일 필터", "EOF-001", 50, "A-01-1", false, R.drawable.ic_launcher_background),
                PickingItem("P-02", releasingId, "브레이크 패드", "BP-002", 70, "A-01-2", false, R.drawable.ic_launcher_background)
            )
            "O-002" -> listOf(
                PickingItem("P-03", releasingId, "에어컨 필터", "ACF-003", 85, "B-02-1", false, R.drawable.ic_launcher_background)
            )
            "O-003" -> listOf(
                PickingItem("P-05", releasingId, "와이퍼 블레이드", "WB-005", 200, "D-04-1", true, R.drawable.ic_launcher_background)
            )
            "O-004" -> listOf(
                PickingItem("P-01", releasingId, "엔진 오일 필터", "EOF-001", 60, "A-01-1", false, R.drawable.ic_launcher_background),
                PickingItem("P-04", releasingId, "타이밍 벨트", "TB-004", 90, "C-03-3", false, R.drawable.ic_launcher_background)
            )
            else -> emptyList()
        }
    }
}
