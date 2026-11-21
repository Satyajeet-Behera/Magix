package com.example.data.assistant

import com.example.domain.contract.AssistantEngine
import com.example.domain.model.AssistantState
import com.example.domain.model.ChatMessage
import com.example.domain.model.ChatSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Phase-1-Only Mock Assistant Implementation.
 * Clearly demarcated as a development harness to validate UI, state transitions,
 * audio visualizers, and command dispatch without pretending to be real Gemini AI.
 */
class MockAssistantEngine : AssistantEngine {

    private val _state = MutableStateFlow(AssistantState.IDLE)
    override val state: StateFlow<AssistantState> = _state.asStateFlow()

    override suspend fun processUserPrompt(prompt: String): ChatMessage {
        val cleanPrompt = prompt.trim()
        
        // 1. Transition to THINKING
        _state.value = AssistantState.THINKING
        delay(1200)

        // 2. Synthesize structured mock response based on prompt intent
        val (responseBody, simulatedDuration) = generateDevelopmentResponse(cleanPrompt)

        // 3. Transition to SPEAKING
        _state.value = AssistantState.SPEAKING
        delay((simulatedDuration * 1000L).toLong().coerceIn(1500L, 3500L))

        // 4. Transition to SUCCESS
        _state.value = AssistantState.SUCCESS
        delay(900)

        // 5. Settle back into IDLE standby
        _state.value = AssistantState.IDLE

        return ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.MAGIX,
            text = responseBody,
            timestamp = System.currentTimeMillis(),
            stateSnapshot = AssistantState.SUCCESS,
            isDevMock = true,
            audioDurationSeconds = simulatedDuration
        )
    }

    override suspend fun triggerVoiceInput() {
        if (_state.value == AssistantState.LISTENING) {
            _state.value = AssistantState.IDLE
            return
        }
        _state.value = AssistantState.LISTENING
    }

    override suspend fun cancelCurrentTask() {
        _state.value = AssistantState.IDLE
    }

    override fun setSystemState(state: AssistantState) {
        _state.value = state
    }

    private fun generateDevelopmentResponse(prompt: String): Pair<String, Float> {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                "Greetings. All MAGIX local telemetry nodes are synchronized and awaiting your command. How may I assist your workflow?" to 2.2f
            }
            lower.contains("routine") || lower.contains("morning") || lower.contains("automation") -> {
                "[DEV MOCK] Autonomous Routine Pipeline Triggered: Identified 'Morning Routine'. In Phase 2+, this will aggregate weather, calendar nodes, and transit telemetry. Phase 1 local execution verified." to 3.1f
            }
            lower.contains("memory") || lower.contains("remember") -> {
                "[DEV MOCK] Memory Subsystem: Contextual parameter recorded into local Room database. In Phase 2+, this will generate semantic vector embeddings for autonomous recall." to 2.8f
            }
            lower.contains("who are you") || lower.contains("magix") -> {
                "I am MAGIX — an architectural foundation for your personal Android AI assistant. Currently operating in Phase 1 mode within Google AI Studio." to 2.9f
            }
            lower.contains("status") || lower.contains("diagnostic") -> {
                "[DEV MOCK] Diagnostic Report: Core state engine NOMINAL. Local SQLite persistent layer ACTIVE. Voice subsystem configured. Operating within Android sandboxed permissions." to 3.0f
            }
            else -> {
                "[DEV MOCK] Acknowledged directive: \"$prompt\". Synthesizing telemetry parameters. In later phases, this query will route to the Gemini Live reasoning neural brain." to 2.5f
            }
        }
    }
}
