package com.example.agrofarm.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// ✅ FIXED: Using the correct Coil import that matches your gradle file
import coil3.compose.AsyncImage
import com.example.agrofarm.R
import com.example.agrofarm.model.CattleModel
import com.example.agrofarm.repository.CattleRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.CattleViewModel
import androidx.compose.runtime.collectAsState

class CattleDetailsActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CATTLE_ID = "extra_cattle_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
                CattleDetailsApp(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CattleDetailsApp(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val cattleViewModel = remember { CattleViewModel(CattleRepoImpl()) }

    val activity = (LocalContext.current as? Activity)
    val cattleId = remember { activity?.intent?.getStringExtra(CattleDetailsActivity.EXTRA_CATTLE_ID) ?: "" }

    val cattle by cattleViewModel.cattle.observeAsState()
    val isLoading by cattleViewModel.loading.observeAsState(true)

    LaunchedEffect(cattleId) {
        if (cattleId.isNotBlank()) {
            // ✅ FIXED: Calling the REAL getCattleById function in the ViewModel
            cattleViewModel.getCattleById(cattleId)
        } else {
            Toast.makeText(context, "Invalid Cattle ID.", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cattle Details", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                cattle == null -> CattleNotFoundView(onNavigateBack)
                else -> CattleDetailsContent(cattle = cattle!!)
            }
        }
    }
}

// ✅ FIXED: REMOVED the fake, empty getCattleById function that was causing the crash

@Composable
fun CattleDetailsContent(cattle: CattleModel) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        AsyncImage(
            model = cattle.imageUrl,
            contentDescription = cattle.name,
            modifier = Modifier.fillMaxWidth().height(300.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.cattle),
            error = painterResource(id = R.drawable.cattle)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(cattle.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("ID: ${cattle.id.take(8).uppercase()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailInfoRow("Breed", cattle.breed)
                    Divider(Modifier.padding(vertical = 8.dp))
                    DetailInfoRow("Type", cattle.type)
                    Divider(Modifier.padding(vertical = 8.dp))
                    DetailInfoRow("Age", "${cattle.age} years")
                    Divider(Modifier.padding(vertical = 8.dp))
                    DetailInfoRow("Health Status", cattle.healthStatus)
                    Divider(Modifier.padding(vertical = 8.dp))
                    DetailInfoRow("Last Checkup", cattle.lastCheckup.ifBlank { "N/A" })
                }
            }
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$label: ", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.width(120.dp))
        Text(value, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CattleNotFoundView(onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Not Found",
            modifier = Modifier.size(100.dp),
            alpha = 0.5f,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        )
        Spacer(Modifier.height(24.dp))
        Text("Cattle Not Found", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("This animal may have been sold or removed from the database.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) {
            Text("Go Back")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CattleDetailsPreview() {
    AgroFarmTheme {
        val previewCattle = CattleModel(
            id = "12345678",
            name = "Bessie",
            type = "Cow",
            breed = "Holstein",
            age = 4,
            healthStatus = "Vaccinated",
            lastCheckup = "2024-05-01"
        )
        CattleDetailsContent(cattle = previewCattle)
    }
}