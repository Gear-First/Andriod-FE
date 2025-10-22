package com.ljs.and.ui.receiving

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ReceivingCompletedScreen(completedList: List<ReceivingItem>, onItemClick: (ReceivingItem) -> Unit) {
    CompletedList(items = completedList, onItemClick = onItemClick)
}

@Composable
fun CompletedList(items: List<ReceivingItem>, onItemClick: (ReceivingItem) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        items(items) { item ->
            CompletedCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun CompletedCard(item: ReceivingItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                     Text(item.supplier, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                     Text("입고번호: ${item.id}", fontSize = 12.sp, color = Color.Gray)
                }
                Text(item.status, color = Color(0xFF007BFF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Text("입고일시: ${item.completionDate ?: ""}", fontSize = 14.sp, color = Color.Gray)
            Text("품목 개수: ${item.totalQuantity}개", fontSize = 14.sp, color = Color.Gray)
            Text("담당자: ${item.manager}", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("상세보기", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReceivingCompletedScreenPreview() {
    val dummyList = listOf(
        ReceivingItem("R-003", "거래처 C", "2024.09.20", "2024.09.20 14:30", 200, "최담당", "완료")
    )
    MaterialTheme {
        ReceivingCompletedScreen(completedList = dummyList, onItemClick = {})
    }
}
