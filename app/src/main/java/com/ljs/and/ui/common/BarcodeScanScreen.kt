package com.ljs.and.ui.common

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ljs.and.activity.common.QrCameraActivity
import com.ljs.and.ui.Screen

@Composable
fun BarcodeScanScreen(navController: NavController) {
    var qrCode by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            qrCode = data?.getStringExtra("qr_result")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { 
                val intent = Intent(context, QrCameraActivity::class.java)
                cameraLauncher.launch(intent)
            },
            modifier = Modifier.size(128.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Open Camera", modifier = Modifier.fillMaxSize())
        }

        Spacer(modifier = Modifier.height(32.dp))

        qrCode?.let {
            Text(text = "스캔된 QR 코드: $it")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    navController.navigate(Screen.InventoryRequestForm.createRoute(it))
                },
                enabled = it.isNotEmpty()
            ) {
                Text(text = "QR 정보로 신청하기")
            }
        }
    }
}
