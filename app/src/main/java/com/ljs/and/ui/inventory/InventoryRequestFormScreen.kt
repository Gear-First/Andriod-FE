package com.ljs.and.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryRequestFormScreen(
    navController: NavHostController,
    viewModel: InventoryViewModel = viewModel()
) {
    var partName by remember { mutableStateOf("엔진 오일 필터") }
    var partCode by remember { mutableStateOf("EOF-001") }
    var requestQuantity by remember { mutableStateOf("10") }
    val reasonOptions = listOf("불량", "부족")
    var selectedReason by remember { mutableStateOf(reasonOptions[0]) }
    var requester by remember { mutableStateOf("김신청") }
    val requestDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var notes by remember { mutableStateOf("") }

    val isFormValid by remember {
        derivedStateOf {
            partName.isNotBlank() && partCode.isNotBlank() && requestQuantity.isNotBlank() && requester.isNotBlank()
        }
    }

    Scaffold(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    TitledTextField(label = "부품명", value = partName, onValueChange = { partName = it })
                    Spacer(modifier = Modifier.height(16.dp))
                    TitledTextField(label = "부품코드", value = partCode, onValueChange = { partCode = it })
                    Spacer(modifier = Modifier.height(16.dp))
                    TitledTextField(label = "신청 수량", value = requestQuantity, onValueChange = { requestQuantity = it }, keyboardType = KeyboardType.Number)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("신청 사유", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Row {
                        reasonOptions.forEach { reason ->
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = (reason == selectedReason),
                                        onClick = { selectedReason = reason }
                                    )
                                    .padding(end = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (reason == selectedReason),
                                    onClick = { selectedReason = reason },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF007BFF))
                                )
                                Text(reason)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    TitledTextField(label = "신청자", value = requester, onValueChange = { requester = it })
                    Spacer(modifier = Modifier.height(16.dp))
                    TitledTextField(label = "신청일자", value = requestDate, onValueChange = {}, readOnly = true)
                    Spacer(modifier = Modifier.height(16.dp))
                    TitledTextField(label = "비고", value = notes, onValueChange = { notes = it }, singleLine = false, modifier = Modifier.height(100.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.addInventoryRequest(
                        partName = partName,
                        partCode = partCode,
                        quantity = requestQuantity.toIntOrNull() ?: 0,
                        reason = selectedReason,
                        requester = requester
                    )
                    navController.navigate(Screen.Inventory.route + "?tab=1") {
                        popUpTo(Screen.Inventory.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                enabled = isFormValid
            ) {
                Text("신청하기", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun TitledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp), color = Color.DarkGray)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF007BFF),
                unfocusedBorderColor = Color.LightGray,
                unfocusedContainerColor = if (readOnly) Color(0xFFF5F5F5) else Color.White,
                focusedContainerColor = if (readOnly) Color(0xFFF5F5F5) else Color.White
            ),
            readOnly = readOnly,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryRequestFormScreenPreview() {
    MaterialTheme {
        InventoryRequestFormScreen(navController = rememberNavController())
    }
}
