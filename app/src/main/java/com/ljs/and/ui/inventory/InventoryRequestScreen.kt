package com.ljs.and.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ljs.and.data.InventoryRequest
import com.ljs.and.data.RequestStatus
import com.ljs.and.ui.Screen

@Composable
fun InventoryRequestScreen(navController: NavHostController) {
    InventoryRequestContent(onNavigateToForm = { navController.navigate(Screen.InventoryRequestForm.route) })
}

@Composable
fun InventoryRequestContent(onNavigateToForm: () -> Unit) {
    val sampleRequests = listOf(
        InventoryRequest(1, "엔진오일", 23, "2025.10.16", "불량", RequestStatus.PENDING),
        InventoryRequest(2, "엔진오일", 23, "2025.10.16", "불량", RequestStatus.APPROVED)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("재고 신청 리스트", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                InventoryRequestFilterDropDown()
            }
            Spacer(modifier = Modifier.height(16.dp))
            InventoryRequestList(requests = sampleRequests)
        }

        Button(
            onClick = onNavigateToForm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("재고 신청", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun InventoryRequestFilterDropDown() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("전체") }
    val options = listOf("전체", "대기", "승인", "완료")

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selectedOptionText, fontSize = 16.sp)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Filter Options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOptionText = option
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun InventoryRequestList(requests: List<InventoryRequest>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(requests) { request ->
            InventoryRequestCard(request = request)
        }
    }
}

@Composable
fun InventoryRequestCard(request: InventoryRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("품목명: ${request.itemName}", fontWeight = FontWeight.Bold)
                Text("수량: ${request.quantity}개")
                Text("요청일: ${request.requestDate}")
                Text("상태: ${request.reason}")
            }

            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red),
                ) {
                    Text(
                        text = "취소",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
            }
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End,
            ) {
                val statusColor = if (request.status == RequestStatus.PENDING) Color.Red else MaterialTheme.colorScheme.primary
                OutlinedButton(
                    onClick = { /* Do nothing */ },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, statusColor),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Text(request.status.displayName, color = statusColor)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryRequestScreenPreview() {
    InventoryRequestContent(onNavigateToForm = {})
}

@Preview(showBackground = true)
@Composable
fun InventoryRequestCardPreview() {
    val sampleRequest = InventoryRequest(1, "엔진오일", 23, "2025.10.16", "불량", RequestStatus.PENDING)
    InventoryRequestCard(request = sampleRequest)
}
