package com.example.domain.contract

import com.example.domain.model.ActionResult
import kotlinx.coroutines.flow.StateFlow

data class AgentExecutionResult(
    val success: Boolean,
    val summary: String,
    val steps: List<AgentExecutionStep>,
    val cancelled: Boolean = false
)

interface AgentEngine {
    val currentPlan: StateFlow<AgentPlan?>
    val pendingConfirmation: StateFlow<ActionResult?>
    val isExecuting: StateFlow<Boolean>

    suspend fun executeDirective(
        directive: String,
        onProgress: (String) -> Unit
    ): AgentExecutionResult

    fun cancelExecution()
    fun confirmPendingAction(confirmed: Boolean)
}
