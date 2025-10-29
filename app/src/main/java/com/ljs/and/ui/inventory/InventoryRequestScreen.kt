package com.ljs.and.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen
//import com.ljs.and.data.InventoryRequest
//import com.ljs.and.data.RequestStatus
import com.ljs.and.ui.theme.AndTheme

@Composable
fun InventoryRequestScreen(
    navController: NavHostController,
    viewModel: InventoryViewModel = viewModel()
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<InventoryRequest?>(null) }

    val requestState by viewModel.requestState.collectAsState()

    val filteredList = remember(requestState.selectedFilter, requestState.requestList) {
        requestState.requestList.filter { request ->
            !request.isCanceled && when (requestState.selectedFilter) {
                "대기" -> request.status == RequestStatus.PENDING
                "승인" -> request.status == RequestStatus.APPROVED
//                "완료" -> request.status == RequestStatus.COMPLETED
                else -> true // "전체"
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F7)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = 0.dp
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp), // Button space
                contentPadding = PaddingValues(16.dp),
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
                            onOptionSelected = { viewModel.updateRequestFilter(it) }
                        )
                    }
                }
                items(filteredList) { request ->
                    InventoryRequestCard(
                        request = request,
                        onCancelClick = {
                            selectedRequest = request
                            showCancelDialog = true
                        }
                    )
                }
            }

            Button(
                onClick = { navController.navigate(Screen.InventoryRequestForm.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
            ) {
                Text("재고 신청", modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
            }
        }
    }

    if (showCancelDialog) {
        CancelRequestDialog(
            onConfirm = {
                selectedRequest?.let { viewModel.cancelRequest(it.id) }
                showCancelDialog = false
                selectedRequest = null
            },
            onDismiss = {
                showCancelDialog = false
                selectedRequest = null
            }
        )
    }
}

@Composable
fun CancelRequestDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림") },
        text = { Text("취소되었습니다.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("확인")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RequestFilterDropdown(selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("전체", "대기", "승인")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        Row(
            modifier = Modifier
                .menuAnchor()
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedOption, fontWeight = FontWeight.Bold)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(100.dp)
                .background(Color.White)
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
internal fun InventoryRequestCard(request: InventoryRequest, onCancelClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 120.dp) // Ensure minimum height for alignment
                .padding(16.dp)
        ) {
            // Main text content
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 80.dp), // Prevent text overlapping with buttons
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${request.itemName} / ${request.itemCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("신청 수량: ${request.quantity}개", fontSize = 14.sp, color = Color.Gray)
                Text("신청일자: ${request.requestDate}", fontSize = 14.sp, color = Color.Gray)
                Text("신청자: ${request.requester}", fontSize = 14.sp, color = Color.Gray)
            }

            // Cancel button (top right)
            if (request.status == RequestStatus.PENDING) {
                Button(
                    onClick = onCancelClick,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(24.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text("취소", color = Color.White, fontSize = 12.sp)
                }
            }

            // Status button (bottom right)
            StatusButton(
                status = request.status,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun StatusButton(status: RequestStatus, modifier: Modifier = Modifier) {
    val (text, color) = when (status) {
        RequestStatus.PENDING -> "대기" to Color.Red
        RequestStatus.APPROVED -> "승인" to Color(0xFF007BFF)
//        RequestStatus.COMPLETED -> "완료" to Color.Gray
    }

    OutlinedButton(
        onClick = { /* Status change? */ },
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        modifier = modifier
    ) {
        Text(text)
    }
}


@Preview(showBackground = true)
@Composable
fun InventoryRequestScreenPreview() {
    AndTheme {
        InventoryRequestScreen(navController = rememberNavController())
    }
}
