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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
                CropsDetailsApp(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropsDetailsApp(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }

    val activity = (LocalContext.current as? Activity)
    val productId = remember { activity?.intent?.getStringExtra(EXTRA_PRODUCT_ID) ?: "" }

    val product by productViewModel.product.observeAsState()
    val isLoading by productViewModel.loading.observeAsState(true)
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Quick action states
    var showStockDialog by remember { mutableStateOf(false) }
    var showPriceDialog by remember { mutableStateOf(false) }

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
                navigationIcon = { 
                    IconButton(onClick = onNavigateBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) 
                    } 
                },
                actions = {
                    IconButton(onClick = {
                        // Navigate back to CropsActivity with edit flag
                        val intent = android.content.Intent(context, CropsActivity::class.java)
                        intent.putExtra("EDIT_PRODUCT_ID", productId)
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
                product == null -> NotFoundView(onNavigateBack)
                else -> {
                    CropsDetailsContent(
                        product = product!!,
                        onDelete = {
                            productViewModel.deleteProduct(product!!.productId) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) onNavigateBack()
                            }
                        },
                        showStockDialog = showStockDialog,
                        onShowStockDialog = { showStockDialog = it },
                        showPriceDialog = showPriceDialog,
                        onShowPriceDialog = { showPriceDialog = it }
                    )
                }
            }
        }
    }

    if (showDeleteDialog && product != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${product!!.name}?") },
            text = { Text("This action cannot be undone. Are you sure you want to delete this crop from your inventory?") },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.deleteProduct(product!!.productId) { success, msg ->
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

    // Stock Management Dialog
    if (showStockDialog && product != null) {
        var stockChange by remember { mutableStateOf("") }
        var isAdding by remember { mutableStateOf(true) }
        
        AlertDialog(
            onDismissRequest = { showStockDialog = false },
            title = { 
                Text(
                    if (isAdding) "Add Stock" else "Remove Stock",
                    color = if (isAdding) Color(0xFF4CAF50) else Color(0xFF1976D2)
                ) 
            },
            text = {
                Column {
                    Text("Current Stock: ${product!!.quantity} ${product!!.unit}")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Toggle buttons
                    Row {
                        FilterChip(
                            onClick = { isAdding = true },
                            label = { Text("Add", fontSize = 12.sp) },
                            selected = isAdding,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            onClick = { isAdding = false },
                            label = { Text("Remove", fontSize = 12.sp) },
                            selected = !isAdding,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1976D2).copy(alpha = 0.2f)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = stockChange,
                        onValueChange = { stockChange = it.filter { char -> char.isDigit() } },
                        label = { Text("Enter Quantity") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val change = stockChange.toIntOrNull() ?: 0
                        val newQuantity = if (isAdding) {
                            product!!.quantity + change
                        } else {
                            maxOf(0, product!!.quantity - change)
                        }
                        
                        val updatedProduct = product!!.copy(quantity = newQuantity)
                        productViewModel.updateProduct(updatedProduct.productId, updatedProduct) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        showStockDialog = false
                    },
                    enabled = stockChange.toIntOrNull() != null && stockChange.toInt() > 0
                ) { 
                    Text(if (isAdding) "Add" else "Remove") 
                }
            },
            dismissButton = {
                TextButton(onClick = { showStockDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Price Edit Dialog
    if (showPriceDialog && product != null) {
        var newPrice by remember { mutableStateOf(product!!.price.toString()) }
        
        AlertDialog(
            onDismissRequest = { showPriceDialog = false },
            title = { Text("Edit Price", color = Color(0xFFFF9800)) },
            text = {
                Column {
                    Text("Current Price: ₹${String.format("%.0f", product!!.price)}/${product!!.unit}")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it },
                        label = { Text("New Price (₹/${product!!.unit})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Icon(
                                Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = Color(0xFFFF9800)
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceValue = newPrice.toDoubleOrNull() ?: 0.0
                        val updatedProduct = product!!.copy(price = priceValue)
                        productViewModel.updateProduct(updatedProduct.productId, updatedProduct) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        showPriceDialog = false
                    },
                    enabled = newPrice.toDoubleOrNull() != null && newPrice.toDouble() > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) { 
                    Text("Update Price") 
                }
            },
            dismissButton = {
                TextButton(onClick = { showPriceDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CropsDetailsContent(
    product: ProductModel, 
    onDelete: () -> Unit,
    showStockDialog: Boolean,
    onShowStockDialog: (Boolean) -> Unit,
    showPriceDialog: Boolean,
    onShowPriceDialog: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Hero Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.grass),
                error = painterResource(id = R.drawable.grass)
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 100f
                        )
                    )
            )
            
            // Name and category overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    product.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            product.category,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    if (product.isOrganic) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Organic",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            // Price badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "${String.format("%.0f", product.price)}/${product.unit}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
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
            CropQuickStat(
                icon = Icons.Default.Inventory,
                value = "${product.quantity}",
                label = "${product.unit} Available",
                valueColor = if (product.quantity > 10) MaterialTheme.colorScheme.primary else Color(0xFFFF5722)
            )
            CropQuickStat(
                icon = Icons.Default.CalendarMonth,
                value = formatDateShort(product.harvestDate),
                label = "Harvested"
            )
            CropQuickStat(
                icon = Icons.Default.Eco,
                value = if (product.isOrganic) "Yes" else "No",
                label = "Organic",
                valueColor = if (product.isOrganic) Color(0xFF4CAF50) else Color.Gray
            )
        }

        // Quick Actions Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Quick Actions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Remove Stock Button
                    ElevatedButton(
                        onClick = { onShowStockDialog(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFFE3F2FD)
                        )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove Stock", color = Color(0xFF1976D2), fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Add Stock Button
                    ElevatedButton(
                        onClick = { onShowStockDialog(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFFE8F5E8)
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Stock", color = Color(0xFF4CAF50), fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Edit Price Button
                    ElevatedButton(
                        onClick = { onShowPriceDialog(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFFFFF3E0)
                        )
                    ) {
                        Icon(
                            Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Price", color = Color(0xFFFF9800), fontSize = 12.sp)
                    }
                }
            }
        }

        // Stock Alert (if low)
        if (product.quantity <= 10 && product.quantity > 0) {
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
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Low Stock Alert",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            "Only ${product.quantity} ${product.unit} remaining",
                            fontSize = 14.sp,
                            color = Color(0xFFE65100).copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Description Section
        if (product.description.isNotBlank()) {
            CropDetailsSectionCard(
                title = "Description",
                icon = Icons.Default.Description
            ) {
                Text(
                    product.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
            }
        }

        // Product Details Section
        CropDetailsSectionCard(
            title = "Product Details",
            icon = Icons.Default.Info
        ) {
            CropDetailsRow(Icons.Default.Category, "Category", product.category)
            CropDetailsRow(Icons.Default.Scale, "Unit", product.unit)
            CropDetailsRow(Icons.Default.CurrencyRupee, "Price", "₹${String.format("%.2f", product.price)} per ${product.unit}")
            CropDetailsRow(Icons.Default.Inventory2, "Quantity Available", "${product.quantity} ${product.unit}")
            CropDetailsRow(Icons.Default.CalendarMonth, "Harvest Date", formatDate(product.harvestDate))
            CropDetailsRow(Icons.Default.Eco, "Organic", if (product.isOrganic) "Yes - Certified Organic" else "No - Conventional")
        }

        // Location Section
        if (product.location.isNotBlank()) {
            CropDetailsSectionCard(
                title = "Location",
                icon = Icons.Default.LocationOn
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        product.location,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Total Value Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total Inventory Value",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "₹${String.format("%,.2f", product.price * product.quantity)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CropQuickStat(
    icon: ImageVector,
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun CropDetailsSectionCard(
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
fun CropDetailsRow(icon: ImageVector, label: String, value: String) {
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

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun NotFoundView(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Grass,
            contentDescription = "Not Found",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Product Not Found",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "This product may have been deleted or does not exist.",
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
            Text("Go Back to Crops")
        }
    }
}

fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Not specified"
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateShort(timestamp: Long): String {
    if (timestamp == 0L) return "N/A"
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
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
            price = 45.0,
            quantity = 8,
            unit = "kg",
            description = "Juicy, red tomatoes, grown locally and organically. Perfect for salads, sauces, and sandwiches. Harvested at peak ripeness for maximum flavor.",
            isOrganic = true,
            location = "Farm Section A, North Field",
            harvestDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 2)
        )
        CropsDetailsContent(
            product = previewProduct, 
            onDelete = {},
            showStockDialog = false,
            onShowStockDialog = { },
            showPriceDialog = false,
            onShowPriceDialog = { }
        )
    }
}
