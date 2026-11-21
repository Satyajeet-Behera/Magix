package com.example.data.assistant

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.domain.contract.VoiceEngine
import com.example.domain.model.MagixVoiceProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

class AndroidVoiceEngine(
    private val context: Context,
    initialProfile: MagixVoiceProfile = MagixVoiceProfile.PUCK_MALE,
    initialSpeed: Float = 1.0f
) : VoiceEngine {

    private val _activeVoiceProfile = MutableStateFlow(initialProfile)
    override val activeVoiceProfile: StateFlow<MagixVoiceProfile> = _activeVoiceProfile.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(initialSpeed)
    override val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val scope = CoroutineScope(Dispatchers.Main)
    private val utteranceCallbacks = ConcurrentHashMap<String, () -> Unit>()

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsInitialized = true
                    tts?.language = Locale.US

                    try {
                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                        tts?.setAudioAttributes(audioAttributes)
                    } catch (_: Exception) {}

                    applyVoiceParameters(_activeVoiceProfile.value, _playbackSpeed.value)
                    setupUtteranceListener()
                }
            }
        } catch (e: Exception) {
            // Graceful fallback for non-TTS environments (e.g. unit tests / emulator without TTS)
            isTtsInitialized = false
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                scope.launch { _isSpeaking.value = true }
            }

            override fun onDone(utteranceId: String?) {
                scope.launch { _isSpeaking.value = false }
                utteranceId?.let { id ->
                    utteranceCallbacks.remove(id)?.invoke()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                scope.launch { _isSpeaking.value = false }
                utteranceId?.let { id ->
                    utteranceCallbacks.remove(id)?.invoke()
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                scope.launch { _isSpeaking.value = false }
                utteranceId?.let { id ->
                    utteranceCallbacks.remove(id)?.invoke()
                }
            }
        })
    }

    private fun applyVoiceParameters(profile: MagixVoiceProfile, speed: Float) {
        tts?.let { engine ->
            engine.setSpeechRate(speed)

            // Select real Android TTS voice if available
            try {
                val availableVoices = engine.voices
                    if (!availableVoices.isNullOrEmpty()) {
                        val targetVoice = when (profile.internalId) {
                            MagixVoiceProfile.PUCK_MALE.internalId -> {
                                availableVoices.find { voice ->
                                    voice.locale.language.startsWith("en") &&
                                            (voice.name.contains("male", ignoreCase = true) ||
                                                    voice.name.contains("en-us-x-sfg", ignoreCase = true))
                                }
                            }
                            MagixVoiceProfile.KORE_FEMALE.internalId -> {
                                availableVoices.find { voice ->
                                    voice.locale.language.startsWith("en") &&
                                            (voice.name.contains("female", ignoreCase = true) ||
                                                    voice.name.contains("en-us-x-iol", ignoreCase = true))
                                }
                            }
                            else -> null
                        }
                        if (targetVoice != null) {
                            engine.voice = targetVoice
                        }
                    }
                } catch (_: Exception) {}

            // Pitch adjustment for distinct personality
            when (profile.internalId) {
                MagixVoiceProfile.PUCK_MALE.internalId -> {
                    engine.setPitch(0.92f) // Deep, resonant, calm masculine JARVIS timbre
                }
                MagixVoiceProfile.KORE_FEMALE.internalId -> {
                    engine.setPitch(1.16f) // Clear, articulate, high-fidelity feminine timbre
                }
                else -> {
                    engine.setPitch(1.0f)
                }
            }
        }
    }

    override suspend fun setVoiceProfile(profile: MagixVoiceProfile) {
        _activeVoiceProfile.value = profile
        applyVoiceParameters(profile, _playbackSpeed.value)
    }

    override suspend fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applyVoiceParameters(_activeVoiceProfile.value, speed)
    }

    override suspend fun auditionVoice(profile: MagixVoiceProfile) {
        setVoiceProfile(profile)
        val previewPhrase = if (profile == MagixVoiceProfile.PUCK_MALE) {
            "MAGIX acoustic channel active Boss. Puck neural voice synthesized."
        } else {
            "MAGIX acoustic channel active. Kore neural voice online and ready."
        }
        speak(previewPhrase)
    }

    override suspend fun stopAudition() {
        stop()
    }

    override suspend fun speak(text: String, onDone: (() -> Unit)?) {
        if (!isTtsInitialized || tts == null) {
            // Simulated speech pulse if TTS engine is absent on host (e.g. test environment)
            _isSpeaking.value = true
            kotlinx.coroutines.delay(1200)
            _isSpeaking.value = false
            onDone?.invoke()
            return
        }

        applyVoiceParameters(_activeVoiceProfile.value, _playbackSpeed.value)
        val utteranceId = UUID.randomUUID().toString()
        if (onDone != null) {
            utteranceCallbacks[utteranceId] = onDone
        }

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    override suspend fun speakAndWait(text: String) {
        if (text.isBlank()) return

        if (!isTtsInitialized || tts == null) {
            // Fallback for tests or missing TTS
            _isSpeaking.value = true
            val simulatedDuration = (text.length * 40L).coerceIn(800L, 3500L)
            kotlinx.coroutines.delay(simulatedDuration)
            _isSpeaking.value = false
            return
        }

        suspendCancellableCoroutine { continuation ->
            applyVoiceParameters(_activeVoiceProfile.value, _playbackSpeed.value)
            val utteranceId = UUID.randomUUID().toString()

            utteranceCallbacks[utteranceId] = {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }

            continuation.invokeOnCancellation {
                utteranceCallbacks.remove(utteranceId)
                try {
                    tts?.stop()
                } catch (_: Exception) {}
                _isSpeaking.value = false
            }

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    override suspend fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        // Trigger and clear all pending callbacks
        val pending = utteranceCallbacks.values.toList()
        utteranceCallbacks.clear()
        pending.forEach { it.invoke() }
        _isSpeaking.value = false
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        utteranceCallbacks.clear()
        _isSpeaking.value = false
        isTtsInitialized = false
    }
}
