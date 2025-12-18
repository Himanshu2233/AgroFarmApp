package com.example.agrofarm.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.agrofarm.model.UserModel
import com.example.agrofarm.repository.UserRepo
import com.google.firebase.auth.FirebaseUser

/**
 * UserViewModel - Manages user authentication and profile data
 * Handles Login, Register, Logout operations
 */
class UserViewModel(private val repo: UserRepo) : ViewModel() {

    // LiveData for user data
    private val _user = MutableLiveData<UserModel?>()
    val user: MutableLiveData<UserModel?> get() = _user

    // Loading state
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: MutableLiveData<Boolean> get() = _loading

    // Message for toasts/errors
    private val _message = MutableLiveData<String>()
    val message: MutableLiveData<String> get() = _message

    /**
     * Register new user with email and password
     */
    fun register(
        userModel: UserModel,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.register(userModel, password) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    /**
     * Login user with email and password
     */
    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.login(email, password) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    /**
     * Logout current user
     */
    fun logout(callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.logout { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            _user.postValue(null)
            callback(success, msg)
        }
    }

    /**
     * Get current logged-in user
     */
    fun getCurrentUser(): FirebaseUser? {
        return repo.getCurrentUser()
    }

    /**
     * Get user data from Firebase Database
     */
    fun getUserData(userId: String) {
        _loading.postValue(true)
        repo.getUserData(userId) { success, msg, userData ->
            _loading.postValue(false)
            if (success && userData != null) {
                _user.postValue(userData)
            } else {
                _user.postValue(null)
                _message.postValue(msg)
            }
        }
    }

    /**
     * Update user profile data
     */
    fun updateUserData(
        userId: String,
        userModel: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.updateUserData(userId, userModel) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    /**
     * Reset password via email
     */
    fun resetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.resetPassword(email) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    /**
     * Forget Password - Sends reset email (calls resetPassword)
     * This is an alias for resetPassword for better naming in UI
     */
    fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        resetPassword(email, callback)
    }
}