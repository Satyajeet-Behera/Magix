package com.example.data.assistant

import com.example.domain.contract.AgentEngine
import com.example.domain.contract.AssistantEngine
import com.example.domain.model.AssistantState
import com.example.domain.model.ChatMessage
import com.example.domain.model.ChatSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class RealAssistantEngine(
    private val agentEngine: AgentEngine,
    private val onProgressUpdate: (String) -> Unit = {}
) : AssistantEngine {

    private val _state = MutableStateFlow(AssistantState.IDLE)
    override val state: StateFlow<AssistantState> = _state.asStateFlow()

    override suspend fun processUserPrompt(prompt: String): ChatMessage {
        _state.value = AssistantState.THINKING

        val result = agentEngine.executeDirective(prompt) { progress ->
            onProgressUpdate(progress)
            if (progress.startsWith("Executing")) {
                _state.value = AssistantState.EXECUTING
            }
        }

        val finalState = when {
            result.cancelled -> AssistantState.IDLE
            result.success -> AssistantState.SUCCESS
            else -> AssistantState.ERROR
        }
        _state.value = finalState

        return ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.MAGIX,
            text = result.summary,
            timestamp = System.currentTimeMillis(),
            stateSnapshot = finalState,
            isDevMock = false
        )
    }

    override suspend fun triggerVoiceInput() {
        _state.value = AssistantState.LISTENING
    }

    override suspend fun cancelCurrentTask() {
        agentEngine.cancelExecution()
        _state.value = AssistantState.IDLE
    }

    override fun setSystemState(state: AssistantState) {
        _state.value = state
    }
}
