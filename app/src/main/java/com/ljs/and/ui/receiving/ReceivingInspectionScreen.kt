package com.ljs.and.ui.receiving

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.ljs.and.data.model.InspectorInfo
import com.ljs.and.data.model.ReceivingLine
import com.ljs.and.data.model.ReceivingNoteDetail
import com.ljs.and.ui.Screen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingInspectionScreen(
    navController: NavController,
    viewModel: ReceivingViewModel = viewModel(factory = ReceivingViewModelFactory()),
    isReadOnly: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedNoteDetail = uiState.selectedReceivingNoteDetail
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                navController.currentBackStackEntry?.savedStateHandle?.let {
                    val lineId = it.get<Long>("lineId")
                    val inspectedQty = it.get<Int>("inspectedQty")
                    val rejected = it.get<Boolean>("rejected")
                    val lineRemark = it.get<String?>("lineRemark")

                    if (lineId != null && inspectedQty != null && rejected != null) {
                        viewModel.updateReceivingLine(lineId, inspectedQty, rejected, lineRemark)
                        it.remove<Long>("lineId")
                        it.remove<Int>("inspectedQty")
                        it.remove<Boolean>("rejected")
                        it.remove<String?>("lineRemark")
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allItemsInspected = !isReadOnly &&
            selectedNoteDetail?.lines?.isNotEmpty() == true &&
            selectedNoteDetail.lines.all { it.status == "ACCEPTED" || it.status.startsWith("COMPLETED") || it.status == "REJECTED"}

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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
                onConfirm = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refreshDoneList", true)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() },
                onComplete = {
                    selectedNoteDetail?.noteId?.let { noteId ->
                        Log.d("ReceivingInspection", "검수 완료 요청: $noteId")
                        val inspectorInfo = InspectorInfo(
                            inspectorName = "홍길동",
                            inspectorDept = "품질관리팀",
                            inspectorPhone = "010-1234-5678"
                        )
                        viewModel.completeReceiving(noteId, inspectorInfo)
                    }
                },
                isCompleteEnabled = allItemsInspected
            )
        },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        if (uiState.isLoading && selectedNoteDetail == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (selectedNoteDetail != null) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                InspectionHeader(
                    supplier = selectedNoteDetail.supplierName,
                    date = selectedNoteDetail.expectedReceiveDate ?: "",
                    inspector = if (isReadOnly) selectedNoteDetail.inspectorName else null
                )
                InspectionList(
                    noteDetail = selectedNoteDetail,
                    isReadOnly = isReadOnly,
                    onItemClick = { line ->
                        navController.navigate(
                            Screen.ManualInput.createRoute(
                                flowType = "receiving",
                                noteId = selectedNoteDetail.noteId,
                                lineId = line.lineId,
                                currentQty = line.inspectedQty,
                                orderedQty = line.orderedQty,
                                lineRemark = line.lineRemark
                            )
                        )
                    }
                )
            }
        }
    }

    LaunchedEffect(uiState.receivingCompletion) {
        uiState.receivingCompletion?.let {
            Log.d("ReceivingInspection", "입고 완료 완료됨: ${it.completedAt}")
            delay(400)
            navController.previousBackStackEntry?.savedStateHandle?.set("refreshDoneList", true)
            navController.popBackStack()
            viewModel.clearReceivingCompletionEvent()
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
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            } else {
                Text(if (isReadOnly) "입고 상세" else "검수 중", fontWeight = FontWeight.Bold)
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
fun InspectionHeader(supplier: String, date: String, inspector: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("공급 업체: $supplier", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("예정일: $date", fontSize = 14.sp, color = Color.Gray)
            inspector?.let {
                Text("담당자: $it", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun InspectionList(
    noteDetail: ReceivingNoteDetail,
    isReadOnly: Boolean,
    onItemClick: (ReceivingLine) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(noteDetail.lines, key = { it.lineId }) { line ->
            InspectionItemCard(line, noteDetail, isReadOnly, onClick = { onItemClick(line) })
        }
    }
}

@Composable
fun InspectionItemCard(
    line: ReceivingLine,
    noteDetail: ReceivingNoteDetail,
    isReadOnly: Boolean,
    onClick: () -> Unit
) {
    val isInspected = line.status == "ACCEPTED" || line.status.startsWith("COMPLETED")
    val isRejected = line.status == "REJECTED"

    // 버튼 텍스트와 색상 로직 수정
    val buttonText = when {
        isRejected -> "불량"
        isInspected -> "검수완료"
        else -> "검수 전"
    }
    val buttonColor = when {
        isRejected -> Color.Red
        isInspected -> Color(0xFF007BFF)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isReadOnly && !isInspected && !isRejected, onClick = onClick),
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
                Text(noteDetail.supplierName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${line.product.name} / ${line.product.lot}", fontSize = 14.sp)
                Text("입고 번호: ${noteDetail.receivingNo}", fontSize = 12.sp, color = Color.Gray)
                Text("요청 수량: ${line.orderedQty}", fontSize = 12.sp, color = Color.Gray)
                Text("검수 수량: ${line.inspectedQty}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AsyncImage(
                    model = line.product.imgUrl,
                    contentDescription = "Product Image",
                    modifier = Modifier.size(80.dp)
                )
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, buttonColor),
                    enabled = false,
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
fun InspectionBottomBar(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
                    Text("검수 완료", color = Color.White)
                }
            }
        }
    }
}
