package com.example.agrofarm.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agrofarm.R
import com.example.agrofarm.model.UserModel
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userViewModel = UserViewModel(UserRepoImpl())

            AgroFarmTheme {
                ProfileApp(
                    userViewModel = userViewModel,
                    onNavigateBack = { finish() },
                    // ✅ FIXED: This now navigates to the EditProfileActivity
                    onEditProfile = {
                        val intent = Intent(this, EditProfileActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileApp(
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit
) {
    val user by userViewModel.user.observeAsState()
    val isLoading by userViewModel.loading.observeAsState(true)
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            userViewModel.getUserData(currentUserId)
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (user != null) {
                ProfileContent(user = user!!, onEditProfile = onEditProfile)
            } else {
                Text("Could not load user profile.", textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun ProfileContent(user: UserModel, onEditProfile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4CAF50))
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = user.fullName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = user.email,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        // User Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32))
                Spacer(Modifier.height(8.dp))
                ProfileInfoRow("Full Name", user.fullName)
                Divider()
                ProfileInfoRow("Email", user.email)
                Divider()
                ProfileInfoRow("Date of Birth", user.dob.ifBlank { "Not specified" })
            }
        }

        // Edit Button
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(12.dp)
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
        Text(text = label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Gray)
        Text(text = value, modifier = Modifier.weight(1.5f), fontSize = 16.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.End)
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
            dob = "1990-01-15"
        )
        val previewViewModel = UserViewModel(UserRepoImpl()).apply {
            this.user.value = previewUser
        }
        ProfileApp(
            userViewModel = previewViewModel,
            onNavigateBack = {},
            onEditProfile = {}
        )
    }
}
