
package com.ljs.and.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen
import com.ljs.and.ui.theme.AndTheme

internal enum class RequestStatus(val displayName: String) {
    PENDING("대기"),
    APPROVED("승인")
}

internal data class InventoryRequest(
    val id: Int,
    val itemName: String,
    val quantity: Int,
    val requestDate: String,
    val reason: String,
    val status: RequestStatus,
    val isCanceled: Boolean = false,
)

private val dummyRequestList = listOf(
    InventoryRequest(1, "엔진오일", 23, "2025.10.16", "불량", RequestStatus.PENDING, isCanceled = true),
    InventoryRequest(2, "엔진오일", 23, "2025.10.16", "불량", RequestStatus.APPROVED)
)

@Composable
fun InventoryRequestScreen(navController: NavHostController) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
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
                    RequestFilterDropdown()
                }
            }
            items(dummyRequestList) { request ->
                InventoryRequestCard(request = request)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RequestFilterDropdown() {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("전체", "대기", "승인")
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .menuAnchor()
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedOptionText, fontWeight = FontWeight.Bold)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 80.dp).background(Color.White)
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        selectedOptionText = selectionOption
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
internal fun InventoryRequestCard(request: InventoryRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("품목명: ${request.itemName}", fontWeight = FontWeight.Bold)
                Text("수량: ${request.quantity}개", color = Color.Gray)
                Text("요청일: ${request.requestDate}", color = Color.Gray)
                Text("상태: ${request.reason}", color = Color.Gray)
            }

            if (request.isCanceled) {
                Card(
                    modifier = Modifier.align(Alignment.TopEnd),
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

            val statusButtonColor = when(request.status) {
                RequestStatus.PENDING -> Color.Red
                RequestStatus.APPROVED -> Color(0xFF007BFF)
            }
            OutlinedButton(
                onClick = { /* Status change? */ },
                modifier = Modifier.align(Alignment.BottomEnd),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, statusButtonColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = statusButtonColor)
            ) {
                Text(request.status.displayName)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryRequestScreenPreview() {
    AndTheme {
        InventoryRequestScreen(navController = rememberNavController())
    }
}
