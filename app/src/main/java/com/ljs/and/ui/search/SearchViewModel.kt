package com.ljs.and.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ljs.and.data.model.InventoryItem
import com.ljs.and.data.model.SearchItem
import com.ljs.and.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class SearchTab { SEARCH, INVENTORY }

class SearchViewModel(private val searchRepository: SearchRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _warehouseCode = MutableStateFlow("")
    val warehouseCode: StateFlow<String> = _warehouseCode

    private val _selectedTab = MutableStateFlow(SearchTab.SEARCH)
    val selectedTab: StateFlow<SearchTab> = _selectedTab

    private val _searchList = MutableStateFlow<List<SearchItem>>(emptyList())
    val searchList: StateFlow<List<SearchItem>> = _searchList

    private val _inventoryList = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryList: StateFlow<List<InventoryItem>> = _inventoryList

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onWarehouseCodeChange(code: String) {
        _warehouseCode.value = code
    }

    fun onTabSelected(tab: SearchTab) {
        _selectedTab.value = tab
        search()
    }

    fun search() {
        viewModelScope.launch {
            when (_selectedTab.value) {
                SearchTab.SEARCH -> {
                    val result = searchRepository.getSearchList(
                        query = _searchQuery.value.takeIf { it.isNotBlank() },
                        warehouseCode = _warehouseCode.value.takeIf { it.isNotBlank() }
                    )
                    _searchList.value = result
                }
                SearchTab.INVENTORY -> {
                    val result = searchRepository.getInventoryList(
                        query = _searchQuery.value.takeIf { it.isNotBlank() },
                        warehouseCode = _warehouseCode.value.takeIf { it.isNotBlank() }
                    )
                    _inventoryList.value = result
                }
            }
        }
    }
}
