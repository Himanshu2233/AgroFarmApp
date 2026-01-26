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
import androidx.compose.material.icons.filled.*
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
import coil3.compose.AsyncImage
import com.example.agrofarm.R
import com.example.agrofarm.model.ProductModel
import com.example.agrofarm.repository.ProductRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.view.CropsDetails.Companion.EXTRA_PRODUCT_ID
import com.example.agrofarm.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.collectAsState

class CropsDetails : ComponentActivity() {

    companion object {
        // Key to pass the product ID between activities
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
                // ✅ FIXED: Activity is now very simple.
                CropsDetailsApp(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropsDetailsApp(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    // ✅ FIXED: ViewModel created simply, without a factory.
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }

    // Get the productId from the activity's intent
    val activity = (LocalContext.current as? Activity)
    val productId = remember { activity?.intent?.getStringExtra(EXTRA_PRODUCT_ID) ?: "" }

    val product by productViewModel.product.observeAsState()
    val isLoading by productViewModel.loading.observeAsState(true) // Start with loading

    // Fetch the product details when the screen is first displayed
    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            productViewModel.getProductById(productId)
        } else {
            Toast.makeText(context, "Invalid Product ID.", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Details", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                product == null -> NotFoundView(onNavigateBack)
                else -> {
                    CropsDetailsContent(
                        product = product!!,
                        onDelete = {
                            productViewModel.deleteProduct(product!!.productId) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) onNavigateBack()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CropsDetailsContent(product: ProductModel, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier.fillMaxWidth().height(300.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.logo),
            error = painterResource(id = R.drawable.logo)
        )

        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(product.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(product.category, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Price", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("₹${String.format("%.2f", product.price)}/${product.unit}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Available", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("${product.quantity} ${product.unit}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (product.quantity > 10) MaterialTheme.colorScheme.primary else Color(0xFFFF5722))
                    }
                }
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(20.dp))

                if (product.description.isNotBlank()) {
                    Text("Description", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(product.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 20.sp)
                    Spacer(Modifier.height(16.dp))
                }

                InfoRow("Location", product.location.ifBlank { "Not specified" })
                InfoRow("Harvest Date", formatDate(product.harvestDate))
                InfoRow("Type", if (product.isOrganic) "🌱 Organic" else "Regular")

                Spacer(Modifier.height(24.dp))

                Button(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))) {
                    Icon(Icons.Default.Delete, "Delete")
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Product")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to permanently delete '${product.name}'?") },
            confirmButton = { Button(onClick = { onDelete(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun NotFoundView(onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painter = painterResource(R.drawable.logo), "Product not found", modifier = Modifier.size(100.dp), alpha = 0.3f, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)))
        Spacer(Modifier.height(24.dp))
        Text("Product Not Found", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("This product may have been deleted or does not exist.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) { Text("Go Back to Crops") }
    }
}

fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Not specified"
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun CropsDetailsPreview() {
    AgroFarmTheme {
        val previewProduct = ProductModel(
            productId = "1",
            name = "Fresh Tomatoes",
            category = "Vegetable",
            price = 2.50,
            quantity = 50,
            unit = "kg",
            description = "Juicy, red tomatoes, grown locally and organically. Perfect for salads, sauces, and sandwiches.",
            isOrganic = true,
            location = "Kathmandu Valley",
            harvestDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 2)
        )
        CropsDetailsContent(product = previewProduct, onDelete = {})
    }
}
