package com.example.agrofarm

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.agrofarm.view.LoginScreen
import com.example.agrofarm.view.MainActivity
import com.example.agrofarm.view.RegisterScreen
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeScreenInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    // ==================== UI DISPLAY TESTS ====================

    @Test
    fun welcomeScreen_displaysAppLogo() {
        // App logo should be displayed
        composeRule.onNodeWithText("AgroFarm")
            .assertIsDisplayed()
    }

    @Test
    fun welcomeScreen_displaysAppTitle() {
        composeRule.onNodeWithText("AgroFarm")
            .assertIsDisplayed()
    }

    @Test
    fun welcomeScreen_displaysTagline() {
        composeRule.onNodeWithText("Smart Farming Solutions")
            .assertIsDisplayed()
    }

    @Test
    fun welcomeScreen_displaysLoginButton() {
        composeRule.onNodeWithText("LOGIN")
            .assertIsDisplayed()
    }

    @Test
    fun welcomeScreen_displaysSignUpButton() {
        composeRule.onNodeWithText("SIGN UP")
            .assertIsDisplayed()
    }

    // ==================== BUTTON INTERACTION TESTS ====================

    @Test
    fun loginButton_isClickable() {
        composeRule.onNodeWithText("LOGIN")
            .performClick()
        
        composeRule.waitForIdle()
        
        // Should navigate to LoginScreen
        Intents.intended(hasComponent(LoginScreen::class.java.name))
    }

    @Test
    fun signUpButton_isClickable() {
        composeRule.onNodeWithText("SIGN UP")
            .performClick()
        
        composeRule.waitForIdle()
        
        // Should navigate to RegisterScreen
        Intents.intended(hasComponent(RegisterScreen::class.java.name))
    }

    // ==================== LAYOUT TESTS ====================

    @Test
    fun welcomeScreen_displaysAllRequiredElements() {
        // Test that all main UI elements are present
        composeRule.onNodeWithText("AgroFarm").assertIsDisplayed()
        composeRule.onNodeWithText("Smart Farming Solutions").assertIsDisplayed()
        composeRule.onNodeWithText("LOGIN").assertIsDisplayed()
        composeRule.onNodeWithText("SIGN UP").assertIsDisplayed()
    }
}