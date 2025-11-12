package com.ljs.and.ui.inventory

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen
import com.ljs.and.ui.common.TitledTextField
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryRequestFormScreen(
    navController: NavHostController,
    viewModel: InventoryViewModel,
    partId: Long?,
    partName: String?,
    partCode: String?,
    price: Int?,
    safetyStockQty: Int?,
    qr: String?
) {
    val context = LocalContext.current

    var partIdState by remember { mutableStateOf("") }
    var partNameState by remember { mutableStateOf("") }
    var partCodeState by remember { mutableStateOf("") }
    var priceState by remember { mutableStateOf(0) }
    var safetyStockQtyState by remember { mutableStateOf(0) }
    var requestQuantity by remember { mutableStateOf("") }

    var isPreFilled by remember { mutableStateOf(false) }

    LaunchedEffect(qr, partId) {
        if (qr != null) {
            try {
                val json = JSONObject(qr)
                partIdState = json.getLong("partId").toString()
                partNameState = json.getString("partName")
                partCodeState = json.getString("partCode")
                requestQuantity = json.optInt("quantity", 1).toString()
                isPreFilled = true
            } catch (e: Exception) {
                Toast.makeText(context, "잘못된 QR 코드입니다.", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        } else if (partId != null && partId != 0L) {
            partIdState = partId.toString()
            partNameState = partName ?: ""
            partCodeState = partCode ?: ""
            priceState = price ?: 0
            safetyStockQtyState = safetyStockQty ?: 0
            isPreFilled = true
        }
    }

    val creationState by viewModel.purchaseOrderCreationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isFormValid by remember {
        derivedStateOf {
            (partIdState.toLongOrNull() ?: 0L) > 0L &&
            partNameState.isNotBlank() &&
            partCodeState.isNotBlank() &&
            (requestQuantity.toIntOrNull() ?: 0) > 0
        }
    }

    LaunchedEffect(creationState) {
        if (creationState.isSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("재고 요청이 완료되었습니다.")
            }
            navController.navigate(Screen.InventoryHome.createRoute(filter = "재고신청")) {
                popUpTo(Screen.InventoryRequestForm.route) { inclusive = true }
                launchSingleTop = true
            }
            viewModel.resetPurchaseOrderCreationState()
        }
        creationState.error?.let {
            scope.launch { snackbarHostState.showSnackbar("오류: $it") }
            viewModel.resetPurchaseOrderCreationState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("재고 신청", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F7))
            )
        },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding() + 80.dp // 버튼 공간 확보
                    )
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 기존 카드 입력 필드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TitledTextField(
                            label = "부품아이디",
                            value = partIdState,
                            onValueChange = { partIdState = it },
                            readOnly = isPreFilled,
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TitledTextField(
                            label = "부품명",
                            value = partNameState,
                            onValueChange = { partNameState = it },
                            readOnly = isPreFilled
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TitledTextField(
                            label = "부품코드",
                            value = partCodeState,
                            onValueChange = { partCodeState = it },
                            readOnly = isPreFilled
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val safetyStockValue = if (isPreFilled) safetyStockQtyState.toString() else "0"
                        TitledTextField(
                            label = "안전재고",
                            value = safetyStockValue,
                            onValueChange = {},
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TitledTextField(
                            label = "신청 수량",
                            value = requestQuantity,
                            onValueChange = { requestQuantity = it },
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

            }

            // ✅ 하단 고정 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F7))
                    .padding(start = 16.dp, end = 16.dp, bottom = 48.dp, top = 12.dp) // ✅ 하단 띄움
                    .align(Alignment.BottomCenter)
            ) {
                Button(
                    onClick = {
                        val quantity = requestQuantity.toIntOrNull()
                        if (isFormValid && quantity != null) {
                            viewModel.createPurchaseOrder(
                                partIdState.toLong(),
                                partNameState,
                                partCodeState,
                                priceState,
                                quantity
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                    enabled = isFormValid && !creationState.isLoading
                ) {
                    Text("신청하기", color = Color.White, fontSize = 16.sp)
                }
            }

            // 로딩 표시
            if (creationState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryRequestFormScreenPreview() {
    MaterialTheme {
        InventoryRequestFormScreen(rememberNavController(), viewModel(), 1L, "Part Name", "Part Code", 10000, 10, null)
    }
}
