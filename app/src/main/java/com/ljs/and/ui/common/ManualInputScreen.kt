
package com.ljs.and.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(navController: NavController, flowType: String) {
    val isReceiving = flowType == "receiving"
    val title = if (isReceiving) "입고" else "출고"
    val idLabel = if (isReceiving) "입고 번호" else "출고 번호"
    val partnerLabel = if (isReceiving) "공급 업체" else "거래처"

    var receivingNumber by remember { mutableStateOf("IN - ABCD") }
    var supplier by remember { mutableStateOf("현대 모비스") }
    var partNameAndCode by remember { mutableStateOf("엔진 오일 / EO-12345") }
    var quantity by remember { mutableStateOf("100") }
    var storageLocation by remember { mutableStateOf("A - 30") }
    var inspector by remember { mutableStateOf("홍길동") }
    val inspectionDateTime by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())) }

    var isDefective by remember { mutableStateOf(false) }
    var defectType by remember { mutableStateOf<String?>(null) }
    var remarks by remember { mutableStateOf("") }

    val isFormValid by derivedStateOf {
        receivingNumber.isNotBlank() &&
                supplier.isNotBlank() &&
                partNameAndCode.isNotBlank() &&
                quantity.isNotBlank() &&
                storageLocation.isNotBlank() &&
                inspector.isNotBlank() &&
                (!isDefective || (defectType != null && remarks.isNotBlank()))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            ManualInputBottomBar(
                onCancel = { navController.popBackStack() },
                onComplete = {
                    // 1. "검수 완료" 상태를 ReceivingInspectionScreen으로 전달합니다.
                    navController.getBackStackEntry(Screen.ReceivingInspection.route).savedStateHandle.set("manualInputCompleted", true)
                    // 2. BarcodeScanScreen을 건너뛰고 ReceivingInspectionScreen으로 돌아갑니다.
                    navController.popBackStack(Screen.ReceivingInspection.route, inclusive = false)
                },
                isCompleteEnabled = isFormValid
            )
        },
        containerColor = Color(0xFFF0F2F5) // A slightly gray background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Part Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("부품 정보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TitledTextField(label = "부품명/코드", value = partNameAndCode, onValueChange = { partNameAndCode = it })
                    TitledTextField(label = partnerLabel, value = supplier, onValueChange = { supplier = it })
                    TitledTextField(label = idLabel, value = receivingNumber, onValueChange = { receivingNumber = it })
                }
            }

            // Inspection Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("검수 정보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TitledTextField(
                            label = "입고 수량",
                            value = quantity,
                            onValueChange = { quantity = it },
                            modifier = Modifier.weight(1f)
                        )
                        TitledTextField(
                            label = "보관위치",
                            value = storageLocation,
                            onValueChange = { storageLocation = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    TitledTextField(label = "검수자 명", value = inspector, onValueChange = { inspector = it })
                    TitledTextField(label = "검수 일시", value = inspectionDateTime, onValueChange = {}, readOnly = true)
                }
            }

            // Defect Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DefectSelection(
                        isDefective = isDefective,
                        onDefectiveChange = { isDefective = it },
                        defectType = defectType,
                        onDefectTypeChange = { defectType = it },
                        remarks = remarks,
                        onRemarksChange = { remarks = it }
                    )
                }
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
    readOnly: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
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
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true,
            readOnly = readOnly
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefectSelection(
    isDefective: Boolean,
    onDefectiveChange: (Boolean) -> Unit,
    defectType: String?,
    onDefectTypeChange: (String) -> Unit,
    remarks: String,
    onRemarksChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val defectTypes = listOf("파손", "오염", "누락")

    Column {
        Text("불량 여부", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = !isDefective,
                onClick = { onDefectiveChange(false) }
            )
            Text("정상")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = isDefective,
                onClick = { onDefectiveChange(true) }
            )
            Text("불량")
        }

        if (isDefective) {
            Spacer(modifier = Modifier.height(16.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    value = defectType ?: "불량 유형 선택",
                    onValueChange = {},
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF007BFF),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    defectTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                onDefectTypeChange(selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TitledTextField(
                label = "비고",
                value = remarks,
                onValueChange = onRemarksChange
            )
        }
    }
}

@Composable
fun ManualInputBottomBar(onCancel: () -> Unit, onComplete: () -> Unit, isCompleteEnabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
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
            Text("검수 확인", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManualInputScreenPreview() {
    MaterialTheme {
        ManualInputScreen(navController = rememberNavController(), flowType = "receiving")
    }
}
