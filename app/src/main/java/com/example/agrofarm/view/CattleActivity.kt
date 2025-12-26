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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agrofarm.R
import com.example.agrofarm.model.CattleModel
import com.example.agrofarm.repository.CattleRepoImpl
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.viewmodel.CattleViewModel
import com.example.agrofarm.viewmodel.UserViewModel

class CattleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgroFarmTheme {
                CattleApp(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CattleApp(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    // ✅ FIXED: ViewModels are created simply, without factories.
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val cattleViewModel = remember { CattleViewModel(CattleRepoImpl()) }

    val currentUser = userViewModel.getCurrentUser()
    val allCattle by cattleViewModel.cattleList.observeAsState(emptyList())
    val isLoading by cattleViewModel.loading.observeAsState(false)

    var showDialog by remember { mutableStateOf<CattleModel?>(null) } // For Add/Edit

    // Check authentication and load data
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, LoginScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        } else {
            cattleViewModel.getAllCattle()
        }
    }

    // Filter cattle belonging to the current user
    val myCattle = remember(allCattle, currentUser) {
        allCattle?.filter { it.farmerId == currentUser?.uid } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Cattle", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = { showDialog = CattleModel() }, // Open dialog for adding
                containerColor = Color(0xFF4CAF50)
            ) {
                Icon(Icons.Default.Add, "Add Cattle", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && myCattle.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (myCattle.isEmpty()) {
                EmptyCattleView(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myCattle, key = { it.id }) { cattle ->
                        CattleCard(
                            cattle = cattle,
                            onEdit = { showDialog = cattle },
                            onDelete = {
                                cattleViewModel.deleteCattle(cattle.id) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) cattleViewModel.getAllCattle()
                                }
                            },
                            onClick = { /* TODO: Navigate to Cattle Details */ }
                        )
                    }
                }
            }
        }
    }

    showDialog?.let { cattle ->
        AddEditCattleDialog(
            cattle = cattle,
            onDismiss = { showDialog = null },
            onConfirm = { updatedCattle ->
                val finalCattle = if (updatedCattle.id.isBlank()) {
                    updatedCattle.copy(farmerId = currentUser!!.uid)
                } else {
                    updatedCattle
                }

                if (finalCattle.id.isBlank()) {
                    cattleViewModel.addCattle(finalCattle) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) cattleViewModel.getAllCattle()
                    }
                } else {
                    cattleViewModel.updateCattle(finalCattle.id, finalCattle) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) cattleViewModel.getAllCattle()
                    }
                }
                showDialog = null
            }
        )
    }
}

@Composable
fun CattleCard(
    cattle: CattleModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = cattle.name,
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cattle.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${cattle.breed} - ${cattle.type}", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Age: ${cattle.age} years", fontSize = 12.sp, color = Color.DarkGray)
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp)) {
                        Text(cattle.healthStatus, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
                    }
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "Options")
                }
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
            title = { Text("Delete Cattle?") },
            text = { Text("Are you sure you want to delete ${cattle.name}?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AddEditCattleDialog(cattle: CattleModel, onDismiss: () -> Unit, onConfirm: (CattleModel) -> Unit) {
    val isEditing = cattle.id.isNotBlank()
    var name by remember { mutableStateOf(cattle.name) }
    var type by remember { mutableStateOf(cattle.type.ifBlank { "Cow" }) }
    var breed by remember { mutableStateOf(cattle.breed) }
    var age by remember { mutableStateOf(if(isEditing) cattle.age.toString() else "") }
    var healthStatus by remember { mutableStateOf(cattle.healthStatus.ifBlank { "Healthy" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Cattle" else "Add Cattle") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (e.g., Cow)") })
                OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Breed") })
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age (years)") })
                OutlinedTextField(value = healthStatus, onValueChange = { healthStatus = it }, label = { Text("Health Status") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(cattle.copy(
                    name = name,
                    type = type,
                    breed = breed,
                    age = age.toIntOrNull() ?: 0,
                    healthStatus = healthStatus
                ))
            }) { Text(if (isEditing) "Save" else "Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EmptyCattleView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Settings, "Empty", modifier = Modifier.size(80.dp), tint = Color.LightGray) // ✅ FIXED: Changed icon
        Spacer(Modifier.height(16.dp))
        Text("No cattle added yet", fontSize = 18.sp, color = Color.Gray)
        Text("Tap + to add cattle", fontSize = 14.sp, color = Color.LightGray)
    }
}

@Preview(showBackground = true)
@Composable
fun CattleAppPreview() {
    AgroFarmTheme {
        CattleApp(onNavigateBack = {})
    }
}