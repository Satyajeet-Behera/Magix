package com.example.domain.model

data class MagixVoiceProfile(
    val id: String,
    val name: String,
    val internalId: String,
    val gender: String,
    val personality: String,
    val sampleQuote: String
) {
    companion object {
        val PUCK_MALE = MagixVoiceProfile(
            id = "voice_male_puck",
            name = "MAGIX Male",
            internalId = "Puck",
            gender = "Male",
            personality = "Confident, energetic and intelligent",
            sampleQuote = "Ready when you are, Commander. All telemetry feeds and local subroutines are nominal."
        )

        val KORE_FEMALE = MagixVoiceProfile(
            id = "voice_female_kore",
            name = "MAGIX Female",
            internalId = "Kore",
            gender = "Female",
            personality = "Calm, precise and sophisticated",
            sampleQuote = "Telemetry initialized. Ready to orchestrate phone workflows with complete precision."
        )

        val ALL_PROFILES = listOf(PUCK_MALE, KORE_FEMALE)

        fun findByIdOrDefault(id: String): MagixVoiceProfile {
            return ALL_PROFILES.find { it.internalId.equals(id, ignoreCase = true) || it.id.equals(id, ignoreCase = true) }
                ?: PUCK_MALE
        }
    }
}

enum class VoiceSpeed(val speed: Float, val label: String) {
    SLOW(0.75f, "0.75x"),
    NORMAL(1.0f, "1.0x"),
    FAST(1.25f, "1.25x"),
    VERY_FAST(1.5f, "1.5x");

    companion object {
        fun fromSpeed(speed: Float): VoiceSpeed {
            return entries.minByOrNull { kotlin.math.abs(it.speed - speed) } ?: NORMAL
        }
    }
}
