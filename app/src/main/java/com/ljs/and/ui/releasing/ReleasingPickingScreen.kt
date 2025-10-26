package com.ljs.and.ui.releasing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ljs.and.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleasingPickingScreen(
    navController: NavController,
    viewModel: ReleasingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedItem = uiState.selectedReleasingItem
    val pickingList = uiState.pickingList

    val isReadOnly = selectedItem?.status == "완료"

    val allItemsCompleted = !isReadOnly && pickingList.isNotEmpty() && pickingList.all { it.isPicked }
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val manualInputResult = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<Boolean>("manualInputCompleted")
                if (manualInputResult == true) {
                    viewModel.completeCurrentPicking()
                    navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("manualInputCompleted")
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Scaffold(
        topBar = {
            PickingTopAppBar(
                isReadOnly = isReadOnly,
                isSearchVisible = isSearchVisible,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchVisibilityChange = { isSearchVisible = it },
                onNavigateBack = { navController.popBackStack() },
                onPerformSearch = {
                    if (searchQuery.isNotBlank()) {
                        navController.navigate(Screen.SearchResult.createRoute("releasing", searchQuery))
                    }
                    isSearchVisible = false
                }
            )
        },
        bottomBar = {
            PickingBottomBar(
                isReadOnly = isReadOnly,
                onConfirm = { 
                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedTab", 1)
                    navController.popBackStack() 
                },
                onCancel = { navController.popBackStack() },
                onComplete = {
                    viewModel.completeAllPicking()
                    navController.popBackStack()
                },
                isCompleteEnabled = allItemsCompleted
            )
        },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (selectedItem != null) {
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                PickingHeader(customer = selectedItem.customer, date = selectedItem.expectedDate)
                PickingList(
                    items = pickingList,
                    selectedReleasingItem = selectedItem,
                    isReadOnly = isReadOnly,
                    onItemClick = { itemId ->
                        viewModel.setCurrentPickingItem(itemId)
                        navController.navigate(Screen.BarcodeScan.createRoute("releasing"))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickingTopAppBar(
    isReadOnly: Boolean,
    isSearchVisible: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchVisibilityChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
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
                Text(if(isReadOnly) "출고 상세" else "피킹 중", fontWeight = FontWeight.Bold)
            }
        },
        navigationIcon = {
            if (isSearchVisible) {
                IconButton(onClick = { onSearchVisibilityChange(false) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            } else {
                IconButton(onClick = onNavigateBack) {
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
fun PickingHeader(customer: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("거래처: $customer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("날짜: $date", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PickingList(
    items: List<PickingItem>,
    selectedReleasingItem: ReleasingItem,
    isReadOnly: Boolean,
    onItemClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            PickingItemCard(
                item = item,
                releasingItem = selectedReleasingItem,
                isReadOnly = isReadOnly,
                onClick = { onItemClick(item.id) }
            )
        }
    }
}

@Composable
fun PickingItemCard(item: PickingItem, releasingItem: ReleasingItem, isReadOnly: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(releasingItem.customer, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${item.partName} / ${item.partCode}", fontSize = 14.sp)
                Text("출고번호: ${item.releasingId}", fontSize = 12.sp, color = Color.Gray)
                Text("출고수량: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
                Text("위치: ${item.location}", fontSize = 12.sp, color = Color.Gray)
                Text("담당자: ${releasingItem.manager}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.imageUrl?.let {
                    Image(
                        painter = painterResource(id = it),
                        contentDescription = "Product Image",
                        modifier = Modifier.size(80.dp)
                    )
                }
                val isPicked = item.isPicked
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isPicked) Color(0xFF007BFF) else Color.Red),
                    enabled = !isReadOnly && !isPicked
                ) {
                    Text(
                        text = if (isPicked) "피킹완료" else "피킹 중",
                        color = if (isPicked) Color(0xFF007BFF) else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PickingBottomBar(
    isReadOnly: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    isCompleteEnabled: Boolean
) {
    Surface(
//        shadowElevation = 8.dp,
        color = Color(0xFFF5F5F7)
    ) {
        if (isReadOnly) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
                ) {
                    Text("확인", color = Color.White)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text("취소", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007BFF),
                        disabledContainerColor = Color.LightGray
                    ),
                    enabled = isCompleteEnabled
                ) {
                    Text("피킹 완료", color = Color.White)
                }
            }
        }
    }
}
