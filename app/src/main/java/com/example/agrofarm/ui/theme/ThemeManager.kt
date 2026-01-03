package com.example.agrofarm.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ThemeManager handles dark mode preference persistence and state
 */
object ThemeManager {
    private const val PREFS_NAME = "agrofarm_theme_prefs"
    private const val KEY_DARK_MODE = "dark_mode"
    
    private var prefs: SharedPreferences? = null
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isDarkMode.value = prefs?.getBoolean(KEY_DARK_MODE, false) ?: false
    }
    
    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        prefs?.edit()?.putBoolean(KEY_DARK_MODE, newValue)?.apply()
    }
    
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs?.edit()?.putBoolean(KEY_DARK_MODE, enabled)?.apply()
    }
}

/**
 * CompositionLocal for accessing dark mode state in Composables
 */
val LocalDarkMode = compositionLocalOf { false }
