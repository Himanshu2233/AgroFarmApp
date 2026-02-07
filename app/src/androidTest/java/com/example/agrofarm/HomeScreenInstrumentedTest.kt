package com.example.agrofarm

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.agrofarm.view.CattleActivity
import com.example.agrofarm.view.CropsActivity
import com.example.agrofarm.view.HomeScreen
import com.example.agrofarm.view.InventoryActivity
import com.example.agrofarm.view.ProfileActivity
import com.example.agrofarm.view.ReportsActivity
import com.example.agrofarm.view.SettingsActivity
import com.example.agrofarm.view.WeatherActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<HomeScreen>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    // ==================== UI ELEMENT TESTS ====================

    @Test
    fun homeScreen_displaysWelcomeCard() {
        composeRule.onNodeWithText("Welcome Back!")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysAgroFarmTitle() {
        composeRule.onNodeWithText("AgroFarm")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysFarmManagementSection() {
        composeRule.onNodeWithText("Farm Management")
            .assertIsDisplayed()
    }

    // ==================== FEATURE CARD TESTS ====================

    @Test
    fun homeScreen_displaysCropsCard() {
        composeRule.onNodeWithText("Crops")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysWeatherCard() {
        composeRule.onNodeWithText("Weather")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysInventoryCard() {
        composeRule.onNodeWithText("Inventory")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysCattleCard() {
        composeRule.onNodeWithText("Cattle")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysReportsCard() {
        composeRule.onNodeWithText("Reports")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysSettingsCard() {
        composeRule.onNodeWithText("Settings")
            .assertIsDisplayed()
    }

    // ==================== NAVIGATION TESTS ====================

    @Test
    fun clickCrops_navigatesToCropsActivity() {
        composeRule.onNodeWithText("Crops")
            .performClick()

        Intents.intended(hasComponent(CropsActivity::class.java.name))
    }

    @Test
    fun clickWeather_navigatesToWeatherActivity() {
        composeRule.onNodeWithText("Weather")
            .performClick()

        Intents.intended(hasComponent(WeatherActivity::class.java.name))
    }

    @Test
    fun clickInventory_navigatesToInventoryActivity() {
        composeRule.onNodeWithText("Inventory")
            .performClick()

        Intents.intended(hasComponent(InventoryActivity::class.java.name))
    }

    @Test
    fun clickCattle_navigatesToCattleActivity() {
        composeRule.onNodeWithText("Cattle")
            .performClick()

        Intents.intended(hasComponent(CattleActivity::class.java.name))
    }

    @Test
    fun clickReports_navigatesToReportsActivity() {
        composeRule.onNodeWithText("Reports")
            .performClick()

        Intents.intended(hasComponent(ReportsActivity::class.java.name))
    }

    @Test
    fun clickSettings_navigatesToSettingsActivity() {
        composeRule.onNodeWithText("Settings")
            .performClick()

        Intents.intended(hasComponent(SettingsActivity::class.java.name))
    }

    @Test
    fun clickProfileIcon_navigatesToProfileActivity() {
        composeRule.onNodeWithContentDescription("Profile")
            .performClick()

        Intents.intended(hasComponent(ProfileActivity::class.java.name))
    }

    // ==================== THEME TOGGLE TEST ====================

    @Test
    fun clickDarkModeToggle_togglesTheme() {
        // Find and click the dark mode toggle button
        composeRule.onNodeWithContentDescription("Switch to Dark Mode")
            .performClick()

        // After clicking, it should show "Switch to Light Mode"
        composeRule.onNodeWithContentDescription("Switch to Light Mode")
            .assertIsDisplayed()
    }
}
