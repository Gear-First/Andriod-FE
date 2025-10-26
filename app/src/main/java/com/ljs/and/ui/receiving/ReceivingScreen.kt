package com.ljs.and.ui.receiving

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ljs.and.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingScreen(
    navController: NavController,
    viewModel: ReceivingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("입고 대기", "입고 완료") // 탭 이름 수정
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val (pendingItems, completedItems) = uiState.receivingList.partition { it.status == "대기" }

    Scaffold(
        topBar = { 
            ReceivingTopAppBar(
                isSearchVisible = isSearchVisible, 
                searchQuery = searchQuery, 
                onSearchQueryChange = { searchQuery = it }, 
                onSearchVisibilityChange = { isSearchVisible = it },
                onPerformSearch = {
                    if (searchQuery.isNotBlank()) {
                        navController.navigate(Screen.SearchResult.createRoute("receiving", searchQuery))
                    }
                    isSearchVisible = false
                    focusManager.clearFocus()
                }
            ) 
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ReceivingTabRow(
                selectedTabIndex = selectedTabIndex,
                tabs = tabs,
                onTabSelected = { selectedTabIndex = it }
            )
            if (uiState.isLoading && uiState.receivingList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTabIndex) {
                    0 -> PendingScreen(
                        pendingList = pendingItems,
                        onItemClick = { item ->
                            viewModel.selectReceivingItem(item)
                            navController.navigate(Screen.ReceivingInspection.createRoute(item.supplier, item.expectedDate))
                        }
                    )
                    1 -> ReceivingCompletedScreen(
                        completedList = completedItems,
                        onItemClick = { item ->
                            viewModel.selectReceivingItem(item)
                            navController.navigate(Screen.ReceivingInspection.createRoute(item.supplier, item.expectedDate))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingTopAppBar(
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
                Text("입고", fontWeight = FontWeight.Bold)
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
fun ReceivingTabRow(selectedTabIndex: Int, tabs: List<String>, onTabSelected: (Int) -> Unit) {
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
fun PendingScreen(pendingList: List<ReceivingItem>, onItemClick: (ReceivingItem) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(pendingList) { item ->
            PendingCard(item = item, onStartInspection = { onItemClick(item) })
        }
    }
}

@Composable
fun PendingCard(item: ReceivingItem, onStartInspection: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clickable(onClick = onStartInspection),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("공급처: ${item.supplier}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.status, color = Color.Red, fontWeight = FontWeight.Bold)
            }
            Text("입고번호: ${item.id}", fontSize = 14.sp, color = Color.Gray)
            Text("입고 예정일: ${item.expectedDate}", fontSize = 14.sp, color = Color.Gray)
            Text("품목 수량: ${item.totalQuantity}개", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onStartInspection,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
            ) {
                Text("검수 시작", color = Color.White)
            }
        }
    }
}
