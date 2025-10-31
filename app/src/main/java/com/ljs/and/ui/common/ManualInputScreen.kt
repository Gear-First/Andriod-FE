package com.ljs.and.ui.common

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.Screen

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    navController: NavController,
    flowType: String,
    lineId: Long,
    currentQty: Int
) {
    val isReceiving = flowType == "receiving"
    val title = if (isReceiving) "검수 수량 입력" else "피킹 수량 입력"
    val quantityLabel = if (isReceiving) "검수 수량" else "피킹 수량"
    val bottomButtonText = if (isReceiving) "검수 확인" else "피킹 확인"

    var quantity by remember { mutableStateOf(currentQty.toString()) }
    var hasIssue by remember { mutableStateOf(false) }

    val isFormValid by derivedStateOf {
        quantity.toIntOrNull() != null
    }

    val TAG = "ManualInputScreen"

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
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {

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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    IssueSelection(
                        hasIssue = hasIssue,
                        onIssueChange = { hasIssue = it }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ManualInputBottomBar(
                onCancel = { navController.popBackStack() },
                onComplete = {
                    Log.d(TAG, "Complete button clicked.")
                    try {
                        val targetRoute = if (isReceiving) Screen.ReceivingInspection.route else Screen.ReleasingPicking.route
                        navController.getBackStackEntry(targetRoute).savedStateHandle.let {
                            val qty = quantity.toIntOrNull() ?: 0
                            it["lineId"] = lineId
                            if (isReceiving) {
                                it["inspectedQty"] = qty
                            } else {
                                it["pickedQty"] = qty
                            }
                            it["hasIssue"] = hasIssue
                            Log.d(TAG, "Data set to savedStateHandle for $targetRoute: lineId=$lineId, qty=$qty, issue=$hasIssue")
                        }
                        navController.popBackStack(targetRoute, inclusive = false)
                        Log.d(TAG, "Popped back stack to $targetRoute.")
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
    hasIssue: Boolean,
    onIssueChange: (Boolean) -> Unit
) {
    Column {
        Text("이슈 여부", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = !hasIssue,
                onClick = { onIssueChange(false) }
            )
            Text("정상")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = hasIssue,
                onClick = { onIssueChange(true) }
            )
            Text("이슈 발생")
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
        ManualInputScreen(navController = rememberNavController(), flowType = "receiving", lineId = 1L, currentQty = 90)
    }
}
