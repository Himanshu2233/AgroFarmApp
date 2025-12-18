package com.example.agrofarm.view

import android.content.Intent
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
import androidx.compose.ui.draw.clip
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
import com.example.agrofarm.R
import com.example.agrofarm.model.ProductModel
import com.example.agrofarm.repository.ProductRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*

class CropsDetails : ComponentActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val productId = intent.getStringExtra(EXTRA_PRODUCT_ID) ?: ""

        setContent {
            AgroFarmTheme {
                CropsDetailsApp(
                    productId = productId,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropsDetailsApp(
    productId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }

    val product by productViewModel.product.observeAsState()
    val isLoading by productViewModel.loading.observeAsState(false)

    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            android.util.Log.d("CropsDetails", "Loading product: $productId")
            productViewModel.getProductById(productId)
        } else {
            Toast.makeText(context, "Invalid product ID", Toast.LENGTH_SHORT).show()
            (context as? ComponentActivity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF4CAF50)
                    )
                }
                product == null -> {
                    // ✅ FIXED: Using drawable image instead of Material Icon
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Using logo image with gray tint for error state
                        Image(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = "Product not found",
                            modifier = Modifier.size(100.dp),
                            alpha = 0.3f,
                            colorFilter = ColorFilter.tint(Color.Gray)
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Text(
                            "Product not found",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            "This product may have been deleted or does not exist",
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Go Back to Crops")
                        }
                    }
                }
                else -> {
                    CropsDetailsContent(
                        product = product!!,
                        onEdit = {
                            Toast.makeText(context, "Edit functionality", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            productViewModel.deleteProduct(product!!.productId) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    // Navigate back after successful deletion
                                    (context as? ComponentActivity)?.finish()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CropsDetailsContent(
    product: ProductModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Product Image
        Image(
            painter = painterResource(
                when (product.category) {
                    "Vegetables" -> R.drawable.logo
                    "Fruits" -> R.drawable.logo
                    "Grains" -> R.drawable.logo
                    "Dairy" -> R.drawable.logo
                    else -> R.drawable.logo
                }
            ),
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )

        // Product Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Name and Category
                Text(
                    text = product.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(Modifier.height(4.dp))

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = product.category,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2E7D32)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Price and Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Price",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            "₹${String.format("%.2f", product.price)}/${product.unit}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Available",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            "${product.quantity} ${product.unit}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (product.quantity > 10) Color(0xFF4CAF50) else Color(0xFFFF5722)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(Modifier.height(20.dp))

                // Description
                if (product.description.isNotBlank()) {
                    Text(
                        "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        product.description,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Additional Info
                InfoRow("Location", product.location.ifBlank { "Not specified" })
                InfoRow("Harvest Date", formatDate(product.harvestDate))
                InfoRow(
                    "Type",
                    if (product.isOrganic) "🌱 Organic" else "Regular"
                )

                Spacer(Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit")
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Crop?") },
            text = { 
                Text("Are you sure you want to delete ${product.name}? This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            fontSize = 14.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.SemiBold
        )
    }
}

fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Not specified"
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun CropsDetailsPreview() {
    AgroFarmTheme {
        CropsDetailsContent(
            product = ProductModel(
                productId = "1",
                farmerId = "farmer1",
                name = "Fresh Tomatoes",
                description = "Organic red tomatoes grown in natural conditions",
                price = 45.0,
                quantity = 150,
                category = "Vegetables",
                unit = "kg",
                location = "Punjab Farm",
                isOrganic = true,
                harvestDate = System.currentTimeMillis()
            ),
            onEdit = {},
            onDelete = {}
        )
    }
}