package com.ljs.and.ui.receiving

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ljs.and.data.model.CreateReceivingLine
import com.ljs.and.data.model.CreateReceivingRequest
import com.ljs.and.data.model.InspectorInfo
import com.ljs.and.data.model.ReceivingCompletion
import com.ljs.and.data.model.ReceivingNote
import com.ljs.and.data.model.ReceivingNoteDetail
import com.ljs.and.data.model.UpdateReceivingLineRequest
import com.ljs.and.data.remote.RetrofitClient
import com.ljs.and.data.repository.ReceivingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class ReceivingUiState(
    val notDoneReceivingList: List<ReceivingNote> = emptyList(),
    val doneReceivingList: List<ReceivingNote> = emptyList(),
    val searchResultList: List<ReceivingNote> = emptyList(), // 검색 결과
    val selectedReceivingNoteDetail: ReceivingNoteDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val notDonePage: Int = 0,
    val donePage: Int = 0,
    val searchPage: Int = 0,
    val canLoadMoreNotDone: Boolean = true,
    val canLoadMoreDone: Boolean = true,
    val canLoadMoreSearch: Boolean = true,
    val createdReceivingNote: ReceivingNoteDetail? = null,
    val receivingCompletion: ReceivingCompletion? = null,
    val rejectionProcessCompleted: Boolean = false // 재입고 처리 완료 상태
)

class ReceivingViewModel(private val repository: ReceivingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceivingUiState())
    val uiState: StateFlow<ReceivingUiState> = _uiState.asStateFlow()

    private val TAG = "ReceivingViewModel"

    fun searchReceivingNotes(query: String?, warehouseCode: String?) {
        if (uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, searchResultList = emptyList(), searchPage = 0, canLoadMoreSearch = true) }
            try {
                val response = repository.getReceivingNotes(
                    q = query,
                    status = "all",
                    date = null,
                    dateFrom = null,
                    dateTo = null,
                    warehouseCode = warehouseCode,
                    receivingNo = null,
                    supplierName = null,
                    page = 0,
                    size = 20,
                    sort = null
                )
                if (response.success) {
                    val newItems = response.data?.items ?: emptyList()
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            searchResultList = newItems,
                            searchPage = 1,
                            canLoadMoreSearch = newItems.size == 20
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


    @RequiresApi(Build.VERSION_CODES.O)
    fun processRejectedItemAndReRequest(lineId: Long, rejectedQty: Int, remark: String?) {
        val noteDetail = _uiState.value.selectedReceivingNoteDetail
        val targetLine = noteDetail?.lines?.find { it.lineId == lineId }
        if (noteDetail == null || targetLine == null) {
            _uiState.update { it.copy(errorMessage = "필요한 입고 정보가 없습니다.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. 현재 라인 불량 처리 (검수 수량 0, 불량 플래그 true)
                val updateRequest = UpdateReceivingLineRequest(inspectedQty = 0, rejected = true)
                val updateResponse = repository.updateReceivingLine(noteDetail.noteId, lineId, updateRequest)
                if (!updateResponse.success) throw Exception("기존 품목 불량 처리 실패: ${updateResponse.message}")

                // 2. 재입고 요청 생성
                val requestedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                val newReceivingRequest = CreateReceivingRequest(
                    supplierName = noteDetail.supplierName,
                    warehouseCode = noteDetail.warehouseCode,
                    receivingNo = null, // 서버에서 자동 생성
                    requestedAt = requestedAt,
                    expectedReceiveDate = null, // 서버에서 자동 계산 (requestedAt + 2일)
                    remark = "[재입고] 원본 입고번호: ${noteDetail.receivingNo} - 사유: $remark",
                    lines = listOf(
                        CreateReceivingLine(
                            productId = targetLine.product.id,
                            orderedQty = rejectedQty, // 불량 수량만큼 재요청
                            lineRemark = "자동 재입고 요청"
                        )
                    )
                )
                val reRequestResponse = repository.createReceivingRequest(newReceivingRequest)
                if (!reRequestResponse.success) throw Exception("재입고 요청 실패: ${reRequestResponse.message}")

                // 3. UI 상태 업데이트
                loadReceivingNoteDetail(noteDetail.noteId) // 상세 정보 다시 로드
                _uiState.update { it.copy(isLoading = false, rejectionProcessCompleted = true) }

            } catch (e: Exception) {
                Log.e(TAG, "재입고 처리 중 오류 발생", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun clearRejectionProcessEvent() {
        _uiState.update { it.copy(rejectionProcessCompleted = false) }
    }

    fun createReceivingRequest(request: CreateReceivingRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.createReceivingRequest(request)
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, createdReceivingNote = response.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun completeReceiving(noteId: Long, inspectorInfo: InspectorInfo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.completeReceiving(noteId, inspectorInfo)
                Log.d(TAG, "completeReceiving 응답: ${Gson().toJson(response)}")

                if (response.success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            receivingCompletion = response.data
                        )
                    }
                    Log.d(TAG, "입고 완료 성공 ✅ noteId=$noteId")
                } else {
                    Log.e(TAG, "입고 완료 실패 ❌: ${response.message}")
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "completeReceiving 예외 발생", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun clearReceivingCompletionEvent() {
        _uiState.update { it.copy(receivingCompletion = null) }
    }

    fun refreshAllLists(
    date: String? = null,
    dateFrom: String? = null,
    dateTo: String? = null,
    warehouseCode: String? = null
    ) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d(TAG, "Refreshing all lists...")

            try {
                val notDoneDeferred = async { repository.getNotDoneReceivingNotes(date, dateFrom, dateTo, warehouseCode, 0, 20, null) }
                val doneDeferred = async { repository.getDoneReceivingNotes(date, dateFrom, dateTo, warehouseCode, 0, 20, null) }

                val notDoneResponse = notDoneDeferred.await()
                val doneResponse = doneDeferred.await()

                val notDoneItems = if (notDoneResponse.success) notDoneResponse.data?.items ?: emptyList() else emptyList()
                val doneItems = if (doneResponse.success) doneResponse.data?.items ?: emptyList() else emptyList()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        notDoneReceivingList = notDoneItems,
                        doneReceivingList = doneItems,
                        notDonePage = 1,
                        donePage = 1,
                        canLoadMoreNotDone = notDoneItems.size == 20,
                        canLoadMoreDone = doneItems.size == 20
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing lists", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }


    fun loadNotDoneReceivingNotes(date: String? = null, dateFrom: String? = null, dateTo: String? = null, warehouseCode: String? = null) {
        if (uiState.value.isLoading || !uiState.value.canLoadMoreNotDone) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val pageToLoad = uiState.value.notDonePage
            Log.d(TAG, "Requesting not-done notes: page=$pageToLoad, date=$date, warehouseCode=$warehouseCode")

            try {
                val response = repository.getNotDoneReceivingNotes(date, dateFrom, dateTo, warehouseCode, pageToLoad, 20, null)
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

    fun loadDoneReceivingNotes(date: String? = null, dateFrom: String? = null, dateTo: String? = null, warehouseCode: String? = null) {
        if (uiState.value.isLoading || !uiState.value.canLoadMoreDone) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val pageToLoad = uiState.value.donePage
            Log.d(TAG, "Requesting done notes: page=$pageToLoad, date=$date, warehouseCode=$warehouseCode")

            try {
                val response = repository.getDoneReceivingNotes(date, dateFrom, dateTo, warehouseCode, pageToLoad, 20, null)
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

    fun updateReceivingLine(lineId: Long, inspectedQty: Int, rejected: Boolean, lineRemark: String?) {
        val noteId = uiState.value.selectedReceivingNoteDetail?.noteId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d(TAG, "Updating inspection for noteId: $noteId, lineId: $lineId, qty: $inspectedQty, rejected: $rejected, remark: $lineRemark")
            try {
                val request = UpdateReceivingLineRequest(inspectedQty, rejected)
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
