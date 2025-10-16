package com.ljs.and.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryRequestFormScreen(
    navController: NavHostController
) {
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                FormTextField(label = "품목명", value = itemName, onValueChange = { itemName = it })
                Spacer(modifier = Modifier.height(16.dp))
                FormTextField(label = "공급 업체", value = supplier, onValueChange = { supplier = it })
                Spacer(modifier = Modifier.height(16.dp))
                FormTextField(label = "요청 수량", value = quantity, onValueChange = { quantity = it })
                Spacer(modifier = Modifier.height(24.dp))

                Text("요청 사유", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    reasonOptions.forEach { reason ->
                        Row(
                            Modifier
                                .selectable(
                                    selected = (reason == selectedReason),
                                    onClick = { selectedReason = reason }
                                )
                                .padding(end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (reason == selectedReason),
                                onClick = { selectedReason = reason }
                            )
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                FormTextField(label = "담당자", value = manager, onValueChange = { manager = it }, readOnly = true)
                Spacer(modifier = Modifier.height(16.dp))
                FormTextField(label = "요청일", value = requestDate, onValueChange = { /* Read-only */ }, readOnly = true)
            }

            Button(
                onClick = { /* TODO: Submit form and navigate up */ navController.navigateUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("신청", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun FormTextField(label: String, value: String, onValueChange: (String) -> Unit, readOnly: Boolean = false) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            readOnly = readOnly,
            enabled = !readOnly,
            colors = TextFieldDefaults.colors(
                disabledTextColor = LocalContentColor.current.copy(LocalContentColor.current.alpha),
                disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun InventoryRequestFormScreenPreview() {
    // Fake NavController for preview
    // InventoryRequestFormScreen(navController = NavHostController(LocalContext.current))
}
