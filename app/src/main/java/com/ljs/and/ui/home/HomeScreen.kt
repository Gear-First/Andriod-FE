package com.ljs.and.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var showNotificationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HomeTopAppBar(onNotificationClick = { showNotificationDialog = true })
        },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GreetingSection()
            StatusCards(navController)
            ChartSection { dialogType -> showDialogType = dialogType }
            QuickActions(navController)
        }
    }

    when (showDialogType) {
        "inventory" -> InventoryChartModal { showDialogType = null }
        "weekly" -> WeeklyInOutChartModal { showDialogType = null }
    }

    if (showNotificationDialog) {
        NotificationDialog(onDismiss = { showNotificationDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(onNotificationClick: () -> Unit) {
    TopAppBar(
        title = { Text("Gear First", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF007BFF)) },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F7))
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
                title = "불량재고",
                count = "6개",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.InventoryRequestForm.route) }
            )
        }
    }
}

@Composable
fun StatusCard(title: String, count: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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

data class NotificationItem(val title: String, val content: String, val time: String)

@Composable
fun NotificationDialog(onDismiss: () -> Unit) {
    val notifications = remember {
        listOf(
            NotificationItem("새로운 공지", "시스템 점검이 2024-09-15에 예정되어 있습니다.", "1시간 전"),
            NotificationItem("입고 완료", "SKU-12345 상품이 입고되었습니다.", "3시간 전"),
            NotificationItem("재고 부족 알림", "상품 '''샘플-A'''의 재고가 10개 미만입니다.", "1일 전"),
            NotificationItem("출고 예정", "주문 #98765에 대한 상품 출고가 예정되어 있습니다.", "2일 전")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                notifications.forEach { notification ->
                    Column {
                        Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notification.content, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notification.time,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
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
            .size(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
    AndTheme {
        HomeScreen(navController = rememberNavController())
    }
}
