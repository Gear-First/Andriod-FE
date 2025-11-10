package com.ljs.and.ui.inventory

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryRequestFormScreen(
    navController: NavHostController,
    viewModel: InventoryViewModel,
    partId: Long,
    partName: String?,
    partCode: String?,
    price: Int,
    safetyStockQty: Int
) {
    val isPreFilled = partId != 0L

    var partIdState by remember { mutableStateOf(if (isPreFilled) partId.toString() else "") }
    var partNameState by remember { mutableStateOf(partName ?: "") }
    var partCodeState by remember { mutableStateOf(partCode ?: "") }
    val priceState = if(isPreFilled) price else 0
    
    var requestQuantity by remember { mutableStateOf("") }

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
                    .padding(top = innerPadding.calculateTopPadding() - 15.dp, bottom = innerPadding.calculateBottomPadding())
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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
                        TitledTextField(label = "부품명", value = partNameState, onValueChange = { partNameState = it }, readOnly = isPreFilled)
                        Spacer(modifier = Modifier.height(16.dp))
                        TitledTextField(label = "부품코드", value = partCodeState, onValueChange = { partCodeState = it }, readOnly = isPreFilled)
                        Spacer(modifier = Modifier.height(16.dp))
                        val safetyStockValue = if (isPreFilled) safetyStockQty.toString() else "0"
                        TitledTextField(label = "안전재고", value = safetyStockValue, onValueChange = {}, readOnly = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        TitledTextField(label = "신청 수량", value = requestQuantity, onValueChange = { requestQuantity = it }, keyboardType = KeyboardType.Number)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val quantity = requestQuantity.toIntOrNull()
                        if (isFormValid && quantity != null) {
                            viewModel.createPurchaseOrder(partIdState.toLong(), partNameState, partCodeState, priceState, quantity)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                    enabled = isFormValid && !creationState.isLoading
                ) {
                    Text("신청하기", color = Color.White, fontSize = 16.sp)
                }
            }

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
        InventoryRequestFormScreen(rememberNavController(), viewModel(), 1L, "Part Name", "Part Code", 10000, 10)
    }
}
