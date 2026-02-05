package com.example.agrofarm.view

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.agrofarm.model.UserModel
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.UserViewModel
import androidx.compose.runtime.collectAsState

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
                val userViewModel = remember { UserViewModel(UserRepoImpl()) }

                EditProfileApp(
                    userViewModel = userViewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileApp(userViewModel: UserViewModel, onNavigateBack: () -> Unit) {
    val user by userViewModel.user.observeAsState()
    val isLoading by userViewModel.loading.observeAsState(false)
    val message by userViewModel.message.observeAsState()
    var hasAttemptedLoad by remember { mutableStateOf(false) }
    val currentUserId = userViewModel.getCurrentUser()?.uid

    LaunchedEffect(Unit) {
        currentUserId?.let {
            hasAttemptedLoad = true
            userViewModel.getUserData(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading || !hasAttemptedLoad -> {
                    CircularProgressIndicator()
                }
                user != null -> {
                    EditProfileContent(
                        user = user!!,
                        userViewModel = userViewModel,
                        onSaveSuccess = onNavigateBack
                    )
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Could not load profile data.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (!message.isNullOrEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                message ?: "",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                currentUserId?.let { userViewModel.getUserData(it) }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Retry")
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = onNavigateBack
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    user: UserModel,
    userViewModel: UserViewModel,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    
    // Personal Info
    var fullName by remember { mutableStateOf(user.fullName) }
    var dob by remember { mutableStateOf(user.dob) }
    var gender by remember { mutableStateOf(user.gender) }
    var phone by remember { mutableStateOf(user.phone) }
    
    // Farm Info
    var farmName by remember { mutableStateOf(user.farmName) }
    var farmSize by remember { mutableStateOf(user.farmSize) }
    var farmAddress by remember { mutableStateOf(user.farmAddress) }
    var farmingExperience by remember { mutableStateOf(if (user.farmingExperience > 0) user.farmingExperience.toString() else "") }
    var specialization by remember { mutableStateOf(user.specialization) }
    var profileImageUrl by remember { mutableStateOf(user.profileImageUrl) }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val isSaving by userViewModel.loading.observeAsState(false)
    
    // Dropdowns
    var genderExpanded by remember { mutableStateOf(false) }
    var specializationExpanded by remember { mutableStateOf(false) }
    
    val genderOptions = listOf("Male", "Female", "Other", "Prefer not to say")
    val specializationOptions = listOf(
        "Dairy Farming", "Organic Crops", "Poultry", "Mixed Farming",
        "Fruit Cultivation", "Vegetable Farming", "Grain Production",
        "Livestock", "Aquaculture", "Other"
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            isUploading = true
            // Upload image to Cloudinary
            userViewModel.uploadProfileImage(context, it) { url ->
                isUploading = false
                if (url != null) {
                    profileImageUrl = url
                    Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Profile Image Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null || profileImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = imageUri ?: profileImageUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Overlay for changing image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "Add Photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap to change photo",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Personal Information Section
        SectionHeader(
            icon = Icons.Default.Person,
            title = "Personal Information"
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text("Date of Birth") },
            leadingIcon = { Icon(Icons.Default.Cake, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("YYYY-MM-DD") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Gender Dropdown
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                leadingIcon = { Icon(Icons.Default.Wc, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp)
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

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            placeholder = { Text("+91 9876543210") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Farm Information Section
        SectionHeader(
            icon = Icons.Default.Agriculture,
            title = "Farm Information"
        )

        OutlinedTextField(
            value = farmName,
            onValueChange = { farmName = it },
            label = { Text("Farm Name") },
            leadingIcon = { Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g., Green Valley Farm") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = farmSize,
            onValueChange = { farmSize = it },
            label = { Text("Farm Size") },
            leadingIcon = { Icon(Icons.Default.Landscape, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g., 50 acres") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = farmAddress,
            onValueChange = { farmAddress = it },
            label = { Text("Farm Address") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            placeholder = { Text("Village, District, State") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = farmingExperience,
            onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) farmingExperience = it },
            label = { Text("Years of Farming Experience") },
            leadingIcon = { Icon(Icons.Default.WorkHistory, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Specialization Dropdown
        ExposedDropdownMenuBox(
            expanded = specializationExpanded,
            onExpandedChange = { specializationExpanded = !specializationExpanded }
        ) {
            OutlinedTextField(
                value = specialization,
                onValueChange = { specialization = it },
                label = { Text("Farming Specialization") },
                leadingIcon = { Icon(Icons.Default.Category, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = specializationExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                placeholder = { Text("Select or type") },
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = specializationExpanded,
                onDismissRequest = { specializationExpanded = false }
            ) {
                specializationOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { specialization = option; specializationExpanded = false }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                val updatedUser = user.copy(
                    fullName = fullName.trim(),
                    dob = dob.trim(),
                    gender = gender,
                    phone = phone.trim(),
                    farmName = farmName.trim(),
                    farmSize = farmSize.trim(),
                    farmAddress = farmAddress.trim(),
                    farmingExperience = farmingExperience.toIntOrNull() ?: 0,
                    specialization = specialization.trim(),
                    profileImageUrl = profileImageUrl
                )
                userViewModel.updateUserData(user.userId, updatedUser) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        onSaveSuccess()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isSaving && !isUploading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Changes", fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier.padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    AgroFarmTheme {
        val previewUser = UserModel(
            fullName = "Joe Farmer",
            dob = "1990-01-15",
            gender = "Male",
            phone = "+91 9876543210",
            farmName = "Green Valley Farm",
            farmSize = "50 acres",
            farmAddress = "Village Road",
            farmingExperience = 10,
            specialization = "Dairy Farming"
        )
        val previewViewModel = UserViewModel(UserRepoImpl())
        EditProfileContent(user = previewUser, userViewModel = previewViewModel, onSaveSuccess = {})
    }
}
