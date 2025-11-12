package com.ljs.and.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TitledTextField(
    label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier,
    readOnly: Boolean = false, singleLine: Boolean = true, keyboardType: KeyboardType = KeyboardType.Text
) {
    var isFocused by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp), color = Color.DarkGray)
        }
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().onFocusChanged { focusState -> isFocused = focusState.isFocused },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF007BFF),
                unfocusedBorderColor = Color.LightGray,
                unfocusedContainerColor = if (readOnly) Color(0xFFF5F5F5) else Color.White,
                focusedContainerColor = if (readOnly) Color(0xFFF5F5F5) else Color.White
            ),
            readOnly = readOnly, singleLine = singleLine, keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}