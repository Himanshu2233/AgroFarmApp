package com.example.agrofarm.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agrofarm.model.UserModel
import com.example.agrofarm.repository.UserRepo
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

// --- Activity Class ---
class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Initialize ViewModel
            val userViewModel = remember { UserViewModel(UserRepoImpl()) }

            // Get the current user's ID dynamically from Firebase Auth
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            AgroFarmTheme {
                if (currentUserId == null) {
                    // If for some reason the user is not logged in, handle it gracefully.
                    Toast.makeText(this, "User not logged in.", Toast.LENGTH_LONG).show()
                    // Redirecting to LoginScreen
                    val intent = Intent(this, LoginScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish() // Close the profile activity
                } else {
                    // If the user is logged in, show the profile screen
                    ProfileApp(
                        userViewModel = userViewModel,
                        userId = currentUserId,
                        onNavigateBack = { finish() },
                        onEditProfile = {
                            // TODO: In the future, navigate to an "Edit Profile" screen
                            Toast.makeText(this, "Edit Profile Clicked!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

// --- Main App Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileApp(
    userViewModel: UserViewModel,
    userId: String,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit
) {
    var user by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val updatedContext by rememberUpdatedState(context)


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (user != null) {
                ProfileContent(user = user!!, onEditProfile = onEditProfile)
            } else {
                // This state occurs if the user is authenticated but their data is not in the Realtime Database
                Text("Could not find user profile data.", modifier = Modifier.align(Alignment.Center).padding(16.dp))
            }
        }
    }
}

// --- UI Content Composables ---
@Composable
fun ProfileContent(user: UserModel, onEditProfile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                ProfileInfoRow("First Name", user.fullName)
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                ProfileInfoRow("Email", user.email)
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                ProfileInfoRow("Date of Birth", user.dob)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Icon", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile", color = Color.White)
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
        Text(text = value, modifier = Modifier.weight(1.5f), fontSize = 16.sp, fontWeight = FontWeight.Normal)
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun ProfileAppPreview() {
    AgroFarmTheme {
        val mockRepo = object : UserRepo by UserRepoImpl() {
            fun getUserById(userId: String, callback: (Boolean, String, UserModel?) -> Unit) {
                callback(
                    true, "Success",
                    UserModel(
                        userId = "123",
                        email = "farmer.joe@example.com",
                        fullName = "Joe",
                        dob = "1990-01-15"
                    )
                )
            }
        }

        ProfileApp(
            userViewModel = UserViewModel(mockRepo),
            userId = "123",
            onNavigateBack = {},
            onEditProfile = {}
        )
    }
}
