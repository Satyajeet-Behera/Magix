package com.example.domain.contract

import com.example.domain.model.MagixVoiceProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Future Extension Contract: VoiceEngine
 * In Phase 1: MockVoiceEngine manages profile selection, speed, and acoustic tone previews.
 * In Phase 2+: Connects to Gemini Live audio websocket, local or cloud neural TTS.
 */
interface VoiceEngine {
    val activeVoiceProfile: StateFlow<MagixVoiceProfile>
    val playbackSpeed: StateFlow<Float>
    val isSpeaking: StateFlow<Boolean>

    suspend fun setVoiceProfile(profile: MagixVoiceProfile)
    suspend fun setSpeed(speed: Float)
    suspend fun auditionVoice(profile: MagixVoiceProfile)
    suspend fun stopAudition()
    suspend fun speak(text: String, onDone: (() -> Unit)? = null)
    suspend fun speakAndWait(text: String)
    suspend fun stop()
}
