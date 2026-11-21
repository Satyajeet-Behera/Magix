package com.example.domain.model

data class ActionResult(
    val success: Boolean,
    val actionName: String = "",
    val reason: String = "",
    val evidence: String = "",
    val nextAction: String? = null,
    val requiresConfirmation: Boolean = false,
    val confirmationPrompt: String? = null
) {
    companion object {
        fun success(actionName: String, evidence: String = "Operation executed successfully", nextAction: String? = null): ActionResult =
            ActionResult(
                success = true,
                actionName = actionName,
                evidence = evidence,
                nextAction = nextAction
            )

        fun failure(actionName: String, reason: String, nextAction: String? = null): ActionResult =
            ActionResult(
                success = false,
                actionName = actionName,
                reason = reason,
                nextAction = nextAction
            )

        fun confirmationRequired(actionName: String, prompt: String): ActionResult =
            ActionResult(
                success = false,
                actionName = actionName,
                requiresConfirmation = true,
                confirmationPrompt = prompt,
                reason = "User confirmation required for sensitive operation"
            )
    }
}
