package com.example.agrofarm

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.example.agrofarm.view.HomeScreen
import com.example.agrofarm.view.LoginScreen
import com.example.agrofarm.view.RegisterScreen
import com.example.agrofarm.view.ForgetPassword
import org.junit.After

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<LoginScreen>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testLoginScreen_displaysAllElements() {
        // Verify login screen UI elements are displayed
        composeRule.onNodeWithText("AgroFarm")
            .assertIsDisplayed()
        
        composeRule.onNodeWithText("Login")
            .assertIsDisplayed()
        
        composeRule.onNodeWithTag("emailField")
            .assertIsDisplayed()
        
        composeRule.onNodeWithTag("passwordField")
            .assertIsDisplayed()
        
        composeRule.onNodeWithTag("loginButton")
            .assertIsDisplayed()
    }

    @Test
    fun testLoginScreen_emailFieldAcceptsInput() {
        composeRule.onNodeWithTag("emailField")
            .performTextInput("test@example.com")
        
        // Field should still be displayed after input
        composeRule.onNodeWithTag("emailField")
            .assertIsDisplayed()
    }

    @Test
    fun testLoginScreen_passwordFieldAcceptsInput() {
        composeRule.onNodeWithTag("passwordField")
            .performTextInput("password123")
        
        composeRule.onNodeWithTag("passwordField")
            .assertIsDisplayed()
    }

    @Test
    fun testLoginButton_isClickable() {
        // Enter credentials first
        composeRule.onNodeWithTag("emailField")
            .performTextInput("test@example.com")
        
        composeRule.onNodeWithTag("passwordField")
            .performTextInput("password123")
        
        // Click login button - should not crash
        composeRule.onNodeWithTag("loginButton")
            .performClick()
        
        // Wait for any async operation
        composeRule.waitForIdle()
    }

    @Test
    fun testSuccessfulLogin_navigatesToHomeScreen() {
        // Enter valid credentials
        composeRule.onNodeWithTag("emailField")
            .performTextInput("sahkishor24@gmail.com")
        
        composeRule.onNodeWithTag("passwordField")
            .performTextInput("Himanshu@123")

        // Click Login
        composeRule.onNodeWithTag("loginButton")
            .performClick()

        // Wait for async Firebase login to complete (up to 10 seconds)
        Thread.sleep(5000)
        composeRule.waitForIdle()

        // Verify navigation to HomeScreen
        Intents.intended(hasComponent(HomeScreen::class.java.name))
    }

    @Test
    fun testForgotPassword_navigatesToForgotPasswordScreen() {
        // Click on "Forgot password?" link
        composeRule.onNodeWithText("Forgot password?")
            .performClick()
        
        composeRule.waitForIdle()
        
        Intents.intended(hasComponent(ForgetPassword::class.java.name))
    }

    @Test
    fun testSignUpLink_navigatesToRegisterScreen() {
        // Click on "Sign up" link
        composeRule.onNodeWithText("Sign up")
            .performClick()
        
        composeRule.waitForIdle()
        
        Intents.intended(hasComponent(RegisterScreen::class.java.name))
    }
}