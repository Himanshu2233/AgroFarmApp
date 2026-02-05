package com.example.agrofarm.view

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.agrofarm.model.InventoryModel
import com.example.agrofarm.repository.InventoryRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.InventoryViewModel
import androidx.compose.runtime.collectAsState

class InventoryDetailsActivity : ComponentActivity() {

    companion object {
        const val EXTRA_INVENTORY_ID = "INVENTORY_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        
        val inventoryId = intent.getStringExtra(EXTRA_INVENTORY_ID) ?: ""
        
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
                InventoryDetailsApp(inventoryId, onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailsApp(inventoryId: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val inventoryViewModel = remember { InventoryViewModel(InventoryRepoImpl()) }
    
    val inventoryItem by inventoryViewModel.inventoryItem.observeAsState()
    val loading by inventoryViewModel.loading.observeAsState(false)
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(inventoryId) {
        if (inventoryId.isNotEmpty()) {
            inventoryViewModel.getInventoryItemById(inventoryId)
        } else {
            Toast.makeText(context, "Invalid Item ID.", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Navigate back with edit flag
                        val intent = android.content.Intent(context, InventoryActivity::class.java)
                        intent.putExtra("EDIT_ITEM_ID", inventoryId)
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
    ) { paddingValues ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (inventoryItem == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Item not found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            InventoryDetailsContent(
                item = inventoryItem!!,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
    
    if (showDeleteDialog && inventoryItem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${inventoryItem!!.name}?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        inventoryViewModel.deleteInventoryItem(inventoryItem!!.id) { success, msg ->
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
fun InventoryDetailsContent(
    item: InventoryModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    getCategoryColor(item.category).copy(alpha = 0.3f),
                                    getCategoryColor(item.category).copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getCategoryIcon(item.category),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = getCategoryColor(item.category).copy(alpha = 0.5f)
                    )
                }
            }
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            ),
                            startY = 100f
                        )
                    )
            )
            
            // Category Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                color = getCategoryColor(item.category),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        getCategoryIcon(item.category),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Text(
                        item.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Title and Price
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Condition Badge
                    Surface(
                        color = getConditionColor(item.condition),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            item.condition,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Price Badge
                    if (item.purchasePrice > 0) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "₹${String.format("%.0f", item.purchasePrice)}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // Quick Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InventoryQuickStat(
                icon = Icons.Default.Numbers,
                value = "${item.quantity}",
                label = item.unit,
                color = if (item.quantity <= 5) Color(0xFFF44336) else Color(0xFF4CAF50)
            )
            InventoryQuickStat(
                icon = Icons.Default.AccountBalance,
                value = "₹${String.format("%.0f", item.purchasePrice * item.quantity)}",
                label = "Total Value",
                color = Color(0xFF2196F3)
            )
            InventoryQuickStat(
                icon = Icons.Default.Build,
                value = item.condition,
                label = "Condition",
                color = getConditionColor(item.condition)
            )
        }
        
        // Low Stock Alert
        if (item.quantity <= 5) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Low Stock Alert",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                        Text(
                            "Only ${item.quantity} ${item.unit} remaining. Consider restocking soon.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
        
        // Needs Repair Alert
        if (item.condition == "Needs Repair" || item.condition == "Poor") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = Color(0xFF9C27B0),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Maintenance Required",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9C27B0)
                        )
                        Text(
                            "This item is in ${item.condition.lowercase()} condition and may need repair or replacement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
        
        // Description Section
        if (item.description.isNotEmpty()) {
            InventorySectionCard(title = "Description") {
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        
        // Item Details Section
        InventorySectionCard(title = "Item Details") {
            InventoryDetailsRow("Category", item.category)
            InventoryDetailsRow("Quantity", "${item.quantity} ${item.unit}")
            InventoryDetailsRow("Condition", item.condition)
            if (item.purchasePrice > 0) {
                InventoryDetailsRow("Unit Price", "₹${String.format("%.2f", item.purchasePrice)}")
                InventoryDetailsRow("Total Value", "₹${String.format("%.2f", item.purchasePrice * item.quantity)}")
            }
            if (item.purchaseDate.isNotEmpty()) {
                InventoryDetailsRow("Purchase Date", item.purchaseDate)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Storage & Supplier Section
        if (item.location.isNotEmpty() || item.supplier.isNotEmpty()) {
            InventorySectionCard(title = "Storage & Supplier") {
                if (item.location.isNotEmpty()) {
                    InventoryDetailsRow("Storage Location", item.location)
                }
                if (item.supplier.isNotEmpty()) {
                    InventoryDetailsRow("Supplier", item.supplier)
                }
                if (item.warrantyExpiry.isNotEmpty()) {
                    InventoryDetailsRow("Warranty Until", item.warrantyExpiry)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        
        // Maintenance Section
        if (item.lastMaintenanceDate.isNotEmpty() || item.nextMaintenanceDate.isNotEmpty()) {
            InventorySectionCard(title = "Maintenance") {
                if (item.lastMaintenanceDate.isNotEmpty()) {
                    InventoryDetailsRow("Last Maintenance", item.lastMaintenanceDate)
                }
                if (item.nextMaintenanceDate.isNotEmpty()) {
                    InventoryDetailsRow("Next Maintenance Due", item.nextMaintenanceDate)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        
        // Notes Section
        if (item.notes.isNotEmpty()) {
            InventorySectionCard(title = "Notes") {
                Text(
                    item.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun InventoryQuickStat(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InventorySectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            content()
        }
    }
}

@Composable
fun InventoryDetailsRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
