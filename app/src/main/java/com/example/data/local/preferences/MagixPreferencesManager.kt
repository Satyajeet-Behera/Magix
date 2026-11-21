package com.example.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.domain.model.ThemeMode
import com.example.domain.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MagixPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("magix_settings_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): UserSettings {
        val savedThemeName = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name)
        val themeMode = ThemeMode.fromString(savedThemeName)
        val isDark = when (themeMode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> prefs.getBoolean(KEY_IS_DARK, true)
        }

        return UserSettings(
            displayName = prefs.getString(KEY_DISPLAY_NAME, "Commander") ?: "Commander",
            formOfAddress = prefs.getString(KEY_FORM_OF_ADDRESS, "Boss") ?: "Boss",
            selectedVoiceId = prefs.getString(KEY_VOICE_ID, "Puck") ?: "Puck",
            voiceSpeed = prefs.getFloat(KEY_VOICE_SPEED, 1.0f),
            themeMode = themeMode,
            isDarkTheme = isDark,
            wakeWordEnabled = prefs.getBoolean(KEY_WAKE_WORD, false),
            backgroundAssistantEnabled = prefs.getBoolean(KEY_BG_ASSISTANT, false),
            livePresenceEnabled = prefs.getBoolean(KEY_LIVE_PRESENCE, true)
        )
    }

    fun updateDisplayName(name: String) {
        val clean = name.trim().ifBlank { "Commander" }
        prefs.edit { putString(KEY_DISPLAY_NAME, clean) }
        _settingsFlow.value = _settingsFlow.value.copy(displayName = clean)
    }

    fun updateFormOfAddress(form: String) {
        val clean = form.trim().ifBlank { "Boss" }
        prefs.edit { putString(KEY_FORM_OF_ADDRESS, clean) }
        _settingsFlow.value = _settingsFlow.value.copy(formOfAddress = clean)
    }

    fun updateSelectedVoice(voiceId: String) {
        prefs.edit { putString(KEY_VOICE_ID, voiceId) }
        _settingsFlow.value = _settingsFlow.value.copy(selectedVoiceId = voiceId)
    }

    fun updateVoiceSpeed(speed: Float) {
        prefs.edit { putFloat(KEY_VOICE_SPEED, speed) }
        _settingsFlow.value = _settingsFlow.value.copy(voiceSpeed = speed)
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        val isDark = when (themeMode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> true // Resolved dynamically in Composable via isSystemInDarkTheme()
        }
        prefs.edit {
            putString(KEY_THEME_MODE, themeMode.name)
            putBoolean(KEY_IS_DARK, isDark)
        }
        _settingsFlow.value = _settingsFlow.value.copy(
            themeMode = themeMode,
            isDarkTheme = isDark
        )
    }

    fun updateLivePresenceEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_LIVE_PRESENCE, enabled) }
        _settingsFlow.value = _settingsFlow.value.copy(livePresenceEnabled = enabled)
    }

    companion object {
        private const val KEY_DISPLAY_NAME = "key_display_name"
        private const val KEY_FORM_OF_ADDRESS = "key_form_of_address"
        private const val KEY_VOICE_ID = "key_voice_id"
        private const val KEY_VOICE_SPEED = "key_voice_speed"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_IS_DARK = "key_is_dark"
        private const val KEY_WAKE_WORD = "key_wake_word"
        private const val KEY_BG_ASSISTANT = "key_bg_assistant"
        private const val KEY_LIVE_PRESENCE = "key_live_presence"
    }
}

