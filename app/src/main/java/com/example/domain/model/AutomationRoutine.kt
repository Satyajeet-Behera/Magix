package com.example.domain.model

data class AutomationRoutine(
    val id: String,
    val title: String,
    val description: String,
    val schedule: String,
    val isEnabled: Boolean,
    val actionsSummary: List<String>,
    val category: String,
    val lastExecutedTime: Long? = null
)
