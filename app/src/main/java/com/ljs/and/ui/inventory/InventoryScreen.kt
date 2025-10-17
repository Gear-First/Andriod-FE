
package com.ljs.and.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ljs.and.data.InventoryItem
import com.ljs.and.data.ItemStatus
import com.ljs.and.ui.Screen
import com.ljs.and.ui.theme.AndTheme

private val dummyInventoryList = listOf(
    InventoryItem(1, "IN-ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, 1, "https://picsum.photos/200", ItemStatus.COMPLETED, "이지수"),
    InventoryItem(2, "IN-EFGH", "현대 오토에버", "타이어", "B-01-1", 5, 2, "https://picsum.photos/201", ItemStatus.DEFECTIVE, "김철수"),
    InventoryItem(3, "IN-IJKL", "기아", "브레이크 패드", "C-02-3", 10, 5, "https://picsum.photos/202", ItemStatus.NORMAL, "박영희"),
    InventoryItem(4, "IN-MNOP", "GM", "헤드라이트", "D-04-5", 0, 3, "https://picsum.photos/203", ItemStatus.SOLD_OUT, "이민준"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavHostController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("재고 조회", "재고 신청")
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                isSearchVisible = isSearchVisible,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchVisibilityChange = { isSearchVisible = it },
                onPerformSearch = {
                    if (searchQuery.isNotBlank()) {
                        navController.navigate(Screen.SearchResult.createRoute("inventory", searchQuery))
                    }
                    isSearchVisible = false
                    focusManager.clearFocus()
                }
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
            ) {
            InventoryTabRow(
                selectedTabIndex = selectedTabIndex,
                tabs = tabs,
                onTabSelected = { selectedTabIndex = it }
            )
            when (selectedTabIndex) {
                0 -> InventoryStatusScreen(navController, dummyInventoryList)
                1 -> InventoryRequestScreen(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryTopAppBar(
    isSearchVisible: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchVisibilityChange: (Boolean) -> Unit,
    onPerformSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    TopAppBar(
        title = {
            if (isSearchVisible) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("검색") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        onPerformSearch()
                        focusManager.clearFocus()
                    }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            } else {
                Text("재고", fontWeight = FontWeight.Bold)
            }
        },
        navigationIcon = {
            if (isSearchVisible) {
                IconButton(onClick = { onSearchVisibilityChange(false) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (isSearchVisible) {
                IconButton(onClick = {
                    onPerformSearch()
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            } else {
                IconButton(onClick = { onSearchVisibilityChange(true) }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun InventoryTabRow(selectedTabIndex: Int, tabs: List<String>, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            val containerColor = if (isSelected) Color(0xFF007BFF) else Color.White
            val contentColor = if (isSelected) Color.White else Color.Black
            val border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray)

            Button(
                onClick = { onTabSelected(index) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                border = border,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(title)
            }
        }
    }
}

@Composable
fun InventoryStatusScreen(navController: NavHostController, inventoryList: List<InventoryItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            InventorySummaryCard(totalItems = 240, totalQuantity = 124800, lackingItems = 15)
        }
        item {
            FilterDropdown()
        }
        items(inventoryList) { item ->
            InventoryItemCard(item = item, onClick = {
                // navController.navigate(Screen.InventoryDetail.createRoute(item.id))
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown() {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("전체", "정상", "부족", "불량", "누락", "소진")
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .menuAnchor()
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedOptionText, fontWeight = FontWeight.Bold)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 80.dp).background(Color.White)
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        selectedOptionText = selectionOption
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
fun InventoryItemCard(item: InventoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            item.imageUrl?.let {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(it).crossfade(true).build(),
                    contentDescription = item.name,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Outlined.Build, contentDescription = "Inventory Item", modifier = Modifier.size(40.dp), tint = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("공급 업체: ${item.supplier}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("부품명: ${item.name}", fontSize = 14.sp, color = Color.Gray)
                Text("위치: ${item.location}", fontSize = 14.sp, color = Color.Gray)
                Text("현재고: ${item.currentStock}", fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = item.status.displayName,
                color = when (item.status) {
                    ItemStatus.NORMAL, ItemStatus.COMPLETED -> Color.Green
                    ItemStatus.LOW_STOCK, ItemStatus.MISSING -> Color(0xFFFFA500) // Orange
                    ItemStatus.DEFECTIVE, ItemStatus.SOLD_OUT -> Color.Red
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InventorySummaryCard(totalItems: Int, totalQuantity: Int, lackingItems: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryRow("총 품목:", "$totalItems 개")
            SummaryRow("전체 수량:", "$totalQuantity 개")
            SummaryRow("부족 품목:", "$lackingItems 개")
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
    AndTheme {
        InventoryScreen(navController = rememberNavController())
    }
}
