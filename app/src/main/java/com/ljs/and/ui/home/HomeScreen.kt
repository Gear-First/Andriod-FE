package com.ljs.and.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen
import com.ljs.and.ui.theme.AndTheme
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(onNotificationClick = { viewModel.onEvent(HomeEvent.ShowNotificationDialog) })
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
            GreetingSection(
                userName = uiState.userName,
                selectedDate = uiState.selectedDate,
                onDateClick = { viewModel.onEvent(HomeEvent.ShowDatePicker) }
            )
            StatusCards(navController, uiState.status, uiState.isTodaySelected, uiState.selectedDate)
            ChartSection { chartType -> viewModel.onEvent(HomeEvent.ShowChart(chartType)) }
            QuickActions(navController)
        }
    }

    if (uiState.isDatePickerVisible) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { viewModel.onEvent(HomeEvent.HideDatePicker) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onEvent(HomeEvent.DateSelected(it))
                        }
                    }
                ) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { viewModel.onEvent(HomeEvent.HideDatePicker) }) { Text("취소") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    uiState.visibleChart?.let {
        when (it) {
            ChartType.INVENTORY -> InventoryChartModal(
                inventoryData = uiState.inventoryItems,
                onDismiss = { viewModel.onEvent(HomeEvent.HideChart) }
            )
            ChartType.WEEKLY -> WeeklyInOutChartModal(
                weeklyData = uiState.weeklyInOutData,
                dateRange = uiState.weeklyChartDateRange,
                onDismiss = { viewModel.onEvent(HomeEvent.HideChart) }
            )
        }
    }

    if (uiState.isNotificationDialogVisible) {
        NotificationDialog(
            notifications = uiState.notifications,
            onDismiss = { viewModel.onEvent(HomeEvent.HideNotificationDialog) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(onNotificationClick: () -> Unit) {
    TopAppBar(
        title = { Text("Gear First", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF111827)) },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F7))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreetingSection(userName: String, selectedDate: String, onDateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("안녕하세요, ${userName}님!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onDateClick)
        ) {
            Text(selectedDate, fontSize = 16.sp, color = Color.Gray)
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select Date")
        }
    }
}

@Composable
fun StatusCards(navController: NavController, status: StatusData, isTodaySelected: Boolean, selectedDate: String) {
    val dateText = if (isTodaySelected) {
        "오늘"
    } else {
        try {
            val parser = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val formatter = SimpleDateFormat("M월 d일", Locale.getDefault())
            val date = parser.parse(selectedDate)
            if (date != null) formatter.format(date) else "선택일"
        } catch (e: Exception) {
            "선택일" // Fallback
        }
    }
    val inboundText = "$dateText 입고 예정"
    val outboundText = "$dateText 출고 예정"

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusCard(
                title = inboundText,
                count = "${status.inboundCount}건",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.ReceivingHome.route) }
            )
            StatusCard(
                title = outboundText,
                count = "${status.outboundCount}건",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.ReleasingHome.route) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusCard(
                title = "부족 재고 품목",
                count = "${status.lowStockCount}개",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.InventoryHome.createRoute(filter = "부족")) }
            )
            StatusCard(
                title = "재고 신청 리스트",
                count = "${status.requestCount}개",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.InventoryHome.createRoute(filter = "재고신청")) }
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
fun ChartSection(onChartClick: (ChartType) -> Unit) {
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
                icon = Icons.Default.DonutLarge,
                label = "품목별 재고",
                onClick = { onChartClick(ChartType.INVENTORY) }
            )
            ChartItem(
                icon = Icons.Default.Equalizer,
                label = "주간 입출고",
                onClick = { onChartClick(ChartType.WEEKLY) }
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
fun NotificationDialog(notifications: List<NotificationItem>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                notifications.forEach { notification ->
                    NotificationListItem(notification)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = Color(0xFF007BFF))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun NotificationListItem(notification: NotificationItem) {
    val icon = when (notification.type) {
        NotificationType.NOTICE -> Icons.Default.Info
        NotificationType.INBOUND -> Icons.Default.ArrowBack
        NotificationType.STOCK_ALERT -> Icons.Default.Warning
        NotificationType.OUTBOUND -> Icons.Default.ArrowForward
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(notification.content, fontSize = 14.sp)
        }
        Text(
            text = notification.time,
            fontSize = 12.sp,
            color = Color.Gray,
        )
    }
}

@Composable
fun QuickActions(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickAction(
            icon = Icons.Default.QrCodeScanner,
            label = "재고신청(QR)",
            onClick = { navController.navigate(Screen.BarcodeScan.route) },
            modifier = Modifier.weight(1f)
        )
        QuickAction(
            icon = Icons.Default.Inventory,
            label = "재고 조회",
            onClick = { navController.navigate(Screen.Search.createRoute("inventory")) },
            modifier = Modifier.weight(1f)
        )
        QuickAction(
            icon = Icons.Default.Search,
            label = "입출고 조회",
            onClick = { navController.navigate(Screen.Search.createRoute("search")) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 16.sp, textAlign = TextAlign.Center)
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
