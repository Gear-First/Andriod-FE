package com.ljs.and.ui.receiving

import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ljs.and.data.model.ReceivingNote
import com.ljs.and.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingScreen(
    navController: NavController,
    viewModel: ReceivingViewModel = viewModel(factory = ReceivingViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    // 1. Get the initial tab index from savedStateHandle
    val initialTabIndex = navController.currentBackStackEntry?.savedStateHandle?.get<Int>("selectedTab") ?: 0
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }

    val tabs = listOf("입고 대기", "입고 완료")
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d("ReceivingScreen", "ON_RESUME: Checking for signals.")
                navController.currentBackStackEntry?.savedStateHandle?.let { handle ->
                    // 2. Set the tab index and immediately refresh if it's from the handle
                    handle.get<Int>("selectedTab")?.let {
                        Log.d("ReceivingScreen", "Selected tab signal received: $it")
                        selectedTabIndex = it
                        viewModel.refreshAllLists() // Refresh data for the new tab
                        handle.remove<Int>("selectedTab")
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 3. Load data only when the screen is first created or when a tab is manually selected
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 0) {
            viewModel.loadNotDoneReceivingNotes()
        } else {
            viewModel.loadDoneReceivingNotes()
        }
    }

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
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            ReceivingTabRow(
                selectedTabIndex = selectedTabIndex,
                tabs = tabs,
                onTabSelected = { 
                    selectedTabIndex = it
                    // No need to call refresh here, LaunchedEffect will handle it.
                }
            )
            
            when (selectedTabIndex) {
                0 -> PendingScreen(
                    pendingList = uiState.notDoneReceivingList,
                    onItemClick = { item ->
                        viewModel.loadReceivingNoteDetail(item.noteId)
                        navController.navigate(Screen.ReceivingInspection.createRoute(isReadOnly = false))
                    },
                    onLoadMore = { viewModel.loadNotDoneReceivingNotes() },
                    isLoading = uiState.isLoading,
                    canLoadMore = uiState.canLoadMoreNotDone
                )
                1 -> ReceivingCompletedScreen(
                    completedList = uiState.doneReceivingList,
                    onItemClick = { item ->
                        viewModel.loadReceivingNoteDetail(item.noteId)
                        navController.navigate(Screen.ReceivingInspection.createRoute(isReadOnly = true))
                    },
                    onLoadMore = { viewModel.loadDoneReceivingNotes() },
                    isLoading = uiState.isLoading,
                    canLoadMore = uiState.canLoadMoreDone
                )
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
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
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
            IconButton(onClick = { onSearchVisibilityChange(!isSearchVisible) }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFF5F5F7)
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
                Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun PendingScreen(
    pendingList: List<ReceivingNote>,
    onItemClick: (ReceivingNote) -> Unit,
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
            Text("입고 대기 항목이 없습니다.")
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
                PendingCard(item = item, onStartInspection = { onItemClick(item) })
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
fun PendingCard(item: ReceivingNote, onStartInspection: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStartInspection),
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
                        .background(Color.Red, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text("입고번호: ${item.noteId}", fontSize = 14.sp, color = Color.Gray)
            Text("입고 예정일: ${item.completedAt ?: ""}", fontSize = 14.sp, color = Color.Gray) // 이 필드는 API에 따라 변경될 수 있습니다.
            Text("품목 종류: ${item.itemKindsNumber}종", fontSize = 14.sp, color = Color.Gray)
            Text("총 수량: ${item.totalQty}개", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = onStartInspection,
                    modifier = Modifier
                        .width(330.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF007BFF)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF007BFF)
                    )
                ) {
                    Text(
                        "검수 시작",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
