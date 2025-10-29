package com.ljs.and.ui.receiving

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ljs.and.data.model.ReceivingNote

@Composable
fun ReceivingCompletedScreen(
    completedList: List<ReceivingNote>,
    onItemClick: (ReceivingNote) -> Unit,
    onLoadMore: () -> Unit,
    isLoading: Boolean,
    canLoadMore: Boolean
) {
    val listState = rememberLazyListState()

    // 스크롤이 리스트의 끝에 도달했는지 확인
    val reachedBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index == listState.layoutInfo.totalItemsCount - 1 && canLoadMore
        }
    }

    // 리스트 끝에 도달하면 추가 데이터 로드
    LaunchedEffect(reachedBottom) {
        if (reachedBottom && !isLoading) {
            onLoadMore()
        }
    }

    if (completedList.isEmpty() && !isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("입고 완료된 항목이 없습니다.")
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(completedList, key = { it.noteId }) { item ->
                CompletedCard(item = item, onClick = { onItemClick(item) })
            }
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedCard(item: ReceivingNote, onClick: () -> Unit) {
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
                    text = "공급 업체: ${item.supplierName}",
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
            Text("입고번호: ${item.noteId}", fontSize = 14.sp, color = Color.Gray)
            Text("입고일시: ${item.completedAt ?: ""}", fontSize = 14.sp, color = Color.Gray)
            Text("품목 종류: ${item.itemKindsNumber}종", fontSize = 14.sp, color = Color.Gray)
            Text("총 수량: ${item.totalQty}개", fontSize = 14.sp, color = Color.Gray)

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
fun ReceivingCompletedScreenPreview() {
    val dummyList = listOf(
        ReceivingNote(9007199254740991, "거래처 C", 1, 200, "COMPLETED_OK", "2025-10-27T12:25:11")
    )
    MaterialTheme {
        ReceivingCompletedScreen(completedList = dummyList, onItemClick = {}, onLoadMore = {}, isLoading = false, canLoadMore = false)
    }
}
