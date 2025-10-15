
package com.ljs.and.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(navController: NavController) {
    var receiptNumber by remember { mutableStateOf("IN - ABCD") }
    var supplier by remember { mutableStateOf("현대 모비스") }
    var part by remember { mutableStateOf("엔진 오일") }
    var location by remember { mutableStateOf("A - 30") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("입고", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            ManualInputBottomBar(
                onCancel = { navController.popBackStack() },
                onComplete = {
                    // TODO: Pass data back and navigate
                    // For now, just pop back to the previous screen
                    navController.navigate(Screen.ReceivingInspection.route)
                }
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TitledTextField(label = "입고 번호", value = receiptNumber, onValueChange = { receiptNumber = it })
            Spacer(modifier = Modifier.height(16.dp))
            TitledTextField(label = "공급 업체", value = supplier, onValueChange = { supplier = it })
            Spacer(modifier = Modifier.height(16.dp))
            TitledTextField(label = "부품", value = part, onValueChange = { part = it })
            Spacer(modifier = Modifier.height(16.dp))
            TitledTextField(label = "위치", value = location, onValueChange = { location = it })
        }
    }
}

@Composable
fun TitledTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused || value.isNotEmpty()) Color(0xFF007BFF) else Color.LightGray

    Column {
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
                unfocusedBorderColor = borderColor
            ),
            singleLine = true
        )
    }
}

@Composable
fun ManualInputBottomBar(onCancel: () -> Unit, onComplete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
        ) {
            Text("완료", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManualInputScreenPreview() {
    MaterialTheme {
        ManualInputScreen(navController = rememberNavController())
    }
}
