package com.example.domain.contract

import com.example.domain.model.ActionResult
import kotlinx.coroutines.flow.StateFlow

data class AgentExecutionStep(
    val stepIndex: Int,
    val toolName: String,
    val arguments: Map<String, String>,
    val status: String,
    val result: ActionResult? = null
)

data class AgentPlan(
    val reasoning: String,
    val steps: List<AgentExecutionStep>,
    val directSpeechResponse: String? = null
)

interface GeminiEngine {
    val isOnline: StateFlow<Boolean>
    val isProcessing: StateFlow<Boolean>

    suspend fun planAndReason(
        userPrompt: String,
        screenContext: String = "",
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): AgentPlan

    suspend fun synthesizeFinalResponse(
        originalPrompt: String,
        plan: AgentPlan,
        executedSteps: List<AgentExecutionStep>
    ): String
}
