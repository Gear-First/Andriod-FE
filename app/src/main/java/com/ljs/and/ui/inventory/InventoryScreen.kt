package com.ljs.and.ui.inventory

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ljs.and.ui.Screen
import com.ljs.and.ui.theme.AndTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavHostController,
    filter: String?,
    viewModel: InventoryViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("재고 조회", "재고 신청")
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val inventoryState by viewModel.inventoryState.collectAsState()

    LaunchedEffect(filter) {
        filter?.let {
            if (it == "재고신청") {
                selectedTabIndex = 1
            } else {
                selectedTabIndex = 0
                viewModel.updateInventoryFilter(it)
            }
        }
    }

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
        containerColor = Color(0xFFF5F5F7)
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
                0 -> InventoryStatusScreen(
                    inventoryState = inventoryState,
                    onFilterChange = { viewModel.updateInventoryFilter(it) },
                    onItemClick = { /* navController.navigate(...) */ },
                    onPageChange = { viewModel.goToPage(it) },
                    onNextPage = { viewModel.goToNextPage() },
                    onPreviousPage = { viewModel.goToPreviousPage() }
                )
                1 -> InventoryRequestScreen(
                    navController = navController,
                    viewModel = viewModel
                )
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
            containerColor = Color(0xFFF5F5F7)
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
fun InventoryStatusScreen(
    inventoryState: InventoryState,
    onFilterChange: (String) -> Unit,
    onItemClick: (InventoryItem) -> Unit,
    onPageChange: (Int) -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            InventorySummaryDashboard(
                totalItems = inventoryState.totalItems,
                totalQuantity = inventoryState.totalQuantity.toInt(),
                lackingItems = inventoryState.lackingItems,
                defectiveItems = inventoryState.defectiveItems
            )
        }
        item {
            FilterDropdown(
                selectedOption = inventoryState.selectedFilter,
                onOptionSelected = onFilterChange
            )
        }

        if (inventoryState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize() // Fill the parent LazyColumn
                        .padding(vertical = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (inventoryState.inventoryList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(vertical = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("표시할 항목이 없습니다.")
                }
            }
        } else {
            items(inventoryState.inventoryList) { item ->
                InventoryItemCard(item = item, onClick = { onItemClick(item) })
            }

            if (inventoryState.totalPages > 1) {
                item {
                    PaginationControls(
                        currentPage = inventoryState.currentPage,
                        totalPages = inventoryState.totalPages,
                        onPageChange = onPageChange,
                        onNextPage = onNextPage,
                        onPreviousPage = onPreviousPage
                    )
                }
            }
        }
    }
}


@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousPage, enabled = currentPage > 1) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page")
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Page numbers
        val maxPageNumbersToShow = 5
        val startPage = ((currentPage - 1) / maxPageNumbersToShow) * maxPageNumbersToShow + 1
        val endPage = (startPage + maxPageNumbersToShow - 1).coerceAtMost(totalPages)

        for (i in startPage..endPage) {
            Text(
                text = i.toString(),
                modifier = Modifier
                    .clickable { onPageChange(i) }
                    .padding(8.dp),
                fontWeight = if (i == currentPage) FontWeight.Bold else FontWeight.Normal,
                color = if (i == currentPage) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onNextPage, enabled = currentPage < totalPages) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page")
        }
    }
}


@Composable
fun InventorySummaryDashboard(totalItems: Int, totalQuantity: Int, lackingItems: Int, defectiveItems: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📦 총 품목: ${totalItems}종", fontSize = 16.sp)
            Text("📊 전체 수량: ${totalQuantity}개", fontSize = 16.sp)
            Text("⚠️ 부족 품목: ${lackingItems}개", fontSize = 16.sp)
//            Text("🛠️ 불량 품목: ${defectiveItems}개", fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("전체", "정상", "부족")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .menuAnchor()
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedOption, fontWeight = FontWeight.Bold)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White).width(100.dp)

        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
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

@Composable
fun StatusBadge(status: ItemStatus) {
    val (text, color) = when (status) {
        ItemStatus.NORMAL, ItemStatus.COMPLETED -> "정상" to Color(0xFF28A745)
        ItemStatus.LOW_STOCK -> "부족" to Color(0xFFFFC107)
//        ItemStatus.SOLD_OUT -> "소진" to Color(0xFFDC3545)
//        ItemStatus.DEFECTIVE -> "불량" to Color(0xFF6C757D)
        else -> status.displayName to Color.Gray
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

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    AndTheme {
        InventoryScreen(navController = rememberNavController(), filter = null)
    }
}
