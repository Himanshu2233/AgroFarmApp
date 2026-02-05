package com.example.agrofarm.repository

import android.content.Context
import android.net.Uri
import com.example.agrofarm.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepo {

    // Register new user
    fun register(
        userModel: UserModel,
        password: String,
        callback: (Boolean, String) -> Unit
    )

    // Login user
    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    )

    // Logout user
    fun logout(callback: (Boolean, String) -> Unit)

    // Get current user
    fun getCurrentUser(): FirebaseUser?

    // Get user data from database
    fun getUserData(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )

    // Update user data
    fun updateUserData(
        userId: String,
        userModel: UserModel,
        callback: (Boolean, String) -> Unit
    )

    // Reset password
    fun resetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    )

    // Upload profile image to Cloudinary
    fun uploadProfileImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )
}