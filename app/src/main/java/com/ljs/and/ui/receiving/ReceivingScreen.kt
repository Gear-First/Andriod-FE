package com.ljs.and.ui.receiving

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen

// Main Screen Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { ReceivingTopAppBar() }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ReceivingStatusTabs(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
            when (selectedTabIndex) {
                0 -> PendingScreen()
                1 -> InspectingScreen(navController = navController)
                2 -> CompletedScreen()
            }
        }
    }
}

// Top App Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingTopAppBar() {
    TopAppBar(
        title = { Text("입고", fontWeight = FontWeight.Bold) },
        actions = {
            IconButton(onClick = { /* 검색 */ }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
            IconButton(onClick = { /* 정렬/필터 */ }) {
                Icon(Icons.Default.Menu, contentDescription = "Sort/Filter")
            }
        }
    )
}

// Status Tabs
@Composable
fun ReceivingStatusTabs(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val statuses = listOf("입고 대기", "검수 중", "완료")
    TabRow(selectedTabIndex = selectedTabIndex) {
        statuses.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(title) }
            )
        }
    }
}

// 1. Pending Screen
@Composable
fun PendingScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dummyPendingList) { item ->
            PendingCard(item = item)
        }
    }
}

@Composable
fun PendingCard(item: PendingDeliveryNote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = "Delivery Truck",
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("공급 업체: ${item.supplier}", fontWeight = FontWeight.Bold)
                    Text("날짜: ${item.date}", fontSize = 14.sp, color = Color.Gray)
                    Text("품목: ${item.itemCount}개", fontSize = 14.sp, color = Color.Gray)
                }
            }
            Button(
                onClick = { /* 검수 시작 */ },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("검수 시작")
            }
        }
    }
}

// 2. Inspecting Screen
@Composable
fun InspectingScreen(navController: NavController) {
    val supplier = "현대 모비스"
    val date = "2025.10.13"

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "공급 업체: $supplier", fontSize = 14.sp, color = Color.Gray)
            Text(text = "날짜: $date", fontSize = 14.sp, color = Color.Gray)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyInspectingList) { item ->
                InspectingItemCard(item = item, onClick = {
                    navController.navigate(Screen.BarcodeScan.createRoute("receiving"))
                })
            }
        }
        Button(
            onClick = { /* 검수 완료 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("검수 완료", fontSize = 16.sp)
        }
    }
}

@Composable
fun InspectingItemCard(item: InspectingItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Placeholder for an image
                Spacer(modifier = Modifier.height(8.dp))
                StatusPillButton(status = item.status)
            }
        }
    }
}

// 3. Completed Screen
@Composable
fun CompletedScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dummyCompletedList) { item ->
            CompletedCard(item = item)
        }
    }
}

@Composable
fun CompletedCard(item: CompletedDeliveryNote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = "Delivery Truck",
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("공급 업체: ${item.supplier}", fontWeight = FontWeight.Bold)
                    Text("품목: ${item.itemCount}개", fontSize = 14.sp, color = Color.Gray)
                    Text("담당자: ${item.manager}", fontSize = 14.sp, color = Color.Gray)
                }
            }
            StatusPillButton(status = "완료")
        }
    }
}

@Composable
fun StatusPillButton(status: String) {
    val (containerColor, contentColor, borderColor) = when (status) {
        "완료" -> Triple(Color(0xFFF0F0F0), Color.Black, Color.Transparent)
        "검수 중" -> Triple(Color.White, Color.Red, Color.Red)
        else -> Triple(Color.Gray, Color.White, Color.Transparent)
    }

    Button(
        onClick = { },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null
    ) {
        Text(status)
    }
}


// Data classes
data class PendingDeliveryNote(val supplier: String, val date: String, val itemCount: Int)
data class InspectingItem(
    val id: String,
    val supplier: String,
    val partName: String,
    val location: String,
    val quantity: Int,
    val manager: String,
    val status: String
)
data class CompletedDeliveryNote(val supplier: String, val itemCount: Int, val manager: String)

// Dummy Data
val dummyPendingList = listOf(
    PendingDeliveryNote("현대 모비스", "2025.10.13", 23),
    PendingDeliveryNote("현대 오토에버", "2025.10.13", 23)
)

val dummyInspectingList = listOf(
    InspectingItem("IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "검수 중"),
    InspectingItem("IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "완료")
)

val dummyCompletedList = listOf(
    CompletedDeliveryNote("현대 모비스", 23, "이지수")
)



//-------------------------------------------------------

// Preview
@Preview(showBackground = true, name = "Pending Screen")
@Composable
fun PendingScreenPreview() {
    MaterialTheme {
        PendingScreen()
    }
}

@Preview(showBackground = true, name = "Inspecting Screen")
@Composable
fun InspectingScreenPreview() {
     MaterialTheme {
        InspectingScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Completed Screen")
@Composable
fun CompletedScreenPreview() {
     MaterialTheme {
        CompletedScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ReceivingScreenPreview() {
    MaterialTheme {
        ReceivingScreen(navController = rememberNavController())
    }
}
