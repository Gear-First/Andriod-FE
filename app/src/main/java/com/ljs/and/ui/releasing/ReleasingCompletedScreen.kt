package com.ljs.and.ui.releasing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class CompletedReleasing(val customer: String, val date: String, val itemCount: Int, val manager: String)

val dummyCompletedList = listOf(
    CompletedReleasing("현대 자동차", "2025.10.13", 15, "이지수")
)

@Composable
fun ReleasingCompletedScreen(navController: NavController) {
    CompletedList(items = dummyCompletedList)
}

@Composable
fun CompletedList(items: List<CompletedReleasing>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(items) { item ->
            CompletedCard(item = item)
        }
    }
}

@Composable
fun CompletedCard(item: CompletedReleasing) {
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
                    contentDescription = "Details",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("담당자: ${item.manager}", fontSize = 14.sp, color = Color.Gray)
                OutlinedButton(
                    onClick = { /* No action */ },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text("완료", color = Color.Black)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReleasingCompletedScreenPreview() {
    MaterialTheme {
        ReleasingCompletedScreen(navController = rememberNavController())
    }
}
