package com.ljs.and.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ljs.and.data.model.TokenManager
import com.ljs.and.ui.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(1500) // 1.5 seconds delay
        val destination = if (TokenManager.getAccessToken() != null) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }
//        val destination = Screen.Home.route // Directly navigate to home
        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Gear First",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A80F0)
        )
    }
}
