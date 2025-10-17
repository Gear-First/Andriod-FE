package com.ljs.and.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ljs.and.R
import com.ljs.and.data.ItemStatus

sealed class SearchResult {
    data class Receiving(val item: ReceivingSearchResultItem) : SearchResult()
    data class Releasing(val item: ReleasingSearchResultItem) : SearchResult()
    data class Inventory(val item: InventorySearchResultItem) : SearchResult()
}

data class ReceivingSearchResultItem(
    val productCode: String,
    val productName: String,
    val quantity: Int,
    val standard: String
)

data class ReleasingSearchResultItem(
    val id: String,
    val customer: String,
    val partName: String,
    val location: String,
    val quantity: Int,
    val manager: String,
    val status: String,
    val imageUrl: Int? = null
)

data class InventorySearchResultItem(
    val receiptNumber: String,
    val supplier: String,
    val name: String,
    val location: String,
    val currentStock: Int,
    val imageUrl: String,
    val status: ItemStatus,
    val manager: String? = null
)

val dummyReceivingResults = listOf(
    ReceivingSearchResultItem("P001", "노트북", 5, "15인치"),
    ReceivingSearchResultItem("P002", "마우스", 10, "무선"),
)

val dummyReleasingResults = listOf(
    ReleasingSearchResultItem("OUT - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "피킹 중", R.drawable.ic_launcher_background),
    ReleasingSearchResultItem("OUT - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "완료", R.drawable.ic_launcher_background),
)

val dummyInventoryResults = listOf(
    InventorySearchResultItem("IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "https://picsum.photos/200", ItemStatus.COMPLETED),
    InventorySearchResultItem("IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "https://picsum.photos/200", ItemStatus.DEFECTIVE, "이지수"),
    InventorySearchResultItem("IN - EFGH", "보쉬", "브레이크 패드", "B-01-1", 10, "https://picsum.photos/201", ItemStatus.NORMAL),
    InventorySearchResultItem("IN - IJKL", "한국타이어", "타이어", "C-07-5", 8, "https://picsum.photos/202", ItemStatus.LOW_STOCK),
    InventorySearchResultItem("IN - IJKL", "한국타이어", "타이어", "C-07-5", 8, "https://picsum.photos/202", ItemStatus.LOW_STOCK)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(navController: NavController, flowType: String, initialQuery: String) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    val searchResults = when (flowType) {
        "receiving" -> dummyReceivingResults.map { SearchResult.Receiving(it) }
        "releasing" -> dummyReleasingResults.map { SearchResult.Releasing(it) }
        "inventory" -> dummyInventoryResults.map { SearchResult.Inventory(it) }
        else -> emptyList()
    }

    Scaffold(
        containerColor = Color.White,
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
                    IconButton(onClick = { /* TODO: Perform search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 12.dp,
//                bottom = paddingValues.calculateBottomPadding() + 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(searchResults) {
                    result ->
                when (result) {
                    is SearchResult.Receiving -> ReceivingResultCard(result.item)
                    is SearchResult.Releasing -> ReleasingResultCard(result.item)
                    is SearchResult.Inventory -> InventoryResultCard(result.item)
                }
            }
        }
    }
}

@Composable
fun ReceivingResultCard(item: ReceivingSearchResultItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("상품코드: ${item.productCode}", fontSize = 14.sp, color = Color.Gray)
                Text("규격: ${item.standard}", fontSize = 14.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("수량: ${item.quantity}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ReleasingResultCard(item: ReleasingSearchResultItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("[출고 번호] ${item.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("공급 업체: ${item.customer}", fontSize = 13.sp)
                Text("부품: ${item.partName}", fontSize = 13.sp)
                Text("위치: ${item.location}, 수량: ${item.quantity}", fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            item.imageUrl?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "Product Image",
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("담당자", fontSize = 12.sp, color = Color.Gray)
                Text(item.manager, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val (text, color, textColor) = when (item.status) {
                    "피킹 중" -> Triple("피킹 중", Color.White, Color.Red)
                    else -> Triple("완료", Color(0xFFE0E0E0), Color.Black)
                }
                val border = if (item.status == "피킹 중") BorderStroke(1.dp, Color.Red) else BorderStroke(1.dp, Color.LightGray)
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(20.dp),
                    border = border,
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = color, contentColor = textColor)
                ) {
                    Text(text)
                }
            }
        }
    }
}

@Composable
fun InventoryResultCard(item: InventorySearchResultItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("[입고 번호] ${item.receiptNumber}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("공급 업체: ${item.supplier}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("부품: ${item.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("위치: ${item.location}, 수량: ${item.currentStock}", fontSize = 14.sp)
                item.manager?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("담당자: $it", fontSize = 14.sp, color = Color.Red)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.name,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.status == ItemStatus.DEFECTIVE) Color.Red else Color.LightGray
                    ),
//                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(item.status.displayName, color = Color.White)
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Receiving Result")
@Composable
fun ReceivingResultScreenPreview() {
    SearchResultScreen(navController = rememberNavController(), flowType = "receiving", initialQuery = "노트북")
}

@Preview(showBackground = true, name = "Releasing Result")
@Composable
fun ReleasingResultScreenPreview() {
    SearchResultScreen(navController = rememberNavController(), flowType = "releasing", initialQuery = "현대 모비스")
}

@Preview(showBackground = true, name = "Inventory Result")
@Composable
fun InventoryResultScreenPreview() {
    SearchResultScreen(navController = rememberNavController(), flowType = "inventory", initialQuery = "엔진 오일")
}
