
package com.ljs.and.ui.receiving

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.R
import com.ljs.and.ui.Screen

data class InspectingItem(
    val id: String,
    val supplier: String,
    val partName: String,
    val location: String,
    val quantity: Int,
    val manager: String,
    var status: String,
    val imageUrl: Int? = null
)

val dummyInspectingList = mutableStateListOf(
    InspectingItem("IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "검수 중", R.drawable.ic_launcher_background),
    InspectingItem("IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "완료", R.drawable.ic_launcher_background)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingInspectionScreen(navController: NavController, supplier: String, date: String) {
    val allItemsCompleted = dummyInspectingList.all { it.status == "완료" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("입고", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            InspectionBottomBar(
                onCancel = {  navController.navigate(Screen.Receiving.route) },
                onComplete = {
                    if (allItemsCompleted) {
                        navController.navigate(Screen.Receiving.route) { // Navigate back to the main Receiving screen
                            popUpTo(Screen.Receiving.route) { inclusive = true }
                        }
                    } else {
                        // Optional: Show a message to the user that not all items are completed
                    }
                },
                isCompleteEnabled = allItemsCompleted
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color.White) ) {
            InspectionHeader(supplier = supplier, date = date)
            InspectionList(navController = navController, items = dummyInspectingList)
        }
    }
}

@Composable
fun InspectionHeader(supplier: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("공급 업체: $supplier", fontSize = 14.sp, color = Color.Gray)
            Text("날짜: $date", fontSize = 14.sp, color = Color.Gray)
        }
        OutlinedButton(
            onClick = {},
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF007BFF))
        ) {
            Text("검수 중", color = Color(0xFF007BFF))
        }
    }
}

@Composable
fun InspectionList(navController: NavController, items: MutableList<InspectingItem>) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            InspectionItemCard(
                item = item,
                status = item.status,
                onClick = {
                    if (item.status != "완료") {
                        navController.navigate(Screen.BarcodeScan.route)
                    }
                }
            )
        }
    }
}

@Composable
fun InspectionItemCard(item: InspectingItem, status: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("[입고 번호] ${item.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("공급 업체: ${item.supplier}", fontSize = 13.sp)
                Text("부품: ${item.partName}", fontSize = 13.sp)
                Text("위치: ${item.location}, 수량: ${item.quantity}", fontSize = 13.sp)
                Text("담당자: ${item.manager}", fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                item.imageUrl?.let {
                    Image(
                        painter = painterResource(id = it),
                        contentDescription = "Product Image",
                        modifier = Modifier.size(50.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                StatusButton(status = status, onClick = onClick)
            }
        }
    }
}

@Composable
fun StatusButton(status: String, onClick: () -> Unit) {
    val (text, color, textColor) = if (status == "검수 중") {
        Triple("검수 중", Color.White, Color.Red)
    } else {
        Triple("완료", Color(0xFFE0E0E0), Color.Black)
    }
    val border = BorderStroke(1.dp, if (status == "검수 중") Color.Red else Color.LightGray)

    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = border,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = color),
        enabled = status != "완료"
    ) {
        Text(text, color = textColor)
    }
}


@Composable
fun InspectionBottomBar(onCancel: () -> Unit, onComplete: () -> Unit, isCompleteEnabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Text("취소", color = Color.Black)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = onComplete,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCompleteEnabled) Color(0xFF007BFF) else Color.LightGray
            ),
            enabled = isCompleteEnabled
        ) {
            Text("검수 완료", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReceivingInspectionScreenPreview() {
    MaterialTheme {
        ReceivingInspectionScreen(navController = rememberNavController(), supplier = "현대 모비스", date = "2025.10.13")
    }
}
