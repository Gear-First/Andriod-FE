package com.ljs.and.ui.inventory

import android.R.attr.top
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ljs.and.data.InventoryItem
import com.ljs.and.data.ItemStatus
import com.ljs.and.ui.Screen // Added import
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavHostController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("재고 조회", "재고 신청")
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                InventorySearchTopAppBar(
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        if (it.isNotBlank()) {
                            navController.navigate(Screen.SearchResult.createRoute("inventory", it))
                        }
                    },
                    onClose = { isSearchActive = false }
                )
            } else {
                InventoryDefaultTopAppBar(
                    onSearchClick = { isSearchActive = true }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()) // top만 사용
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> InventoryStatusScreen()
                1 -> InventoryRequestScreen(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryDefaultTopAppBar(onSearchClick: () -> Unit) {
    TopAppBar(
        title = { Text("재고", fontWeight = FontWeight.Bold) },
        actions = {
            Text(
                text = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date()),
                modifier = Modifier.padding(end = 8.dp),
                fontSize = 16.sp
            )
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventorySearchTopAppBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClose: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    TopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("검색") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(searchQuery) }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (searchQuery.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun InventoryStatusScreen() {
    val sampleItems = listOf(
        InventoryItem(1, "IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, 5, "https://picsum.photos/200", ItemStatus.COMPLETED),
        InventoryItem(2, "IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, 5, "https://picsum.photos/200", ItemStatus.DEFECTIVE, "이지수"),
        InventoryItem(3, "IN - EFGH", "보쉬", "브레이크 패드", "B-01-1", 10, 10, "https://picsum.photos/201", ItemStatus.NORMAL),
        InventoryItem(4, "IN - IJKL", "한국타이어", "타이어", "C-07-5", 8, 12, "https://picsum.photos/202", ItemStatus.LOW_STOCK)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        InventorySummaryCard(totalItems = 240, totalQuantity = 124800, lackingItems = 15)
        Spacer(modifier = Modifier.height(24.dp))
        FilterDropDown()
        Spacer(modifier = Modifier.height(16.dp))
        InventoryList(items = sampleItems)
    }
}

@Composable
fun FilterDropDown() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("전체") }
    val options = listOf("전체", "부족 품목만 보기")

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selectedOptionText, fontSize = 16.sp)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Filter Options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOptionText = option
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun InventoryList(items: List<InventoryItem>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items) { item ->
            InventoryItemCard(item = item)
        }
    }
}

@Composable
fun InventoryItemCard(item: InventoryItem) {
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
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(item.status.displayName, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun InventorySummaryCard(totalItems: Int, totalQuantity: Int, lackingItems: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryRow("총 품목:", totalItems.toString())
            SummaryRow("전체 수량:", totalQuantity.toString())
            SummaryRow("부족 품목:", lackingItems.toString())
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    // InventoryScreen(navController = NavHostController(LocalContext.current))
}

@Preview(showBackground = true)
@Composable
fun InventoryItemCardPreview() {
    val sampleItem = InventoryItem(2, "IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, 5, "https://picsum.photos/200", ItemStatus.DEFECTIVE, "이지수")
    InventoryItemCard(item = sampleItem)
}
