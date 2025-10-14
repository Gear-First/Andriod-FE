package com.ljs.and.ui.releasing

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
fun ReleasingScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { ReleasingTopAppBar() }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ReleasingStatusTabs(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
            when (selectedTabIndex) {
                0 -> RequestedScreen()
                1 -> PickingScreen(navController = navController)
                2 -> ReleaseCompletedScreen()
            }
        }
    }
}

// Top App Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleasingTopAppBar() {
    TopAppBar(
        title = { Text("출고", fontWeight = FontWeight.Bold) },
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
fun ReleasingStatusTabs(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val statuses = listOf("출고 요청", "피킹 중", "완료")
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

// 1. Requested Screen
@Composable
fun RequestedScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dummyRequestedList) { item ->
            RequestedCard(item = item)
        }
    }
}

@Composable
fun RequestedCard(item: ReleaseRequest) {
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
                    imageVector = Icons.Default.Build,
                    contentDescription = "Delivery Truck",
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("요청 업체: ${item.requester}", fontWeight = FontWeight.Bold)
                    Text("날짜: ${item.date}", fontSize = 14.sp, color = Color.Gray)
                    Text("품목: ${item.itemCount}개", fontSize = 14.sp, color = Color.Gray)
                }
            }
            Button(
                onClick = { /* 출고 시작 */ },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("출고 시작")
            }
        }
    }
}

// 2. Picking Screen
@Composable
fun PickingScreen(navController: NavController) {
    val requester = "현대 모비스"
    val date = "2025.10.13"

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "요청 업체: $requester", fontSize = 14.sp, color = Color.Gray)
            Text(text = "날짜: $date", fontSize = 14.sp, color = Color.Gray)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyPickingList) { item ->
                PickingItemCard(item = item, onClick = {
                    navController.navigate(Screen.BarcodeScan.createRoute("releasing"))
                })
            }
        }
        Button(
            onClick = { /* 피킹 완료 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("피킹 완료", fontSize = 16.sp)
        }
    }
}

@Composable
fun PickingItemCard(item: PickingItem, onClick: () -> Unit) {
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
                Text("[출고 번호] ${item.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
fun ReleaseCompletedScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dummyReleaseCompletedList) { item ->
            ReleaseCompletedCard(item = item)
        }
    }
}

@Composable
fun ReleaseCompletedCard(item: CompletedRelease) {
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
                    imageVector = Icons.Default.Build,
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
        "피킹 중" -> Triple(Color.White, Color.Red, Color.Red)
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
data class ReleaseRequest(val requester: String, val date: String, val itemCount: Int)
data class PickingItem(
    val id: String,
    val supplier: String,
    val partName: String,
    val location: String,
    val quantity: Int,
    val manager: String,
    val status: String
)
data class CompletedRelease(val supplier: String, val itemCount: Int, val manager: String)

// Dummy Data
val dummyRequestedList = listOf(
    ReleaseRequest("현대 모비스", "2025.10.13", 23),
    ReleaseRequest("현대 오토에버", "2025.10.13", 23)
)

val dummyPickingList = listOf(
    PickingItem("OUT - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "피킹 중"),
    PickingItem("OUT - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "완료")
)

val dummyReleaseCompletedList = listOf(
    CompletedRelease("현대 모비스", 23, "이지수")
)



//----------------------------------------------------------

// Preview
@Preview(showBackground = true, name = "Requested Screen")
@Composable
fun RequestedScreenPreview() {
    MaterialTheme {
        RequestedScreen()
    }
}

@Preview(showBackground = true, name = "Picking Screen")
@Composable
fun PickingScreenPreview() {
     MaterialTheme {
        PickingScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Completed Screen")
@Composable
fun ReleaseCompletedScreenPreview() {
     MaterialTheme {
        ReleaseCompletedScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ReleasingScreenPreview() {
    MaterialTheme {
        ReleasingScreen(navController = rememberNavController())
    }
}
