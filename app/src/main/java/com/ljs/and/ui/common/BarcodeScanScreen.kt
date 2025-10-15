package com.ljs.and.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
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


@Composable
fun BarcodeScanScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("입고", "출고")

    Column(
        modifier = Modifier
            .fillMaxSize()
            // .padding(16.dp) // 👈 Column 전체에 적용되던 padding 제거
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "QR 인식",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            // 👇 제목에만 위쪽과 좌우 여백(padding) 추가
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp) // 👈 변경됨
        )
        Spacer(modifier = Modifier.height(24.dp))

        ScanModeTabs(selectedTabIndex = selectedTabIndex, tabs = tabs, onTabSelected = { selectedTabIndex = it })

        Spacer(modifier = Modifier.height(40.dp))

        CameraPreview()

        Spacer(modifier = Modifier.weight(1f))

        ScanActionButtons(
            onCancel = { navController.popBackStack() },
            onManualInput = { navController.navigate(Screen.ManualInput.route) }
        )
    }
}

@Composable
fun ScanModeTabs(selectedTabIndex: Int, tabs: List<String>, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(0.7f),
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
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                border = border,
                modifier = Modifier.weight(1f)
            ) {
                Text(title)
            }
        }
    }
}

@Composable
fun CameraPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = "Camera Icon",
            modifier = Modifier.size(100.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ScanActionButtons(onCancel: () -> Unit, onManualInput: () -> Unit) {
    Row(
        // 👇 하단 버튼 Row에만 좌우, 아래 여백(padding) 추가
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp), // 👈 변경됨
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
            onClick = onManualInput,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
        ) {
            Text("수동 입력", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BarcodeScanScreenPreview() {
    MaterialTheme {
        BarcodeScanScreen(navController = rememberNavController())
    }
}