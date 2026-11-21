package com.example.domain.contract

import com.example.domain.model.AssistantState
import com.example.domain.model.ChatMessage
import kotlinx.coroutines.flow.StateFlow

/**
 * Future Extension Contract: AssistantEngine
 * In Phase 1: MockAssistantEngine satisfies this contract.
 * In Phase 2+: Connects to Gemini Live, Gemini Reasoning, Multimodal models.
 */
interface AssistantEngine {
    val state: StateFlow<AssistantState>

    suspend fun processUserPrompt(prompt: String): ChatMessage

    suspend fun triggerVoiceInput(): Unit

    suspend fun cancelCurrentTask(): Unit

    fun setSystemState(state: AssistantState): Unit
}
