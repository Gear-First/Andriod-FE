package com.ljs.and.ui.receiving

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingInspectionScreen(
    navController: NavController,
    viewModel: ReceivingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedItem = uiState.selectedReceivingItem
    val inspectionList = uiState.inspectionList

    val isReadOnly = selectedItem?.status == "완료"

    val allItemsCompleted = !isReadOnly && inspectionList.isNotEmpty() && inspectionList.all { it.isInspected }
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
                    viewModel.completeCurrentInspection()
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
            InspectionTopAppBar(
                isReadOnly = isReadOnly,
                isSearchVisible = isSearchVisible,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchVisibilityChange = { isSearchVisible = it },
                onNavigateBack = { navController.popBackStack() },
                onPerformSearch = {
                    if (searchQuery.isNotBlank()) {
                        navController.navigate(Screen.SearchResult.createRoute("receiving", searchQuery))
                    }
                    isSearchVisible = false
                }
            )
        },
        bottomBar = {
            InspectionBottomBar(
                isReadOnly = isReadOnly,
                onConfirm = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
                onComplete = {
                    viewModel.completeAllInspections()
                    navController.popBackStack()
                },
                isCompleteEnabled = allItemsCompleted
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (selectedItem != null) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                InspectionHeader(supplier = selectedItem.supplier, date = selectedItem.expectedDate)
                InspectionList(
                    items = inspectionList,
                    selectedReceivingItem = selectedItem,
                    isReadOnly = isReadOnly,
                    navController = navController,
                    onItemClick = { itemId ->
                        viewModel.setCurrentInspectionItem(itemId)
                        navController.navigate(Screen.BarcodeScan.createRoute("receiving"))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionTopAppBar(
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
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            } else {
                Text(if(isReadOnly) "입고 상세" else "검수 중", fontWeight = FontWeight.Bold)
            }
        },
        actions = {
            IconButton(onClick = { onSearchVisibilityChange(!isSearchVisible) }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun InspectionHeader(supplier: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("공급 업체: $supplier", fontSize = 14.sp, color = Color.Gray)
            Text("날짜: $date", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun InspectionList(
    items: List<InspectionItem>,
    selectedReceivingItem: ReceivingItem,
    isReadOnly: Boolean,
    navController: NavController,
    onItemClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            InspectionItemCard(
                item = item,
                receivingItem = selectedReceivingItem,
                isReadOnly = isReadOnly,
                onClick = { onItemClick(item.id) }
            )
        }
    }
}

@Composable
fun InspectionItemCard(item: InspectionItem, receivingItem: ReceivingItem, isReadOnly: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick, enabled = !isReadOnly),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            item.imageUrl?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "Product Image",
                    modifier = Modifier.size(60.dp).padding(end = 16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if(item.isInspected) "검수완료" else "검수중",
                        color = if(item.isInspected) Color(0xFF007BFF) else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(receivingItem.supplier, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Text("${item.partName} / ${item.partCode}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("입고번호: ${item.receivingId}", fontSize = 12.sp, color = Color.Gray)
                Text("입고수량: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
                Text("위치: ${item.location}", fontSize = 12.sp, color = Color.Gray)
                Text("담당자: ${receivingItem.manager}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun InspectionBottomBar(
    isReadOnly: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    isCompleteEnabled: Boolean
) {
    if (isReadOnly) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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
                .padding(16.dp),
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
                    containerColor = if (isCompleteEnabled) Color(0xFF007BFF) else Color.LightGray
                ),
                enabled = isCompleteEnabled
            ) {
                Text("검수 완료", color = Color.White)
            }
        }
    }
}
