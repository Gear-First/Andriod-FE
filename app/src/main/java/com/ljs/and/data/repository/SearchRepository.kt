package com.ljs.and.data.repository

import com.ljs.and.data.model.InventoryItem
import com.ljs.and.data.model.SearchItem
import com.ljs.and.data.remote.SearchApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SearchRepository(private val searchApiService: SearchApiService) {

    suspend fun getSearchList(query: String?, warehouseCode: String?): List<SearchItem> = coroutineScope {
        val receivingNotesDeferred = async {
            searchApiService.getReceivingNotes(
                query = query,
                warehouseCode = warehouseCode,
                status = null,
                dateFrom = null,
                dateTo = null
            )
        }
        val shippingNotesDeferred = async {
            searchApiService.getShippingNotes(
                query = query,
                warehouseCode = warehouseCode,
                status = null,
                dateFrom = null,
                dateTo = null
            )
        }

        val receivingItems = receivingNotesDeferred.await().data?.items?.map {
            SearchItem(
                type = "입고",
                no = it.receivingNo,
                partnerName = it.supplierName,
                warehouseCode = it.warehouseCode,
                status = it.status,
                requestedAt = it.requestedAt
            )
        } ?: emptyList()

        val shippingItems = shippingNotesDeferred.await().data?.items?.map {
            SearchItem(
                type = "출고",
                no = it.shippingNo,
                partnerName = it.branchName,
                warehouseCode = it.warehouseCode,
                status = it.status,
                requestedAt = it.requestedAt
            )
        } ?: emptyList()

        (receivingItems + shippingItems).sortedByDescending { it.requestedAt }
    }

    suspend fun getInventoryList(query: String?, warehouseCode: String?): List<InventoryItem> {
        return searchApiService.getInventoryOnHand(
            query = query,
            warehouseCode = warehouseCode,
            minQty = null,
            maxQty = null
        ).data?.items ?: emptyList()
    }
}
