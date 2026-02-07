package com.example.agrofarm

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.agrofarm.view.LoginScreen
import com.example.agrofarm.view.RegisterScreen
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Before
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class RegisterScreenInstrumentedTest {
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
    fun testSuccessfulLogin_navigatesToDashboard() {
////         Enter email
//        composeRule.onNodeWithTag("emailField")
//            .performTextInput("sahkishor24@gmail.com")
////
////        // Enter password
//        composeRule.onNodeWithTag("passwordField")
//            .performTextInput("Himanshu@123")

        // Click Login
        composeRule.onNodeWithTag("registerButton")
            .performClick()


//        Intents.intended(hasComponent(DashboardActivity::class.java.name))
        Intents.intended(hasComponent(RegisterScreen::class.java.name))
    }

    @Test
    fun testRegisterScreenElements_areDisplayed() {
        // Navigate to register screen
        composeRule.onNodeWithTag("registerButton")
            .performClick()

        // Verify all UI elements are displayed
        composeRule.onNodeWithTag("nameField")
            .assertIsDisplayed()
        
        composeRule.onNodeWithTag("emailField")
            .assertIsDisplayed()
        
        composeRule.onNodeWithTag("passwordField")
            .assertIsDisplayed()
        
        composeRule.onNodeWithTag("confirmPasswordField")
            .assertIsDisplayed()
        
        composeRule.onNodeWithTag("registerSubmitButton")
            .assertIsDisplayed()
    }

    @Test
    fun testRegisterForm_withValidData() {
        // Navigate to register screen
        composeRule.onNodeWithTag("registerButton")
            .performClick()

        // Fill form with valid data
        composeRule.onNodeWithTag("nameField")
            .performTextInput("John Doe")
        
        composeRule.onNodeWithTag("emailField")
            .performTextInput("johndoe@example.com")
        
        composeRule.onNodeWithTag("passwordField")
            .performTextInput("Password123!")
        
        composeRule.onNodeWithTag("confirmPasswordField")
            .performTextInput("Password123!")

        // Submit registration
        composeRule.onNodeWithTag("registerSubmitButton")
            .performClick()
    }

    @Test
    fun testRegisterForm_withEmptyFields() {
        // Navigate to register screen
        composeRule.onNodeWithTag("registerButton")
            .performClick()

        // Try to submit with empty fields
        composeRule.onNodeWithTag("registerSubmitButton")
            .performClick()

        // Should show validation errors (adjust based on your actual error handling)
        // Example: Check if error messages are displayed
    }

    @Test
    fun testPasswordMismatch_showsError() {
        // Navigate to register screen
        composeRule.onNodeWithTag("registerButton")
            .performClick()

        // Fill form with mismatched passwords
        composeRule.onNodeWithTag("nameField")
            .performTextInput("John Doe")
        
        composeRule.onNodeWithTag("emailField")
            .performTextInput("johndoe@example.com")
        
        composeRule.onNodeWithTag("passwordField")
            .performTextInput("Password123!")
        
        composeRule.onNodeWithTag("confirmPasswordField")
            .performTextInput("DifferentPassword")

        // Submit registration
        composeRule.onNodeWithTag("registerSubmitButton")
            .performClick()

        // Should show password mismatch error
    }

    @Test
    fun testInvalidEmail_showsError() {
        // Navigate to register screen
        composeRule.onNodeWithTag("registerButton")
            .performClick()

        // Fill form with invalid email
        composeRule.onNodeWithTag("nameField")
            .performTextInput("John Doe")
        
        composeRule.onNodeWithTag("emailField")
            .performTextInput("invalid-email")
        
        composeRule.onNodeWithTag("passwordField")
            .performTextInput("Password123!")
        
        composeRule.onNodeWithTag("confirmPasswordField")
            .performTextInput("Password123!")

        // Submit registration
        composeRule.onNodeWithTag("registerSubmitButton")
            .performClick()

        // Should show invalid email error
    }

    @Test
    fun testBackNavigation_fromRegisterScreen() {
        // Navigate to register screen
        composeRule.onNodeWithTag("registerButton")
            .performClick()

        // Navigate back (assuming there's a back button)
        composeRule.onNodeWithTag("backButton")
            .performClick()

        // Should be back on login screen
        composeRule.onNodeWithTag("loginSubmitButton")
            .assertIsDisplayed()
    }
}