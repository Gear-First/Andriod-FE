package com.ljs.and.ui.releasing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ljs.and.data.model.CreateShippingRequest
import com.ljs.and.data.model.PagedShippingNotes
import com.ljs.and.data.model.ShippingNoteDetail
import com.ljs.and.data.model.UpdateShippingLineRequest
import com.ljs.and.data.remote.RetrofitClient
import com.ljs.and.data.repository.ReleasingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReleasingUiState(
    val notDoneShippingNotes: PagedShippingNotes? = null,
    val doneShippingNotes: PagedShippingNotes? = null,
    val selectedShippingNoteDetail: ShippingNoteDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdShippingNote: ShippingNoteDetail? = null
)

class ReleasingViewModel(private val repository: ReleasingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReleasingUiState())
    val uiState: StateFlow<ReleasingUiState> = _uiState.asStateFlow()

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

    fun loadNotDoneShippingNotes(date: String? = null, warehouseId: Long? = null, page: Int = 0, size: Int = 20, sort: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getNotDoneShippingNotes(date, warehouseId, page, size, sort)
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, notDoneShippingNotes = response.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadDoneShippingNotes(date: String? = null, warehouseId: Long? = null, page: Int = 0, size: Int = 20, sort: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getDoneShippingNotes(date, warehouseId, page, size, sort)
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, doneShippingNotes = response.data) }
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
                println("[ReleasingViewModel] Server Response: $response") // Log the full response
                if (response.success) {
                    println("[ReleasingViewModel] Data received: ${response.data}") // Log the data part
                    _uiState.update { it.copy(isLoading = false, selectedShippingNoteDetail = response.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateShippingLine(noteId: Long, lineId: Long, allocatedQty: Int, pickedQty: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = UpdateShippingLineRequest(allocatedQty, pickedQty)
                val response = repository.updateShippingLine(noteId, lineId, request)
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
