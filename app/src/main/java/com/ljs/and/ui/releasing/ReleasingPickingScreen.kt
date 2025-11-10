package com.ljs.and.ui.releasing

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ljs.and.data.model.AssigneeInfo
import com.ljs.and.data.model.ShippingLine
import com.ljs.and.data.model.ShippingNoteDetail
import com.ljs.and.ui.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleasingPickingScreen(
    navController: NavController,
    noteId: Long,
    isReadOnly: Boolean,
    viewModel: ReleasingViewModel = viewModel(factory = ReleasingViewModelFactory())
) {
    LaunchedEffect(noteId) {
        viewModel.loadShippingNoteDetail(noteId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val selectedItem = uiState.selectedShippingNoteDetail

    val isCompletable by remember(uiState.selectedShippingNoteDetail) {
        derivedStateOf {
            val detail = uiState.selectedShippingNoteDetail
            !isReadOnly &&
                    detail != null &&
                    detail.lines.isNotEmpty() &&
                    detail.lines.all { it.status == "READY" || it.status == "SHORTAGE" }
        }
    }

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                val lineId = savedStateHandle?.get<Long>("lineId")
                val pickedQty = savedStateHandle?.get<Int>("pickedQty")
                val lineRemark = savedStateHandle?.get<String>("lineRemark")

                if (lineId != null && pickedQty != null) {
                    viewModel.updateShippingLine(lineId, pickedQty, lineRemark)
                    savedStateHandle.remove<Long>("lineId")
                    savedStateHandle.remove<Int>("pickedQty")
                    savedStateHandle.remove<String>("lineRemark")
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.shippingCompletion) {
        uiState.shippingCompletion?.let {
            delay(400)
            navController.previousBackStackEntry?.savedStateHandle?.set("refreshDoneList", true)
            navController.popBackStack()
            viewModel.clearShippingCompletionEvent()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearErrorMessage()
            }
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
                onConfirm = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
                onComplete = {
                    viewModel.completeShipping(noteId, AssigneeInfo("김담당", "물류팀", "010-9876-5432"))
                },
                isCompleteEnabled = isCompletable
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        if (uiState.isLoading && selectedItem == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (selectedItem != null) {
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                PickingHeader(
                    customer = selectedItem.branchName,
                    date = selectedItem.requestedAt,
                    assigneeName = selectedItem.assigneeName,
                    isReadOnly = isReadOnly
                )
                PickingList(
                    items = selectedItem.lines,
                    selectedShippingNoteDetail = selectedItem,
                    isReadOnly = isReadOnly,
                    onItemClick = { line ->
                        navController.navigate(Screen.ManualInput.createRoute(
                            flowType = "releasing",
                            noteId = noteId,
                            lineId = line.lineId,
                            currentQty = line.pickedQty,
                            orderedQty = line.orderedQty,
                            lineRemark = null
                        ))
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
fun PickingHeader(customer: String, date: String, assigneeName: String?, isReadOnly: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("거래처: $customer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("요청일: $date", fontSize = 14.sp, color = Color.Gray)
            if (isReadOnly) {
                assigneeName?.let {
                    Text("담당자: $it", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PickingList(
    items: List<ShippingLine>,
    selectedShippingNoteDetail: ShippingNoteDetail,
    isReadOnly: Boolean,
    onItemClick: (ShippingLine) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items, key = { it.lineId }) { item ->
            PickingItemCard(
                item = item,
                shippingNoteDetail = selectedShippingNoteDetail,
                isReadOnly = isReadOnly,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
fun PickingItemCard(
    item: ShippingLine,
    shippingNoteDetail: ShippingNoteDetail,
    isReadOnly: Boolean,
    onClick: () -> Unit
) {
    val isPicked = item.pickedQty >= item.orderedQty
    val isShortage = item.status == "SHORTAGE"

    val buttonText = when {
        isPicked -> "피킹완료"
        isShortage -> "부족"
        else -> "피킹 전"
    }

    val buttonColor = when {
        isPicked -> Color(0xFF007BFF)
        isShortage -> Color.Red
        else -> Color.Gray
    }

    val isButtonClickable = !isReadOnly && !isPicked

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
                modifier = Modifier.weight(1f).clickable(enabled = isButtonClickable, onClick = onClick),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(shippingNoteDetail.branchName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${item.product.name ?: ""} / ${item.product.lot ?: ""}", fontSize = 14.sp)
                Text("출고번호: ${shippingNoteDetail.shippingNo}", fontSize = 12.sp, color = Color.Gray)
                Text("요청수량: ${item.orderedQty}", fontSize = 12.sp, color = Color.Gray)
                Text("피킹수량: ${item.pickedQty}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = item.product.imgUrl ?: "",
                    contentDescription = "Product Image",
                    modifier = Modifier.size(80.dp)
                )

                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, buttonColor),
                    enabled = isButtonClickable,
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

@Composable
fun PickingBottomBar(
    isReadOnly: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    isCompleteEnabled: Boolean
) {
    Surface(
        color = Color(0xFFF5F5F7),
        shadowElevation = 8.dp
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
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text("취소", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
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
