package com.example.agrofarm.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.agrofarm.R
import com.example.agrofarm.model.CattleModel
import com.example.agrofarm.repository.CattleRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.CattleViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource

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

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cattleId) {
        if (cattleId.isNotBlank()) {
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
                navigationIcon = { 
                    IconButton(onClick = onNavigateBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) 
                    } 
                },
                actions = {
                    IconButton(onClick = {
                        // Navigate back to CattleActivity with edit flag
                        val intent = android.content.Intent(context, CattleActivity::class.java)
                        intent.putExtra("EDIT_CATTLE_ID", cattleId)
                        context.startActivity(intent)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Edit, "Edit", tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                cattle == null -> CattleNotFoundView(onNavigateBack)
                else -> CattleDetailsContent(
                    cattle = cattle!!,
                    onDelete = {
                        cattleViewModel.deleteCattle(cattle!!.id) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) onNavigateBack()
                        }
                    }
                )
            }
        }
    }

    if (showDeleteDialog && cattle != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${cattle!!.name}?") },
            text = { Text("This action cannot be undone. Are you sure you want to delete this animal from your records?") },
            confirmButton = {
                Button(
                    onClick = {
                        cattleViewModel.deleteCattle(cattle!!.id) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) onNavigateBack()
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CattleDetailsContent(cattle: CattleModel, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            AsyncImage(
                model = cattle.imageUrl,
                contentDescription = cattle.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.cattle),
                error = painterResource(id = R.drawable.cattle)
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 100f
                        )
                    )
            )
            
            // Name and basic info overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        cattle.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    // Gender indicator
                    if (cattle.gender.isNotBlank()) {
                        Surface(
                            color = if (cattle.gender == "Male") Color(0xFF2196F3) else Color(0xFFE91E63),
                            shape = CircleShape
                        ) {
                            Icon(
                                if (cattle.gender == "Male") Icons.Default.Male else Icons.Default.Female,
                                contentDescription = cattle.gender,
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp).size(16.dp)
                            )
                        }
                    }
                }
                Text(
                    "${cattle.breed} ${cattle.type}",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                if (cattle.tagNumber.isNotBlank()) {
                    Text(
                        "Tag: ${cattle.tagNumber}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Health status badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                color = getHealthStatusColor(cattle.healthStatus),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        cattle.healthStatus.ifBlank { "Unknown" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

        // Quick Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickStatItem(
                icon = Icons.Default.Cake,
                value = "${cattle.age}",
                label = "Years Old"
            )
            QuickStatItem(
                icon = Icons.Default.Scale,
                value = if (cattle.weight > 0) "${cattle.weight.toInt()}" else "N/A",
                label = "kg Weight"
            )
            if (cattle.milkProduction > 0) {
                QuickStatItem(
                    icon = Icons.Default.WaterDrop,
                    value = "${cattle.milkProduction}",
                    label = "L/Day Milk"
                )
            }
        }

        // Pregnancy Alert (if applicable)
        if (cattle.isPregnant) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ChildCare,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Pregnant",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        if (cattle.expectedDelivery.isNotBlank()) {
                            Text(
                                "Expected delivery: ${cattle.expectedDelivery}",
                                fontSize = 14.sp,
                                color = Color(0xFFE65100).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Basic Information Section
        DetailsSectionCard(
            title = "Basic Information",
            icon = Icons.Default.Info
        ) {
            DetailsRow(Icons.Default.Pets, "Type", cattle.type)
            DetailsRow(Icons.Default.Diversity3, "Breed", cattle.breed)
            DetailsRow(Icons.Default.Wc, "Gender", cattle.gender.ifBlank { "Not specified" })
            DetailsRow(Icons.Default.Cake, "Age", "${cattle.age} years")
            DetailsRow(Icons.Default.Scale, "Weight", if (cattle.weight > 0) "${cattle.weight} kg" else "Not recorded")
            if (cattle.tagNumber.isNotBlank()) {
                DetailsRow(Icons.Default.Tag, "Tag Number", cattle.tagNumber)
            }
        }

        // Health & Vaccination Section
        DetailsSectionCard(
            title = "Health & Vaccination",
            icon = Icons.Default.HealthAndSafety
        ) {
            DetailsRow(Icons.Default.Favorite, "Health Status", cattle.healthStatus.ifBlank { "Not specified" })
            DetailsRow(Icons.Default.CalendarMonth, "Last Checkup", cattle.lastCheckup.ifBlank { "Not recorded" })
            DetailsRow(Icons.Default.Vaccines, "Vaccination Status", cattle.vaccinationStatus.ifBlank { "Not recorded" })
            if (cattle.lastVaccination.isNotBlank()) {
                DetailsRow(Icons.Default.EventAvailable, "Last Vaccination", cattle.lastVaccination)
            }
        }

        // Production & Feed Section (for dairy animals)
        if (cattle.milkProduction > 0 || cattle.feedType.isNotBlank()) {
            DetailsSectionCard(
                title = "Production & Feed",
                icon = Icons.Default.Agriculture
            ) {
                if (cattle.milkProduction > 0) {
                    DetailsRow(Icons.Default.WaterDrop, "Daily Milk Production", "${cattle.milkProduction} liters")
                }
                if (cattle.feedType.isNotBlank()) {
                    DetailsRow(Icons.Default.Grass, "Feed Type", cattle.feedType)
                }
            }
        }

        // Purchase Information Section
        if (cattle.purchaseDate.isNotBlank() || cattle.purchasePrice > 0) {
            DetailsSectionCard(
                title = "Purchase Information",
                icon = Icons.Default.ShoppingCart
            ) {
                if (cattle.purchaseDate.isNotBlank()) {
                    DetailsRow(Icons.Default.CalendarMonth, "Purchase Date", cattle.purchaseDate)
                }
                if (cattle.purchasePrice > 0) {
                    DetailsRow(Icons.Default.CurrencyRupee, "Purchase Price", "₹${String.format("%,.0f", cattle.purchasePrice)}")
                }
            }
        }

        // Notes Section
        if (cattle.notes.isNotBlank()) {
            DetailsSectionCard(
                title = "Notes",
                icon = Icons.Default.Notes
            ) {
                Text(
                    cattle.notes,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun QuickStatItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(12.dp).size(24.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun DetailsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DetailsRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getHealthStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "healthy" -> Color(0xFF4CAF50)
        "sick", "quarantine" -> Color(0xFFF44336)
        "under treatment", "recovering" -> Color(0xFFFF9800)
        "pregnant", "lactating" -> Color(0xFF2196F3)
        else -> Color(0xFF9E9E9E)
    }
}

@Composable
fun CattleNotFoundView(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Pets,
            contentDescription = "Not Found",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Cattle Not Found",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "This animal may have been sold or removed from the database.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
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
            name = "Lakshmi",
            type = "Cow",
            breed = "Gir",
            age = 4,
            gender = "Female",
            healthStatus = "Healthy",
            lastCheckup = "2024-05-01",
            weight = 450.0,
            milkProduction = 12.5,
            tagNumber = "GIR-2024-101",
            vaccinationStatus = "Up to date",
            lastVaccination = "2024-03-15",
            feedType = "Green fodder + concentrate",
            purchaseDate = "2020-06-15",
            purchasePrice = 85000.0,
            isPregnant = true,
            expectedDelivery = "2024-08-20",
            notes = "Very calm and easy to handle. Excellent milk quality."
        )
        CattleDetailsContent(cattle = previewCattle, onDelete = {})
    }
}
