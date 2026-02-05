package com.example.agrofarm.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.collectAsState

class ProfileActivity : ComponentActivity() {
    private lateinit var userViewModel: UserViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        userViewModel = UserViewModel(UserRepoImpl())
        
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()

            AgroFarmTheme(darkTheme = isDarkMode) {
                ProfileApp(
                    userViewModel = userViewModel,
                    onNavigateBack = { finish() },
                    onEditProfile = {
                        val intent = Intent(this, EditProfileActivity::class.java)
                        startActivity(intent)
                    },
                    context = this
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh user data when returning from EditProfileActivity
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            userViewModel.getUserData(userId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileApp(
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    context: ComponentActivity
) {
    val user by userViewModel.user.observeAsState()
    val isLoading by userViewModel.loading.observeAsState(false)
    val message by userViewModel.message.observeAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var hasAttemptedLoad by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            hasAttemptedLoad = true
            userViewModel.getUserData(currentUserId)
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, "Edit Profile", tint = Color.White)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading || !hasAttemptedLoad -> {
                    CircularProgressIndicator()
                }
                user != null -> {
                    ProfileContent(
                        user = user!!, 
                        onEditProfile = onEditProfile,
                        onLogout = {
                            // Sign out from Firebase
                            FirebaseAuth.getInstance().signOut()
                            
                            // Navigate to login screen (matching SettingsActivity pattern)
                            val intent = Intent(context, LoginScreen::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                            context.finish()
                        }
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
                            "Could not load user profile.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium
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
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContent(user: UserModel, onEditProfile: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Image
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    if (user.profileImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Default Profile",
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = user.fullName.ifBlank { "Farmer" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = user.email,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                if (user.farmName.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = user.farmName,
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                icon = Icons.Default.CalendarMonth,
                value = if (user.farmingExperience > 0) "${user.farmingExperience} yrs" else "N/A",
                label = "Experience"
            )
            StatCard(
                icon = Icons.Default.Landscape,
                value = user.farmSize.ifBlank { "N/A" },
                label = "Farm Size"
            )
            StatCard(
                icon = Icons.Default.Star,
                value = user.specialization.ifBlank { "General" }.take(10),
                label = "Specialization"
            )
        }

        // Personal Information Card
        ProfileSectionCard(
            title = "Personal Information",
            icon = Icons.Default.Person
        ) {
            ProfileDetailRow(Icons.Default.Badge, "Full Name", user.fullName.ifBlank { "Not specified" })
            ProfileDetailRow(Icons.Default.Email, "Email", user.email)
            ProfileDetailRow(Icons.Default.Cake, "Date of Birth", user.dob.ifBlank { "Not specified" })
            ProfileDetailRow(Icons.Default.Wc, "Gender", user.gender.ifBlank { "Not specified" })
            ProfileDetailRow(Icons.Default.Phone, "Phone", user.phone.ifBlank { "Not specified" })
        }

        // Farm Information Card
        ProfileSectionCard(
            title = "Farm Information",
            icon = Icons.Default.Agriculture
        ) {
            ProfileDetailRow(Icons.Default.Home, "Farm Name", user.farmName.ifBlank { "Not specified" })
            ProfileDetailRow(Icons.Default.Landscape, "Farm Size", user.farmSize.ifBlank { "Not specified" })
            ProfileDetailRow(Icons.Default.LocationOn, "Address", user.farmAddress.ifBlank { "Not specified" })
            ProfileDetailRow(Icons.Default.WorkHistory, "Experience", 
                if (user.farmingExperience > 0) "${user.farmingExperience} years" else "Not specified"
            )
            ProfileDetailRow(Icons.Default.Category, "Specialization", user.specialization.ifBlank { "Not specified" })
        }

        Spacer(Modifier.height(16.dp))

        // Edit Button
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(16.dp))

        // Logout Button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
                containerColor = Color.Transparent
            ),
            border = BorderStroke(
                1.dp, 
                MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(icon: ImageVector, value: String, label: String) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ProfileSectionCard(
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
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileAppPreview() {
    AgroFarmTheme {
        val previewUser = UserModel(
            userId = "123",
            email = "farmer.joe@example.com",
            fullName = "Joe Farmer",
            dob = "1990-01-15",
            gender = "Male",
            phone = "+91 9876543210",
            farmName = "Green Valley Farm",
            farmSize = "50 acres",
            farmAddress = "Village Road, District",
            farmingExperience = 15,
            specialization = "Dairy Farming"
        )
        ProfileContent(user = previewUser, onEditProfile = {}, onLogout = {})
    }
}
