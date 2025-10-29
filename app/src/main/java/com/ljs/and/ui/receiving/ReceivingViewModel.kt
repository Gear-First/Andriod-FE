package com.ljs.and.ui.receiving

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ljs.and.data.model.ReceivingNote
import com.ljs.and.data.model.ReceivingNoteDetail
import com.ljs.and.data.model.UpdateInspectionRequest
import com.ljs.and.data.remote.RetrofitClient
import com.ljs.and.data.repository.ReceivingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReceivingUiState(
    val notDoneReceivingList: List<ReceivingNote> = emptyList(),
    val doneReceivingList: List<ReceivingNote> = emptyList(),
    val selectedReceivingNoteDetail: ReceivingNoteDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val notDonePage: Int = 0,
    val donePage: Int = 0,
    val canLoadMoreNotDone: Boolean = true,
    val canLoadMoreDone: Boolean = true
)

class ReceivingViewModel(private val repository: ReceivingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceivingUiState())
    val uiState: StateFlow<ReceivingUiState> = _uiState.asStateFlow()

    private val TAG = "ReceivingViewModel"

    fun refreshAllLists(date: String? = null, warehouseId: Long? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d(TAG, "Refreshing all lists...")

            try {
                // 두 API를 동시에 호출
                val notDoneDeferred = async { repository.getNotDoneReceivingNotes(date, warehouseId, 0, 20) }
                val doneDeferred = async { repository.getDoneReceivingNotes(date, warehouseId, 0, 20) }

                val notDoneResponse = notDoneDeferred.await()
                val doneResponse = doneDeferred.await()

                val notDoneItems = if (notDoneResponse.success) notDoneResponse.data?.items ?: emptyList() else emptyList()
                val doneItems = if (doneResponse.success) doneResponse.data?.items ?: emptyList() else emptyList()

                Log.d(TAG, "Refreshed not-done: ${notDoneItems.size}, done: ${doneItems.size}")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        notDoneReceivingList = notDoneItems,
                        doneReceivingList = doneItems,
                        notDonePage = 1,
                        donePage = 1,
                        canLoadMoreNotDone = notDoneItems.isNotEmpty() && notDoneItems.size == 20,
                        canLoadMoreDone = doneItems.isNotEmpty() && doneItems.size == 20,
                        errorMessage = if (!notDoneResponse.success) notDoneResponse.message else if (!doneResponse.success) doneResponse.message else null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing lists", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
    
    fun loadNotDoneReceivingNotes(date: String? = null, warehouseId: Long? = null) {
        if (uiState.value.isLoading || !uiState.value.canLoadMoreNotDone) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val pageToLoad = uiState.value.notDonePage
            Log.d(TAG, "Requesting not-done notes: page=$pageToLoad, date=$date, warehouseId=$warehouseId")

            try {
                val response = repository.getNotDoneReceivingNotes(date, warehouseId, pageToLoad, 20)
                if (response.success) {
                    val newItems = response.data?.items ?: emptyList()
                    Log.d(TAG, "Successfully loaded ${newItems.size} not-done items.")
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            notDoneReceivingList = currentState.notDoneReceivingList + newItems,
                            notDonePage = pageToLoad + 1,
                            canLoadMoreNotDone = newItems.isNotEmpty() && newItems.size == 20
                        )
                    }
                } else {
                    Log.e(TAG, "Failed to load not-done notes: ${response.message}")
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading not-done notes", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadDoneReceivingNotes(date: String? = null, warehouseId: Long? = null) {
        if (uiState.value.isLoading || !uiState.value.canLoadMoreDone) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val pageToLoad = uiState.value.donePage
            Log.d(TAG, "Requesting done notes: page=$pageToLoad, date=$date, warehouseId=$warehouseId")

            try {
                val response = repository.getDoneReceivingNotes(date, warehouseId, pageToLoad, 20)
                if (response.success) {
                    val newItems = response.data?.items ?: emptyList()
                    Log.d(TAG, "Successfully loaded ${newItems.size} done items.")
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            doneReceivingList = currentState.doneReceivingList + newItems,
                            donePage = pageToLoad + 1,
                            canLoadMoreDone = newItems.isNotEmpty() && newItems.size == 20
                        )
                    }
                } else {
                    Log.e(TAG, "Failed to load done notes: ${response.message}")
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading done notes", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadReceivingNoteDetail(noteId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d(TAG, "Requesting detail for noteId: $noteId")
            try {
                val response = repository.getReceivingNoteDetail(noteId)
                if (response.success) {
                    Log.d(TAG, "Successfully loaded detail for noteId: $noteId")
                    _uiState.update { it.copy(isLoading = false, selectedReceivingNoteDetail = response.data) }
                } else {
                    Log.e(TAG, "Failed to load detail for noteId $noteId: ${response.message}")
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading detail for noteId $noteId", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateInspectionResult(lineId: Long, inspectedQty: Int, hasIssue: Boolean) {
        val noteId = uiState.value.selectedReceivingNoteDetail?.noteId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d(TAG, "Updating inspection for noteId: $noteId, lineId: $lineId, qty: $inspectedQty, issue: $hasIssue")
            try {
                val request = UpdateInspectionRequest(inspectedQty, hasIssue)
                val response = repository.updateReceivingLine(noteId, lineId, request)
                if (response.success) {
                    Log.d(TAG, "Successfully updated inspection for lineId: $lineId")
                    Log.d(TAG, "Response data: ${Gson().toJson(response.data)}")
                    _uiState.update { it.copy(isLoading = false, selectedReceivingNoteDetail = response.data) }
                } else {
                    Log.e(TAG, "Failed to update inspection for lineId $lineId: ${response.message}")
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating inspection for lineId $lineId", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}

class ReceivingViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReceivingViewModel::class.java)) {
            val repository = ReceivingRepository(RetrofitClient.receivingApiService)
            @Suppress("UNCHECKED_CAST")
            return ReceivingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
