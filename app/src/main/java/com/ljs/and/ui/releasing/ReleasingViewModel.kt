package com.ljs.and.ui.releasing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ljs.and.data.model.AssigneeInfo
import com.ljs.and.data.model.CreateShippingRequest
import com.ljs.and.data.model.ShippingCompletion
import com.ljs.and.data.model.ShippingNote
import com.ljs.and.data.model.ShippingNoteDetail
import com.ljs.and.data.model.UpdateShippingLineRequest
import com.ljs.and.data.remote.RetrofitClient
import com.ljs.and.data.repository.ReleasingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ReleasingUiState(
    val notDoneShippingList: List<ShippingNote> = emptyList(),
    val doneShippingList: List<ShippingNote> = emptyList(),
    val selectedShippingNoteDetail: ShippingNoteDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val notDonePage: Int = 0,
    val donePage: Int = 0,
    val canLoadMoreNotDone: Boolean = true,
    val canLoadMoreDone: Boolean = true,
    val createdShippingNote: ShippingNoteDetail? = null,
    val shippingCompletion: ShippingCompletion? = null
)

class ReleasingViewModel(private val repository: ReleasingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReleasingUiState())
    val uiState: StateFlow<ReleasingUiState> = _uiState.asStateFlow()

    private val TAG = "ReleasingViewModel"

    fun createShippingRequest(request: CreateShippingRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.createShippingRequest(request)
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, createdShippingNote = response.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun completeShipping(noteId: Long, assigneeInfo: AssigneeInfo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.completeShipping(noteId, assigneeInfo)
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, shippingCompletion = response.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "completeShipping HttpException: $errorBody", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = errorBody ?: e.message()) }
            } catch (e: IOException) {
                Log.e(TAG, "completeShipping IOException", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Network error: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "completeShipping exception", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun refreshAllLists(date: String? = null, dateFrom: String? = null, dateTo: String? = null, warehouseCode: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d(TAG, "Refreshing all lists...")

            try {
                val notDoneDeferred = async { repository.getNotDoneShippingNotes(date, dateFrom, dateTo, warehouseCode, 0, 20, null) }
                val doneDeferred = async { repository.getDoneShippingNotes(date, dateFrom, dateTo, warehouseCode, 0, 20, null) }

                val notDoneResponse = notDoneDeferred.await()
                val doneResponse = doneDeferred.await()

                val notDoneItems = if (notDoneResponse.success) notDoneResponse.data?.items ?: emptyList() else emptyList()
                val doneItems = if (doneResponse.success) doneResponse.data?.items ?: emptyList() else emptyList()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        notDoneShippingList = notDoneItems,
                        doneShippingList = doneItems,
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

    fun loadNotDoneShippingNotes(date: String? = null, dateFrom: String? = null, dateTo: String? = null, warehouseCode: String? = null) {
        if (uiState.value.isLoading || !uiState.value.canLoadMoreNotDone) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val pageToLoad = uiState.value.notDonePage
            try {
                val response = repository.getNotDoneShippingNotes(date, dateFrom, dateTo, warehouseCode, pageToLoad, 20, null)
                if (response.success) {
                    val newItems = response.data?.items ?: emptyList()
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            notDoneShippingList = currentState.notDoneShippingList + newItems,
                            notDonePage = pageToLoad + 1,
                            canLoadMoreNotDone = newItems.isNotEmpty() && newItems.size == 20
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadDoneShippingNotes(date: String? = null, dateFrom: String? = null, dateTo: String? = null, warehouseCode: String? = null) {
        if (uiState.value.isLoading || !uiState.value.canLoadMoreDone) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val pageToLoad = uiState.value.donePage
            try {
                val response = repository.getDoneShippingNotes(date, dateFrom, dateTo, warehouseCode, pageToLoad, 20, null)
                if (response.success) {
                    val newItems = response.data?.items ?: emptyList()
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            doneShippingList = currentState.doneShippingList + newItems,
                            donePage = pageToLoad + 1,
                            canLoadMoreDone = newItems.isNotEmpty() && newItems.size == 20
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadShippingNoteDetail(noteId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getShippingNoteDetail(noteId)
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, selectedShippingNoteDetail = response.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateShippingLine(lineId: Long, pickedQty: Int, lineRemark: String?) {
        val noteDetail = _uiState.value.selectedShippingNoteDetail ?: return
        val noteId = noteDetail.noteId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = UpdateShippingLineRequest(pickedQty, lineRemark)
                val response = repository.updateShippingLine(noteId, lineId, request)
                if (response.success) {
                    val updatedDetail = response.data ?: noteDetail
                    val newLines = updatedDetail.lines.map { line ->
                        if (line.lineId == lineId) {
                            val newStatus = if (pickedQty < line.orderedQty) "SHORTAGE" else line.status
                            line.copy(pickedQty = pickedQty, status = newStatus)
                        } else line
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedShippingNoteDetail = updatedDetail.copy(lines = newLines)
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun clearShippingCompletionEvent() {
        _uiState.update { it.copy(shippingCompletion = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

class ReleasingViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReleasingViewModel::class.java)) {
            val repository = ReleasingRepository(RetrofitClient.releasingApiService)
            @Suppress("UNCHECKED_CAST")
            return ReleasingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
