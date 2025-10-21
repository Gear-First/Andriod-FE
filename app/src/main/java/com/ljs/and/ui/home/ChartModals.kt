package com.ljs.and.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// 1. Inventory Donut Chart Modal
data class InventoryItemData(val name: String, val quantity: Int, val color: Color)

val inventoryData = listOf(
    InventoryItemData("엔진오일", 17439, Color(0xFF0D47A1)),
    InventoryItemData("브레이크패드", 9478, Color(0xFF1976D2)),
    InventoryItemData("부동액", 18197, Color(0xFF2196F3)),
    InventoryItemData("타이어", 12510, Color(0xFF64B5F6)),
    InventoryItemData("필터", 14406, Color(0xFFBBDEFB))
)

@Composable
fun InventoryChartModal(onDismiss: () -> Unit) {
    val total = inventoryData.sumOf { it.quantity }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("2025.10.01 상위 품목", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                DonutChart(inventoryData, total)
                Spacer(modifier = Modifier.height(24.dp))
                InventoryLegend(inventoryData)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
                ) {
                    Text("확인", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun DonutChart(data: List<InventoryItemData>, total: Int) {
    val totalValue = data.sumOf { it.quantity }.toFloat()
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            data.forEach { item ->
                val sweepAngle = (item.quantity / totalValue) * 360f
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 60f)
                )
                startAngle += sweepAngle
            }
        }
        Text(text = "%,d".format(total), fontSize = 32.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InventoryLegend(data: List<InventoryItemData>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        data.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(item.color, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.name, modifier = Modifier.weight(1f), fontSize = 14.sp)
                Text("%,d".format(item.quantity), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// 2. Weekly In/Out Bar Chart Modal
data class InOutData(val day: String, val inbound: Float, val outbound: Float)

val weeklyData = listOf(
    InOutData("S", 30f, 25f),
    InOutData("M", 40f, 35f),
    InOutData("T", 60f, 45f),
    InOutData("W", 90f, 80f),
    InOutData("T", 50f, 40f),
    InOutData("F", 35f, 30f),
    InOutData("S", 25f, 20f),
)

@Composable
fun WeeklyInOutChartModal(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("2025.10.01 - 2025.10.08", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("주간 입출고 그래프", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                BidirectionalBarChart(weeklyData)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
                ) {
                    Text("확인", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun BidirectionalBarChart(data: List<InOutData>) {
    val maxVal = data.maxOfOrNull { maxOf(it.inbound, it.outbound) } ?: 1f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("입고", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach {
                Box(modifier = Modifier.width(20.dp).height((it.inbound / maxVal * 100).dp).background(Color(0xFF64B5F6), RoundedCornerShape(4.dp)))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
             data.forEach { Text(it.day, color = Color.Gray, fontSize = 12.sp) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
             data.forEach {
                Box(modifier = Modifier.width(20.dp).height((it.outbound / maxVal * 100).dp).background(Color(0xFF90CAF9), RoundedCornerShape(4.dp)))
            }
        }
         Spacer(modifier = Modifier.height(8.dp))
        Text("출고", fontSize = 14.sp, color = Color.Gray)
    }
}