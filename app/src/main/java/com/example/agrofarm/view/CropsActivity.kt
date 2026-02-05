package com.example.agrofarm.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.agrofarm.R
import com.example.agrofarm.model.ProductModel
import com.example.agrofarm.repository.ProductRepoImpl
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.ProductViewModel
import com.example.agrofarm.viewmodel.UserViewModel
import androidx.compose.runtime.collectAsState

class CropsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
                CropsApp(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropsApp(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val currentUser = userViewModel.getCurrentUser()
    val allProducts by productViewModel.allProducts.observeAsState(emptyList())
    val isLoading by productViewModel.loading.observeAsState(false)

    var productToEdit by remember { mutableStateOf<ProductModel?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    
    // Handle edit intent from details page
    val activity = (context as? ComponentActivity)
    val editProductId = remember { activity?.intent?.getStringExtra("EDIT_PRODUCT_ID") }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            productViewModel.getAllProducts()
        } else {
            showAuthDialog = true
        }
    }
    
    // Open edit dialog if coming from details page
    LaunchedEffect(editProductId, allProducts) {
        if (!editProductId.isNullOrBlank() && allProducts.isNotEmpty()) {
            val productToOpen = allProducts.find { it.productId == editProductId }
            if (productToOpen != null) {
                productToEdit = productToOpen
                showDialog = true
                // Clear the intent extra to avoid re-opening
                activity?.intent?.removeExtra("EDIT_PRODUCT_ID")
            }
        }
    }

    val myProducts = remember(allProducts, currentUser) {
        allProducts.filter { it.farmerId == currentUser?.uid }
    }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { onNavigateBack() },
            title = { Text("Login Required") },
            text = { Text("Please login to access your crops inventory.") },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(context, LoginScreen::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                }) { Text("Go to Login") }
            },
            dismissButton = { TextButton(onClick = onNavigateBack) { Text("Cancel") } }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Crops (${myProducts.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { productToEdit = null; showDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, "Add Crop", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && myProducts.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (myProducts.isEmpty()) {
                EmptyView(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myProducts, key = { it.productId }) { product ->
                        CropCard(
                            product = product,
                            onClick = {
                                val intent = Intent(context, CropsDetails::class.java).apply { putExtra(CropsDetails.EXTRA_PRODUCT_ID, product.productId) }
                                context.startActivity(intent)
                            },
                            onEdit = { productToEdit = product; showDialog = true },
                            onDelete = {
                                productViewModel.deleteProduct(product.productId) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) productViewModel.getAllProducts()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEditProductDialog(
            product = productToEdit,
            productViewModel = productViewModel,
            onDismiss = { showDialog = false },
            onConfirm = { updatedProduct ->
                val finalProduct = if (productToEdit == null) {
                    updatedProduct.copy(farmerId = currentUser!!.uid, harvestDate = System.currentTimeMillis())
                } else {
                    updatedProduct
                }

                if (productToEdit == null) {
                    productViewModel.addProduct(finalProduct) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) productViewModel.getAllProducts()
                    }
                } else {
                    productViewModel.updateProduct(finalProduct.productId, finalProduct) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) productViewModel.getAllProducts()
                    }
                }
                showDialog = false
                productToEdit = null
            }
        )
    }
}

@Composable
fun CropCard(product: ProductModel, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Image with organic badge
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.grass),
                    error = painterResource(id = R.drawable.grass)
                )
                // Organic badge
                if (product.isOrganic) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(2.dp),
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Eco,
                            contentDescription = "Organic",
                            tint = Color.White,
                            modifier = Modifier.padding(2.dp).size(12.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (product.isOrganic) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Organic",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
                Text(product.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "₹${String.format("%.0f", product.price)}/${product.unit}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = if (product.quantity > 10) MaterialTheme.colorScheme.primaryContainer else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "${product.quantity} ${product.unit}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (product.quantity > 10) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F)
                        )
                    }
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Options") }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit(); showMenu = false }, leadingIcon = { Icon(Icons.Default.Edit, "Edit") })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { showDeleteDialog = true; showMenu = false }, leadingIcon = { Icon(Icons.Default.Delete, "Delete") })
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Crop?") },
            text = { Text("Are you sure you want to delete ${product.name}?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    product: ProductModel?,
    productViewModel: ProductViewModel,
    onDismiss: () -> Unit,
    onConfirm: (ProductModel) -> Unit
) {
    val context = LocalContext.current
    val isEditing = product != null

    // Categories and Units options
    val categories = listOf("Vegetable", "Fruit", "Grain", "Legume", "Spice", "Herb", "Nut", "Oilseed", "Fodder", "Other")
    val units = listOf("kg", "gram", "quintal", "ton", "piece", "dozen", "bunch", "liter", "bag")

    var name by remember(product?.productId) { mutableStateOf(product?.name ?: "") }
    var price by remember(product?.productId) { mutableStateOf(product?.price?.toString() ?: "") }
    var quantity by remember(product?.productId) { mutableStateOf(product?.quantity?.toString() ?: "") }
    var category by remember(product?.productId) { mutableStateOf(product?.category ?: categories[0]) }
    var unit by remember(product?.productId) { mutableStateOf(product?.unit ?: units[0]) }
    var description by remember(product?.productId) { mutableStateOf(product?.description ?: "") }
    var isOrganic by remember(product?.productId) { mutableStateOf(product?.isOrganic ?: false) }
    var location by remember(product?.productId) { mutableStateOf(product?.location ?: "") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    var imageUri by remember(product?.productId) { mutableStateOf<Uri?>(null) }
    var imageUrl by remember(product?.productId) { mutableStateOf(product?.imageUrl ?: "") }
    var isUploading by remember { mutableStateOf(false) }

    // Validation
    val isFormValid = name.isNotBlank() && price.toDoubleOrNull() != null && quantity.toIntOrNull() != null

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            isUploading = true
            productViewModel.uploadProductImage(context, it) { url ->
                isUploading = false
                if (url != null) {
                    imageUrl = url
                } else {
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Grass,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Edit Crop" else "Add New Crop", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Image Picker
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isUploading) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF4CAF50))
                                Spacer(Modifier.height(8.dp))
                                Text("Uploading...", fontSize = 12.sp, color = Color.Gray)
                            }
                        } else if (imageUrl.isNotBlank() || imageUri != null) {
                            AsyncImage(
                                model = imageUri ?: imageUrl,
                                contentDescription = "Product Image",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Change image overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, "Change Image", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, "Add Image", modifier = Modifier.size(48.dp), tint = Color(0xFF4CAF50))
                                Spacer(Modifier.height(8.dp))
                                Text("Tap to add crop image", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Crop Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Crop Name *") },
                    leadingIcon = { Icon(Icons.Default.Grass, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = name.isBlank() && isEditing
                )

                Spacer(Modifier.height(12.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF4CAF50)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { category = option; categoryExpanded = false },
                                leadingIcon = {
                                    if (category == option) Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Price and Quantity Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) price = it },
                        label = { Text("Price (₹) *") },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color(0xFF4CAF50)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = price.isNotEmpty() && price.toDoubleOrNull() == null
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) quantity = it },
                        label = { Text("Qty *") },
                        leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFF4CAF50)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Unit Dropdown
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded }
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = Color(0xFF4CAF50)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        units.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { unit = option; unitExpanded = false },
                                leadingIcon = {
                                    if (unit == option) Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Farm Section A") }
                )

                Spacer(Modifier.height(12.dp))

                // Organic Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isOrganic) Color(0xFFE8F5E9) else Color(0xFFF5F5F5))
                        .clickable { isOrganic = !isOrganic }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Eco,
                            contentDescription = null,
                            tint = if (isOrganic) Color(0xFF4CAF50) else Color.Gray
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Organic Produce", fontWeight = FontWeight.Medium)
                            Text("Certified organic farming", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = isOrganic,
                        onCheckedChange = { isOrganic = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50), checkedTrackColor = Color(0xFFA5D6A7))
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                    placeholder = { Text("Add details about your crop...") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(ProductModel(
                        productId = product?.productId ?: "",
                        farmerId = product?.farmerId ?: "",
                        name = name.trim(),
                        price = price.toDoubleOrNull() ?: 0.0,
                        quantity = quantity.toIntOrNull() ?: 0,
                        category = category,
                        unit = unit,
                        description = description.trim(),
                        imageUrl = imageUrl,
                        harvestDate = product?.harvestDate ?: 0L,
                        isOrganic = isOrganic,
                        location = location.trim()
                    ))
                },
                enabled = isFormValid && !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(if (isEditing) Icons.Default.Save else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Save Changes" else "Add Crop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun EmptyView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painterResource(id = R.drawable.grass), "Empty", modifier = Modifier.size(100.dp))
        Spacer(Modifier.height(16.dp))
        Text("No crops found.", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        Text("Tap the '+' button to add your first crop.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
fun CropsAppPreview() {
    AgroFarmTheme {
        CropsApp(onNavigateBack = {})
    }
}
