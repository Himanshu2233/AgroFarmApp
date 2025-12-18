package com.example.agrofarm.repository

import com.example.agrofarm.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepoImpl : UserRepo {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

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
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(false, "User not found", null)
                    return
                }

                try {
                    val user = snapshot.getValue(UserModel::class.java)
                    if (user != null) {
                        callback(true, "User data loaded", user)
                    } else {
                        callback(false, "Error parsing user data", null)
                    }
                } catch (e: Exception) {
                    callback(false, "Error: ${e.message}", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
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
}