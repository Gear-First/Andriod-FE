package com.ljs.and.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen
import com.ljs.and.ui.theme.AndTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var showDialogType by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { HomeTopAppBar(scrollBehavior = scrollBehavior) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            GreetingSection()
            Spacer(modifier = Modifier.height(24.dp))
            StatusCards(navController)
            Spacer(modifier = Modifier.height(24.dp))
            ChartSection { dialogType -> showDialogType = dialogType }
            Spacer(modifier = Modifier.height(24.dp))
            QuickActions(navController)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    when (showDialogType) {
        "inventory" -> InventoryChartModal { showDialogType = null }
        "weekly" -> WeeklyInOutChartModal { showDialogType = null }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(scrollBehavior: TopAppBarScrollBehavior) {
    TopAppBar(
        title = { Text("Gear First", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF007BFF)) },
        actions = {
            IconButton(onClick = { /* 알림 화면으로 이동 */ }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun GreetingSection() {
    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    val currentDate = sdf.format(Date())

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("안녕하세요, 김창고님!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(currentDate, fontSize = 16.sp, color = Color.Gray)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Date")
        }
    }
}

@Composable
fun StatusCards(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusCard(
                title = "오늘 입고 예정",
                count = "12건",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Receiving.route) }
            )
            StatusCard(
                title = "오늘 출고 예정",
                count = "8건",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Releasing.route) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusCard(
                title = "부족 재고 품목",
                count = "3개",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Inventory.route) }
            )
            StatusCard(
                title = "처리 대기 품목",
                count = "6개",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.PendingItems.route) }
            )
        }
    }
}

@Composable
fun StatusCard(title: String, count: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(count, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChartSection(onChartClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChartItem(
                icon = Icons.Default.Build,
                label = "품목별 재고",
                onClick = { onChartClick("inventory") }
            )
            ChartItem(
                icon = Icons.Default.Build,
                label = "주간 입출고",
                onClick = { onChartClick("weekly") }
            )
        }
    }
}

@Composable
fun ChartItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(48.dp), tint = Color(0xFF007BFF))
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 16.sp)
    }
}

@Composable
fun QuickActions(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        QuickAction(
            icon = Icons.Default.Add,
            label = "입고 등록",
            onClick = { navController.navigate(Screen.BarcodeScan.createRoute("receiving")) }
        )
        QuickAction(
            icon = Icons.Default.Build,
            label = "출고 등록",
            onClick = { navController.navigate(Screen.BarcodeScan.createRoute("releasing")) }
        )
        QuickAction(
            icon = Icons.Default.Search,
            label = "재고 조회",
            onClick = { navController.navigate(Screen.InventoryRequestForm.route) }
        )
    }
}

@Composable
fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    AndTheme {
        HomeScreen(navController = rememberNavController())
    }
}
