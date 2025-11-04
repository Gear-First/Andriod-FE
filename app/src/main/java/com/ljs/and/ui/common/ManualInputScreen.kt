package com.ljs.and.ui.common

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen
import com.ljs.and.ui.receiving.ReceivingViewModel
import com.ljs.and.ui.receiving.ReceivingViewModelFactory

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    navController: NavController,
    flowType: String,
    noteId: Long, 
    lineId: Long,
    currentQty: Int,
    viewModel: ReceivingViewModel = viewModel(factory = ReceivingViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val noteDetail = uiState.selectedReceivingNoteDetail
    val line = noteDetail?.lines?.find { it.lineId == lineId }

    LaunchedEffect(noteId) {
        if (noteId != -1L && noteDetail?.noteId != noteId) {
            viewModel.loadReceivingNoteDetail(noteId)
        }
    }

    val isReceiving = flowType == "receiving"
    var quantity by remember { mutableStateOf(currentQty.toString()) }
    var rejected by remember { mutableStateOf(false) }
    var lineRemark by remember { mutableStateOf("") }

    val title = if (isReceiving) "검수 수량 입력" else "피킹 수량 입력"
    val quantityLabel = when {
        isReceiving && rejected -> "불량 수량"
        isReceiving -> "검수 수량"
        else -> "피킹 수량"
    }
    val bottomButtonText = when {
        isReceiving && rejected -> "재입고 신청"
        isReceiving -> "검수 확인"
        else -> "피킹 확인"
    }

    val isFormValid by derivedStateOf {
        val qty = quantity.toIntOrNull()
        qty != null && (!rejected || (rejected && qty > 0))
    }

    val TAG = "ManualInputScreen"

    LaunchedEffect(uiState.rejectionProcessCompleted) {
        if (uiState.rejectionProcessCompleted) {
            // ReceivingInspectionScreen으로 바로 돌아가도록 수정
            navController.popBackStack(Screen.ReceivingInspection.route, inclusive = false)
            viewModel.clearRejectionProcessEvent() 
        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.isLoading && line == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (isReceiving && noteDetail != null && line != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TitledTextField(label = "공급업체", value = noteDetail.supplierName, onValueChange = {}, readOnly = true)
                            TitledTextField(label = "부품명", value = line.product.name ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "부품코드", value = line.product.serial ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "입고번호", value = noteDetail.receivingNo ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "요청수량", value = line.orderedQty.toString(), onValueChange = {}, readOnly = true)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(quantityLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TitledTextField(
                            label = quantityLabel,
                            value = quantity,
                            onValueChange = { quantity = it },
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                if (isReceiving) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            IssueSelection(
                                rejected = rejected,
                                onIssueChange = { rejected = it }
                            )
                            if (rejected) {
                                Spacer(modifier = Modifier.height(16.dp))
                                TitledTextField(
                                    label = "특이사항",
                                    value = lineRemark,
                                    onValueChange = { lineRemark = it }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                ManualInputBottomBar(
                    onCancel = { navController.popBackStack() },
                    onComplete = {
                        try {
                            if (isReceiving && rejected) {
                                val rejectedQty = quantity.toIntOrNull()
                                if (rejectedQty != null && rejectedQty > 0) {
                                    viewModel.processRejectedItemAndReRequest(lineId, rejectedQty, lineRemark)
                                } else {
                                    Log.e(TAG, "Invalid form for rejection")
                                }
                            } else {
                                val targetRoute = if (isReceiving) Screen.ReceivingInspection.route else Screen.ReleasingPicking.route
                                navController.getBackStackEntry(targetRoute).savedStateHandle.let {
                                    val qty = quantity.toIntOrNull() ?: 0
                                    it["lineId"] = lineId
                                    if (isReceiving) {
                                        it["inspectedQty"] = qty
                                        it["rejected"] = rejected
                                        it["lineRemark"] = if (rejected) lineRemark else null
                                    } else {
                                        it["pickedQty"] = qty
                                    }
                                }
                                navController.popBackStack(targetRoute, inclusive = false)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error during onComplete", e)
                        }
                    },
                    isCompleteEnabled = isFormValid,
                    buttonText = bottomButtonText
                )
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
    keyboardType: KeyboardType = KeyboardType.Text
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
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
fun IssueSelection(
    rejected: Boolean,
    onIssueChange: (Boolean) -> Unit
) {
    Column {
        Text("불량 여부", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = !rejected,
                onClick = { onIssueChange(false) }
            )
            Text("정상")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = rejected,
                onClick = { onIssueChange(true) }
            )
            Text("불량")
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
        ManualInputScreen(navController = rememberNavController(), flowType = "receiving", noteId = -1L, lineId = 1L, currentQty = 90)
    }
}

@Preview(showBackground = true)
@Composable
fun ManualInputScreenReleasingPreview() {
    MaterialTheme {
        ManualInputScreen(navController = rememberNavController(), flowType = "releasing", noteId = -1L, lineId = 1L, currentQty = 90)
    }
}
