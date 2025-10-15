package com.ljs.and.ui.releasing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

// Data classes
data class PendingReleasingNote(val customer: String, val date: String, val itemCount: Int)

// Dummy Data
val dummyPendingList = listOf(
    PendingReleasingNote("현대 자동차", "2025.10.13", 15),
    PendingReleasingNote("기아", "2025.10.14", 8)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleasingScreen(navController: NavController, initialTabIndex: Int = 0) {
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("출고 대기", "완료")

    Scaffold(
        topBar = { ReleasingTopAppBar() },
        containerColor = Color.White
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ReleasingTabRow(
                selectedTabIndex = selectedTabIndex,
                tabs = tabs,
                onTabSelected = { selectedTabIndex = it }
            )
            when (selectedTabIndex) {
                0 -> PendingReleasingScreen(navController = navController, pendingList = dummyPendingList)
                1 -> ReleasingCompletedScreen(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleasingTopAppBar() {
    TopAppBar(
        title = { Text("출고", fontWeight = FontWeight.Bold) },
        actions = {
            IconButton(onClick = { /* TODO: 검색 기능 구현 */ }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun ReleasingTabRow(selectedTabIndex: Int, tabs: List<String>, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            val containerColor = if (isSelected) Color(0xFF007BFF) else Color.White
            val contentColor = if (isSelected) Color.White else Color.Black
            val border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray)

            Button(
                onClick = { onTabSelected(index) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                border = border,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(title)
            }
        }
    }
}


@Composable
fun PendingReleasingScreen(navController: NavController, pendingList: List<PendingReleasingNote>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(pendingList) { item ->
            PendingReleasingCard(item = item, onStartPicking = {
                navController.navigate(Screen.ReleasingPicking.createRoute(item.customer, item.date))
            })
        }
    }
}

@Composable
fun PendingReleasingCard(item: PendingReleasingNote, onStartPicking: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Build,
                    contentDescription = "Shipping Truck",
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("거래처: ${item.customer}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("날짜: ${item.date}", fontSize = 14.sp, color = Color.Gray)
                    Text("품목: ${item.itemCount}개", fontSize = 14.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onStartPicking,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Text("피킹 시작", color = Color.Black)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReleasingScreenPreview() {
    MaterialTheme {
        ReleasingScreen(navController = rememberNavController())
    }
}
