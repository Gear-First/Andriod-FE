package com.ljs.and.ui.more

import androidx.lifecycle.ViewModel
import com.ljs.and.data.model.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MoreUiState(
    val userName: String = "",
    val email: String = "",
    val warehouseName: String = ""
)

class MoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MoreUiState())
    val uiState: StateFlow<MoreUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        _uiState.update {
            it.copy(
                userName = UserManager.userName ?: "사용자",
                email = UserManager.email ?: "정보 없음",
                warehouseName = UserManager.warehouseName ?: "창고 정보 없음"
            )
        }
    }
}