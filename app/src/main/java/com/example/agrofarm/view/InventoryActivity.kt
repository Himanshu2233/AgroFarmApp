package com.example.agrofarm.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.agrofarm.model.InventoryModel
import com.example.agrofarm.repository.InventoryRepoImpl
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.InventoryViewModel
import com.example.agrofarm.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.collectAsState

class InventoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
                InventoryApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryApp() {
    val context = LocalContext.current
    val inventoryViewModel = remember { InventoryViewModel(InventoryRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    
    val inventoryList by inventoryViewModel.inventoryList.observeAsState(emptyList())
    val loading by inventoryViewModel.loading.observeAsState(false)
    val currentUser by userViewModel.user.observeAsState()
    
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryModel?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Handle edit intent from details page
    val activity = (context as? ComponentActivity)
    val editItemId = remember { activity?.intent?.getStringExtra("EDIT_ITEM_ID") }
    
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
            inventoryViewModel.getAllInventoryItems()
        }
    }
    
    // Open edit dialog if coming from details page
    LaunchedEffect(editItemId, inventoryList) {
        if (!editItemId.isNullOrBlank() && inventoryList.isNotEmpty()) {
            val itemToOpen = inventoryList.find { it.id == editItemId }
            if (itemToOpen != null) {
                editingItem = itemToOpen
                showDialog = true
                // Clear the intent extra
                activity?.intent?.removeExtra("EDIT_ITEM_ID")
            }
        }
    }
    
    val categories = listOf("All", "Tools", "Equipment", "Seeds", "Fertilizers", "Pesticides", "Animal Feed", "Other")
    
    val filteredItems = if (selectedCategory == "All") {
        inventoryList.filter { it.farmerId == userId }
    } else {
        inventoryList.filter { it.farmerId == userId && it.category == selectedCategory }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory (${filteredItems.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    editingItem = null
                    showDialog = true 
                },
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Add Item") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        leadingIcon = if (selectedCategory == category) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val totalValue = filteredItems.sumOf { it.purchasePrice * it.quantity }
                val lowStockCount = filteredItems.count { it.quantity <= 5 }
                val needsRepairCount = filteredItems.count { it.condition == "Needs Repair" || it.condition == "Poor" }
                
                StatMiniCard(
                    title = "Total Value",
                    value = "₹${String.format("%.0f", totalValue)}",
                    icon = Icons.Default.AccountBalance,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Low Stock",
                    value = lowStockCount.toString(),
                    icon = Icons.Default.Warning,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Needs Repair",
                    value = needsRepairCount.toString(),
                    icon = Icons.Default.Build,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (selectedCategory == "All") "No inventory items yet" 
                            else "No items in $selectedCategory",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Add your farm tools, equipment, and supplies",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        InventoryItemCard(
                            item = item,
                            onEdit = {
                                editingItem = item
                                showDialog = true
                            },
                            onDelete = {
                                inventoryViewModel.deleteInventoryItem(item.id) { success, msg ->
                                    if (success) {
                                        inventoryViewModel.getAllInventoryItems()
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onClick = {
                                val intent = Intent(context, InventoryDetailsActivity::class.java)
                                intent.putExtra(InventoryDetailsActivity.EXTRA_INVENTORY_ID, item.id)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showDialog) {
        AddEditInventoryDialog(
            item = editingItem,
            farmerId = userId,
            onDismiss = { 
                showDialog = false
                editingItem = null
            },
            onSave = { newItem ->
                if (editingItem != null) {
                    inventoryViewModel.updateInventoryItem(editingItem!!.id, newItem) { success, msg ->
                        if (success) {
                            inventoryViewModel.getAllInventoryItems()
                            showDialog = false
                            editingItem = null
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    inventoryViewModel.addInventoryItem(newItem) { success, msg ->
                        if (success) {
                            inventoryViewModel.getAllInventoryItems()
                            showDialog = false
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            inventoryViewModel = inventoryViewModel
        )
    }
}

private fun UserViewModel.getUserById(userId: String) {}

@Composable
fun StatMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        getCategoryIcon(item.category),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                
                // Category Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    color = getCategoryColor(item.category),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        item.category.take(4),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quantity Badge
                    Surface(
                        color = if (item.quantity <= 5) 
                            Color(0xFFF44336).copy(alpha = 0.1f) 
                        else 
                            MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "${item.quantity} ${item.unit}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.quantity <= 5) 
                                Color(0xFFF44336) 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // Condition Badge
                    Surface(
                        color = getConditionColor(item.condition).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            item.condition,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = getConditionColor(item.condition)
                        )
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.location.isNotEmpty()) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            item.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    
                    Text(
                        "₹${String.format("%.0f", item.purchasePrice)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More options")
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
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFF44336)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFF44336)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditInventoryDialog(
    item: InventoryModel?,
    farmerId: String,
    onDismiss: () -> Unit,
    onSave: (InventoryModel) -> Unit,
    inventoryViewModel: InventoryViewModel
) {
    val context = LocalContext.current
    val isEditing = item != null
    
    var name by remember(item?.id) { mutableStateOf(item?.name ?: "") }
    var category by remember(item?.id) { mutableStateOf(item?.category ?: "Tools") }
    var description by remember(item?.id) { mutableStateOf(item?.description ?: "") }
    var quantity by remember(item?.id) { mutableStateOf(item?.quantity?.toString() ?: "") }
    var unit by remember(item?.id) { mutableStateOf(item?.unit ?: "pieces") }
    var purchaseDate by remember(item?.id) { mutableStateOf(item?.purchaseDate ?: "") }
    var purchasePrice by remember(item?.id) { mutableStateOf(item?.purchasePrice?.toString() ?: "") }
    var condition by remember(item?.id) { mutableStateOf(item?.condition ?: "Good") }
    var location by remember(item?.id) { mutableStateOf(item?.location ?: "") }
    var supplier by remember(item?.id) { mutableStateOf(item?.supplier ?: "") }
    var notes by remember(item?.id) { mutableStateOf(item?.notes ?: "") }
    var imageUrl by remember(item?.id) { mutableStateOf(item?.imageUrl ?: "") }
    var selectedImageUri by remember(item?.id) { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var conditionExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    
    val categories = listOf("Tools", "Equipment", "Seeds", "Fertilizers", "Pesticides", "Animal Feed", "Other")
    val conditions = listOf("New", "Good", "Fair", "Poor", "Needs Repair")
    val units = listOf("pieces", "bags", "kg", "liters", "boxes", "sets", "pairs")
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            isUploading = true
            inventoryViewModel.uploadInventoryImage(context, uri) { url ->
                isUploading = false
                if (url != null) {
                    imageUrl = url
                    Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Edit Item" else "Add Inventory Item")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Image Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (isUploading) {
                        CircularProgressIndicator()
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, Modifier.size(32.dp))
                            Text("Add Photo", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                // Quantity and Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantity *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Purchase Price
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Purchase Price (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    leadingIcon = { Text("₹") }
                )
                
                // Condition Dropdown
                ExposedDropdownMenuBox(
                    expanded = conditionExpanded,
                    onExpandedChange = { conditionExpanded = it }
                ) {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Condition") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(conditionExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = conditionExpanded,
                        onDismissRequest = { conditionExpanded = false }
                    ) {
                        conditions.forEach { cond ->
                            DropdownMenuItem(
                                text = { Text(cond) },
                                onClick = {
                                    condition = cond
                                    conditionExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Storage Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                )
                
                // Supplier
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || quantity.isBlank()) {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val newItem = InventoryModel(
                        id = item?.id ?: "",
                        farmerId = farmerId,
                        name = name,
                        category = category,
                        description = description,
                        quantity = quantity.toIntOrNull() ?: 0,
                        unit = unit,
                        purchaseDate = purchaseDate,
                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                        condition = condition,
                        location = location,
                        imageUrl = imageUrl,
                        supplier = supplier,
                        notes = notes,
                        isActive = item?.isActive ?: true
                    )
                    onSave(newItem)
                },
                enabled = !isUploading
            ) {
                Text(if (isEditing) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Tools" -> Icons.Default.Build
        "Equipment" -> Icons.Default.Settings
        "Seeds" -> Icons.Default.Grass
        "Fertilizers" -> Icons.Default.Science
        "Pesticides" -> Icons.Default.BugReport
        "Animal Feed" -> Icons.Default.Pets
        else -> Icons.Default.Inventory2
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Tools" -> Color(0xFF795548)
        "Equipment" -> Color(0xFF607D8B)
        "Seeds" -> Color(0xFF4CAF50)
        "Fertilizers" -> Color(0xFF8BC34A)
        "Pesticides" -> Color(0xFFFF9800)
        "Animal Feed" -> Color(0xFF9C27B0)
        else -> Color(0xFF2196F3)
    }
}

fun getConditionColor(condition: String): Color {
    return when (condition) {
        "New" -> Color(0xFF4CAF50)
        "Good" -> Color(0xFF8BC34A)
        "Fair" -> Color(0xFFFF9800)
        "Poor" -> Color(0xFFF44336)
        "Needs Repair" -> Color(0xFF9C27B0)
        else -> Color(0xFF607D8B)
    }
}
