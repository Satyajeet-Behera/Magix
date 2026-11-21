package com.example.data.assistant

import com.example.domain.contract.VoiceEngine
import com.example.domain.model.MagixVoiceProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Phase 1 Mock Voice Engine.
 * Simulates profile selection and audio audition without calling real Gemini Audio/TTS yet.
 */
class MockVoiceEngine(
    initialProfile: MagixVoiceProfile = MagixVoiceProfile.PUCK_MALE,
    initialSpeed: Float = 1.0f,
    private val onStateChange: ((Boolean) -> Unit)? = null
) : VoiceEngine {

    private val _activeVoiceProfile = MutableStateFlow(initialProfile)
    override val activeVoiceProfile: StateFlow<MagixVoiceProfile> = _activeVoiceProfile.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(initialSpeed)
    override val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var auditionJob: Job? = null

    override suspend fun setVoiceProfile(profile: MagixVoiceProfile) {
        _activeVoiceProfile.value = profile
    }

    override suspend fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    override suspend fun auditionVoice(profile: MagixVoiceProfile) = coroutineScope {
        stopAudition()
        auditionJob = launch {
            _activeVoiceProfile.value = profile
            _isSpeaking.value = true
            onStateChange?.invoke(true)
            
            // Audition simulated playback duration based on speed
            val duration = (2500L / _playbackSpeed.value).toLong()
            delay(duration)
            
            _isSpeaking.value = false
            onStateChange?.invoke(false)
        }
    }

    override suspend fun stopAudition() {
        auditionJob?.cancel()
        auditionJob = null
        _isSpeaking.value = false
        onStateChange?.invoke(false)
    }

    override suspend fun speak(text: String, onDone: (() -> Unit)?) = coroutineScope {
        stop()
        auditionJob = launch {
            _isSpeaking.value = true
            onStateChange?.invoke(true)
            val duration = ((text.length * 40L).coerceIn(400L, 2000L) / _playbackSpeed.value).toLong()
            delay(duration)
            _isSpeaking.value = false
            onStateChange?.invoke(false)
            onDone?.invoke()
        }
    }

    override suspend fun speakAndWait(text: String) {
        if (text.isBlank()) return
        stop()
        coroutineScope {
            _isSpeaking.value = true
            onStateChange?.invoke(true)
            val duration = ((text.length * 40L).coerceIn(400L, 2000L) / _playbackSpeed.value).toLong()
            delay(duration)
            _isSpeaking.value = false
            onStateChange?.invoke(false)
        }
    }

    override suspend fun stop() {
        stopAudition()
    }
}
