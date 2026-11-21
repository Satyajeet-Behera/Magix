package com.example.data.assistant

import android.content.Context
import com.example.domain.contract.AgentEngine
import com.example.domain.contract.AuditLogger
import com.example.domain.contract.GeminiEngine
import com.example.domain.contract.GreetingEngine
import com.example.domain.contract.SpeechRecognitionEngine
import com.example.domain.contract.VoiceEngine
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantState
import com.example.domain.model.ChatMessage
import com.example.domain.model.ChatSender
import com.example.domain.model.MagixVoiceProfile
import com.example.domain.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Single source of truth session manager for MAGIX.
 * Manages bidirectional voice interactions, TextToSpeech voice synthesis,
 * barge-in interruptions, continuous conversations, and phone control tools.
 */
class MagixSessionManager(
    private val context: Context,
    private val agentEngine: AgentEngine,
    private val geminiEngine: GeminiEngine,
    private val voiceEngine: VoiceEngine,
    private val speechRecognitionEngine: SpeechRecognitionEngine,
    private val greetingEngine: GreetingEngine,
    private val auditLogger: AuditLogger,
    private val getUserSettings: () -> UserSettings
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _sessionState = MutableStateFlow(AssistantState.IDLE)
    val sessionState: StateFlow<AssistantState> = _sessionState.asStateFlow()

    private val _activeTranscript = MutableStateFlow("")
    val activeTranscript: StateFlow<String> = _activeTranscript.asStateFlow()

    private val _taskProgress = MutableStateFlow<String?>(null)
    val taskProgress: StateFlow<String?> = _taskProgress.asStateFlow()

    private val _isContinuousSessionActive = MutableStateFlow(false)
    val isContinuousSessionActive: StateFlow<Boolean> = _isContinuousSessionActive.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "msg_init_welcome",
                sender = ChatSender.MAGIX,
                text = "MAGIX Phase 2 active. Neural planner, controlled tools, and local memory engines online Boss.",
                timestamp = System.currentTimeMillis() - 60000L,
                stateSnapshot = AssistantState.IDLE,
                isDevMock = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = voiceEngine.isSpeaking
    val isListening: StateFlow<Boolean> = speechRecognitionEngine.isListening
    val pendingConfirmation: StateFlow<ActionResult?> = agentEngine.pendingConfirmation

    private var sessionJob: Job? = null
    private var silenceTimeoutJob: Job? = null

    /**
     * Start a new voice-first session.
     * @param isVoiceInvocation true if awakened via "Hey, Magix!" or assistant role gesture.
     */
    fun startSession(isVoiceInvocation: Boolean = true) {
        sessionJob?.cancel()
        silenceTimeoutJob?.cancel()

        sessionJob = scope.launch {
            val settings = getUserSettings()
            val voice = MagixVoiceProfile.findByIdOrDefault(settings.selectedVoiceId)
            voiceEngine.setVoiceProfile(voice)
            voiceEngine.setSpeed(settings.voiceSpeed)

            _isContinuousSessionActive.value = true

            if (isVoiceInvocation) {
                // Time-aware spoken greeting: "Hello Boss. Good morning. How can I help you?"
                val formOfAddress = settings.formOfAddress.ifBlank { "Boss" }
                val greeting = greetingEngine.generateInvocationGreeting(formOfAddress)

                _sessionState.value = AssistantState.SPEAKING
                _activeTranscript.value = greeting

                // Spoken TTS output
                voiceEngine.speakAndWait(greeting)
            }

            // Immediately transition to listening for user directive
            listenForDirective()
        }
    }

    /**
     * Open speech recognition to capture spoken directive.
     */
    fun listenForDirective() {
        silenceTimeoutJob?.cancel()
        _sessionState.value = AssistantState.LISTENING
        _taskProgress.value = null

        speechRecognitionEngine.startListening(
            onResult = { recognizedText ->
                _activeTranscript.value = recognizedText
                handleRecognizedSpeech(recognizedText)
            },
            onError = { error ->
                if (_sessionState.value == AssistantState.LISTENING) {
                    _sessionState.value = AssistantState.IDLE
                    _activeTranscript.value = ""
                }
            }
        )
    }

    /**
     * Handle incoming speech from speech recognition engine.
     */
    private fun handleRecognizedSpeech(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) {
            _sessionState.value = AssistantState.IDLE
            return
        }

        // 1. Interruption / Stop Check
        if (clean.equals("stop", ignoreCase = true) ||
            clean.equals("magix stop", ignoreCase = true) ||
            clean.equals("cancel", ignoreCase = true) ||
            clean.equals("halt", ignoreCase = true)
        ) {
            handleBargeIn()
            return
        }

        // 2. Invocation wake check
        if (clean.equals("hey magix", ignoreCase = true) ||
            clean.equals("hey, magix", ignoreCase = true) ||
            clean.equals("hey magix!", ignoreCase = true)
        ) {
            startSession(isVoiceInvocation = true)
            return
        }

        // 3. Process directive
        processDirective(clean)
    }

    /**
     * Process directive through AgentEngine, tools, and synthesize spoken response.
     */
    fun processDirective(directive: String) {
        silenceTimeoutJob?.cancel()
        sessionJob?.cancel()

        sessionJob = scope.launch {
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = ChatSender.USER,
                text = directive,
                timestamp = System.currentTimeMillis(),
                stateSnapshot = AssistantState.LISTENING,
                isDevMock = false
            )
            _chatMessages.value = _chatMessages.value + userMsg

            _sessionState.value = AssistantState.THINKING
            _activeTranscript.value = directive

            val result = agentEngine.executeDirective(directive) { progress ->
                _taskProgress.value = progress
                if (progress.startsWith("Executing")) {
                    _sessionState.value = AssistantState.EXECUTING
                }
            }

            val finalState = when {
                result.cancelled -> AssistantState.IDLE
                result.success -> AssistantState.SUCCESS
                else -> AssistantState.ERROR
            }
            _sessionState.value = finalState

            val assistantMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = ChatSender.MAGIX,
                text = result.summary,
                timestamp = System.currentTimeMillis(),
                stateSnapshot = finalState,
                isDevMock = false
            )
            _chatMessages.value = _chatMessages.value + assistantMsg

            // Spoken TTS feedback to the user
            if (result.summary.isNotBlank() && !result.cancelled) {
                _sessionState.value = AssistantState.SPEAKING
                val settings = getUserSettings()
                val voice = MagixVoiceProfile.findByIdOrDefault(settings.selectedVoiceId)
                voiceEngine.setVoiceProfile(voice)
                voiceEngine.setSpeed(settings.voiceSpeed)

                voiceEngine.speakAndWait(result.summary)
            }

            // Continuous conversation handling
            if (_isContinuousSessionActive.value && !result.cancelled) {
                // Transition directly to listening for the next follow-up command
                listenForDirective()
                scheduleSilenceTimeout()
            } else {
                _sessionState.value = AssistantState.IDLE
                _activeTranscript.value = ""
                _taskProgress.value = null
            }
        }
    }

    /**
     * Barge-in interruption: immediately stops speaking and halts any current task.
     */
    fun handleBargeIn() {
        sessionJob?.cancel()
        silenceTimeoutJob?.cancel()

        scope.launch {
            voiceEngine.stop()
            agentEngine.cancelExecution()
            speechRecognitionEngine.stopListening()

            _sessionState.value = AssistantState.IDLE
            _taskProgress.value = null
            _activeTranscript.value = "Halted."

            val settings = getUserSettings()
            voiceEngine.setSpeed(settings.voiceSpeed)
            voiceEngine.speak("Stopped Boss.")
        }
    }

    /**
     * Emergency halt without spoken response.
     */
    fun emergencyHalt() {
        sessionJob?.cancel()
        silenceTimeoutJob?.cancel()

        scope.launch {
            voiceEngine.stop()
            agentEngine.cancelExecution()
            speechRecognitionEngine.stopListening()
            _sessionState.value = AssistantState.IDLE
            _taskProgress.value = null
            _activeTranscript.value = ""
            _isContinuousSessionActive.value = false
        }
    }

    /**
     * Inactivity timer to return to low-power IDLE state after conversational pause.
     */
    private fun scheduleSilenceTimeout() {
        silenceTimeoutJob?.cancel()
        silenceTimeoutJob = scope.launch {
            delay(5000L) // 5 seconds of conversational listening before returning to idle
            if (_sessionState.value == AssistantState.LISTENING) {
                speechRecognitionEngine.stopListening()
                _sessionState.value = AssistantState.IDLE
                _activeTranscript.value = ""
                _isContinuousSessionActive.value = false
            }
        }
    }

    fun setSystemState(state: AssistantState) {
        _sessionState.value = state
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }
}
