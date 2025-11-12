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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.ljs.and.data.model.ShippingNote
import com.ljs.and.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleasingScreen(
    navController: NavController,
    viewModel: ReleasingViewModel = viewModel(factory = ReleasingViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("출고 대기", "출고 완료")
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = Unit) {
        viewModel.refreshAllLists()
    }

    LaunchedEffect(key1 = navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refreshDoneList")?.let {
            if (it) {
                viewModel.refreshAllLists()
                selectedTabIndex = 1
                navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refreshDoneList")
            }
        }
    }

    Scaffold(
        topBar = { 
            ReleasingTopAppBar(
                isSearchVisible = isSearchVisible, 
                searchQuery = searchQuery, 
                onSearchQueryChange = { searchQuery = it }, 
                onSearchVisibilityChange = { isSearchVisible = it },
                onPerformSearch = {
                    if (searchQuery.isNotBlank()) {
                        navController.navigate(Screen.SearchResult.createRoute("releasing", searchQuery))
                    }
                    isSearchVisible = false
                    focusManager.clearFocus()
                }
            ) 
        },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            ReleasingTabRow(
                selectedTabIndex = selectedTabIndex,
                tabs = tabs,
                onTabSelected = { selectedTabIndex = it }
            )
            if (uiState.isLoading && uiState.notDoneShippingList.isEmpty() && uiState.doneShippingList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTabIndex) {
                    0 -> PendingScreen(
                        pendingList = uiState.notDoneShippingList,
                        onItemClick = { item ->
                            viewModel.loadShippingNoteDetail(item.noteId)
                            navController.navigate(Screen.ReleasingPicking.createRoute(item.noteId, isReadOnly = false))
                        },
                        onLoadMore = { viewModel.loadNotDoneShippingNotes() },
                        isLoading = uiState.isLoading,
                        canLoadMore = uiState.canLoadMoreNotDone
                    )
                    1 -> ReleasingCompletedScreen(
                        completedList = uiState.doneShippingList,
                        onItemClick = { item ->
                            viewModel.loadShippingNoteDetail(item.noteId)
                            navController.navigate(Screen.ReleasingPicking.createRoute(item.noteId, isReadOnly = true))
                        },
                        onLoadMore = { viewModel.loadDoneShippingNotes() },
                        isLoading = uiState.isLoading,
                        canLoadMore = uiState.canLoadMoreDone
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleasingTopAppBar(
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
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            } else {
                Text("출고", fontWeight = FontWeight.Bold)
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
            IconButton(onClick = { onSearchVisibilityChange(!isSearchVisible) }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F7))
    )
}

@Composable
fun ReleasingTabRow(selectedTabIndex: Int, tabs: List<String>, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            val containerColor = if (isSelected) Color(0xFF111827) else Color.White
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
                Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun PendingScreen(
    pendingList: List<ShippingNote>,
    onItemClick: (ShippingNote) -> Unit,
    onLoadMore: () -> Unit,
    isLoading: Boolean,
    canLoadMore: Boolean
) {
    val listState = rememberLazyListState()

    val reachedBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index == listState.layoutInfo.totalItemsCount - 1 && canLoadMore
        }
    }

    LaunchedEffect(reachedBottom) {
        if (reachedBottom && !isLoading) {
            onLoadMore()
        }
    }

    if (pendingList.isEmpty() && !isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("출고 대기 항목이 없습니다.")
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
            items(pendingList, key = { it.noteId }) { item ->
                PendingCard(item = item, onStartPicking = { onItemClick(item) })
            }
            if (isLoading && pendingList.isNotEmpty()) {
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
fun PendingCard(item: ShippingNote, onStartPicking: () -> Unit) {
    val statusColor = when (item.status) {
        "PENDING" -> Color.Red
        "IN_PROGRESS" -> Color.Red
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStartPicking),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    text = "거래처: ${item.branchName}", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = item.status,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(statusColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text("출고번호: ${item.shippingNo}", fontSize = 14.sp, color = Color.Gray)
            Text("품목 종류: ${item.itemKindsNumber}종", fontSize = 14.sp, color = Color.Gray)
            Text("총 수량: ${item.totalQty}개", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onStartPicking,
                    modifier = Modifier
                        .width(330.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
                ) {
                    Text(
                        "피킹 시작",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
