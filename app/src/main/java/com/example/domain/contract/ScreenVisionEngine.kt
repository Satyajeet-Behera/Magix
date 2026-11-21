package com.example.domain.contract

data class UIElementNode(
    val id: String,
    val text: String?,
    val contentDescription: String?,
    val bounds: String,
    val isClickable: Boolean
)

data class ScreenPerceptionResult(
    val appPackage: String,
    val activityName: String,
    val visibleElements: List<UIElementNode>,
    val timestamp: Long
)

interface ScreenVisionEngine {
    val isVisionEnabled: Boolean

    suspend fun analyzeCurrentScreen(): ScreenPerceptionResult?
}
