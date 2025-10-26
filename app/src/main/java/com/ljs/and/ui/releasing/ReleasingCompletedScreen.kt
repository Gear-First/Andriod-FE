package com.ljs.and.ui.releasing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

@Composable
fun ReleasingCompletedScreen(completedList: List<ReleasingItem>, onItemClick: (ReleasingItem) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        items(completedList) { item ->
            CompletedCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun CompletedCard(item: ReleasingItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    text = "거래처: ${item.customer}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = item.status,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color(0xFF007BFF), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text("출고번호: ${item.id}", fontSize = 14.sp, color = Color.Gray)
            Text("출고일시: ${item.completionDate ?: ""}", fontSize = 14.sp, color = Color.Gray)
            Text("품목 개수: ${item.totalQuantity}개", fontSize = 14.sp, color = Color.Gray)
            Text("담당자: ${item.manager}", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .width(330.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(
                        "상세 보기",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F7)
@Composable
fun ReleasingCompletedScreenPreview() {
    val dummyList = listOf(
        ReleasingItem("O-003", "거래처 C", "2024.09.20", "2024.09.20 14:30", 200, "최담당", "완료")
    )
    MaterialTheme {
        ReleasingCompletedScreen(completedList = dummyList, onItemClick = {})
    }
}
