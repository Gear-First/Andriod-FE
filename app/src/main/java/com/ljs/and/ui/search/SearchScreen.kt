package com.ljs.and.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ljs.and.data.model.InventoryItem
import com.ljs.and.data.model.SearchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController? = null,
    viewModel: SearchViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val warehouseCode by viewModel.warehouseCode.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchList by viewModel.searchList.collectAsState()
    val inventoryList by viewModel.inventoryList.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F5F7),
        topBar = {
            // 🔹 왼쪽 정렬형 TopAppBar
            TopAppBar(
                title = {
                    Text(
                        text = "통합 조회",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F7)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 🔍 검색 필드 1
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search Query") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),   // ✅ 이렇게 수정
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF007BFF),
                    unfocusedIndicatorColor = Color(0xFFD0D0D0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔍 검색 필드 2
            OutlinedTextField(
                value = warehouseCode,
                onValueChange = { viewModel.onWarehouseCodeChange(it) },
                label = { Text("Warehouse Code") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF007BFF),
                    unfocusedIndicatorColor = Color(0xFFD0D0D0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔘 탭 버튼 + 조회 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SearchTabButton(
                        selected = selectedTab == SearchTab.SEARCH,
                        onClick = { viewModel.onTabSelected(SearchTab.SEARCH) },
                        text = "입출고"
                    )
                    SearchTabButton(
                        selected = selectedTab == SearchTab.INVENTORY,
                        onClick = { viewModel.onTabSelected(SearchTab.INVENTORY) },
                        text = "재고"
                    )
                }

                Button(
                    onClick = { viewModel.search() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Text("조회", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 📋 결과 리스트
            when (selectedTab) {
                SearchTab.SEARCH -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(searchList) { item ->
                            SearchItemCard(item)
                        }
                    }
                }

                SearchTab.INVENTORY -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(inventoryList) { item ->
                            InventoryItemCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTabButton(selected: Boolean, onClick: () -> Unit, text: String) {
    val bgColor = if (selected) Color(0xFF007BFF) else Color.White
    val textColor = if (selected) Color.White else Color.Gray
    val borderColor = if (selected) Color(0xFF007BFF) else Color(0xFFD0D0D0)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, borderColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .height(40.dp)
            .width(90.dp)
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchItemCard(item: SearchItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("${item.type} | ${item.no}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("거래처명: ${item.partnerName ?: "거래처 없음"}", color = Color.Gray)
            Text("창고명: ${item.warehouseCode}", color = Color.Gray)
            Text("상태: ${item.status}", color = Color.Gray)
            Text("요청일: ${item.requestedAt}", color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryItemCard(item: InventoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("${item.part.name} (${item.part.code})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("창고: ${item.warehouseCode}", color = Color.Gray)
            Text("현재고: ${item.onHandQty}", color = Color.Gray)
            Text("안전재고: ${item.safetyStockQty}", color = Color.Gray)
            item.supplierName?.let { Text("공급처: $it", color = Color.Gray) }
        }
    }
}
