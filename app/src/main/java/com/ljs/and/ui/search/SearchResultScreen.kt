package com.ljs.and.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    navController: NavController,
    flowType: String,
    initialQuery: String,
    viewModel: SearchResultViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    val searchResults by viewModel.searchResults.collectAsState()

    LaunchedEffect(flowType, searchQuery) {
        viewModel.search(flowType, searchQuery)
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F7),
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("검색") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.search(flowType, searchQuery) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F7)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.background(Color(0xFFF5F5F7)),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 12.dp,
                bottom = paddingValues.calculateBottomPadding() + 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(searchResults) { result ->
                when (result) {
                    is SearchResult.Receiving -> ReceivingResultCard(item = result.item, onClick = {}) // Updated Card
                    is SearchResult.Releasing -> ReleasingResultCard(item = result.item, onClick = {}) // Updated Card
                    is SearchResult.Inventory -> InventoryItemCard(item = result.item, onClick = {}) // Updated Card
                    is SearchResult.Pending -> PendingResultCard(result.item)
                }
            }
        }
    }
}

// Updated to match InspectionItemCard from ReceivingInspectionScreen.kt
@Composable
fun ReceivingResultCard(item: ReceivingSearchResultItem, onClick: () -> Unit) {
    val isInspected = item.status == "ACCEPTED" || item.status.startsWith("COMPLETED")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(item.supplierName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${item.productName} / ${item.productLot}", fontSize = 14.sp)
                Text("입고 번호: ${item.receivingNo}", fontSize = 12.sp, color = Color.Gray)
                Text("요청 수량: ${item.orderedQty}", fontSize = 12.sp, color = Color.Gray)
                Text("검수 수량: ${item.inspectedQty}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = item.imgUrl,
                    contentDescription = "Product Image",
                    modifier = Modifier.size(80.dp)
                )
                OutlinedButton(
                    onClick = { /* Clicks are handled by the card */ },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isInspected) Color(0xFF007BFF) else Color.Red),
                    enabled = false,
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = if (isInspected) "검수완료" else "검수 전",
                        color = if (isInspected) Color(0xFF007BFF) else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Updated to match PickingItemCard from ReleasingPickingScreen.kt
@Composable
fun ReleasingResultCard(item: ReleasingSearchResultItem, onClick: () -> Unit) {
    val isPicked = item.pickedQty >= item.allocatedQty

    val buttonText = when {
        isPicked -> "피킹완료"
        item.status == "SHORTAGE" -> "부족"
        else -> "피킹 전"
    }

    val buttonColor = when {
        isPicked -> Color(0xFF007BFF)
        item.status == "SHORTAGE" -> Color.Red
        else -> Color.Red
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(item.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${item.productName} / ${item.productLot}", fontSize = 14.sp)
                Text("출고번호: ${item.shippingNo}", fontSize = 12.sp, color = Color.Gray)
                Text("할당수량: ${item.allocatedQty}", fontSize = 12.sp, color = Color.Gray)
                Text("피킹수량: ${item.pickedQty}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = item.imgUrl,
                    contentDescription = "Product Image",
                    modifier = Modifier.size(80.dp)
                )
                OutlinedButton(
                    onClick = { /* Clicks are handled by the card */ },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, buttonColor),
                    enabled = false,
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = buttonText,
                        color = buttonColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Copied from InventoryScreen.kt
@Composable
fun InventoryItemCard(item: InventorySearchResultItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("${item.name} / ${item.code}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.supplier, fontSize = 14.sp)
                Text("현재/최소: ${item.currentStock}/${item.minimumStock}", fontSize = 12.sp, color = Color.Gray)
                Text("위치: ${item.location}", fontSize = 12.sp, color = Color.Gray)
                Text("최근 입출고: ${item.lastTransactionDate}", fontSize = 12.sp, color = Color.Gray)
                Text("담당자: ${item.manager ?: ""}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.imageUrl?.let {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(it).crossfade(true).build(),
                        contentDescription = item.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Build, contentDescription = "No Image", modifier = Modifier.size(40.dp), tint = Color.Gray)
                }
                StatusBadge(status = item.status)
            }
        }
    }
}

// Copied from InventoryScreen.kt
@Composable
fun StatusBadge(status: ItemStatus) {
    val (text, color) = when (status) {
        ItemStatus.NORMAL, ItemStatus.COMPLETED -> "정상" to Color(0xFF28A745)
        ItemStatus.LOW_STOCK -> "부족" to Color(0xFFFFC107)
        ItemStatus.DEFECTIVE -> "불량" to Color(0xFF6C757D)
    }

    OutlinedButton(
        onClick = { /* 아무것도 안함 */ },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun PendingResultCard(item: PendingItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("[${item.type} 번호] ${item.number}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("공급 업체: ${item.company}", fontSize = 14.sp, color = Color.Gray)
                    Text("부품: ${item.partName}", fontSize = 14.sp, color = Color.Gray)
                    Text("위치: ${item.location}, 수량: ${item.quantity}", fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(16.dp))
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.partName,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Column {
                    Text("담당자", fontSize = 12.sp, color = Color.Gray)
                    Text(item.manager, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { /* No action */ },
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.Red),
                    modifier = Modifier.size(width = 80.dp, height = 40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(item.status, color = Color.Red, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Receiving Result")
@Composable
fun ReceivingResultScreenPreview() {
    SearchResultScreen(navController = rememberNavController(), flowType = "receiving", initialQuery = "현대")
}

@Preview(showBackground = true, name = "Releasing Result")
@Composable
fun ReleasingResultScreenPreview() {
    SearchResultScreen(navController = rememberNavController(), flowType = "releasing", initialQuery = "현대")
}

@Preview(showBackground = true, name = "Inventory Result")
@Composable
fun InventoryResultScreenPreview() {
    SearchResultScreen(navController = rememberNavController(), flowType = "inventory", initialQuery = "오일")
}

@Preview(showBackground = true, name = "Pending Result")
@Composable
fun PendingResultScreenPreview() {
    SearchResultScreen(navController = rememberNavController(), flowType = "pending", initialQuery = "엔진")
}
