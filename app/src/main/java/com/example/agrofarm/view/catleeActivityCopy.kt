//package com.example.agrofarm.view
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.viewModels
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.agrofarm.R
//import com.example.agrofarm.model.CattleModel
//import com.example.agrofarm.repository.CattleRepoImpl
//import com.example.agrofarm.repository.UserRepoImpl
//import com.example.agrofarm.ui.theme.AgroFarmTheme
//import com.example.agrofarm.viewmodel.CattleViewModel
//import com.example.agrofarm.viewmodel.UserViewModel
//
//
//class catleeActivityCopy : ComponentActivity() {
//
//    private val userViewModel: UserViewModel by viewModels { UserViewModel.Factory(UserRepoImpl()) }
//    private val cattleViewModel: CattleViewModel by viewModels { CattleViewModel.Factory(CattleRepoImpl()) }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        if (userViewModel.getCurrentUser() == null) {
//            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, LoginScreen::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()
//            return
//        }
//
//        setContent {
//            AgroFarmTheme {
//                CattleApp(
//                    userViewModel = userViewModel,
//                    cattleViewModel = cattleViewModel,
//                    onNavigateBack = { finish() }
//                )
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CattleApp(userViewModel: UserViewModel, cattleViewModel: CattleViewModel, onNavigateBack: () -> Unit) {
//    val context = LocalContext.current
//    val currentUser = userViewModel.getCurrentUser()!!
//
//    val allCattle by cattleViewModel.cattleList.observeAsState(emptyList())
//    val isLoading by cattleViewModel.loading.observeAsState(false)
//
//    var showDialog by remember { mutableStateOf<CattleModel?>(null) } // Used for both Add and Edit
//
//    LaunchedEffect(Unit) {
//        cattleViewModel.getAllCattle()
//    }
//
//    val myCattle = remember(allCattle, currentUser) {
//        allCattle?.filter { it.farmerId == currentUser.uid } ?: emptyList()
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Manage Cattle", fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color(0xFF4CAF50),
//                    titleContentColor = Color.White
//                )
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { showDialog = CattleModel() }, // Open dialog with an empty model for adding
//                containerColor = Color(0xFF4CAF50)
//            ) {
//                Icon(Icons.Default.Add, "Add Cattle", tint = Color.White)
//            }
//        }
//    ) { padding ->
//        Box(
//            modifier = Modifier.fillMaxSize().padding(padding)
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//            } else if (myCattle.isEmpty()) {
//                EmptyCattleView(Modifier.fillMaxSize())
//            } else {
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize(),
//                    contentPadding = PaddingValues(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    items(myCattle, key = { it.id }) { cattle ->
//                        CattleCard(
//                            cattle = cattle,
//                            onDelete = { cattleId ->
//                                cattleViewModel.deleteCattle(cattleId) { success, msg ->
//                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                                    if(success) cattleViewModel.getAllCattle()
//                                }
//                            },
//                            onEdit = { showDialog = it },
//                            onClick = {
//                                // TODO: Create CattleDetailsActivity and navigate
//                                Toast.makeText(context, "View details for ${cattle.name}", Toast.LENGTH_SHORT).show()
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    // --- Add/Edit Dialog ---
//    showDialog?.let {
//        AddEditCattleDialog(
//            cattle = it,
//            onDismiss = { showDialog = null },
//            onConfirm = { updatedCattle ->
//                if (updatedCattle.id.isBlank()) { // Adding new cattle
//                    val newCattle = updatedCattle.copy(farmerId = currentUser.uid)
//                    cattleViewModel.addCattle(newCattle) { success, msg ->
//                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                        if(success) cattleViewModel.getAllCattle()
//                    }
//                } else { // Editing existing cattle
//                    cattleViewModel.updateCattle(updatedCattle.id, updatedCattle) { success, msg ->
//                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                        if(success) cattleViewModel.getAllCattle()
//                    }
//                }
//                showDialog = null
//            }
//        )
//    }
//}
//
//@Composable
//fun CattleCard(
//    cattle: CattleModel,
//    onDelete: (String) -> Unit,
//    onEdit: () -> Unit,
//    onClick: () -> Unit
//) {
//    var showMenu by remember { mutableStateOf(false) }
//    var showDeleteDialog by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(2.dp),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Row(
//            modifier = Modifier.padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Image(
//                painter = painterResource(R.drawable.logo),
//                contentDescription = null,
//                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
//                contentScale = ContentScale.Crop
//            )
//            Spacer(Modifier.width(12.dp))
//            Column(modifier = Modifier.weight(1f)) {
//                Text(cattle.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
//                Text("${cattle.breed} - ${cattle.type}", fontSize = 12.sp, color = Color.Gray)
//                Spacer(Modifier.height(4.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text("Age: ${cattle.age} years", fontSize = 12.sp, color = Color.DarkGray)
//                    Surface(
//                        color = Color(0xFFE8F5E9),
//                        shape = RoundedCornerShape(6.dp)
//                    ) {
//                        Text(cattle.healthStatus, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
//                    }
//                }
//            }
//            Box {
//                IconButton(onClick = { showMenu = true }) {
//                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
//                }
//                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
//                    DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit(); showMenu = false }, leadingIcon = { Icon(Icons.Default.Edit, null) })
//                    DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { showDeleteDialog = true; showMenu = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
//                }
//            }
//        }
//    }
//
//    if (showDeleteDialog) {
//        AlertDialog(
//            onDismissRequest = { showDeleteDialog = false },
//            title = { Text("Delete Cattle?") },
//            text = { Text("Are you sure you want to delete ${cattle.name}?") },
//            confirmButton = { TextButton(onClick = { onDelete(cattle.id); showDeleteDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("Delete") } },
//            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
//        )
//    }
//}
//
//@Composable
//fun EmptyCattleView(modifier: Modifier = Modifier) {
//    Column(
//        modifier = modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
//        Spacer(Modifier.height(16.dp))
//        Text("No cattle added yet", fontSize = 18.sp, color = Color.Gray)
//        Text("Tap + to add your first cattle", fontSize = 14.sp, color = Color.LightGray)
//    }
//}
//
//@Composable
//fun AddEditCattleDialog(cattle: CattleModel, onDismiss: () -> Unit, onConfirm: (CattleModel) -> Unit) {
//    val isEditing = cattle.id.isNotBlank()
//    var name by remember { mutableStateOf(cattle.name) }
//    var type by remember { mutableStateOf(cattle.type) }
//    var breed by remember { mutableStateOf(cattle.breed) }
//    var age by remember { mutableStateOf(cattle.age.toString()) }
//    var healthStatus by remember { mutableStateOf(cattle.healthStatus) }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(if (isEditing) "Edit Cattle" else "Add Cattle") },
//        text = {
//            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
//                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (e.g., Cow)") })
//                OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Breed") })
//                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age (years)") })
//                OutlinedTextField(value = healthStatus, onValueChange = { healthStatus = it }, label = { Text("Health Status") })
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    if (name.isNotBlank() && breed.isNotBlank() && age.isNotBlank()) {
//                        val updatedCattle = cattle.copy(
//                            name = name,
//                            type = type,
//                            breed = breed,
//                            age = age.toIntOrNull() ?: 0,
//                            healthStatus = healthStatus.ifBlank { "Healthy" }
//                        )
//                        onConfirm(updatedCattle)
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
//            ) { Text(if (isEditing) "Save" else "Add") }
//        },
//        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun CattleAppPreview() {
//    AgroFarmTheme {
//        CattleApp(UserViewModel(UserRepoImpl()), CattleViewModel(CattleRepoImpl()), onNavigateBack = {})
//    }
//}