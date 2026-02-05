package com.example.agrofarm.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.agrofarm.R
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
                SplashScreenContent()
            }
        }
    }
}

@Composable
fun SplashScreenContent() {
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val context = LocalContext.current as ComponentActivity

    // ✅ FIXED: This LaunchedEffect now handles intelligent navigation
    LaunchedEffect(Unit) {
        // Delay for 2 seconds to show the splash screen
        delay(2000)

        // Check if a user is currently logged in
        val currentUser = userViewModel.getCurrentUser()

        val destination = if (currentUser != null) {
            // If logged in, go to HomeScreen
            HomeScreen::class.java
        } else {
            // If not logged in, go to Welcome Screen (MainActivity)
            MainActivity::class.java
        }

        // Create the intent and navigate
        val intent = Intent(context, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
        context.finish() // Close the splash screen so the user can't go back to it
    }

    // Splash screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    AgroFarmTheme {
        SplashScreenContent()
    }
}