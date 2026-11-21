package com.example.data.assistant

import com.example.domain.contract.AccessibilityController
import com.example.domain.contract.AgentEngine
import com.example.domain.contract.AgentExecutionResult
import com.example.domain.contract.AgentExecutionStep
import com.example.domain.contract.AgentPlan
import com.example.domain.contract.AuditLogger
import com.example.domain.contract.GeminiEngine
import com.example.domain.model.ActionResult
import com.example.domain.tool.ToolRegistry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

class AgentEngineImpl(
    private val geminiEngine: GeminiEngine,
    private val toolRegistry: ToolRegistry,
    private val accessibilityController: AccessibilityController,
    private val auditLogger: AuditLogger
) : AgentEngine {

    private val _currentPlan = MutableStateFlow<AgentPlan?>(null)
    override val currentPlan: StateFlow<AgentPlan?> = _currentPlan.asStateFlow()

    private val _pendingConfirmation = MutableStateFlow<ActionResult?>(null)
    override val pendingConfirmation: StateFlow<ActionResult?> = _pendingConfirmation.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    override val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val cancellationRequested = AtomicBoolean(false)
    private var confirmationDeferred: CompletableDeferred<Boolean>? = null

    override suspend fun executeDirective(
        directive: String,
        onProgress: (String) -> Unit
    ): AgentExecutionResult {
        cancellationRequested.set(false)
        _isExecuting.value = true

        onProgress("Analyzing intent and current screen state...")

        val screenContext = try {
            accessibilityController.captureScreenHierarchySummary()
        } catch (_: Exception) {
            ""
        }

        val plan = geminiEngine.planAndReason(directive, screenContext)
        _currentPlan.value = plan

        if (cancellationRequested.get()) {
            _isExecuting.value = false
            return AgentExecutionResult(false, "Task cancelled by Boss.", emptyList(), cancelled = true)
        }

        // Direct speech response (no tools needed)
        if (plan.steps.isEmpty()) {
            _isExecuting.value = false
            val speech = plan.directSpeechResponse ?: "Directive acknowledged Boss."
            return AgentExecutionResult(true, speech, emptyList())
        }

        // Autonomous step execution with verification, bounds, and safety limits (Sections 10, 11, 12)
        val executedSteps = mutableListOf<AgentExecutionStep>()
        val boundedSteps = plan.steps.take(com.example.domain.model.SecurityPolicy.MAX_ACTION_STEPS)
        val startTime = System.currentTimeMillis()

        for (step in boundedSteps) {
            // Task duration safety limit check
            if (System.currentTimeMillis() - startTime > com.example.domain.model.SecurityPolicy.MAX_TASK_DURATION_MS) {
                _isExecuting.value = false
                auditLogger.logAction("TIMEOUT_ABORT", directive, "ABORTED", "Execution exceeded maximum task duration (60s).")
                return AgentExecutionResult(false, "Safety limit reached: Task exceeded 60 second duration limit.", executedSteps)
            }

            if (cancellationRequested.get()) {
                _isExecuting.value = false
                auditLogger.logAction("EMERGENCY_STOP", directive, "CANCELLED", "User initiated emergency halt.")
                return AgentExecutionResult(false, "Autonomous execution paused by emergency stop.", executedSteps, cancelled = true)
            }

            onProgress("Executing Step ${step.stepIndex}/${boundedSteps.size}: ${step.toolName}")

            // Sensitive action confirmation check
            val tool = toolRegistry.getTool(step.toolName)
            var userConfirmed = false

            if (tool?.isSensitive == true) {
                onProgress("Waiting for confirmation on sensitive action: ${step.toolName}")
                val confirmResult = ActionResult.confirmationRequired(
                    step.toolName,
                    "MAGIX requires your confirmation before executing: ${step.toolName} ${step.arguments}"
                )
                _pendingConfirmation.value = confirmResult

                val deferred = CompletableDeferred<Boolean>()
                confirmationDeferred = deferred
                userConfirmed = deferred.await()
                _pendingConfirmation.value = null

                if (!userConfirmed) {
                    val stepResult = ActionResult.failure(step.toolName, "Cancelled by user during confirmation.")
                    executedSteps.add(step.copy(status = "REJECTED", result = stepResult))
                    _isExecuting.value = false
                    return AgentExecutionResult(false, "Sensitive action ${step.toolName} was rejected.", executedSteps)
                }
            }

            // Execute tool
            var stepResult = toolRegistry.executeTool(step.toolName, step.arguments, userConfirmed)

            // Bounded Verification and Intelligent Recovery (Max 2 retries)
            var retryCount = 0
            while (!stepResult.success && retryCount < com.example.domain.model.SecurityPolicy.MAX_RETRIES && !cancellationRequested.get()) {
                retryCount++
                onProgress("Step ${step.toolName} attempt $retryCount: ${stepResult.reason}. Attempting recovery...")
                delay(600) // Brief stabilization wait

                // Recovery attempt 1: if tap failed, check if screen changed or re-inspect
                if (step.toolName == "tap" && step.arguments["target_text"] != null) {
                    val fallbackText = step.arguments["target_text"]!!
                    val reClicked = accessibilityController.performClickOnNode(fallbackText)
                    if (reClicked) {
                        stepResult = ActionResult.success(step.toolName, "Recovered via accessibility tree re-traversal on attempt $retryCount.")
                    }
                } else if (step.toolName == "open_app") {
                    // Retry open app with alternate flag
                    stepResult = toolRegistry.executeTool(step.toolName, step.arguments, userConfirmed)
                }
            }

            executedSteps.add(step.copy(
                status = if (stepResult.success) "SUCCESS" else "FAILED",
                result = stepResult
            ))

            // If a critical step failed after recovery, break early instead of blindly continuing
            if (!stepResult.success) {
                onProgress("Step ${step.toolName} could not be completed.")
                break
            }

            delay(400) // Realistic UI interval between autonomous operations
        }

        _isExecuting.value = false
        val finalSpeech = geminiEngine.synthesizeFinalResponse(directive, plan, executedSteps)
        val allSucceeded = executedSteps.isNotEmpty() && executedSteps.all { it.result?.success == true }

        return AgentExecutionResult(
            success = allSucceeded,
            summary = finalSpeech,
            steps = executedSteps,
            cancelled = cancellationRequested.get()
        )
    }

    override fun cancelExecution() {
        cancellationRequested.set(true)
        confirmationDeferred?.complete(false)
        _pendingConfirmation.value = null
        _isExecuting.value = false
    }

    override fun confirmPendingAction(confirmed: Boolean) {
        confirmationDeferred?.complete(confirmed)
        _pendingConfirmation.value = null
    }
}
