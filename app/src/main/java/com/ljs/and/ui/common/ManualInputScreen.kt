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
    val title = if (isReceiving) "입고 수기입력" else "출고 수기입력"
    val idLabel = if (isReceiving) "입고 번호" else "출고 번호"
    val partnerLabel = if (isReceiving) "공급 업체" else "거래처"
    val quantityLabel = if (isReceiving) "입고 수량" else "출고 수량"
    val userLabel = if (isReceiving) "검수자 명" else "피킹 담당자"
    val dateTimeLabel = if (isReceiving) "검수 일시" else "피킹 일시"
    val bottomButtonText = if (isReceiving) "검수 확인" else "피킹 확인"

    var itemNumber by remember { mutableStateOf(if (isReceiving) "IN - ABCD" else "OUT - EFGH") }
    var partner by remember { mutableStateOf("현대 모비스") }
    var partName by remember { mutableStateOf("엔진 오일") }
    var partCode by remember { mutableStateOf("EO-12345") }
    var quantity by remember { mutableStateOf("100") }
    var storageLocation by remember { mutableStateOf("A - 30") }
    var user by remember { mutableStateOf("홍길동") }
    val processingDateTime by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())) }

    var isDefective by remember { mutableStateOf(false) }
    var defectType by remember { mutableStateOf<String?>(null) }
    var remarks by remember { mutableStateOf("") }

    val isFormValid by derivedStateOf {
        itemNumber.isNotBlank() &&
                partner.isNotBlank() &&
                partName.isNotBlank() &&
                partCode.isNotBlank() &&
                quantity.isNotBlank() &&
                storageLocation.isNotBlank() &&
                user.isNotBlank() &&
                (!isDefective || (defectType != null))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F7))
            )
        },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding() - 15.dp,
                    bottom = innerPadding.calculateBottomPadding()
                )
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("부품 정보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TitledTextField(label = "부품명", value = partName, onValueChange = { partName = it })
                    TitledTextField(label = "부품코드", value = partCode, onValueChange = { partCode = it })
                    TitledTextField(label = partnerLabel, value = partner, onValueChange = { partner = it })
                    TitledTextField(label = idLabel, value = itemNumber, onValueChange = { itemNumber = it })
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(if (isReceiving) "검수 정보" else "피킹 정보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TitledTextField(
                            label = quantityLabel,
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
                    TitledTextField(label = userLabel, value = user, onValueChange = { user = it })
                    TitledTextField(label = dateTimeLabel, value = processingDateTime, onValueChange = {}, readOnly = true)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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

            ManualInputBottomBar(
                onCancel = { navController.popBackStack() },
                onComplete = {
                    if (isReceiving) {
                        navController.getBackStackEntry(Screen.ReceivingInspection.route).savedStateHandle["manualInputCompleted"] = true
                        navController.popBackStack(Screen.ReceivingInspection.route, false)
                    } else { // Releasing
                        navController.getBackStackEntry(Screen.ReleasingPicking.route).savedStateHandle["manualInputCompleted"] = true
                        navController.popBackStack(Screen.ReleasingPicking.route, false)
                    }
                },
                isCompleteEnabled = isFormValid,
                buttonText = bottomButtonText
            )
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
fun ManualInputBottomBar(onCancel: () -> Unit, onComplete: () -> Unit, isCompleteEnabled: Boolean, buttonText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.5.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.dp, Color.LightGray),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
            ) {
            Text("취소", color = Color.Black)
        }
        Spacer(modifier = Modifier.width(24.dp))
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
            enabled = isCompleteEnabled,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
        ) {
            Text(buttonText, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManualInputScreenReceivingPreview() {
    MaterialTheme {
        ManualInputScreen(navController = rememberNavController(), flowType = "receiving")
    }
}

@Preview(showBackground = true)
@Composable
fun ManualInputScreenReleasingPreview() {
    MaterialTheme {
        ManualInputScreen(navController = rememberNavController(), flowType = "releasing")
    }
}
