
package com.ljs.and.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ljs.and.ui.common.TitledTextField
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryRequestFormScreen(navController: NavHostController) {
    var itemName by remember { mutableStateOf("IN - ABCD") }
    var supplier by remember { mutableStateOf("현대 모비스") }
    var quantity by remember { mutableStateOf("10") }
    val reasonOptions = listOf("불량", "누락", "소진")
    var selectedReason by remember { mutableStateOf(reasonOptions[0]) }
    var manager by remember { mutableStateOf("이지수") }
    val requestDate by remember { mutableStateOf(SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("재고 신청", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
            ) {
                Text("신청", color = Color.White)
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TitledTextField(label = "품목명", value = itemName, onValueChange = { itemName = it })
            Spacer(modifier = Modifier.height(16.dp))
            TitledTextField(label = "공급 업체", value = supplier, onValueChange = { supplier = it })
            Spacer(modifier = Modifier.height(16.dp))
            TitledTextField(label = "요청 수량", value = quantity, onValueChange = { quantity = it })
            Spacer(modifier = Modifier.height(24.dp))

            Text("요청 사유", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
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
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("담당자", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                TitledTextField(label = "", value = manager, onValueChange = { manager = it }, modifier = Modifier.width(200.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("요청일", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                TitledTextField(label = "", value = requestDate, onValueChange = {}, modifier = Modifier.width(200.dp))
            }
        }
    }
}

@Composable
fun TitledTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused || value.isNotEmpty()) Color(0xFF007BFF) else Color.LightGray

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
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
                unfocusedBorderColor = borderColor
            ),
            singleLine = true
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
