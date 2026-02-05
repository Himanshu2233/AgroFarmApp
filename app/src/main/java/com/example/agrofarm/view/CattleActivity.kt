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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.agrofarm.R
import com.example.agrofarm.model.CattleModel
import com.example.agrofarm.repository.CattleRepoImpl
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.CattleViewModel
import com.example.agrofarm.viewmodel.UserViewModel
import androidx.compose.runtime.collectAsState

class CattleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
                CattleApp(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CattleApp(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val cattleViewModel = remember { CattleViewModel(CattleRepoImpl()) }

    val currentUser = userViewModel.getCurrentUser()
    val allCattle by cattleViewModel.cattleList.observeAsState(emptyList())
    val isLoading by cattleViewModel.loading.observeAsState(false)

    var cattleToEdit by remember { mutableStateOf<CattleModel?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    
    // Handle edit intent from details page
    val activity = (context as? ComponentActivity)
    val editCattleId = remember { activity?.intent?.getStringExtra("EDIT_CATTLE_ID") }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            showAuthDialog = true
        } else {
            cattleViewModel.getAllCattle()
        }
    }
    
    // Open edit dialog if coming from details page
    LaunchedEffect(editCattleId, allCattle) {
        if (!editCattleId.isNullOrBlank() && allCattle.isNotEmpty()) {
            val cattleToOpen = allCattle.find { it.id == editCattleId }
            if (cattleToOpen != null) {
                cattleToEdit = cattleToOpen
                showDialog = true
                // Clear the intent extra to avoid re-opening
                activity?.intent?.removeExtra("EDIT_CATTLE_ID")
            }
        }
    }

    val myCattle = remember(allCattle) {
        allCattle.filter { it.farmerId == currentUser?.uid }
    }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { onNavigateBack() },
            title = { Text("Login Required") },
            text = { Text("Please login to manage your cattle.") },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(context, LoginScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
                title = { Text("Manage Cattle (${myCattle.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { cattleToEdit = null; showDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
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
                            onEdit = { cattleToEdit = cattle; showDialog = true },
                            onDelete = {
                                cattleViewModel.deleteCattle(cattle.id) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) cattleViewModel.getAllCattle()
                                }
                            },
                            onClick = {
                                val intent = Intent(context, CattleDetailsActivity::class.java).apply {
                                    putExtra(CattleDetailsActivity.EXTRA_CATTLE_ID, cattle.id)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEditCattleDialog(
            cattle = cattleToEdit,
            cattleViewModel = cattleViewModel,
            onDismiss = { showDialog = false },
            onConfirm = { updatedCattle ->
                val finalCattle = if (cattleToEdit == null) {
                    updatedCattle.copy(farmerId = currentUser!!.uid)
                } else {
                    updatedCattle
                }

                if (cattleToEdit == null) {
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
                showDialog = false
                cattleToEdit = null
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
            // Image with gender indicator
            Box {
                AsyncImage(
                    model = cattle.imageUrl,
                    contentDescription = cattle.name,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.cattle),
                    error = painterResource(id = R.drawable.cattle)
                )
                // Gender badge
                if (cattle.gender.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp),
                        color = if (cattle.gender == "Male") Color(0xFF2196F3) else Color(0xFFE91E63),
                        shape = CircleShape
                    ) {
                        Icon(
                            if (cattle.gender == "Male") Icons.Default.Male else Icons.Default.Female,
                            contentDescription = cattle.gender,
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
                        cattle.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (cattle.isPregnant) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFFFE0B2),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "🤰",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Text("${cattle.breed} • ${cattle.type}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (cattle.tagNumber.isNotBlank()) {
                    Text("Tag: ${cattle.tagNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${cattle.age} yrs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        if (cattle.weight > 0) {
                            Text(" • ${cattle.weight.toInt()} kg", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                    Surface(
                        color = getHealthBadgeColor(cattle.healthStatus),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            cattle.healthStatus,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
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
            title = { Text("Delete Cattle?") },
            text = { Text("Are you sure you want to delete ${cattle.name}?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCattleDialog(
    cattle: CattleModel?,
    cattleViewModel: CattleViewModel,
    onDismiss: () -> Unit,
    onConfirm: (CattleModel) -> Unit
) {
    val context = LocalContext.current
    val isEditing = cattle != null

    // Dropdown options
    val cattleTypes = listOf("Cow", "Buffalo", "Goat", "Sheep", "Ox", "Bull", "Calf", "Other")
    val healthStatuses = listOf("Healthy", "Sick", "Under Treatment", "Recovering", "Pregnant", "Lactating", "Quarantine")
    val breedsByType = mapOf(
        "Cow" to listOf("Holstein", "Jersey", "Gir", "Sahiwal", "Red Sindhi", "Tharparkar", "Kankrej", "Crossbred", "Other"),
        "Buffalo" to listOf("Murrah", "Mehsana", "Surti", "Jaffarabadi", "Nili-Ravi", "Bhadawari", "Other"),
        "Goat" to listOf("Jamunapari", "Beetal", "Barbari", "Sirohi", "Osmanabadi", "Black Bengal", "Other"),
        "Sheep" to listOf("Merino", "Rambouillet", "Corriedale", "Nellore", "Deccani", "Marwari", "Other"),
        "Ox" to listOf("Hallikar", "Amritmahal", "Khillari", "Kangayam", "Other"),
        "Bull" to listOf("Gir", "Sahiwal", "Ongole", "Hariana", "Other"),
        "Calf" to listOf("Same as parent breed", "Mixed", "Other"),
        "Other" to listOf("Mixed Breed", "Unknown", "Other")
    )
    val genderOptions = listOf("Male", "Female")
    val vaccinationStatuses = listOf("Up to date", "Pending", "Overdue", "Not vaccinated")
    val feedTypes = listOf("Green fodder", "Dry fodder", "Concentrate", "Mixed feed", "Grazing", "Other")

    var name by remember(cattle?.id) { mutableStateOf(cattle?.name ?: "") }
    var type by remember(cattle?.id) { mutableStateOf(cattle?.type ?: cattleTypes[0]) }
    var breed by remember(cattle?.id) { mutableStateOf(cattle?.breed ?: "") }
    var age by remember(cattle?.id) { mutableStateOf(if (isEditing && cattle?.age != null) cattle.age.toString() else "") }
    var healthStatus by remember(cattle?.id) { mutableStateOf(cattle?.healthStatus ?: healthStatuses[0]) }
    var lastCheckup by remember(cattle?.id) { mutableStateOf(cattle?.lastCheckup ?: "") }
    var gender by remember(cattle?.id) { mutableStateOf(cattle?.gender?.ifBlank { "Female" } ?: "Female") }
    var weight by remember(cattle?.id) { mutableStateOf(if (isEditing && cattle?.weight != null && cattle.weight > 0) cattle.weight.toString() else "") }
    var notes by remember(cattle?.id) { mutableStateOf(cattle?.notes ?: "") }
    var tagNumber by remember(cattle?.id) { mutableStateOf(cattle?.tagNumber ?: "") }
    var vaccinationStatus by remember(cattle?.id) { mutableStateOf(cattle?.vaccinationStatus ?: vaccinationStatuses[0]) }
    var milkProduction by remember(cattle?.id) { mutableStateOf(if (isEditing && cattle?.milkProduction != null && cattle.milkProduction > 0) cattle.milkProduction.toString() else "") }
    var feedType by remember(cattle?.id) { mutableStateOf(cattle?.feedType ?: "") }
    var isPregnant by remember(cattle?.id) { mutableStateOf(cattle?.isPregnant ?: false) }

    var typeExpanded by remember { mutableStateOf(false) }
    var breedExpanded by remember { mutableStateOf(false) }
    var healthExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var vaccinationExpanded by remember { mutableStateOf(false) }
    var feedExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var imageUri by remember(cattle?.id) { mutableStateOf<Uri?>(null) }
    var imageUrl by remember(cattle?.id) { mutableStateOf(cattle?.imageUrl ?: "") }
    var isUploading by remember { mutableStateOf(false) }

    // Validation
    val isFormValid = name.isNotBlank() && breed.isNotBlank() && age.toIntOrNull() != null

    // Get breeds for selected type
    val availableBreeds = breedsByType[type] ?: breedsByType["Other"]!!

    // Reset breed when type changes
    LaunchedEffect(type) {
        if (breed !in availableBreeds && !isEditing) {
            breed = ""
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            isUploading = true
            cattleViewModel.uploadCattleImage(context, it) { url ->
                isUploading = false
                if (url != null) {
                    imageUrl = url
                } else {
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        lastCheckup = sdf.format(java.util.Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Edit Cattle" else "Add New Cattle", fontWeight = FontWeight.Bold)
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
                                contentDescription = "Cattle Image",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
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
                                Text("Tap to add cattle photo", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name/Tag *") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Lakshmi, Tag #101") }
                )

                Spacer(Modifier.height(12.dp))

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Animal Type") },
                        leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF4CAF50)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        cattleTypes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { type = option; typeExpanded = false },
                                leadingIcon = {
                                    if (type == option) Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Breed Dropdown (dynamic based on type)
                ExposedDropdownMenuBox(
                    expanded = breedExpanded,
                    onExpandedChange = { breedExpanded = !breedExpanded }
                ) {
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Breed *") },
                        leadingIcon = { Icon(Icons.Default.Diversity3, contentDescription = null, tint = Color(0xFF4CAF50)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        placeholder = { Text("Select or type breed") }
                    )
                    ExposedDropdownMenu(
                        expanded = breedExpanded,
                        onDismissRequest = { breedExpanded = false }
                    ) {
                        availableBreeds.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { breed = option; breedExpanded = false },
                                leadingIcon = {
                                    if (breed == option) Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Age and Gender Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) age = it },
                        label = { Text("Age (years) *") },
                        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = Color(0xFF4CAF50)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { gender = option; genderExpanded = false }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Health Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = healthExpanded,
                    onExpandedChange = { healthExpanded = !healthExpanded }
                ) {
                    OutlinedTextField(
                        value = healthStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Health Status") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = when (healthStatus) {
                                    "Healthy" -> Color(0xFF4CAF50)
                                    "Sick", "Quarantine" -> Color(0xFFF44336)
                                    "Under Treatment", "Recovering" -> Color(0xFFFF9800)
                                    else -> Color(0xFF2196F3)
                                }
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = healthExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = healthExpanded,
                        onDismissRequest = { healthExpanded = false }
                    ) {
                        healthStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = { healthStatus = status; healthExpanded = false },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Circle,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = when (status) {
                                            "Healthy" -> Color(0xFF4CAF50)
                                            "Sick", "Quarantine" -> Color(0xFFF44336)
                                            "Under Treatment", "Recovering" -> Color(0xFFFF9800)
                                            else -> Color(0xFF2196F3)
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Last Checkup Date Picker
                OutlinedTextField(
                    value = lastCheckup,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Last Checkup Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.EditCalendar, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    placeholder = { Text("Tap to select date") }
                )

                Spacer(Modifier.height(12.dp))

                // Tag Number
                OutlinedTextField(
                    value = tagNumber,
                    onValueChange = { tagNumber = it },
                    label = { Text("Tag/ID Number") },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., COW-2024-001") }
                )

                Spacer(Modifier.height(12.dp))

                // Weight (optional)
                OutlinedTextField(
                    value = weight,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) weight = it },
                    label = { Text("Weight (kg)") },
                    leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Optional") }
                )

                Spacer(Modifier.height(12.dp))

                // Milk Production (for dairy animals)
                if (type in listOf("Cow", "Buffalo", "Goat")) {
                    OutlinedTextField(
                        value = milkProduction,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) milkProduction = it },
                        label = { Text("Daily Milk Production (liters)") },
                        leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF4CAF50)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Optional - for dairy animals") }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Vaccination Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = vaccinationExpanded,
                    onExpandedChange = { vaccinationExpanded = !vaccinationExpanded }
                ) {
                    OutlinedTextField(
                        value = vaccinationStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vaccination Status") },
                        leadingIcon = { Icon(Icons.Default.Vaccines, contentDescription = null, tint = Color(0xFF4CAF50)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vaccinationExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = vaccinationExpanded,
                        onDismissRequest = { vaccinationExpanded = false }
                    ) {
                        vaccinationStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = { vaccinationStatus = status; vaccinationExpanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Pregnancy switch (for female animals)
                if (gender == "Female") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPregnant) Color(0xFFFFF3E0) else Color(0xFFF5F5F5))
                            .clickable { isPregnant = !isPregnant }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ChildCare,
                                contentDescription = null,
                                tint = if (isPregnant) Color(0xFFFF9800) else Color.Gray
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Pregnant", fontWeight = FontWeight.Medium)
                                Text("Mark if expecting", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = isPregnant,
                            onCheckedChange = { isPregnant = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFF9800),
                                checkedTrackColor = Color(0xFFFFCC80)
                            )
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 3,
                    placeholder = { Text("Additional notes...") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(CattleModel(
                        id = cattle?.id ?: "",
                        farmerId = cattle?.farmerId ?: "",
                        name = name.trim(),
                        type = type,
                        breed = breed.trim(),
                        age = age.toIntOrNull() ?: 0,
                        healthStatus = healthStatus,
                        lastCheckup = lastCheckup,
                        imageUrl = imageUrl,
                        gender = gender,
                        weight = weight.toDoubleOrNull() ?: 0.0,
                        tagNumber = tagNumber.trim(),
                        vaccinationStatus = vaccinationStatus,
                        milkProduction = milkProduction.toDoubleOrNull() ?: 0.0,
                        notes = notes.trim(),
                        isPregnant = isPregnant
                    ))
                },
                enabled = isFormValid && !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(if (isEditing) Icons.Default.Save else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Save Changes" else "Add Cattle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

fun getHealthBadgeColor(status: String): Color {
    return when (status.lowercase()) {
        "healthy" -> Color(0xFF4CAF50)
        "sick", "quarantine" -> Color(0xFFF44336)
        "under treatment", "recovering" -> Color(0xFFFF9800)
        "pregnant", "lactating" -> Color(0xFF2196F3)
        else -> Color(0xFF9E9E9E)
    }
}

@Composable
fun EmptyCattleView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Pets, "Empty", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("No cattle added yet", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        Text("Tap + to add cattle", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
    }
}

@Preview(showBackground = true)
@Composable
fun CattleAppPreview() {
    AgroFarmTheme {
        CattleApp(onNavigateBack = {})
    }
}
