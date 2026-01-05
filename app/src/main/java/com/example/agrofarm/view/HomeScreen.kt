package com.example.agrofarm.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agrofarm.R
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.UserViewModel

class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize theme manager
        ThemeManager.init(this)
        
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            
            AgroFarmTheme(darkTheme = isDarkMode) {
                HomeContent(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { ThemeManager.toggleDarkMode() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    
    // Animation for the theme toggle icon
    val rotationAngle by animateFloatAsState(
        targetValue = if (isDarkMode) 180f else 0f,
        label = "rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AgroFarm", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    // Dark Mode Toggle
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = Color.White,
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                    // Profile
                    IconButton(onClick = {
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                    }) {
                        Icon(Icons.Default.Person, "Profile", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Welcome Card - uses theme colors
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Welcome Back!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Manage your farm efficiently",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                "Farm Management",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureCard("Crops", R.drawable.crop, Modifier.weight(1f)) {
                        context.startActivity(Intent(context, CropsActivity::class.java))
                    }
                    FeatureCard("Weather", R.drawable.weather, Modifier.weight(1f)) {
                        context.startActivity(Intent(context, WeatherActivity::class.java))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureCard("Inventory", R.drawable.inventory, Modifier.weight(1f)) {
                        context.startActivity(Intent(context, CropsActivity::class.java)) // Placeholder
                    }
                    FeatureCard("Cattle", R.drawable.cattle, Modifier.weight(1f)) {
                        context.startActivity(Intent(context, CattleActivity::class.java))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureCard("Reports", R.drawable.report, Modifier.weight(1f)) {
                        context.startActivity(Intent(context, ReportsActivity::class.java))
                    }
                    FeatureCard("Settings", R.drawable.setting, Modifier.weight(1f)) {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(title: String, iconRes: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(120.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = iconRes), contentDescription = title, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun HomeScreenPreviewLight() {
    AgroFarmTheme(darkTheme = false) {
        HomeContent(isDarkMode = false)
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun HomeScreenPreviewDark() {
    AgroFarmTheme(darkTheme = true) {
        HomeContent(isDarkMode = true)
    }
}