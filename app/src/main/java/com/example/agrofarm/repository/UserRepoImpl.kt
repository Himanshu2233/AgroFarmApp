package com.example.agrofarm.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.agrofarm.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

class UserRepoImpl : UserRepo {

    private val TAG = "UserRepoImpl"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")
    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dlpal9dve",
            "api_key" to "358118928588627",
            "api_secret" to "JQdwGpMaLIOYrrp8kOwJd7X6Ql0"
        )
    )

    override fun register(
        userModel: UserModel,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        if (userModel.email.isBlank() || password.isBlank()) {
            callback(false, "Email and password cannot be empty")
            return
        }

        auth.createUserWithEmailAndPassword(userModel.email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: ""
                val newUser = userModel.copy(userId = userId)

                database.child(userId).setValue(newUser.toMap())
                    .addOnSuccessListener {
                        callback(true, "Registration successful")
                    }
                    .addOnFailureListener { e ->
                        callback(false, "Failed to save user data: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                callback(false, "Registration failed: ${e.message}")
            }
    }

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            callback(false, "Email and password cannot be empty")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                callback(true, "Login successful")
            }
            .addOnFailureListener { e ->
                callback(false, "Login failed: ${e.message}")
            }
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "Logged out successfully")
        } catch (e: Exception) {
            callback(false, "Logout failed: ${e.message}")
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getUserData(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        Log.d(TAG, "getUserData called for userId: $userId")
        
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange - snapshot exists: ${snapshot.exists()}")
                
                if (!snapshot.exists()) {
                    Log.w(TAG, "User not found in database for userId: $userId")
                    // User exists in Auth but not in Database - create profile from Auth data
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        Log.d(TAG, "Creating user profile from Auth data")
                        val newUser = UserModel(
                            userId = userId,
                            email = currentUser.email ?: "",
                            fullName = currentUser.displayName ?: "",
                            role = "farmer"
                        )
                        // Save to database
                        database.child(userId).setValue(newUser.toMap())
                            .addOnSuccessListener {
                                Log.d(TAG, "User profile created successfully")
                                callback(true, "User profile created", newUser)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to create user profile: ${e.message}")
                                callback(false, "Failed to create profile: ${e.message}", null)
                            }
                    } else {
                        callback(false, "User not found in database", null)
                    }
                    return
                }

                try {
                    // Manual parsing to handle missing fields gracefully
                    val user = UserModel(
                        userId = snapshot.child("userId").getValue(String::class.java) ?: userId,
                        email = snapshot.child("email").getValue(String::class.java) ?: "",
                        password = snapshot.child("password").getValue(String::class.java) ?: "",
                        fullName = snapshot.child("fullName").getValue(String::class.java) ?: "",
                        dob = snapshot.child("dob").getValue(String::class.java) ?: "",
                        gender = snapshot.child("gender").getValue(String::class.java) ?: "",
                        phone = snapshot.child("phone").getValue(String::class.java) ?: "",
                        farmName = snapshot.child("farmName").getValue(String::class.java) ?: "",
                        farmSize = snapshot.child("farmSize").getValue(String::class.java) ?: "",
                        farmAddress = snapshot.child("farmAddress").getValue(String::class.java) ?: "",
                        farmingExperience = snapshot.child("farmingExperience").getValue(Int::class.java) ?: 0,
                        specialization = snapshot.child("specialization").getValue(String::class.java) ?: "",
                        profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java) ?: "",
                        role = snapshot.child("role").getValue(String::class.java) ?: "farmer"
                    )
                    Log.d(TAG, "User data loaded successfully: ${user.email}")
                    callback(true, "User data loaded", user)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user data: ${e.message}", e)
                    callback(false, "Error: ${e.message}", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                callback(false, "Database error: ${error.message}", null)
            }
        })
    }

    override fun updateUserData(
        userId: String,
        userModel: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        database.child(userId).setValue(userModel.toMap())
            .addOnSuccessListener {
                callback(true, "Profile updated successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update: ${e.message}")
            }
    }

    override fun resetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        if (email.isBlank()) {
            callback(false, "Email cannot be empty")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                callback(true, "Password reset email sent")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to send email: ${e.message}")
            }
    }

    override fun uploadProfileImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                
                if (inputStream == null) {
                    Log.e(TAG, "Failed to open input stream for URI: $imageUri")
                    Handler(Looper.getMainLooper()).post {
                        callback(null)
                    }
                    return@execute
                }
                
                var fileName = getFileNameFromUri(context, imageUri)
                fileName = fileName?.substringBeforeLast(".") ?: "profile_${System.currentTimeMillis()}"

                Log.d(TAG, "Uploading profile image: $fileName")
                
                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", "agrofarm_profile_$fileName",
                        "resource_type", "image",
                        "folder", "agrofarm_profiles"
                    )
                )

                inputStream.close()
                
                val imageUrl = response["secure_url"] as String?
                Log.d(TAG, "Profile image uploaded successfully: $imageUrl")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload profile image: ${e.message}", e)
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}