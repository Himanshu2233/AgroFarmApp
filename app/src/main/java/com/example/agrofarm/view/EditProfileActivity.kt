package com.example.agrofarm.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.agrofarm.model.UserModel
import com.example.agrofarm.repository.UserRepoImpl
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.viewmodel.UserViewModel

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgroFarmTheme {
                // ✅ FIXED: ViewModel is now created in a simple, direct way. No factory needed.
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
    val isLoading by userViewModel.loading.observeAsState(true) // Start with loading true

    // Fetch the user data once when the screen first appears
    LaunchedEffect(Unit) {
        userViewModel.getCurrentUser()?.uid?.let {
            userViewModel.getUserData(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading || user == null) {
                CircularProgressIndicator()
            } else {
                EditProfileContent(
                    user = user!!,
                    userViewModel = userViewModel,
                    onSaveSuccess = onNavigateBack
                )
            }
        }
    }
}

@Composable
fun EditProfileContent(
    user: UserModel,
    userViewModel: UserViewModel,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf(user.fullName) }
    var dob by remember { mutableStateOf(user.dob) }
    val isSaving by userViewModel.loading.observeAsState(false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text("Date of Birth (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val updatedUser = user.copy(fullName = fullName, dob = dob)
                userViewModel.updateUserData(user.userId, updatedUser) { success, message ->
                    // ✅ FIXED: Show a toast message to the user.
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        onSaveSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Save Changes")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    AgroFarmTheme {
        val previewUser = UserModel(fullName = "Joe Farmer", dob = "1990-01-15")
        val previewViewModel = UserViewModel(UserRepoImpl())
        // This preview just shows the content, not the loading state.
        EditProfileContent(user = previewUser, userViewModel = previewViewModel, onSaveSuccess = {})
    }
}