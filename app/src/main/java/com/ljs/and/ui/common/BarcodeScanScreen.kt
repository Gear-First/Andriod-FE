package com.ljs.and.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen


@Composable
fun BarcodeScanScreen(
    navController: NavController, 
    flowType: String,
    noteId: Long, 
    lineId: Long,
    currentQty: Int,
    orderedQty: Int,
    lineRemark: String?
) {
    val initialTabIndex = if (flowType == "releasing") 1 else 0
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("입고", "출고")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "QR 인식",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        ScanModeTabs(selectedTabIndex = selectedTabIndex, tabs = tabs, onTabSelected = { selectedTabIndex = it })

        Spacer(modifier = Modifier.height(40.dp))

        CameraPreview()

        Spacer(modifier = Modifier.weight(1f))

        ScanActionButtons(
            onCancel = { navController.popBackStack() },
            onManualInput = {
                val targetLineId = if(lineId == -1L) 0L else lineId

                navController.navigate(
                    Screen.ManualInput.createRoute(
                        flowType = if (selectedTabIndex == 0) "receiving" else "releasing",
                        noteId = noteId, 
                        lineId = targetLineId, 
                        currentQty = currentQty,
                        orderedQty = orderedQty,
                        lineRemark = lineRemark
                    )
                )
            },
            modifier = Modifier.padding(bottom = 40.dp)
        )
    }
}

@Composable
fun ScanModeTabs(selectedTabIndex: Int, tabs: List<String>, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(48.dp)
            .clip(RoundedCornerShape(30))
            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(30))
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            val backgroundColor = if (isSelected) Color(0xFF007BFF) else Color.White
            val textColor = if (isSelected) Color.White else Color.Black

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(backgroundColor)
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = title, color = textColor)
            }

            if (index == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color.LightGray)
                )
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
fun ScanActionButtons(onCancel: () -> Unit, onManualInput: () -> Unit, modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
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
        BarcodeScanScreen(navController = rememberNavController(), flowType = "receiving", noteId = -1L, lineId = -1L, currentQty = 0, orderedQty = 0, lineRemark = null)
    }
}
