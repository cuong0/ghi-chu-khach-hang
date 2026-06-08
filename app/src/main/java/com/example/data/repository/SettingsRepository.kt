package com.example.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("lead_notes_settings", Context.MODE_PRIVATE)

    // Dark Theme flow
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("key_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean("key_dark_theme", enabled).apply()
        _isDarkTheme.value = enabled
    }

    // Is Sync Enabled flow
    private val _isSyncEnabled = MutableStateFlow(prefs.getBoolean("key_sync_enabled", false))
    val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled.asStateFlow()

    fun setSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("key_sync_enabled", enabled).apply()
        _isSyncEnabled.value = enabled
    }

    // User authentication / profile states
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("key_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("key_user_name", "Người dùng") ?: "Người dùng")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userPhone = MutableStateFlow(prefs.getString("key_user_phone", "") ?: "")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString("key_user_email", "") ?: "")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userAvatarUri = MutableStateFlow(prefs.getString("key_user_avatar", "") ?: "")
    val userAvatarUri: StateFlow<String> = _userAvatarUri.asStateFlow()

    private val _userAvatarScale = MutableStateFlow(prefs.getFloat("key_user_avatar_scale", 1.0f))
    val userAvatarScale: StateFlow<Float> = _userAvatarScale.asStateFlow()

    private val _userAvatarOffsetX = MutableStateFlow(prefs.getFloat("key_user_avatar_offset_x", 0.0f))
    val userAvatarOffsetX: StateFlow<Float> = _userAvatarOffsetX.asStateFlow()

    private val _userAvatarOffsetY = MutableStateFlow(prefs.getFloat("key_user_avatar_offset_y", 0.0f))
    val userAvatarOffsetY: StateFlow<Float> = _userAvatarOffsetY.asStateFlow()

    fun login(name: String, phone: String, email: String) {
        prefs.edit()
            .putBoolean("key_logged_in", true)
            .putString("key_user_name", name)
            .putString("key_user_phone", phone)
            .putString("key_user_email", email)
            .apply()
        _isLoggedIn.value = true
        _userName.value = name
        _userPhone.value = phone
        _userEmail.value = email
    }

    fun logout() {
        prefs.edit()
            .putBoolean("key_logged_in", false)
            .putBoolean("key_sync_enabled", false) // Turn off synchronization upon explicit signout
            .apply()
        _isLoggedIn.value = false
        _isSyncEnabled.value = false
    }

    fun updateAvatar(uri: String, scale: Float = 1.0f, offsetX: Float = 0.0f, offsetY: Float = 0.0f) {
        prefs.edit()
            .putString("key_user_avatar", uri)
            .putFloat("key_user_avatar_scale", scale)
            .putFloat("key_user_avatar_offset_x", offsetX)
            .putFloat("key_user_avatar_offset_y", offsetY)
            .apply()
        _userAvatarUri.value = uri
        _userAvatarScale.value = scale
        _userAvatarOffsetX.value = offsetX
        _userAvatarOffsetY.value = offsetY
    }

    // Custom Areas flow stored persistently in SharedPreferences
    private val _customAreas = MutableStateFlow(
        prefs.getStringSet("key_custom_areas", emptySet())?.toList() ?: emptyList()
    )
    val customAreas: StateFlow<List<String>> = _customAreas.asStateFlow()

    fun addCustomArea(area: String) {
        val trimmed = area.trim()
        if (trimmed.isEmpty()) return
        val currentSet = prefs.getStringSet("key_custom_areas", emptySet()) ?: emptySet()
        val newSet = currentSet + trimmed
        prefs.edit().putStringSet("key_custom_areas", newSet).apply()
        _customAreas.value = newSet.toList().sorted()
    }

    fun removeCustomArea(area: String) {
        val currentSet = prefs.getStringSet("key_custom_areas", emptySet()) ?: emptySet()
        val newSet = currentSet - area.trim()
        prefs.edit().putStringSet("key_custom_areas", newSet).apply()
        _customAreas.value = newSet.toList().sorted()
    }

    fun clearAllCustomAreas() {
        prefs.edit().remove("key_custom_areas").apply()
        _customAreas.value = emptyList()
    }
}
