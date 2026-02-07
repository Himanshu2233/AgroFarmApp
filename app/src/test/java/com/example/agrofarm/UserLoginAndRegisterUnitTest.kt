package com.example.agrofarm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.agrofarm.model.UserModel
import com.example.agrofarm.repository.UserRepo
import com.example.agrofarm.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UserLoginAndRegisterUnitTest {

    // This rule ensures that LiveData updates happen instantly on the main thread
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun login_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Login successful")
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq("123456"), any())

        var successResult = false
        var messageResult = ""

        viewModel.login("test@gmail.com", "123456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        // Fixed: The mocked message was "Login successful", make sure it matches your assert
        assertEquals("Login successful", messageResult)

        verify(repo).login(eq("test@gmail.com"), eq("123456"), any())
    }

    @Test
    fun register_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        val userModel = UserModel(
            userId = "123",
            email = "test@gmail.com",
            fullName = "Test User"
        )
        val password = "password123"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Registration successful")
            null
        }.`when`(repo).register(eq(userModel), eq(password), any())

        var successResult = false
        var messageResult = ""

        viewModel.register(userModel, password) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Registration successful", messageResult)

        verify(repo).register(eq(userModel), eq(password), any())
    }
}