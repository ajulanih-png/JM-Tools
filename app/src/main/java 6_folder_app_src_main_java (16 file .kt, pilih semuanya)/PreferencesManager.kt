// data/PreferencesManager.kt
package com.JM.Optimizer.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "JM_prefs")

class PreferencesManager(private val context: Context) {
    
    companion object {
        // Keys
        private val POINTER_SPEED = intPreferencesKey("pointer_speed")
        private val MOUSE_SPEED = intPreferencesKey("mouse_speed")
        private val SMART_DPI_ENABLED = booleanPreferencesKey("smart_dpi_enabled")
        private val AUTO_CLEAN_ENABLED = booleanPreferencesKey("auto_clean_enabled")
        private val GRAPHICS_TWEAK_ENABLED = booleanPreferencesKey("graphics_tweak")
        private val NETWORK_TWEAK_ENABLED = booleanPreferencesKey("network_tweak")
        private val DEFAULT_DPI = intPreferencesKey("default_dpi")
        private val DEFAULT_WIDTH = intPreferencesKey("default_width")
        private val DEFAULT_HEIGHT = intPreferencesKey("default_height")
    }
    
    // Pointer Speed
    val pointerSpeed: Flow<Int> = context.dataStore.data.map { 
        it[POINTER_SPEED] ?: 5 
    }
    
    suspend fun setPointerSpeed(speed: Int) {
        context.dataStore.edit { it[POINTER_SPEED] = speed }
    }
    
    // Mouse Speed (-7 to 7)
    val mouseSpeed: Flow<Int> = context.dataStore.data.map { 
        it[MOUSE_SPEED] ?: 0 
    }
    
    suspend fun setMouseSpeed(speed: Int) {
        context.dataStore.edit { it[MOUSE_SPEED] = speed.coerceIn(-7, 7) }
    }
    
    // Smart DPI
    val smartDpiEnabled: Flow<Boolean> = context.dataStore.data.map { 
        it[SMART_DPI_ENABLED] ?: false 
    }
    
    suspend fun setSmartDpiEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SMART_DPI_ENABLED] = enabled }
    }
    
    // Auto Clean
    val autoCleanEnabled: Flow<Boolean> = context.dataStore.data.map { 
        it[AUTO_CLEAN_ENABLED] ?: false 
    }
    
    suspend fun setAutoCleanEnabled(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_CLEAN_ENABLED] = enabled }
    }
    
    // Graphics Tweak
    val graphicsTweakEnabled: Flow<Boolean> = context.dataStore.data.map { 
        it[GRAPHICS_TWEAK_ENABLED] ?: false 
    }
    
    suspend fun setGraphicsTweak(enabled: Boolean) {
        context.dataStore.edit { it[GRAPHICS_TWEAK_ENABLED] = enabled }
    }
    
    // Network Tweak
    val networkTweakEnabled: Flow<Boolean> = context.dataStore.data.map { 
        it[NETWORK_TWEAK_ENABLED] ?: false 
    }
    
    suspend fun setNetworkTweak(enabled: Boolean) {
        context.dataStore.edit { it[NETWORK_TWEAK_ENABLED] = enabled }
    }
    
    // Default DPI
    val defaultDpi: Flow<Int> = context.dataStore.data.map { 
        it[DEFAULT_DPI] ?: 580 
    }
    
    suspend fun setDefaultDpi(dpi: Int) {
        context.dataStore.edit { it[DEFAULT_DPI] = dpi }
    }
    
    // Default Size
    val defaultSize: Flow<Pair<Int, Int>> = context.dataStore.data.map { 
        Pair(it[DEFAULT_WIDTH] ?: 720, it[DEFAULT_HEIGHT] ?: 1600)
    }
    
    suspend fun setDefaultSize(width: Int, height: Int) {
        context.dataStore.edit { 
            it[DEFAULT_WIDTH] = width
            it[DEFAULT_HEIGHT] = height
        }
    }
}