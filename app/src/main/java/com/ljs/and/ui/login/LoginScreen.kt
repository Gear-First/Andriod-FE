package com.ljs.and.ui.login

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ljs.and.data.model.AuthManager
import com.ljs.and.data.model.PkceUtil
import java.util.UUID

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    AuthManager.init(context) // AuthManager 초기화

    val onLoginClicked = {
        val codeVerifier = PkceUtil.generateCodeVerifier()
        val codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier)
        val state = UUID.randomUUID().toString()

        AuthManager.codeVerifier = codeVerifier
        AuthManager.state = state

        val uri = Uri.parse("http://34.120.215.23/auth/oauth2/authorize").buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", "gearfirst-client-mobile")
            .appendQueryParameter("redirect_uri", "gearfirst://callback")
            .appendQueryParameter("scope", "openid email offline_access")
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("state", state)
            .build()

        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Gear First",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A80F0)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onLoginClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A80F0))
            ) {
                Text("OAuth2 로그인", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}
