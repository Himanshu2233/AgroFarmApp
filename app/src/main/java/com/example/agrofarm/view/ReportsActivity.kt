package com.example.agrofarm.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agrofarm.repository.CattleRepoImpl
import com.example.agrofarm.repository.ProductRepoImpl
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.CattleViewModel
import com.example.agrofarm.viewmodel.ProductViewModel
import com.example.agrofarm.viewmodel.UserViewModel
import androidx.compose.runtime.collectAsState
import java.text.NumberFormat
import java.util.Locale

class ReportsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
                ReportsApp(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsApp(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    // ✅ FIXED: ViewModels created simply, without factories
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val cattleViewModel = remember { CattleViewModel(CattleRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val currentUser = userViewModel.getCurrentUser()
    val allProducts by productViewModel.allProducts.observeAsState(emptyList())
    val allCattle by cattleViewModel.cattleList.observeAsState(emptyList())
    val isLoadingProducts by productViewModel.loading.observeAsState(false)
    val isLoadingCattle by cattleViewModel.loading.observeAsState(false)

    // Fetch data when the screen is first displayed
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, LoginScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        } else {
            productViewModel.getAllProducts()
            cattleViewModel.getAllCattle()
        }
    }

    // Filter data for the current user
    val myProducts = remember(allProducts, currentUser) {
        allProducts.filter { it.farmerId == currentUser?.uid }
    }
    val myCattle = remember(allCattle, currentUser) {
        allCattle?.filter { it.farmerId == currentUser?.uid } ?: emptyList()
    }

    // ✅ FIXED: Calculate report values dynamically
    val totalCrops = myProducts.size
    val totalCattle = myCattle.size
    val totalInventoryValue = myProducts.sumOf { it.price * it.quantity }
    val lowStockItems = myProducts.count { it.quantity < 10 && it.quantity > 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ FIXED: Report cards now show live data
            ReportCard(
                title = "Total Crops",
                value = totalCrops.toString(),
                icon = Icons.Default.Grass,
                color = Color(0xFF4CAF50)
            )

            ReportCard(
                title = "Inventory Value",
                value = "₹${NumberFormat.getNumberInstance(Locale.US).format(totalInventoryValue)}",
                icon = Icons.Default.AttachMoney,
                color = Color(0xFF2196F3)
            )

            ReportCard(
                title = "Total Cattle",
                value = totalCattle.toString(),
                icon = Icons.Default.Pets,
                color = Color(0xFFFF9800)
            )

            ReportCard(
                title = "Low Stock Items",
                value = lowStockItems.toString(),
                icon = Icons.Default.Warning,
                color = if (lowStockItems > 0) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ReportCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    tint = color
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsAppPreview() {
    AgroFarmTheme {
        ReportsApp(onNavigateBack = {})
    }
}