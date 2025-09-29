package com.ljs.and.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ljs.and.data.InventoryItem
import com.ljs.and.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InventoryViewModel: ViewModel() {
    private val _items = MutableStateFlow<List<InventoryItem>>(emptyList())
    val items: StateFlow<List<InventoryItem>> = _items

    init {
        fetchItems()
    }

    private fun fetchItems() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getItems()
                if (response.isSuccessful) {
                    _items.value = response.body() ?: emptyList()
                } else {
                    _items.value = emptyList()
                }
            } catch (e: Exception) {
                _items.value = emptyList()
            }
        }
    }
}