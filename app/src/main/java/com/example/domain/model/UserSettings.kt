package com.example.domain.model

data class UserSettings(
    val displayName: String = "Commander",
    val formOfAddress: String = "Boss",
    val selectedVoiceId: String = "Puck",
    val voiceSpeed: Float = 1.0f,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val isDarkTheme: Boolean = true,
    val wakeWordEnabled: Boolean = false,
    val backgroundAssistantEnabled: Boolean = false,
    val livePresenceEnabled: Boolean = true
)

