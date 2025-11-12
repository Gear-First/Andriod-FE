package com.ljs.and.ui.more

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// ---------------------- ENUM ---------------------- //
enum class NotificationType {
    NOTICE,
    INBOUND,
    STOCK_ALERT,
    OUTBOUND
}

// ---------------------- MAIN SCREEN ---------------------- //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController, viewModel: MoreViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.loadUserInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("더보기", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F7))
            )
        },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 유저 정보 카드
            UserInfoCard(uiState)

            // 입출고 로그
            val logs = remember { getDummyLogs() }
            ExpandableMoreItem(
                icon = Icons.Default.Inventory2,
                title = "입출고 로그"
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(logs) { log ->
                        val icon = when (log.type) {
                            NotificationType.INBOUND -> Icons.Default.ArrowBack
                            NotificationType.OUTBOUND -> Icons.Default.ArrowForward
                            else -> Icons.Default.Info
                        }
                        InfoListItem(
                            icon = icon,
                            title = log.title,
                            content = log.content,
                            time = log.timestamp
                        )
                    }
                }
            }

            // 전체 알림
            val notifications = remember { getDummyNotifications() }
            ExpandableMoreItem(
                icon = Icons.Outlined.Notifications,
                title = "전체 알림"
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(notifications) { notification ->
                        val icon = when (notification.type) {
                            NotificationType.NOTICE -> Icons.Default.Info
                            NotificationType.INBOUND -> Icons.Default.ArrowBack
                            NotificationType.STOCK_ALERT -> Icons.Default.Warning
                            NotificationType.OUTBOUND -> Icons.Default.ArrowForward
                        }
                        InfoListItem(
                            icon = icon,
                            title = notification.title,
                            content = notification.content,
                            time = notification.time
                        )
                    }
                }
            }
        }
    }
}

// ---------------------- USER INFO CARD ---------------------- //
@Composable
fun UserInfoCard(uiState: MoreUiState) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("이름: ${uiState.userName}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("이메일: ${uiState.email}", color = Color.Gray, fontSize = 14.sp)
                    Text("창고: ${uiState.warehouseName}", color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Default Profile Icon",
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { /* TODO: Implement logout */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Color.Red),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout Icon", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("로그아웃")
            }
        }
    }
}

// ---------------------- EXPANDABLE ITEM ---------------------- //
@Composable
fun ExpandableMoreItem(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Gray,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color.Gray,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
            if (expanded) {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                content()
            }
        }
    }
}

// ---------------------- INFO LIST ITEM ---------------------- //
@Composable
fun InfoListItem(icon: ImageVector, title: String, content: String, time: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF007BFF)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(content, fontSize = 14.sp)
        }
        Text(
            text = time,
            fontSize = 12.sp,
            color = Color.Gray,
        )
    }
}

// ---------------------- DUMMY DATA ---------------------- //
data class LogItem(val id: Int, val title: String, val content: String, val timestamp: String, val type: NotificationType)
data class NotificationItem(val id: Int, val title: String, val content: String, val time: String, val type: NotificationType)

fun getDummyLogs(): List<LogItem> {
    return (1..25).map { LogItem(it, "입고 완료", "상품 A, 수량: 100개", "${it}분 전", NotificationType.INBOUND) } +
            (26..50).map { LogItem(it, "출고 완료", "상품 B, 수량: 50개", "${it}분 전", NotificationType.OUTBOUND) }
}

fun getDummyNotifications(): List<NotificationItem> {
    return listOf(
        NotificationItem(1, "새로운 공지", "시스템 점검이 2024-09-15에 예정되어 있습니다.", "1시간 전", NotificationType.NOTICE),
        NotificationItem(3, "재고 부족 알림", "상품 '샘플-A'의 재고가 10개 미만입니다.", "1일 전", NotificationType.STOCK_ALERT),
    ) + (5..50).map { i ->
        NotificationItem(
            i,
            "서버 점검 안내",
            "2023년 11월 1일(수) 02:00 ~ 06:00 (4시간)",
            "$i" + "일 전",
            NotificationType.NOTICE
        )
    }
}

// ---------------------- PREVIEW ---------------------- //
@Preview(showBackground = true)
@Composable
fun MoreScreenPreview() {
    MaterialTheme {
        MoreScreen(navController = rememberNavController())
    }
}
