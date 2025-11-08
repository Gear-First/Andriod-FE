package com.ljs.and.ui.common

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import com.ljs.and.ui.releasing.ReleasingViewModel

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    navController: NavController,
    flowType: String,
    noteId: Long,
    lineId: Long,
    currentQty: Int,
    orderedQty: Int,
    lineRemark: String?,
    receivingViewModel: ReceivingViewModel,
    releasingViewModel: ReleasingViewModel
) {
    val isReceiving = flowType == "receiving"

    val releasingUiState by releasingViewModel.uiState.collectAsState()
    val receivingUiState by receivingViewModel.uiState.collectAsState()

    val (noteDetail, line, isLoading) = if (isReceiving) {
        Triple(receivingUiState.selectedReceivingNoteDetail, receivingUiState.selectedReceivingNoteDetail?.lines?.find { it.lineId == lineId }, receivingUiState.isLoading)
    } else {
        Triple(releasingUiState.selectedShippingNoteDetail, releasingUiState.selectedShippingNoteDetail?.lines?.find { it.lineId == lineId }, releasingUiState.isLoading)
    }

    LaunchedEffect(noteId) {
        if (noteId != -1L) {
            if (isReceiving) {
                if (receivingUiState.selectedReceivingNoteDetail?.noteId != noteId) {
                    receivingViewModel.loadReceivingNoteDetail(noteId)
                }
            } else {
                if (releasingUiState.selectedShippingNoteDetail?.noteId != noteId) {
                    releasingViewModel.loadShippingNoteDetail(noteId)
                }
            }
        }
    }

    var quantity by remember { mutableStateOf(currentQty.toString()) }
    var rejected by remember { mutableStateOf(false) } // For receiving
    var currentLineRemark by remember { mutableStateOf(lineRemark ?: "") }

    val title = if (isReceiving) "검수 수량 입력" else "피킹 수량 입력"
    val quantityLabel = if (isReceiving) "검수 수량" else "피킹 수량"

    val bottomButtonText = when {
        isReceiving && rejected -> "재입고 신청"
        isReceiving -> "검수 확인"
        else -> "피킹 확인"
    }

    val isFormValid by derivedStateOf { quantity.toIntOrNull() != null }

    val snackbarHostState = remember { SnackbarHostState() }

    val TAG = "ManualInputScreen"

    LaunchedEffect(receivingUiState.errorMessage) {
        receivingUiState.errorMessage?.let {
            snackbarHostState.showSnackbar("오류: $it")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F7))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            if (isLoading && line == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
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
                        if (isReceiving) {
                            val receivingNote = noteDetail as? com.ljs.and.data.model.ReceivingNoteDetail
                            val receivingLine = line as? com.ljs.and.data.model.ReceivingLine
                            TitledTextField(label = "공급업체", value = receivingNote?.supplierName ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "부품명", value = receivingLine?.product?.name ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "부품코드", value = receivingLine?.product?.lot ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "입고번호", value = receivingNote?.receivingNo ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "요청수량", value = orderedQty.toString(), onValueChange = {}, readOnly = true)
                        } else {
                            val shippingNote = noteDetail as? com.ljs.and.data.model.ShippingNoteDetail
                            val shippingLine = line as? com.ljs.and.data.model.ShippingLine
                            TitledTextField(label = "거래처", value = shippingNote?.branchName ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "부품명", value = shippingLine?.product?.name ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "부품코드", value = shippingLine?.product?.lot ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "출고번호", value = shippingNote?.shippingNo ?: "", onValueChange = {}, readOnly = true)
                            TitledTextField(label = "요청수량", value = shippingLine?.orderedQty.toString(), onValueChange = {}, readOnly = true)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

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
                                    value = currentLineRemark,
                                    onValueChange = { currentLineRemark = it }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                ManualInputBottomBar(
                    onCancel = { navController.popBackStack() },
                    onComplete = {
                        if (isReceiving && rejected) {
                            receivingViewModel.processRejectedItemAndReRequest(lineId, orderedQty, currentLineRemark)
                            navController.popBackStack(Screen.ReceivingInspection.createRoute(false), inclusive = false)
                        } else {
                            try {
                                val targetRoute = if (isReceiving) Screen.ReceivingInspection.createRoute(false) else Screen.ReleasingPicking.createRoute(noteId, false)
                                navController.getBackStackEntry(targetRoute).savedStateHandle.let {
                                    val qty = quantity.toIntOrNull() ?: 0
                                    it["lineId"] = lineId
                                    if (isReceiving) {
                                        it["inspectedQty"] = qty
                                        it["rejected"] = rejected
                                        it["lineRemark"] = if (rejected) currentLineRemark else null
                                    } else {
                                        it["pickedQty"] = qty
                                    }
                                }
                                navController.popBackStack(targetRoute, inclusive = false)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error during onComplete", e)
                            }
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
            enabled = isCompleteEnabled
        ) {
            Text(buttonText, color = Color.White)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ManualInputScreenPreview() {
    MaterialTheme {
        ManualInputScreen(
            navController = rememberNavController(),
            flowType = "receiving",
            noteId = 1,
            lineId = 1,
            currentQty = 10,
            orderedQty = 10,
            lineRemark = "Sample Remark",
            receivingViewModel = viewModel(),
            releasingViewModel = viewModel()
        )
    }
}
