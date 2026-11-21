package com.example.domain.model

enum class ThemeMode(
    val title: String,
    val description: String
) {
    DARK(
        title = "Dark (Obsidian)",
        description = "OLED dark canvas with luminescent cyan telemetry"
    ),
    LIGHT(
        title = "Light (Quantum)",
        description = "Pure high-contrast clean slate with cyan sapphire accents"
    ),
    SYSTEM(
        title = "System Default",
        description = "Automatically adapts to Android OS dark/light mode"
    );

    val prefKey: String
        get() = name.lowercase()

    companion object {
        fun fromString(value: String?): ThemeMode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DARK
        }

        fun fromPrefKey(key: String?): ThemeMode {
            return entries.firstOrNull { it.prefKey.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: DARK
        }
    }
}
