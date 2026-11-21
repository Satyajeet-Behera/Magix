package com.example.domain.tool

import android.content.Context
import com.example.domain.contract.AccessibilityController
import com.example.domain.contract.AuditLogger
import com.example.domain.contract.AutomationRepository
import com.example.domain.contract.MemoryRepository
import com.example.domain.contract.ReminderRepository
import com.example.domain.model.ActionResult
import com.example.domain.model.GlobalActionType

class ToolRegistry(
    private val context: Context,
    private val accessibilityController: AccessibilityController,
    private val memoryRepository: MemoryRepository,
    private val automationRepository: AutomationRepository,
    private val reminderRepository: ReminderRepository,
    private val auditLogger: AuditLogger
) {
    private val toolsMap = mutableMapOf<String, MagixTool>()

    init {
        registerDefaultTools()
    }

    private fun registerDefaultTools() {
        // Applications
        register(OpenAppTool(context))
        register(LaunchSettingsTool(context))

        // Navigation
        register(NavigationTool("go_home", "Navigates to the Android Home screen.", GlobalActionType.HOME, accessibilityController))
        register(NavigationTool("go_back", "Navigates Back one step.", GlobalActionType.BACK, accessibilityController))
        register(NavigationTool("open_recents", "Opens the Android Recent Applications switcher.", GlobalActionType.RECENTS, accessibilityController))
        register(NavigationTool("open_notifications", "Pulls down the system notification shade.", GlobalActionType.NOTIFICATIONS, accessibilityController))
        register(NavigationTool("open_quick_settings", "Opens system quick settings shade.", GlobalActionType.QUICK_SETTINGS, accessibilityController))

        // Screen
        register(NavigationTool("take_screenshot", "Captures an authorized screenshot of the active screen.", GlobalActionType.TAKE_SCREENSHOT, accessibilityController))
        register(InspectScreenTool(accessibilityController))

        // Interaction
        register(TapTool(accessibilityController))
        register(ScrollTool(accessibilityController))
        register(TypeTextTool(accessibilityController))

        // Device
        register(DeviceControlTool(context))

        // Applications & Specialized Chains
        register(YouTubeActionTool(context, accessibilityController))
        register(CreateNoteTool(context, memoryRepository))

        // Communication
        register(ReadEmailsTool(context, accessibilityController))
        register(PrepareMessageTool(context))
        register(PrepareCallTool(context))

        // Reminders
        register(CreateReminderTool(reminderRepository))
        register(ListRemindersTool(reminderRepository))

        // Memory
        register(RememberTool(memoryRepository))
        register(RetrieveMemoryTool(memoryRepository))
    }

    fun register(tool: MagixTool) {
        toolsMap[tool.name] = tool
    }

    fun getTool(name: String): MagixTool? = toolsMap[name]

    fun getAllTools(): List<MagixTool> = toolsMap.values.toList()

    suspend fun executeTool(
        name: String,
        arguments: Map<String, String>,
        userConfirmed: Boolean = false
    ): ActionResult {
        val tool = toolsMap[name] ?: return ActionResult.failure(
            actionName = name,
            reason = "Tool '$name' is not registered in MAGIX ToolRegistry."
        )

        // Validation
        when (val validation = tool.validate(arguments)) {
            is ToolValidationResult.Invalid -> {
                auditLogger.logAction(
                    actionName = name,
                    target = arguments.toString(),
                    status = "FAILED",
                    details = "Validation failed: ${validation.reason}"
                )
                return ActionResult.failure(name, "Argument validation failed: ${validation.reason}")
            }
            ToolValidationResult.Valid -> {}
        }

        // Sensitive confirmation check
        if (tool.isSensitive && !userConfirmed) {
            val prompt = "MAGIX requires confirmation before executing sensitive action '$name' with parameters: $arguments"
            auditLogger.logAction(
                actionName = name,
                target = arguments.toString(),
                status = "CONFIRMATION_PENDING",
                details = prompt
            )
            return ActionResult.confirmationRequired(name, prompt)
        }

        // Execution
        val result = try {
            tool.execute(arguments)
        } catch (e: Exception) {
            ActionResult.failure(name, "Execution exception: ${e.message}")
        }

        // Audit Logging
        auditLogger.logAction(
            actionName = name,
            target = arguments.toString(),
            status = if (result.success) "SUCCESS" else "FAILED",
            details = if (result.success) result.evidence else result.reason,
            evidence = result.evidence
        )

        return result
    }

    fun getToolDeclarationsPrompt(): String {
        return buildString {
            append("You have access to the following MAGIX Android device tools:\n")
            toolsMap.values.forEach { tool ->
                append("• ${tool.name}: ${tool.description}\n")
                if (tool.parameters.isNotEmpty()) {
                    append("  Parameters:\n")
                    tool.parameters.forEach { param ->
                        append("   - ${param.name} (${param.type}): ${param.description}${if (param.required) " [REQUIRED]" else ""}\n")
                    }
                }
            }
        }
    }
}
