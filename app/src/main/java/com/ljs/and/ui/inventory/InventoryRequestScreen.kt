package com.ljs.and.ui.inventory

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ljs.and.data.model.PurchaseOrder
import com.ljs.and.ui.Screen
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
private fun formatRequestDateTime(dateTimeString: String?): String {
    if (dateTimeString == null) return "N/A"
    return try {
        val offsetDateTime = OffsetDateTime.parse(dateTimeString)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        offsetDateTime.format(formatter)
    } catch (e: Exception) {
        dateTimeString.substringBefore("T")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InventoryRequestScreen(
    navController: NavHostController,
    requestState: RequestState,
    onFilterChange: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 필터링 및 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp), // 버튼 공간 확보
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("신청 리스트", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        RequestFilterDropdown(
                            selectedOption = requestState.selectedFilter,
                            onOptionSelected = onFilterChange
                        )
                    }
                }
                if (requestState.isLoading) {
                    item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                } else if (requestState.errorMessage != null) {
                    item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text(text = "오류: ${requestState.errorMessage}") } }
                } else if (requestState.requestList.isEmpty()) {
                    item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("표시할 항목이 없습니다.") } }
                } else {
                    items(requestState.requestList) { order ->
                        InventoryRequestCard(order = order)
                    }
                }
            }
        }

        // 하단 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F7)) // ✅ 회색 배경
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = {
                    navController.navigate(Screen.InventoryRequestForm.route)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
            ) {
                Text("재고 신청", modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RequestFilterDropdown(selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("전체", "대기", "승인", "반려")

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        Row(
            modifier = Modifier.menuAnchor().clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedOption, fontWeight = FontWeight.Bold)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(100.dp).background(Color.White)
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = { onOptionSelected(selectionOption); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InventoryRequestCard(order: PurchaseOrder, onClick: () -> Unit = {}) {
    val item = order.items.firstOrNull()
    val totalQuantity = order.items.sumOf { it.quantity }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val title = when {
                    order.items.size > 1 -> "${item?.partName} 외 ${order.items.size - 1}건"
                    item != null -> "${item.partName} / ${item.partCode}"
                    else -> "부품 정보 없음"
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("접수 번호: ${order.orderNumber}", fontSize = 14.sp)
                Text("신청 수량: ${totalQuantity}개", fontSize = 12.sp, color = Color.Gray)
                Text("신청 일자: ${formatRequestDateTime(order.requestDate)}", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(60.dp).background(Color.Transparent, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {}
                StatusButton(status = order.status)
            }
        }
    }
}

@Composable
fun StatusButton(status: String, modifier: Modifier = Modifier) {
    val (text, color) = when (status) {
        "PENDING" -> "대기" to Color.Red
        "APPROVED" -> "승인" to Color(0xFF007BFF)
        "REJECTED" -> "반려" to Color.Gray
        else -> status to Color.Gray
    }
    OutlinedButton(
        onClick = { /* 상태 변경 기능 추가 시 구현 */ },
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        modifier = modifier
    ) {
        Text(text)
    }
}
