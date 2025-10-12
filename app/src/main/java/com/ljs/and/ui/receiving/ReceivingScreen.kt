package com.ljs.and.ui.receiving

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
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
import com.ljs.and.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingScreen() {
    Scaffold(
        topBar = { ReceivingTopAppBar() },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* 새 입고 등록 */ }) {
                Icon(Icons.Filled.Add, contentDescription = "New Receiving")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            StatusTabs()
            DateFilter("2025.10.08")
            ReceivingList()
        }
    }
}

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

@Composable
fun StatusTabs() {
    val statuses = listOf("전체", "입고 대기", "검수 중", "완료")
    var selectedTabIndex by remember { mutableStateOf(1) }

    TabRow(selectedTabIndex = selectedTabIndex) {
        statuses.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(title) }
            )
        }
    }
}

@Composable
fun DateFilter(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = date, fontSize = 14.sp, color = Color.Gray)
    }
}

// Dummy data for preview
data class ReceivingItem(
    val id: String,
    val supplier: String,
    val partName: String,
    val location: String,
    val quantity: Int,
    val manager: String,
    val status: String,
    val imageUrl: Int? = null
)

val dummyReceivingList = listOf(
    ReceivingItem("IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "완료", R.drawable.ic_engine_oil),
    ReceivingItem("IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "입고 대기", null),
    ReceivingItem("IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "담당자", "검수 중", null)
)

@Composable
fun ReceivingList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dummyReceivingList) { item ->
            ReceivingItemCard(item = item)
        }
    }
}

@Composable
fun ReceivingItemCard(item: ReceivingItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                item.imageUrl?.let {
                    Image(
                        painter = painterResource(id = it),
                        contentDescription = item.partName,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                StatusButton(status = item.status)
            }
        }
    }
}

@Composable
fun StatusButton(status: String) {
    val (text, containerColor, contentColor) = when (status) {
        "완료" -> Triple(status, Color(0xFFE0E0E0), Color.Black)
        "입고 대기" -> Triple(status, Color.White, Color.Blue)
        "검수 중" -> Triple(status, Color.White, Color(0xFFFFA500)) // Orange
        else -> Triple(status, Color.Gray, Color.White)
    }

    Button(
        onClick = { /* 상태 변경 로직 */ },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(text)
    }
}


@Preview(showBackground = true)
@Composable
fun ReceivingScreenPreview() {
    ReceivingScreen()
}
