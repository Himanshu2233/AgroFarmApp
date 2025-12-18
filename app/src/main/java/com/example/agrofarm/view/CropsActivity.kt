package com.example.agrofarm.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agrofarm.R
import com.example.agrofarm.model.ProductModel
import com.example.agrofarm.repository.ProductRepoImpl
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.viewmodel.ProductViewModel
import com.example.agrofarm.viewmodel.UserViewModel

/**
 * CropsActivity - Main Activity for managing crops
 */
class CropsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgroFarmTheme {
                CropsApp(onNavigateBack = { finish() })
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropsApp(onNavigateBack: () -> Unit) {val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val allProducts by productViewModel.allProducts.observeAsState(emptyList())
    val isLoading by productViewModel.loading.observeAsState(false)

    // Get current user
    val currentUser = userViewModel.getCurrentUser()

    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductModel?>(null) }
    var showAuthDialog by remember { mutableStateOf(false) }

    // ✅ FIXED: Check auth and load products
    LaunchedEffect(currentUser) {
        android.util.Log.d("CropsApp", "Current User: ${currentUser?.uid ?: "NULL"}")
        android.util.Log.d("CropsApp", "Current User Email: ${currentUser?.email ?: "NULL"}")

        if (currentUser != null) {
            productViewModel.getAllProducts()
        } else {
            showAuthDialog = true
        }
    }

    // Filter current user's products
    val myProducts = remember(allProducts, currentUser) {
        if (currentUser != null) {
            allProducts.filter { it.farmerId == currentUser.uid }
        } else {
            emptyList()
        }
    }

    // ✅ Auth Error Dialog
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Login Required") },
            text = { Text("Please login to access your crops inventory.") },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(context, LoginScreen::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Go to Login")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onNavigateBack()
                }) {
                    Text("Cancel")
                }
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Crops", fontWeight = FontWeight.Bold)
                        Text(
                            "${myProducts.size} items",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (currentUser != null) {
                FloatingActionButton(
                    onClick = {
                        productToEdit = null
                        showAddDialog = true
                    },
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Icon(Icons.Default.Add, "Add Crop", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading && myProducts.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF4CAF50)
                    )
                }
                myProducts.isEmpty() -> {
                    EmptyView(Modifier.fillMaxSize())
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = myProducts,
                            key = { it.productId }
                        ) { product ->
                            CropCard(
                                product = product,
                                onClick = {
                                    val intent = Intent(context, CropsDetails::class.java).apply {
                                        putExtra(CropsDetails.EXTRA_PRODUCT_ID, product.productId)
                                    }
                                    context.startActivity(intent)
                                },
                                onEdit = {
                                    productToEdit = product
                                    showAddDialog = true
                                },
                                onDelete = {
                                    productViewModel.deleteProduct(product.productId) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) productViewModel.getAllProducts()
                                    }
                                }
                            )
                        }

                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog && currentUser != null) {
        AddEditProductDialog(
            product = productToEdit,
            onDismiss = {
                showAddDialog = false
                productToEdit = null
            },
            onConfirm = { updatedProduct ->
                if (productToEdit == null) {
                    val newProduct = updatedProduct.copy(
                        farmerId = currentUser.uid,
                        harvestDate = System.currentTimeMillis()
                    )
                    productViewModel.addProduct(newProduct) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) {
                            showAddDialog = false
                            productToEdit = null
                            productViewModel.getAllProducts()
                        }
                    }
                } else {
                    productViewModel.updateProduct(
                        productToEdit!!.productId,
                        updatedProduct
                    ) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) {
                            showAddDialog = false
                            productToEdit = null
                            productViewModel.getAllProducts()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun CropCard(
    product: ProductModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.category,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${String.format("%.0f", product.price)}/${product.unit}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )

                    Surface(
                        color = if (product.quantity > 10) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = "Stock",
                                modifier = Modifier.size(14.dp),
                                colorFilter = ColorFilter.tint(
                                    if (product.quantity > 10) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${product.quantity}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (product.quantity > 10) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More options", tint = Color.Gray)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Crop?") },
            text = { Text("Are you sure you want to delete ${product.name}?") },
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
fun EmptyView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "No crops",
            modifier = Modifier.size(120.dp),
            alpha = 0.3f,
            colorFilter = ColorFilter.tint(Color.Gray)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No crops in inventory",
            fontSize = 20.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Tap + button to add your first crop",
            fontSize = 15.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    product: ProductModel?,
    onDismiss: () -> Unit,
    onConfirm: (ProductModel) -> Unit
) {
    val isEditing = product != null

    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(if (isEditing) product?.price.toString() else "") }
    var quantity by remember { mutableStateOf(if (isEditing) product?.quantity.toString() else "") }
    var category by remember { mutableStateOf(product?.category ?: "Vegetables") }
    var unit by remember { mutableStateOf(product?.unit ?: "kg") }
    var location by remember { mutableStateOf(product?.location ?: "") }
    var isOrganic by remember { mutableStateOf(product?.isOrganic ?: false) }

    val categories = listOf("Vegetables", "Fruits", "Grains", "Dairy", "Spices", "Others")
    val units = listOf("kg", "g", "dozen", "piece", "liter", "bag")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditing) "Edit Crop" else "Add New Crop",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Crop Name *") },
                    placeholder = { Text("e.g., Tomatoes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Brief description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price *") },
                        placeholder = { Text("50") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        prefix = { Text("₹ ") }
                    )

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity *") },
                        placeholder = { Text("100") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var expandedCategory by remember { mutableStateOf(false) }
                    var expandedUnit by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expandedCategory)
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedUnit,
                        onExpandedChange = { expandedUnit = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expandedUnit)
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedUnit,
                            onDismissRequest = { expandedUnit = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        expandedUnit = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("Farm location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isOrganic,
                        onCheckedChange = { isOrganic = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50))
                    )
                    Text("Organic Product", fontSize = 15.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()) {
                        val updatedProduct = ProductModel(
                            productId = product?.productId ?: "",
                            farmerId = product?.farmerId ?: "",
                            name = name,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            quantity = quantity.toIntOrNull() ?: 0,
                            category = category,
                            unit = unit,
                            location = location,
                            isOrganic = isOrganic,
                            harvestDate = product?.harvestDate ?: 0L,
                            imageUrl = product?.imageUrl ?: ""
                        )
                        onConfirm(updatedProduct)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
            ) {
                Text(if (isEditing) "Update" else "Add Crop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CropsAppPreview() {
    AgroFarmTheme {
        CropsApp(onNavigateBack = {})
    }
}