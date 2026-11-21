package com.example.domain.tool

import com.example.domain.model.ActionResult

data class ToolParameter(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = true
)

enum class ToolCategory {
    APPLICATIONS,
    NAVIGATION,
    SCREEN,
    INTERACTION,
    DEVICE,
    COMMUNICATION,
    REMINDERS,
    AUTOMATIONS,
    MEMORY
}

sealed class ToolValidationResult {
    object Valid : ToolValidationResult()
    data class Invalid(val reason: String) : ToolValidationResult()
}

interface MagixTool {
    val name: String
    val description: String
    val parameters: List<ToolParameter>
    val category: ToolCategory
    val requiredPermission: String?
    val isSensitive: Boolean

    fun validate(arguments: Map<String, String>): ToolValidationResult
    suspend fun execute(arguments: Map<String, String>): ActionResult
}
