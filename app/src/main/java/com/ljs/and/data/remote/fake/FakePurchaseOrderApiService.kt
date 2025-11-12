package com.ljs.and.data.remote.fake

import com.ljs.and.data.model.ApiResponse
import com.ljs.and.data.model.PaginatedData
import com.ljs.and.data.model.PurchaseOrder
import com.ljs.and.data.model.PurchaseOrderItem
import com.ljs.and.data.model.PurchaseOrderRequest
import com.ljs.and.data.model.PurchaseOrderResponse
import com.ljs.and.data.remote.PurchaseOrderApiService
import kotlinx.coroutines.delay

class FakePurchaseOrderApiService : PurchaseOrderApiService {

    private val dummyOrders: MutableList<PurchaseOrder>

    init {
        val dummyItems = listOf(
            PurchaseOrderItem(
                id = 1L,
                partName = "Dummy Part A",
                partCode = "DPA-001",
                price = 1000,
                quantity = 10,
                totalPrice = 10000L
            ),
            PurchaseOrderItem(
                id = 2L,
                partName = "Dummy Part B",
                partCode = "DPB-002",
                price = 2500,
                quantity = 5,
                totalPrice = 12500L
            )
        )

        dummyOrders = mutableListOf(
            PurchaseOrder(
                orderId = 101L,
                orderNumber = "DUMMY-ORD-101",
                status = "APPROVED", // UI 코드와 상태 문자열 일치
                totalPrice = 22500L,
                requestDate = "2024-08-01",
                items = dummyItems
            ),
            PurchaseOrder(
                orderId = 102L,
                orderNumber = "DUMMY-ORD-102",
                status = "PENDING", // UI 코드와 상태 문자열 일치
                totalPrice = 50000L,
                requestDate = "2024-08-02",
                items = dummyItems.map { it.copy(id = it.id + 2, quantity = it.quantity * 2, totalPrice = it.totalPrice * 2) }
            ),
            PurchaseOrder(
                orderId = 103L,
                orderNumber = "DUMMY-ORD-103",
                status = "REJECTED", // UI 코드와 상태 문자열 일치
                totalPrice = 15000L,
                requestDate = "2024-07-30",
                items = dummyItems.take(1)
            )
        )
    }

    override suspend fun createPurchaseOrder(
        userId: Long?,
        username: String?,
        rank: String?,
        region: String?,
        workType: String?,
        purchaseOrderRequest: PurchaseOrderRequest
    ): ApiResponse<PurchaseOrderResponse> {
        delay(500)

        val newOrderId = (dummyOrders.maxOfOrNull { it.orderId } ?: 0L) + 1
        val newOrderNumber = "PO-DUMMY-${newOrderId}"
        
        val newOrderItems = purchaseOrderRequest.items.mapIndexed { index, item ->
            PurchaseOrderItem(
                id = newOrderId * 10 + index,
                partName = item.partName,
                partCode = item.partCode,
                price = item.price,
                quantity = item.quantity,
                totalPrice = (item.price * item.quantity).toLong()
            )
        }

        val newPurchaseOrder = PurchaseOrder(
            orderId = newOrderId,
            orderNumber = newOrderNumber,
            status = "PENDING", // UI 코드와 상태 문자열 일치
            totalPrice = newOrderItems.sumOf { it.totalPrice },
            requestDate = java.time.LocalDate.now().toString(),
            items = newOrderItems
        )

        dummyOrders.add(0, newPurchaseOrder)

        return ApiResponse(
            status = 200,
            success = true,
            message = "Success",
            data = PurchaseOrderResponse(
                orderId = newOrderId,
                orderNumber = newOrderNumber,
                totalQuantity = purchaseOrderRequest.items.sumOf { it.quantity },
                orderStatus = "PENDING"
            )
        )
    }

    override suspend fun getBranchOrders(
        userId: Long?,
        username: String?,
        rank: String?,
        region: String?,
        workType: String?,
        startDate: String?,
        endDate: String?,
        page: Int?,
        size: Int?
    ): ApiResponse<PaginatedData<PurchaseOrder>> {
        delay(500)

        val p = page ?: 0
        val s = size ?: 10
        val paginatedOrders = dummyOrders.drop(p * s).take(s)

        val paginatedData = PaginatedData(
            content = paginatedOrders,
            pageNumber = p,
            pageSize = s,
            totalElements = dummyOrders.size.toLong(),
            totalPages = (dummyOrders.size + s - 1) / s
        )

        return ApiResponse(
            status = 200,
            success = true,
            message = "Success",
            data = paginatedData
        )
    }

    override suspend fun updateOrderStatus(orderId: Long, status: String): ApiResponse<PurchaseOrder> {
        delay(300)
        val orderIndex = dummyOrders.indexOfFirst { it.orderId == orderId }
        if (orderIndex != -1) {
            val oldOrder = dummyOrders[orderIndex]
            val updatedOrder = oldOrder.copy(status = status)
            dummyOrders[orderIndex] = updatedOrder
            return ApiResponse(200, true, "Status updated", updatedOrder)
        } else {
            return ApiResponse(404, false, "Order not found", null)
        }
    }
}
